"""Capture real screenshots in a separate disposable profile using a legitimate local Minecraft installation.
Windows preview utility; never opens existing worlds or modifies the user's installed mod set.
"""
from pathlib import Path
import argparse
import hashlib
import json
import os
import shutil
import subprocess
import time
import uuid
import zipfile

REPO = Path(__file__).resolve().parents[1]


def allowed(entry):
    rules=entry.get('rules');result=not rules
    for rule in rules or []:
        target=rule.get('os',{})
        if target.get('name','windows')=='windows' and target.get('arch','amd64') in ('amd64','x86_64') and not rule.get('features'):
            result=rule['action']=='allow'
    return result


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--minecraft-install',type=Path,required=True)
    parser.add_argument('--java',type=Path,required=True)
    parser.add_argument('--neo-version',default='21.1.265')
    args=parser.parse_args()
    assert os.name=='nt','This local preview runner targets Windows.'
    install=args.minecraft_install.resolve();profile=REPO/'runs/publication-native';profile.mkdir(parents=True,exist_ok=True)
    base=json.loads((install/'versions/1.21.1/1.21.1.json').read_text(encoding='utf8'))
    version='neoforge-'+args.neo_version
    neo=json.loads((install/'versions'/version/(version+'.json')).read_text(encoding='utf8'))
    libraries={}
    for entry in base['libraries']+neo['libraries']:
        if allowed(entry):
            parts=entry['name'].split(':');libraries[':'.join(parts[:2]+parts[3:])]=entry
    classpath=[];natives=profile/'natives';natives.mkdir(exist_ok=True)
    for entry in libraries.values():
        relative=entry.get('downloads',{}).get('artifact',{}).get('path')
        if not relative:
            group,name,version_number,*classifier=entry['name'].split(':')
            relative=group.replace('.','/')+'/'+name+'/'+version_number+'/'+name+'-'+version_number+('-'+classifier[0] if classifier else '')+'.jar'
        path=install/'libraries'/relative;assert path.is_file(),path;classpath.append(str(path))
        if 'natives-windows' in entry['name']:
            with zipfile.ZipFile(path) as jar:
                for name in jar.namelist():
                    if name.endswith('.dll') and not (natives/Path(name).name).exists():(natives/Path(name).name).write_bytes(jar.read(name))
    version_jar=install/'versions'/version/(version+'.jar')
    if version_jar.exists():classpath.append(str(version_jar))
    sources=[REPO/'build/libs/inscribed-0.8.0.jar',REPO/'build/libs/inscribed-publication-qa.jar']
    mods=profile/'mods';mods.mkdir(exist_ok=True)
    assert {p.name for p in mods.glob('*.jar')} <= {p.name for p in sources},'Unexpected mods in preview profile'
    for path in sources:assert path.is_file(),path;shutil.copy2(path,mods/path.name)
    (profile/'mod-manifest.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar')},indent=2))
    (profile/'options.txt').write_text('autoJump:false\nrenderDistance:4\nsimulationDistance:4\nmaxFps:60\nenableVsync:false\npauseOnLostFocus:false\nguiScale:2\nonboardAccessibility:false\ntutorialStep:none\nlanguage:en_us\n',encoding='utf8')
    variables={'natives_directory':natives,'launcher_name':'InscribedPreview','launcher_version':'0.8.0','classpath':';'.join(classpath),'library_directory':install/'libraries','classpath_separator':';','version_name':version}
    def expand(value):
        for key,content in variables.items():value=value.replace('${'+key+'}',str(content))
        assert '${' not in value,value
        return value
    jvm=[]
    for entry in base['arguments']['jvm']+neo['arguments']['jvm']:
        if isinstance(entry,dict):
            if not allowed(entry):continue
            values=entry['value'] if isinstance(entry['value'],list) else [entry['value']]
        else:values=[entry]
        jvm.extend(expand(v) for v in values)
    game_args=['--username','InscribedPreview','--uuid',str(uuid.uuid3(uuid.NAMESPACE_DNS,'OfflinePlayer:InscribedPreview')),'--accessToken','0','--userType','legacy','--version',version,'--gameDir',str(profile),'--assetsDir',str(install/'assets'),'--assetIndex',base['assetIndex']['id'],'--width','1280','--height','960']
    args_file=profile/'preview-args.txt'
    command=['-Xmx3G','-Dinscribed.publicationQA=true',*jvm,neo['mainClass'],*neo['arguments']['game'],*game_args]
    args_file.write_text('\n'.join('"'+a.replace('\\','\\\\').replace('"','\\"')+'"' for a in command),encoding='utf8')
    result=profile/'screenshots/publication/results.json'
    if result.exists():result.rename(result.with_name('results-previous-'+str(int(time.time()))+'.json'))
    with (profile/'native-client.log').open('w',encoding='utf8') as output:
        process=subprocess.Popen([str(args.java),'@'+str(args_file)],cwd=profile,stdout=output,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
        print(json.dumps({'owned_qa_pid':process.pid,'profile':str(profile)}),flush=True)
        try:
            deadline=time.monotonic()+750
            while not result.exists():
                if process.poll() is not None:raise RuntimeError('Preview exited before producing a report; inspect native-client.log')
                if time.monotonic()>deadline:raise TimeoutError('Preview deadline')
                time.sleep(1)
            report=json.loads(result.read_text(encoding='utf8'));print(json.dumps(report),flush=True)
            assert report['status']=='PASS',report
        finally:
            try:process.wait(timeout=30)
            except subprocess.TimeoutExpired:process.terminate();process.wait(timeout=15)


if __name__=='__main__':main()
