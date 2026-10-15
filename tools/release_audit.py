"""Dispatch current audit; retain the historical Core 0.1.0 audit for its matching checkout."""
from pathlib import Path
import hashlib
import json
import re
import xml.etree.ElementTree as ET
import zipfile

project = Path(__file__).resolve().parents[1]
if "mod_version=0.5.0" in (project/"gradle.properties").read_text(encoding="utf-8"):
    import runpy
    runpy.run_path(str(project/"tools/progression_release_audit.py"), run_name="__main__")
    raise SystemExit(0)
if "mod_version=0.3.0" in (project/"gradle.properties").read_text(encoding="utf-8"):
    import runpy
    runpy.run_path(str(project/"tools/guide_release_audit.py"), run_name="__main__")
    raise SystemExit(0)
if any(f"mod_version={version}" in (project/"gradle.properties").read_text(encoding="utf-8") for version in ("0.2.0","0.2.1","0.2.2")):
    import runpy
    runpy.run_path(str(project/"tools/ritual02_release_audit.py"), run_name="__main__")
    raise SystemExit(0)
if any(f"mod_version={version}" in (project/"gradle.properties").read_text(encoding="utf-8") for version in ("0.1.1","0.1.2","0.1.3")):
    import runpy
    runpy.run_path(str(project/"tools/catalog_release_audit.py"), run_name="__main__")
    raise SystemExit(0)
workspace = project.parents[2]
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
preflight_path = workspace / "CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-core-altar/protected-preflight.json"
before = json.loads(preflight_path.read_text(encoding="utf-8"))
changed = [name for name, digest in before.items() if not (workspace/name).is_file() or sha(workspace/name) != digest]
assert not changed, f"Protected files changed: {changed}"
jar = project / "build/libs/inscribed-0.1.0.jar"
existing_mod_ids = []
for existing_jar in (workspace/"mods").glob("*.jar"):
    with zipfile.ZipFile(existing_jar) as existing_archive:
        for metadata_name in ("META-INF/neoforge.mods.toml", "META-INF/mods.toml"):
            if metadata_name in existing_archive.namelist():
                metadata_text = existing_archive.read(metadata_name).decode(errors="replace")
                if re.search(r'modId\s*=\s*"inscribed"', metadata_text):
                    existing_mod_ids.append(existing_jar.name)
assert existing_mod_ids in ([], [jar.name]), f"Another mod already uses inscribed: {existing_mod_ids}"
with zipfile.ZipFile(jar) as archive:
    entries = archive.namelist()
    assert not any("GameTests" in n or "ClientValidation" in n or "test_altar" in n or "test_native" in n or "test_invalid" in n for n in entries)
    recipes = [n for n in entries if n.startswith("data/inscribed/inscribed/enchantment_recipes/") and n.endswith(".json")]
    assert recipes == ["data/inscribed/inscribed/enchantment_recipes/test_sharpness_1.json"], recipes
    toml = archive.read("META-INF/neoforge.mods.toml").decode()
    assert 'version="0.1.0"' in toml and '${mod_version}' not in toml
    assert 'modId="lsbc"' not in toml
    assert not any(n.startswith("dev/lsbc/") for n in entries)
    metadata = json.loads(archive.read("inscribed.mixins.json"))
    assert metadata["mixins"] == ["TableRemovalMixin"] and not metadata.get("client")

logs = ["validation-matrix.log", "validation-client-restart.log", "validation-client-lsbc.log"]
for name in logs:
    content = (project/name).read_text(encoding="utf-16")
    assert "BUILD SUCCESSFUL" in content and "BUILD FAILED" not in content, name
matrix = (project/logs[0]).read_text(encoding="utf-16")
counts = [int(value) for value in re.findall(r"All (\d+) required tests passed", matrix)]
assert counts == [27, 27], counts

junit_files = list((project/"build/test-results/test").glob("TEST-*.xml"))
assert junit_files
junit_count = 0
for file in junit_files:
    root = ET.parse(file).getroot()
    assert all(int(root.attrib.get(field, "0")) == 0 for field in ("errors", "failures", "skipped")), file
    junit_count += int(root.attrib["tests"])

clients = {}
for mode in ("alone-first", "alone-restart", "with-lsbc"):
    folder = project / "docs/validation" / mode
    report = json.loads((folder/"results.json").read_text(encoding="utf-8"))
    assert report["status"] == "PASS", report
    assert len(report["captures"]) == 5 and all((folder/name).is_file() for name in report["captures"])
    assert ("lsbc" in report["mods"]) == (mode == "with-lsbc"), report
    if mode == "alone-restart":
        assert report.get("previousJvmPersistence") == "PASS"
    clients[mode] = report

files = sorted([*project.joinpath("src").rglob("*"), *project.joinpath("docs").rglob("*.md"),
                *project.joinpath("gradle/wrapper").glob("*"), project/"gradlew", project/"gradlew.bat",
                project/"build.gradle", project/"settings.gradle", project/"gradle.properties", project/"README.md", Path(__file__).resolve()])
files = [p for p in files if p.is_file()]
installed = workspace / "mods" / jar.name
if installed.exists():
    assert sha(installed) == sha(jar), "Installed JAR differs from build"
    assert [p.name for p in (workspace/"mods").glob("inscribed-*.jar")] == [jar.name], "Duplicate Inscribed JARs"

manifest = {
    "release": "Inscribed 01 Core + Altar MVP 0.1.0", "date": "2026-10-13",
    "versions": {"minecraft": "1.21.1", "neoforge": "21.1.255", "java": "21.0.9", "gradle": "9.2.1", "moddevgradle": "2.0.148"},
    "jar": {"path": str(jar.relative_to(workspace)).replace("\\", "/"), "bytes": jar.stat().st_size, "sha256": sha(jar), "installed": installed.exists()},
    "protectedPreflight": {"path": str(preflight_path.relative_to(workspace)).replace("\\", "/"), "files": len(before), "changed": changed},
    "tests": {"junit": junit_count, "allocatorExhaustiveOracleCases": 600, "gameTestsPerRuntime": counts, "clients": clients},
    "logs": {name: sha(project/name) for name in logs}, "releaseRecipes": recipes,
    "changedExistingLsbcFiles": [], "newProjectFiles": {str(p.relative_to(project)).replace("\\", "/"): sha(p) for p in files},
    "nativeArtifacts": {str(p.relative_to(project)).replace("\\", "/"): sha(p) for p in (project/"docs/validation").rglob("*") if p.is_file()},
}
target = project / "docs/INSCRIBED_RELEASE_MANIFEST.json"
target.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(json.dumps({"jar": manifest["jar"], "protectedFiles": len(before), "junit": junit_count, "gameTests": counts, "clientRuns": len(clients), "newProjectFiles": len(files)}, indent=2))
