"""Release audit for combined Inscribed 04/05 implementation and installation."""
from pathlib import Path
import json,hashlib,zipfile,re,tomllib
import xml.etree.ElementTree as ET

P=Path(__file__).resolve().parents[1];W=P.parents[2]
B=W/'CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-inscribed-04'
sha=lambda f:hashlib.sha256(f.read_bytes()).hexdigest()
baseline=json.loads((B/'protected-before-install.json').read_text())
external_path=P/'docs/05-external-config-observations.json'
external=json.loads(external_path.read_text()) if external_path.exists() else []
external_by_name={x['path']:x for x in external}
unclassified=[]
for name,digest in baseline.items():
    f=W/name
    if not f.is_file():unclassified.append(name);continue
    if sha(f)!=digest:
        record=external_by_name.get(name)
        if record is None or record['baselineSha256']!=digest or record['observedSha256']!=sha(f):unclassified.append(name)
assert not unclassified,unclassified
assert all(not x['path'].startswith('mods/') for x in external)
jar=P/'build/libs/inscribed-0.5.0.jar'
with zipfile.ZipFile(jar) as z:
    names=z.namelist()
    assert not any(n.startswith(('dev/lsbc/','mezz/jei/')) or 'GameTests' in n or 'ClientValidation' in n or 'Probe.class' in n for n in names)
    for name in names:
        if name.endswith('.class'):
            assert z.read(name)==(P/'build/classes/java/main'/name).read_bytes(),name
            if not name.startswith('dev/inscribed/compat/'):
                assert b'dev/lsbc/' not in z.read(name) and b'mezz/jei/' not in z.read(name),name
    meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
    assert meta['mods'][0]['version']=='0.5.0'
    deps={x['modId']:x for x in meta['dependencies']['inscribed']}
    assert deps['lsbc']['type']==deps['jei']['type']=='optional'
    assert {k for k,v in deps.items() if v['type']=='required'}=={'minecraft','neoforge'}
    recipes=[n for n in names if n.startswith('data/inscribed/inscribed/enchantment_recipes/') and n.endswith('.json')]
    assert len(recipes)==131
    defaults=json.loads(z.read('inscribed/default-recipes.json'))
    assert len(defaults['enchantment_recipes'])==131
    for name in recipes:
        data=json.loads(z.read(name));old=json.loads((B/'src/main/resources'/name).read_text())
        for key in ('enchantment','level','recipe_visibility','exact_book_level','neoforge:conditions'):
            assert data.get(key)==old.get(key),(name,key)
        for mode in ('infusion','extraction'):
            assert data[mode]['duration_ticks']==old[mode]['duration_ticks']
            assert len(data[mode]['ingredients'])<=8 and all(e.get('count',1)==1 for e in data[mode]['ingredients'])
        assert data['extraction']['failure_chance']==old['extraction']['failure_chance']==.2
        assert data['extraction']['empty_books_required']==old['extraction']['empty_books_required']
        assert data['progression_tier'].startswith('TIER_')
        assert z.read(name)==(P/'src/main/resources'/name).read_bytes()
        key='inscribed:'+name.split('enchantment_recipes/')[1][:-5]
        configured=dict(defaults['enchantment_recipes'][key]);assert configured.pop('enabled') is True;assert configured==data
    assert not any('/test_' in n and not n.endswith('/test_sharpness_1.json') for n in recipes)
    loot=json.loads(z.read('inscribed/default-loot.json'))
    assert len(loot['pools'])==13
    for key,pool in loot['pools'].items():
        name='data/inscribed/inscribed/loot_injections/'+key.split(':')[1]+'.json'
        assert json.loads(z.read(name))==pool and z.read(name)==(P/'src/main/resources'/name).read_bytes()
    assert json.loads(z.read('data/neoforge/loot_modifiers/global_loot_modifiers.json'))=={'replace':False,'entries':['inscribed:exploration']}
    assert 'assets/inscribed/models/item/enchantment_catalyst.json' in names
    for locale in ('en_us','pt_br'):
        chapters=json.loads(z.read(f'assets/inscribed/guide/{locale}.json'));assert len(chapters)==9
        assert set(x['id'] for x in chapters)=={'home','altar','infusion','extraction','library','pedestals','power','recipes','anvil'}
        assert 'item.inscribed.enchantment_catalyst' in json.loads(z.read(f'assets/inscribed/lang/{locale}.json'))
    assert not any('?' in c['title'] or '?' in c['body'] for c in json.loads(z.read('assets/inscribed/guide/pt_br.json')))
consistency=json.loads((P/'docs/05-consistency-audit.json').read_text());assert consistency['warnings']==[] and consistency['recipes']==131 and consistency['verifiedLootTables']==24
server=(P/'validation-05-final.log').read_text(encoding='utf-16');counts=[int(x) for x in re.findall(r'All (\d+) required tests passed',server)];assert counts==[88,88] and 'BUILD SUCCESSFUL' in server
junit=0
for f in (P/'build/test-results/test').glob('TEST-*.xml'):
    root=ET.parse(f).getroot();assert all(int(root.attrib.get(k,0))==0 for k in ('errors','failures','skipped'));junit+=int(root.attrib['tests'])
assert junit==4
matrix={}
for letter,lsbc,jei in [('A',False,False),('B',False,True),('C',True,False),('D',True,True)]:
    folder=P/f'docs/validation/guide05-{letter}'
    report=json.loads((folder/'results.json').read_text());assert report['status']=='PASS' and report['lsbc']==lsbc and report['jei']==jei
    assert report['guideCatalystAndUnitConstraints']=='PASS'
    if jei:assert report['jeiCatalystAndUnitConstraints']=='PASS'
    if lsbc:assert report['codexFifthTabAndSharedGuide']=='PASS' and report['partialKnownWordsIdentitySearchAndIndependentKnowledge']=='PASS'
    if not lsbc:assert report['savedKnowledgeAfterRemovingLSBC']=='PASS'
    assert 'BUILD SUCCESSFUL' in (P/f'validation-05-guide{letter}.log').read_text(encoding='utf-16')
    matrix[letter]=report
polish=json.loads((P/'docs/validation/polish05/results.json').read_text());assert polish['status']=='PASS'
for key in ('physicalInfusionAndCompletion','distinctReverseExtractionFlow','physicalExtractionBookAndCompletion','cancellationEffectCleanup','catalystItemModel'):assert polish[key]=='PASS'
assert len(list((P/'docs/validation/polish05').glob('*.png')))==9
installed=W/'mods/inscribed-0.5.0.jar'
if installed.exists():assert sha(installed)==sha(jar) and [f.name for f in (W/'mods').glob('inscribed-*.jar')]==['inscribed-0.5.0.jar']
manifest={'release':'Inscribed 0.5.0 — stages 04 and 05','date':'2026-10-13',
 'jar':{'path':jar.relative_to(W).as_posix(),'bytes':jar.stat().st_size,'sha256':sha(jar),'installed':installed.exists()},
 'tests':{'junit':junit,'gameTestsPerRuntime':counts,'nativeMatrix':matrix,'nativeRituals':polish},
 'catalog':consistency,'compatibility':{'minecraft':'1.21.1','neoforge':'21.1.255','lsbc':'0.21.0 optional','jei':'19.57.0.451 optional','create':'6.0.10 optional'},
 'protected':{'checked':len(baseline),'unclassifiedChanges':unclassified,'externalConfigObservations':external,'foreignJarChanges':[]},
 'limitations':['No survival-duration balance playthrough or full structure worldgen benchmark','No two remote clients','JEI global ingredient book search is not filtered','Item emissive light does not illuminate surroundings','Vanilla loot/trades preserved; injection is supplemental']}
(P/'docs/INSCRIBED_RELEASE_MANIFEST.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'status':'PASS','jarSha256':sha(jar),'gameTests':counts,'junit':junit,'nativeMatrix':'A/B/C/D PASS','nativeRituals':'PASS','catalogWarnings':0,'installed':installed.exists()}))
