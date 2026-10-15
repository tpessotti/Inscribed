"""Stage the approved local candidate inputs; does not publish or install in main instance."""
from pathlib import Path
import hashlib,json,re,shutil,zipfile
P=Path(__file__).resolve().parents[1];W=P.parents[2];VERSION="0.7.2-rc.1"
assert json.loads((P/"docs/rc08/RELEASE_GATE.json").read_text())["status"]=="PASS"
DEST=W/"CODEX LSBC/INSCRIBED/releases"/VERSION;DEST.mkdir(parents=True,exist_ok=True)
for source in [P/f"build/libs/inscribed-{VERSION}.jar",P/f"build/distributions/inscribed-vanilla-create-{VERSION}.zip"]:shutil.copy2(source,DEST/source.name)
seeds=[P/n for n in ["README.md","CHANGELOG.md","CREDITS.md","LICENSE","THIRD_PARTY_NOTICES.md"]]
seeds += [P/"docs"/n for n in ["RELEASE_NOTES_0.7.2-rc.1.md","INSCRIBED08_REPORT.md","INSCRIBED_RELEASE_CHECKLIST.md","INSCRIBED_COMPATIBILITY.md","INSCRIBED_KNOWN_LIMITATIONS.md","FUTURE_IDEAS.md","INSCRIBED_RELEASE_DESCRIPTIONS.md","INSCRIBED_ADDON_API.md","INSCRIBED_SAVE_MIGRATION.md","INSCRIBED_INTEGRATIONS.md","INSCRIBED_DATAPACK_AUTHORING.md","INSCRIBED_CONFIGURATION.md","INSCRIBED_MODPACK_QUICKSTART.md","INSCRIBED_TROUBLESHOOTING.md","INSCRIBED_CREATE_PONDER.md","INSCRIBED_CATALYST.md","INSCRIBED_LOOT_INJECTION.md","INSCRIBED_RECIPE_AUTHORING.md","MODPACK_CONFIGURATION.md"]]
for folder in ["examples","templates","schemas","docs/schema","docs/rc08"]:seeds += [f for f in (P/folder).rglob("*") if f.is_file()]
selected=set();queue=list(seeds)
while queue:
    path=queue.pop().resolve();assert path.is_relative_to(P.resolve()),path
    if path in selected:continue
    assert path.exists(),path
    if path.is_dir():queue.extend(f for f in path.rglob("*") if f.is_file());continue
    selected.add(path)
    if path.suffix==".md":
        for link in re.findall(r"\]\(([^)]+)\)",path.read_text(encoding="utf-8")):
            if link.startswith(("https:","http:","#")):continue
            target=link.split("#",1)[0].strip("<>");queue.append(path.parent/target)
for path in selected:
    n=path.relative_to(P.resolve()).as_posix();assert not any(x in path.parts for x in ["runs","build","libs","__pycache__"]),n
    assert path.suffix not in [".log",".jar",".class",".pyc"],n
archive=DEST/f"inscribed-{VERSION}-docs.zip"
with zipfile.ZipFile(archive,"w",zipfile.ZIP_DEFLATED) as z:
    for path in sorted(selected):z.write(path,path.relative_to(P.resolve()).as_posix())
shutil.copy2(P/"docs/INSCRIBED08_REPORT.md",DEST/"INSCRIBED08_REPORT.md")
shutil.copy2(P/"docs/RELEASE_NOTES_0.7.2-rc.1.md",DEST/"RELEASE_NOTES_0.7.2-rc.1.md")
shutil.copy2(P/"LICENSE",DEST/"LICENSE")
artifacts=[]
for name in [f"inscribed-{VERSION}.jar",f"inscribed-vanilla-create-{VERSION}.zip",archive.name]:
    file=DEST/name;artifacts.append({"name":name,"bytes":file.stat().st_size,"sha256":hashlib.sha256(file.read_bytes()).hexdigest()})
(DEST/"SHA256SUMS.txt").write_text("".join(f"{a['sha256']}  {a['name']}\n" for a in artifacts),encoding="utf-8")
manifest={"status":"PASS","version":VERSION,"minecraft":"1.21.1","neoforge":"21.1.255","java":21,"artifacts":artifacts,"docsArchiveEntries":len(selected),"primaryInstance":"stable 0.7.1 and customized configs preserved","installedForTesting":"isolated RC runtime profiles","publication":"none","gate":"docs/rc08/RELEASE_GATE.json","fullReport":"docs/INSCRIBED08_REPORT.md"}
(DEST/"RELEASE_MANIFEST.json").write_text(json.dumps(manifest,indent=2),encoding="utf-8")
(DEST/"README.txt").write_text("Inscribed 0.7.2-rc.1 - local candidate, not published.\nInstall one Inscribed JAR on client/server. Official default datapack is already embedded; standalone ZIP is optional and replaces identical IDs. Back up saves/configs before upgrading; explicit JSON entries override same-ID datapacks.\nExtract the docs ZIP for the linked complete report/checklist/notes. Main instance remains on stable 0.7.1.\n",encoding="utf-8")
# Verify packaged local links instead of assuming graph collection worked.
with zipfile.ZipFile(archive) as z:
    names=set(z.namelist())
    for path in selected:
        if path.suffix!=".md":continue
        for link in re.findall(r"\]\(([^)]+)\)",path.read_text(encoding="utf-8")):
            if link.startswith(("https:","http:","#")):continue
            target=(path.parent/link.split("#",1)[0].strip("<>")).resolve()
            if target.is_file():assert target.relative_to(P.resolve()).as_posix() in names,link
print(json.dumps(manifest,indent=2))
