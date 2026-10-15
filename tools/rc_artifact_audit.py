"""Verify canonical packaging, production freeze and artifact boundaries."""
from pathlib import Path
import collections,hashlib,json,zipfile
P=Path(__file__).resolve().parents[1];W=P.parents[2];VERSION="0.7.2-rc.1"
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def validate_archive(path):
    with zipfile.ZipFile(path) as z:
        counts=collections.Counter(z.namelist());duplicates=[n for n,c in counts.items() if c>1]
        if duplicates:raise AssertionError(f"Duplicate archive/recipe IDs in {path.name}: {duplicates}")
        return z.namelist()
def without_unit_count(d):
    if isinstance(d,dict):return {k:without_unit_count(v) for k,v in d.items() if not(k=="count" and v==1)}
    if isinstance(d,list):return [without_unit_count(v) for v in d]
    return d

def audit():
    baseline=W/"CODEX LSBC/INSCRIBED/backups/2026-10-14-pre-rc08";state=json.loads((baseline/"BASE_STATE.json").read_text(encoding="utf-8"))
    changed=[n for n,h in state["files"].items() if n.startswith("src/main/java/") and digest(P/n)!=h];assert not changed,f"Gameplay source changed: {changed}"
    assert digest(P/"LICENSE")==digest(baseline/"LICENSE"),"LICENSE changed"
    jar=P/f"build/libs/inscribed-{VERSION}.jar";pack=P/f"build/distributions/inscribed-vanilla-create-{VERSION}.zip"
    jar_names=validate_archive(jar);pack_names=validate_archive(pack)
    assert not any(n.startswith(("dev/inscribedqa/","mezz/","dev/lsbc/","com/simibubi/","net/createmod/")) for n in jar_names),"Bundled QA/third-party classes"
    assert not any("test_" in n and n!="data/inscribed/inscribed/enchantment_recipes/test_sharpness_1.json" for n in jar_names),"Test fixtures leaked"
    assert not any(n.endswith((".java",".log",".py",".bat")) for n in jar_names),"Source junk leaked"
    canonical=P/"content/inscribed_default_datapack";files=list((canonical/"data").rglob("*.json"));assert len(files)==156
    with zipfile.ZipFile(jar) as j,zipfile.ZipFile(pack) as z:
        for f in files:
            n=f.relative_to(canonical).as_posix();assert j.read(n)==z.read(n)==f.read_bytes(),f"Canonical drift: {n}"
            old=baseline/"src/main/resources"/n;assert without_unit_count(json.loads(old.read_text(encoding="utf-8")))==without_unit_count(json.loads(f.read_text(encoding="utf-8"))),f"Semantic gameplay change: {n}"
        defaults=json.loads(j.read("inscribed/default-recipes.json"));loot=json.loads(j.read("inscribed/default-loot.json"))
        assert len(defaults["enchantment_recipes"])==131 and len(loot["pools"])==13
        for f in (canonical/"data/inscribed/inscribed/enchantment_recipes").rglob("*.json"):
            key="inscribed:"+f.relative_to(canonical/"data/inscribed/inscribed/enchantment_recipes").as_posix()[:-5];a=json.loads(f.read_text(encoding="utf-8"));b=defaults["enchantment_recipes"][key];b={k:v for k,v in b.items() if k!="enabled"};assert a==b,f"Config mirror drift: {key}"
    report={"status":"PASS","productionJavaChanges":changed,"canonicalDataFiles":len(files),"recipes":131,"logicalDefinitions":262,"duplicateArchiveIds":[],"licenseUnchanged":True,"defaultsSemanticallyUnchanged":True,"jar":{"name":jar.name,"bytes":jar.stat().st_size,"sha256":digest(jar)},"datapack":{"name":pack.name,"bytes":pack.stat().st_size,"sha256":digest(pack)}}
    (P/"docs/rc08/ARTIFACT_AUDIT.json").write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2));return report
if __name__=="__main__":audit()
