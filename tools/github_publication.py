"""Owner-authorized Inscribed repository/release operations. Credentials stay in the OS credential helper and memory."""
from pathlib import Path
import argparse
import json
import os
import subprocess
import urllib.request
import urllib.error

REPO = Path(__file__).resolve().parents[1]
API = 'https://api.github.com/repos/tpessotti/Inscribed'


def credential():
    env = dict(os.environ, GIT_TERMINAL_PROMPT='0', GCM_INTERACTIVE='Never')
    result = subprocess.run(['git','credential','fill'], cwd=REPO, input='protocol=https\nhost=github.com\n\n',
                            text=True, capture_output=True, env=env, timeout=30)
    if result.returncode: raise RuntimeError('The local Git credential helper has no non-interactive GitHub credential.')
    values = dict(line.split('=',1) for line in result.stdout.splitlines() if '=' in line)
    if not values.get('password'): raise RuntimeError('GitHub credential unavailable; no credential was printed.')
    return values['password']


def request(token, path='', method='GET', data=None, anonymous=False, binary=None):
    url = path if path.startswith('https://') else API+path
    headers = {'User-Agent':'Inscribed-release-preparation','Accept':'application/vnd.github+json','X-GitHub-Api-Version':'2022-11-28'}
    if not anonymous: headers['Authorization']='Bearer '+token
    body = None
    if data is not None: body=json.dumps(data).encode();headers['Content-Type']='application/json'
    if binary is not None: body=binary;headers['Content-Type']='application/octet-stream'
    try:
        with urllib.request.urlopen(urllib.request.Request(url, body, headers, method=method), timeout=120) as response:
            content=response.read();return json.loads(content) if content else None
    except urllib.error.HTTPError as error:
        if error.code==404 and method=='GET': return None
        raise RuntimeError(f'GitHub {method} failed with HTTP {error.code}; credentials were not logged.') from None


def main():
    parser=argparse.ArgumentParser();parser.add_argument('action',choices=['inspect','public','draft']);args=parser.parse_args()
    token=credential();repo=request(token)
    assert repo and repo['full_name']=='tpessotti/Inscribed'
    if args.action=='inspect':
        print(json.dumps({'repository':repo['full_name'],'visibility':repo['visibility'],'admin':repo.get('permissions',{}).get('admin'),
                          'license':(repo.get('license') or {}).get('spdx_id'),'default_branch':repo['default_branch']}));return
    assert repo.get('permissions',{}).get('admin'), 'Repository admin access is required.'
    if args.action=='public':
        changed=request(token,method='PATCH',data={'visibility':'public','description':'Physical enchanting: build a library, prepare reagents, and infuse or extract enchantments. Minecraft 1.21.1 / NeoForge.'})
        assert not changed['private']
        request(token,'/topics',method='PUT',data={'names':['minecraft','minecraft-mod','neoforge','enchantments','mit-license']})
        visible=request(token,anonymous=True);assert visible and not visible['private']
        result={'repository':visible['html_url'],'visibility':visible['visibility'],'anonymous_read_verified':True,'license':(visible.get('license') or {}).get('spdx_id')}
    else:
        git_args=['git','-c','safe.directory='+REPO.as_posix()]
        git_env=dict(os.environ,GIT_TERMINAL_PROMPT='0',GCM_INTERACTIVE='Never')
        head=subprocess.check_output([*git_args,'rev-parse','HEAD'],cwd=REPO,text=True,env=git_env).strip()
        remote=subprocess.check_output([*git_args,'ls-remote','origin','refs/heads/main'],cwd=REPO,text=True,env=git_env).split()[0]
        assert head==remote, 'Push the reviewed commit before preparing release assets.'
        tag=request(token,'/git/ref/tags/v0.8.0')
        if tag:
            obj=tag['object']
            while obj['type']=='tag':obj=request(token,'/git/tags/'+obj['sha'])['object']
            assert obj['type']=='commit' and obj['sha']==head,'Existing version tag points to a different commit.'
        body=(REPO/'docs/publication/RELEASE_NOTES_0.8.0.md').read_text(encoding='utf8')
        releases=request(token,'/releases');existing=next((r for r in releases if r['tag_name']=='v0.8.0'),None)
        assert existing is None or existing['draft'], 'Never overwrite a published release.'
        payload={'tag_name':'v0.8.0','target_commitish':head,'name':'Inscribed 0.8.0 — Minecraft 1.21.1 / NeoForge','body':body,'draft':True,'prerelease':False}
        release=request(token,'/releases/'+str(existing['id']),method='PATCH',data=payload) if existing else request(token,'/releases',method='POST',data=payload)
        assets=request(token,'/releases/'+str(release['id'])+'/assets')
        folder=REPO/'build/publication'
        names={'inscribed-0.8.0.jar','inscribed-vanilla-create-0.8.0.zip','inscribed-0.8.0-sources.zip','inscribed-0.8.0-publication-kit.zip','SHA256SUMS.txt','RELEASE_0.8.0_VALIDATION.json'}
        for name in sorted(names):
            path=folder/name
            assert path.is_file(),path
            old=next((a for a in assets if a['name']==path.name),None)
            if old: request(token,'/releases/assets/'+str(old['id']),method='DELETE')
            request(token,release['upload_url'].split('{')[0]+'?name='+path.name,method='POST',binary=path.read_bytes())
        final=request(token,'/releases/'+str(release['id']));assert final['draft']
        result={'repository':repo['html_url'],'commit':head,'release':final['html_url'],'draft':True,'assets':[a['name'] for a in final['assets']]}
    output=REPO/'build/publication';output.mkdir(parents=True,exist_ok=True)
    (output/('github-'+args.action+'-receipt.json')).write_text(json.dumps(result,indent=2)+'\n',encoding='utf8')
    print(json.dumps(result,indent=2))


if __name__=='__main__':main()
