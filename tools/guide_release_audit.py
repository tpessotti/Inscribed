"""Audit Inscribed 03: exact JAR, optional isolation, runtime evidence and protected files."""
from pathlib import Path
import hashlib,json,re,zipfile,tomllib
import xml.etree.ElementTree as ET

project=Path(__file__).resolve().parents[1]
workspace=project.parents[2]
backup=workspace/'CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-inscribed-03'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
before=json.loads((backup/'protected-before-install.json').read_text(encoding='utf-8'))
changed=[k for k,v in before.items() if not (workspace/k).is_file() or sha(workspace/k)!=v]
timestamp_only=[]
external_history=[]
external_settings=[]
evidence_path=backup/'external-runtime-property-timestamps.json'
if evidence_path.exists():
    evidence=json.loads(evidence_path.read_text(encoding='utf-8'))
    for name in list(changed):
        record=evidence.get(name,{})
        if record.get('baseline_sha256')!=before[name] or not record.get('reconstructed_original_timestamp'):continue
        raw=(workspace/name).read_bytes();lines=raw.splitlines(keepends=True)
        newline=b'\r\n' if lines[1].endswith(b'\r\n') else b'\n'
        reconstructed=lines[0]+record['reconstructed_original_timestamp'].encode()+newline+b''.join(lines[2:])
        if hashlib.sha256(reconstructed).hexdigest()==before[name]:timestamp_only.append(name);changed.remove(name)
for name in list(changed):
    if name=='config/jei/world/local/New World (1)/lookupHistory.json':
        # The user's real instance was launched during this task. This is mutable
        # JEI lookup history for their world, not a config edit or a validation run.
        external_history.append({'path':name,'baselineSha256':before[name],'currentSha256':sha(workspace/name),'classification':'External active-game JEI lookup history; preserved without writing'})
        changed.remove(name)
    elif name=='config/relics.yaml':
        # External change while the user's instance was open. Verify the exact
        # previous bytes instead of exempting arbitrary edits to this file.
        raw=(workspace/name).read_bytes()
        reconstructed=raw.replace(b'enabledExtendedConfigs: true',b'enabledExtendedConfigs: false')
        if hashlib.sha256(reconstructed).hexdigest()==before[name]:
            external_settings.append({'path':name,'baselineSha256':before[name],'currentSha256':sha(workspace/name),'change':'enabledExtendedConfigs: false -> true','classification':'External configuration change; exact baseline reconstruction verified; preserved without writing'})
            changed.remove(name)
assert not changed,changed
assert sha(backup/'inscribed-0.2.2.jar')=='938aa41e6eef3179606afb4bd520d3cdeb2e06100d92f8b88f3fcc284c70d43a'
jar=project/'build/libs/inscribed-0.3.0.jar'
with zipfile.ZipFile(jar) as z:
    names=z.namelist()
    assert not any(n.startswith(('dev/lsbc/','mezz/jei/')) or ('GameTests' in n or 'ClientValidation' in n or 'Probe.class' in n or 'test_native' in n or 'test_02_' in n or 'test_invalid' in n) for n in names)
    for name in names:
        if name.endswith('.class'):
            assert z.read(name)==(project/'build/classes/java/main'/name).read_bytes(),name
            if not name.startswith('dev/inscribed/compat/'):
                assert b'dev/lsbc/' not in z.read(name) and b'mezz/jei/' not in z.read(name),name
    meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
    assert meta['mods'][0]['version']=='0.3.0'
    dependencies={d['modId']:d for d in meta['dependencies']['inscribed']}
    assert dependencies['lsbc']['type']==dependencies['jei']['type']=='optional'
    assert {k for k,v in dependencies.items() if v['type']=='required'}=={'neoforge','minecraft'}
    recipes=[n for n in names if n.startswith('data/inscribed/inscribed/enchantment_recipes/') and n.endswith('.json')]
    assert len(recipes)==131
    defaults=json.loads(z.read('inscribed/default-recipes.json'))
    assert len(defaults['enchantment_recipes'])==131 and 'inscribed:guide_book' in defaults['crafting_recipes']
    selectors={}
    for name in recipes:
        data=json.loads(z.read(name));old=json.loads((backup/'src/main/resources'/name).read_text(encoding='utf-8'))
        assert data['infusion']==old['infusion'] and data['extraction']==old['extraction'],name
        assert data['recipe_visibility']=='ON_ENCHANTMENT_DISCOVERY',name
        assert data['extraction']['failure_chance']==.2,name
        assert z.read(name)==(project/'src/main/resources'/name).read_bytes(),name
        key='inscribed:'+name.split('enchantment_recipes/')[1][:-5]
        configured=dict(defaults['enchantment_recipes'][key]);assert configured.pop('enabled') is True
        assert configured==data,key
        if data['enchantment'].startswith('create:'):assert {'type':'neoforge:mod_loaded','modid':'create'} in data['neoforge:conditions']
        selectors.setdefault(data['extraction']['ingredients'][1]['item'],set()).add(data['enchantment'])
    assert len(selectors)==44 and all(len(v)==1 for v in selectors.values())
    for language in ['en_us','pt_br']:
        assert len(json.loads(z.read(f'assets/inscribed/guide/{language}.json')))==9
        assert 'enchantment.create.capacity' in json.loads(z.read(f'data/inscribed/observation_languages/{language}.json'))
    assert 'ConfigCraftingRecipesMixin' in json.loads(z.read('inscribed.mixins.json'))['mixins']
server=(project/'validation-03-final-D.log').read_text(encoding='utf-16')
counts=[int(n) for n in re.findall(r'All (\d+) required tests passed',server)]
assert counts==[73,73],counts
assert 'BUILD SUCCESSFUL' in server
junit=0
for f in (project/'build/test-results/test').glob('TEST-*.xml'):
    root=ET.parse(f).getroot();assert all(int(root.attrib.get(k,0))==0 for k in ['failures','errors','skipped']);junit+=int(root.attrib['tests'])
assert junit==4,junit
matrix={}
for letter,lsbc,jei in [('A',False,False),('B',False,True),('C',True,False),('D',True,True)]:
    folder=project/f'docs/validation/guide03-{letter}'
    report=json.loads((folder/'results.json').read_text(encoding='utf-8'))
    assert report['status']=='PASS' and report['lsbc']==lsbc and report['jei']==jei,report
    for check in ['guideItemRightClick','unknownCatalogAndBookshelfNoLearning','multiEnchantmentInventoryAcquisitionAndRefresh','forgetHidesRecipesWithoutInventoryRelearning','bidirectionalEnchantmentRecipeNavigation','reloadRemovesRecipesAndRefreshesJei','explicitDiscoveryAdminCommandsAndJeiRefresh']:assert report[check]=='PASS'
    if lsbc:
        for check in ['partialKnownWordsIdentitySearchAndIndependentKnowledge','guideSearchDoesNotMatchUnknownWords','codexFifthTabAndSharedGuide']:assert report[check]=='PASS'
    else:assert report['savedKnowledgeAfterRemovingLSBC']=='PASS'
    if jei:assert report['jeiCategoriesAndPublicApiPerPlayerVisibility']=='PASS'
    assert (folder/'09-recipe-detail.png').is_file()
    assert 'BUILD SUCCESSFUL' in (project/f'validation-03-final-{letter}.log').read_text(encoding='utf-16')
    matrix[letter]=report
extraction=json.loads((project/'docs/validation/guide03-extraction/results.json').read_text(encoding='utf-8'))
assert extraction['status']=='PASS' and extraction['successfulExtractionLearnsUnknownEnchantment']=='PASS'
assert extraction['extractionPhysicalClickNoSelectionScreen']=='PASS'
assert 'BUILD SUCCESSFUL' in (project/'validation-03-extraction.log').read_text(encoding='utf-16')
# If installation has already happened, require exact bytes and a single Inscribed JAR.
installed=workspace/'mods/inscribed-0.3.0.jar'
if installed.exists():assert sha(installed)==sha(jar) and [f.name for f in (workspace/'mods').glob('inscribed-*.jar')]==['inscribed-0.3.0.jar']
result={'release':'Inscribed 0.3.0 — Guide, Knowledge, LSBC and JEI','date':'2026-10-13',
    'jar':{'path':str(jar.relative_to(workspace)).replace('\\','/'),'bytes':jar.stat().st_size,'sha256':sha(jar),'installed':installed.exists()},
    'tests':{'junit':junit,'gameTestsPerRuntime':counts,'serverRuntimes':['Inscribed alone','Inscribed + Create 6.0.10 + LSBC 0.21.0'],'nativeMatrix':matrix,'nativeExtraction':extraction},
    'compatibility':{'minecraft':'1.21.1','neoforge':'21.1.255','lsbc':'0.21.0 optional','jei':'19.57.0.451 optional'},
    'catalog':{'recipes':131,'vanilla':125,'create':6,'defaultVisibility':'ON_ENCHANTMENT_DISCOVERY','extractionFailure':.2,'distinctExtractionSelectors':44},
    'protected':{'checked':len(before),'changed':changed,'externalJavaPropertiesTimestampOnly':timestamp_only,'externalRuntimeHistory':external_history,'externalConfigurationChanges':external_settings},
    'limits':{'targetsPerEnchantment':12,'ingredientAlternatives':64,'packetBudgetBytes':750000,'jeiGlobalEnchantedBookSearch':'Not overridden','twoRemoteClients':'Not executed; server isolation and individual real-client sessions verified'}}
(project/'docs/INSCRIBED_RELEASE_MANIFEST.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'status':'PASS','jarSha256':sha(jar),'gameTests':counts,'junit':junit,'nativeMatrix':'A/B/C/D PASS','protected':len(before),'installed':installed.exists()}))
