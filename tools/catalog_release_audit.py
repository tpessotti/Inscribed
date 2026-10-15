"""Audit recipe coverage against exact local registries and current release evidence."""
from pathlib import Path
import hashlib
import json
import re
import xml.etree.ElementTree as ET
import zipfile

project = Path(__file__).resolve().parents[1]
version = re.search(r"^mod_version=(.+)$", (project/"gradle.properties").read_text(encoding="utf-8"), re.M).group(1)
inspection = version == "0.1.2"
book_fallback = version == "0.1.3"
workspace = project.parents[2]
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
preflight = workspace / ("CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-book-fallback/protected-before-install.json" if book_fallback else "CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-loupe-inspection/protected-before-install.json" if inspection else "CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-enchantment-catalog/protected-preflight.json")
before = json.loads(preflight.read_text(encoding="utf-8"))
changed = [name for name,digest in before.items() if (sha(workspace/name) if (workspace/name).is_file() else None)!=digest]
assert not changed, changed
catalog = json.loads((project/"docs/ENCHANTMENT_RECIPE_CATALOG.json").read_text(encoding="utf-8"))
expected = {}
for namespace, source in catalog["source_jars"].items():
    path = workspace / source["path"]
    assert sha(path) == source["sha256"]
    with zipfile.ZipFile(path) as archive:
        prefix = f"data/{namespace}/enchantment/"
        for name in archive.namelist():
            if name.startswith(prefix) and name.endswith(".json"):
                enchantment = namespace + ":" + name[len(prefix):-5]
                for level in range(1,json.loads(archive.read(name))["max_level"]+1):
                    expected[(enchantment,level)] = 1

jar = project / f"build/libs/inscribed-{version}.jar"
with zipfile.ZipFile(jar) as archive:
    files = [name for name in archive.namelist() if name.startswith("data/inscribed/inscribed/enchantment_recipes/") and name.endswith(".json")]
    actual = {}
    for name in files:
        data = json.loads(archive.read(name)); key = (data["enchantment"],data["level"])
        assert key not in actual, f"Duplicate level {key}"
        actual[key] = 1
        if key[0].startswith("create:"):
            assert data["neoforge:conditions"] == [{"type":"neoforge:mod_loaded","modid":"create"}]
        assert json.loads((project/"src/main/resources"/name).read_text(encoding="utf-8")) == data
    assert actual == expected and len(files) == 131
    toml = archive.read("META-INF/neoforge.mods.toml").decode()
    assert f'version="{version}"' in toml and 'modId="create"' not in toml and 'modId="lsbc"' not in toml
    assert not any("GameTests" in n or "ClientValidation" in n or "test_invalid" in n or n.startswith("dev/lsbc/") for n in archive.namelist())

log = project / ("validation-book-final.log" if book_fallback else "validation-inspection-final.log" if inspection else "validation-catalog-final.log")
text = log.read_text(encoding="utf-16")
assert "BUILD SUCCESSFUL" in text and "BUILD FAILED" not in text
counts = [int(n) for n in re.findall(r"All (\d+) required tests passed",text)]
assert counts == ([38,38] if book_fallback else [34,34] if inspection else [30,30]), counts
assert "Create 6.0.10 (create)" in text and "LSBC Core 0.21.0 (lsbc)" in text
errors = re.findall(r"Invalid Inscribed recipe ([^: ]+:[^: ]+):",text)
assert all(name.startswith("inscribed:test_invalid_") for name in errors), errors
junit = [ET.parse(path).getroot() for path in (project/"build/test-results/test").glob("TEST-*.xml")]
assert junit and all(all(int(root.attrib.get(k,0))==0 for k in ("errors","failures","skipped")) for root in junit)
installed = workspace / f"mods/inscribed-{version}.jar"
if installed.exists():
    assert sha(installed)==sha(jar)
    assert sorted(p.name for p in (workspace/"mods").glob("inscribed-*.jar")) == [installed.name]

old = workspace / ("CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-book-fallback" if book_fallback else "CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-loupe-inspection" if inspection else "CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-enchantment-catalog")
changes = {}
for directory in ("src","tools","docs"):
    for path in (project/directory).rglob("*"):
        if not path.is_file() or path.name=="INSCRIBED_RELEASE_MANIFEST.json" or path.suffix not in (".java",".json",".py",".md"): continue
        previous = old / path.relative_to(project)
        if not previous.exists() or sha(previous)!=sha(path): changes[str(path.relative_to(project)).replace("\\","/")] = {"change":"modified" if previous.exists() else "added","sha256":sha(path)}
for name in ("build.gradle","gradle.properties","README.md"):
    changes[name] = {"change":"modified","sha256":sha(project/name)}
manifest = {
    "release":f"Inscribed {version}" + (" writable book inspection fallback" if book_fallback else " altar sound, target scale and lens inspection" if inspection else " vanilla + Create enchantment recipes"), "date":"2026-10-13",
    "jar":{"bytes":jar.stat().st_size,"sha256":sha(jar),"installed":installed.exists()},
    "coverage":{"minecraft":{"enchantments":42,"recipes":125},"create":{"version":"6.0.10","enchantments":2,"recipes":6},"total":131,"added":130},
    "tests":{"junit":sum(int(r.attrib["tests"]) for r in junit),"gameTestsPerRuntime":counts,"runtimes":["Inscribed alone","Inscribed + Create 6.0.10 + LSBC 0.21.0"],"logSha256":sha(log)},
    "protectedFiles":{"checked":len(before),"changed":changed},"files":changes,
    "sourceJars":catalog["source_jars"],"recipePaths":files,
    "historicalClientEvidence":"Native screenshots/restart from Core 0.1.0 were retained; not rerun for this data catalog increment.",
}
if inspection:
    manifest["historicalExternalDifferences"] = json.loads((old/"historical-external-differences.json").read_text(encoding="utf-8"))
    manifest["protectedFiles"]["baseline"] = "Immediately before installation; excludes this task's own Inscribed JAR."
    client_log = project/"validation-inspection-client-final.log"
    assert "BUILD SUCCESSFUL" in client_log.read_text(encoding="utf-16")
    evidence = project/"runs/loupeClient/screenshots/inscribed-altar/results.json"
    result = json.loads(evidence.read_text(encoding="utf-8"))
    assert result["status"] == "PASS" and result["lensPhysicalClickAndResultsScreen"] == "PASS", result
    assert "07-lens-results.png" in result["captures"]
    manifest["nativeClientEvidence"] = {"path":str(evidence.relative_to(project)),"sha256":sha(evidence),"captures":result["captures"],"logSha256":sha(client_log),
        "screenshots":{name:sha(evidence.parent/name) for name in result["captures"]}}
    manifest["historicalClientEvidence"] = "Earlier 0.1.0 evidence retained; fresh 0.1.2 lens/ritual validation recorded separately."
if book_fallback:
    evidence = project/"runs/bookClient/screenshots/inscribed-altar/results.json"
    result = json.loads(evidence.read_text(encoding="utf-8"))
    assert result["status"] == "PASS" and result["fallbackBookPhysicalClickPagesAndVanillaScreen"] == "PASS", result
    assert "lsbc" not in result["mods"] and "08-fallback-book.png" in result["captures"]
    manifest["nativeClientEvidence"] = {"path":str(evidence.relative_to(project)),"sha256":sha(evidence),"captures":result["captures"],"screenshots":{name:sha(evidence.parent/name) for name in result["captures"]}}
    manifest["protectedFiles"]["baseline"] = "Before installation; excludes this task's own Inscribed JAR."
    manifest["historicalClientEvidence"] = "Lens native validation from 0.1.2 retained; fresh 0.1.3 fallback book and ritual validation without LSBC."
(project/"docs/INSCRIBED_RELEASE_MANIFEST.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
print(json.dumps({"jar":manifest["jar"],"coverage":manifest["coverage"],"tests":manifest["tests"],"protectedFiles":manifest["protectedFiles"]},indent=2))
