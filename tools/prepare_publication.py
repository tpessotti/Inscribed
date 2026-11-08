"""Audit and assemble the reviewed 0.8.0 upload materials. Does not publish to a mod platform."""
from pathlib import Path
import collections
import hashlib
import json
import re
import shutil
import subprocess
import zipfile
import xml.etree.ElementTree as ET
from urllib.parse import quote
import jsonschema

P=Path(__file__).resolve().parents[1]
VERSION='0.8.0'
SECRET=re.compile(rb'(?:github_pat_[A-Za-z0-9_]{30,}|gh[pousr]_[A-Za-z0-9]{30,}|sk-(?:proj-)?[A-Za-z0-9_-]{32,}|AKIA[0-9A-Z]{16}|-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----)')


def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def git(*args):return subprocess.check_output(['git',*args],cwd=P)
def files():return sorted(set(git('ls-files','--cached','--others','--exclude-standard','-z').decode().split('\0'))-{''})
def logtext(p):
    b=p.read_bytes();return b.decode('utf16' if b.startswith((b'\xff\xfe',b'\xfe\xff')) else 'utf8',errors='replace')


def main():
    out=P/'build/publication';out.mkdir(parents=True,exist_ok=True)
    publication=P/'docs/publication';publication.mkdir(parents=True,exist_ok=True)
    license=(P/'LICENSE').read_bytes()
    assert license==git('show','HEAD:LICENSE') and b'MIT License' in license
    jar=P/f'build/libs/inscribed-{VERSION}.jar';pack=P/f'build/distributions/inscribed-vanilla-create-{VERSION}.zip'
    report={'version':VERSION,'status':'PASS','license':'MIT','license_sha256':digest(P/'LICENSE')}
    with zipfile.ZipFile(jar) as z,zipfile.ZipFile(pack) as d:
        counts=collections.Counter(z.namelist());assert max(counts.values())==1
        assert z.read('LICENSE')==license and b'license="MIT"' in z.read('META-INF/neoforge.mods.toml')
        assert b'version="0.8.0"' in z.read('META-INF/neoforge.mods.toml')
        assert 'assets/inscribed/icon.png' in z.namelist()
        assert z.read('assets/inscribed/icon.png')==(P/'docs/images/project-icon.png').read_bytes()
        forbidden=[n for n in z.namelist() if n.startswith(('dev/inscribedqa/','dev/lsbc/','mezz/','net/createmod/','com/simibubi/')) or n.endswith(('.log','.java','.py'))]
        assert not forbidden,forbidden
        canonical=P/'content/inscribed_default_datapack'
        data=list((canonical/'data').rglob('*.json'));assert len(data)==156
        for path in data:
            name=path.relative_to(canonical).as_posix();assert z.read(name)==d.read(name)==path.read_bytes(),name
        assert b'MIT License' in d.read('LICENSE')
        defaults=json.loads(z.read('inscribed/default-recipes.json'));loot=json.loads(z.read('inscribed/default-loot.json'))
        assert len(defaults['enchantment_recipes'])==131 and len(loot['pools'])==13
        assert not any(n.startswith('data/example/') for n in z.namelist())
        report['canonical_recipes']=131;report['loot_pools']=13;report['canonical_data_files']=156
        report['no_qa_or_optional_provider_classes_bundled']=True
    rows=[ET.parse(p).getroot() for p in (P/'build/test-results/test').glob('TEST-*.xml')]
    units={name:sum(int(row.attrib.get(name,0)) for row in rows) for name in ('tests','failures','errors','skipped')}
    assert units==dict(tests=4,failures=0,errors=0,skipped=0),units
    report['unit']=units
    dedicated=logtext(P/'build/release-080-validation.log')
    assert 'All 106 required tests passed' in dedicated and 'BUILD SUCCESSFUL' in dedicated
    report['dedicated_tests']=106
    packaged=logtext(P/'build/release-080-packaged-validation.log')
    assert 'All 106 required tests passed' in packaged and 'BUILD SUCCESSFUL' in packaged
    installed=P/'runs/publication-dedicated/mods'/jar.name
    assert digest(installed)==digest(jar)
    report['packaged_jar_dedicated_tests']=106
    evidence=publication/'evidence'
    native=json.loads((evidence/'native-results.json').read_text())
    assert native['status']=='PASS' and native['native_purchase'] and native['actual_catalog_ritual']
    assert len(native['images'])==5 and native['optional_runtime_mods']==0
    manifest=json.loads((evidence/'native-mod-manifest.json').read_text())
    assert manifest[jar.name]==digest(jar),'Native preview must use the exact final candidate'
    for name in native['images']:assert (P/'docs/images'/name).is_file(),name
    report['native']=native
    report['images']={name:digest(P/'docs/images'/name) for name in native['images']}
    example=P/'examples/add_enchantment/data/example/inscribed/enchantment_recipes/sharpness_1.json'
    jsonschema.validate(json.loads(example.read_text()),json.loads((P/'schemas/inscribed_enchantment_recipe.schema.json').read_text()))
    assert json.loads((P/'examples/add_enchantment/pack.mcmeta').read_text())['pack']['pack_format']==48
    report['example_schema_and_server_parser']='PASS'
    current=files();hits=[]
    for name in current:
        path=P/name
        if path.suffix.lower() in {'.png','.jpg','.jpeg','.jar','.gif'}:continue
        if path.is_file() and SECRET.search(path.read_bytes()):hits.append(name)
    assert not hits,'Potential credential in current source paths: '+str(hits)
    commits=git('rev-list','--all').decode().splitlines()
    pattern=SECRET.pattern.decode().replace('(?:','(')
    for commit in commits:
        check=subprocess.run(['git','grep','-I','-l','-E',pattern,commit],cwd=P,capture_output=True)
        assert check.returncode in (0,1)
        assert check.returncode==1,'Potential credential in history paths; inspect privately before publication'
    report['secret_scan']={'current_files':len(current),'history_commits':len(commits),'findings':0}
    report['platforms_submitted']=False
    report['modrinth_public_listing_eligibility']='REQUIRES_REVIEW_UNDER_AI_POLICY'
    report['artifacts']=[{'name':p.name,'bytes':p.stat().st_size,'sha256':digest(p)} for p in (jar,pack)]
    report_path=publication/'RELEASE_0.8.0_VALIDATION.json'
    report_path.write_text(json.dumps(report,indent=2)+'\n',encoding='utf8')
    # A platform description needs public links rather than repository-relative image paths.
    text=(P/'README.md').read_text(encoding='utf8').split('## Development and publication')[0]
    text+='\n## License\n\nInscribed uses the MIT license. Development and some bundled assets used generative AI; see the source repository credits and platform disclosure requirements.\n'
    def links(match):
        prefix,target,suffix=match.groups()
        if target.startswith(('http:','https:','#')):return match.group(0)
        base='https://raw.githubusercontent.com/tpessotti/Inscribed/main/' if prefix.startswith('!') else 'https://github.com/tpessotti/Inscribed/blob/main/'
        return prefix+base+quote(target,safe='/#')+suffix
    text=re.sub(r'(!?\[[^\]]*\]\()([^\s)]+)(\))',links,text)
    for platform in ('CURSEFORGE','MODRINTH'):(publication/(platform+'_DESCRIPTION.md')).write_text(text,encoding='utf8')
    # Check presentation links that refer to files in this repository.
    for doc in [P/'README.md',P/'docs/README.pt-BR.md',P/'docs/ADDING_ENCHANTMENTS.md',P/'docs/ADDING_ENCHANTMENTS.pt-BR.md']:
        for link in re.findall(r'!?\[[^\]]*\]\(([^\s)]+)\)',doc.read_text(encoding='utf8')):
            if not link.startswith(('http:','https:','#')):assert (doc.parent/link.split('#')[0]).exists(),(doc,link)
    for artifact in (jar,pack):shutil.copy2(artifact,out/artifact.name)
    shutil.copy2(report_path,out/report_path.name)
    source=out/f'inscribed-{VERSION}-sources.zip'
    with zipfile.ZipFile(source,'w',zipfile.ZIP_DEFLATED) as z:
        for name in files():
            path=P/name
            if path.is_file():z.write(path,name)
    with zipfile.ZipFile(source) as z:
        assert not any(n.startswith(('build/','runs/','libs/','.git/')) for n in z.namelist())
        assert [n for n in z.namelist() if n.endswith('.jar')]==['gradle/wrapper/gradle-wrapper.jar']
        assert 'src/releaseQaSupport/java/dev/inscribedqa/ReleaseQaMod.java' in z.namelist()
        assert 'src/releaseQaResources/META-INF/neoforge.mods.toml' in z.namelist()
    kit=out/f'inscribed-{VERSION}-publication-kit.zip'
    with zipfile.ZipFile(kit,'w',zipfile.ZIP_DEFLATED) as z:
        for name in files():
            if name in {'README.md','LICENSE','CREDITS.md','THIRD_PARTY_NOTICES.md'} or name.startswith(('docs/publication/','docs/images/','examples/add_enchantment/','schemas/')) or name in {'docs/README.pt-BR.md','docs/ADDING_ENCHANTMENTS.md','docs/ADDING_ENCHANTMENTS.pt-BR.md','docs/INSCRIBED_CONFIGURATION.md','docs/INSCRIBED_CATALYST.md'}:
                if (P/name).is_file():z.write(P/name,name)
        z.write(jar,jar.name);z.write(pack,pack.name)
    artifacts=[out/jar.name,out/pack.name,source,kit,out/report_path.name]
    (out/'SHA256SUMS.txt').write_text(''.join(digest(p)+'  '+p.name+'\n' for p in artifacts),encoding='ascii')
    print(json.dumps({'status':'PASS','unit':4,'dedicated':106,'native_images':5,'license':'MIT','secret_scan':report['secret_scan'],'output':str(out),'artifacts':[p.name for p in artifacts]},indent=2))


if __name__=='__main__':main()
