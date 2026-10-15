"""Collect final RC evidence. Fails rather than accepting a missing/failed test."""
from pathlib import Path
import hashlib,json,re,shutil,xml.etree.ElementTree as ET
P=Path(__file__).resolve().parents[1];W=P.parents[2];OUT=P/"docs/rc08";VERSION="0.7.2-rc.1"
def read(p):return json.loads(p.read_text(encoding="utf-8"))
def logtext(path):
    data=path.read_bytes();return data.decode("utf-16" if data.startswith((b"\xff\xfe",b"\xfe\xff")) else "utf-8",errors="replace")
def native(name,relative):
    src=P/"runs/rc08"/relative;data=read(src);assert data.get("status")=="PASS",f"{name}: {data}"
    (OUT/(name+".json")).write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding="utf-8");return data
matrix=[]
for case,mods in zip("ABCDEFG",["Inscribed only","+ JEI","+ LSBC","+ LSBC + JEI","+ Create","+ Create + JEI","+ Create + LSBC + JEI"]):
    log=logtext((P/f"validation-rc08-{case}-server.log"))
    assert "All 100 required tests passed" in log and "BUILD SUCCESSFUL" in log and "verified production JAR origin" in log,case
    client=native("CLIENT_"+case,f"{case}-client/screenshots/inscribed-guide/results.json")
    matrix.append({"case":case,"optionalMods":mods,"dedicatedGameTests":100,"dedicated":"PASS","nativeClient":"PASS"})
extra={}
for name,relative in {
"SMOKE":"fresh-client/screenshots/rc-smoke/results.json","IDLE_RESTART":"fresh-client/screenshots/rc-smoke/restart.json",
"ACTIVE_SAVE":"active-client/screenshots/rc-smoke/results.json","ACTIVE_RESTART":"active-client/screenshots/rc-smoke/restart.json",
"MULTIPLAYER":"multiplayer/server/results.json","MULTIPLAYER_A":"multiplayer/clientA/results.json","MULTIPLAYER_B":"multiplayer/clientB/results.json",
"STRESS":"stress-client/screenshots/rc-stress/results.json","ASPECT":"G-client/screenshots/aspect06/results.json",
"POLISH":"polish-client/screenshots/inscribed-polish04/results.json","PONDER_EN":"ponder-en-client/screenshots/inscribed-ponder/results.json",
"PONDER_PT":"ponder-pt-client/screenshots/inscribed-ponder/results.json","AUTHOR_CLIENT":"author-client/screenshots/inscribed-guide/results.json"}.items():
    extra[name]=native(name,relative)
assert extra["AUTHOR_CLIENT"].get("exampleDatapackReloadAndJeiStructuralRefresh")=="PASS"
author=[]
for case in ["A","E"]:
    for suffix in ["", "-extraction"]:
        log=logtext((P/f"validation-rc08-author-{case}-server{suffix}.log"))
        assert "All 3 required tests passed" in log and "BUILD SUCCESSFUL" in log,case+suffix
        author.append({"case":case,"suite":"authoring extraction/toggles" if suffix else "authoring load/infusion/reload","tests":3,"status":"PASS"})
log=logtext(P/"validation-rc08-author-G-server-extraction.log");assert "All 3 required tests passed" in log and "BUILD SUCCESSFUL" in log
author.append({"case":"G","suite":"authoring extraction and installed LSBC toggle","tests":3,"status":"PASS"})
clean=logtext((P/"validation-rc08-final-clean-build.log"));assert "BUILD SUCCESSFUL" in clean
unit=0
for file in (P/"build/test-results/test").glob("TEST-*.xml"):
    node=ET.parse(file).getroot();assert int(node.get("failures","0"))==0 and int(node.get("errors","0"))==0;unit+=int(node.get("tests","0"))
assert unit==4,unit
startup=logtext((P/"validation-rc08-clean-server.log"));assert "Done (" in startup and "BUILD SUCCESSFUL" in startup and "Inscribed Release QA (development only)" not in startup
base=read(W/"CODEX LSBC/INSCRIBED/backups/2026-10-14-pre-rc08/BASE_STATE.json")
protected={name:hashlib.sha256((W/name).read_bytes()).hexdigest()==sha for name,sha in base["protected"].items()};assert all(protected.values())
assert hashlib.sha256((W/"mods/inscribed-0.7.1.jar").read_bytes()).hexdigest()==base["installedJarSha256"]
artifact=read(OUT/"ARTIFACT_AUDIT.json");assert artifact["status"]=="PASS"
assert artifact["jar"]["sha256"]==read(OUT/"TESTED_JAR.json")["sha256"]
report={"status":"PASS","version":VERSION,"base":{"version":"0.7.1","git":"not a repository","branch":None,"commit":None,"jarSha256":base["installedJarSha256"]},"minecraft":"1.21.1","neoforge":"21.1.255","java":21,"matrix":matrix,"junit":unit,"matrixGameTestExecutions":700,"authoring":author,"cleanReleaseOnlyServer":"PASS","native":extra,"protectedPrimaryInstanceHashes":protected,"artifact":artifact,"blockerBugs":[],"highBugs":[],"humanFullModpackPlaytest":"recommended; not claimed as automated evidence"}
(OUT/"RESULTS.json").write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding="utf-8")
print("PASS: seven dedicated/native cases, 700 matrix GameTests, 15 authoring GameTests, 4 JUnit, lifecycle/multiplayer/stress/assets/Ponder")
