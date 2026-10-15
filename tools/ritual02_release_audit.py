"""Audit the exact 02 build, physical catalog, test evidence and protected files."""
from pathlib import Path
import hashlib
import json
import re
import zipfile
import xml.etree.ElementTree as ET

project = Path(__file__).resolve().parents[1]
workspace = project.parents[2]
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
version=re.search(r"mod_version=(\S+)",(project/"gradle.properties").read_text()).group(1)
config=version=="0.2.2"
risk=version in ("0.2.1","0.2.2")
backup = workspace / ("CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-config-recipes" if config else "CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-extraction-risk" if risk else "CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-inscribed-02")
before = json.loads((backup / "protected-before-install.json").read_text(encoding="utf-8"))
authorized={"config/inscribed-server.toml"} if config else set()
changed = [k for k, v in before.items() if k not in authorized and (sha(workspace/k) if (workspace/k).is_file() else None) != v]
assert not changed, changed
old_jar=backup/("inscribed-0.2.1.jar" if config else "inscribed-0.2.0.jar" if risk else "inscribed-0.1.3.jar")
assert sha(old_jar) == ("97890e969f746285d83939ee616c7ecfc75bfe423acbf77bfbabedf05684b102" if config else "2116987e52cb94e855b59a198d8d8130348d52fa99e73398562996f488b187a3" if risk else "c3ba1bd3b3ef6f0e04a50ed417db8b2105674fc33dabb3abee1da6b4967829b1")
jar = project / f"build/libs/inscribed-{version}.jar"
with zipfile.ZipFile(jar) as z:
    entries = z.namelist()
    recipes = [n for n in entries if n.startswith("data/inscribed/inscribed/enchantment_recipes/") and n.endswith(".json")]
    assert len(recipes) == 131
    assert not any("GameTests" in n or "ClientValidation" in n or "test_02_" in n or "test_native" in n or "test_invalid" in n for n in entries)
    for n in recipes:
        data = json.loads(z.read(n))
        assert "infusion" in data and "extraction" in data, n
        assert z.read(n) == (project/"src/main/resources"/n).read_bytes(), n
        if data["enchantment"].startswith("create:"):
            assert {"type": "neoforge:mod_loaded", "modid": "create"} in data["neoforge:conditions"]
        old = json.loads((backup/"src/main/resources"/n).read_text(encoding="utf-8"))
        assert old["infusion"] == data["infusion"], n
        if risk: assert data["extraction"]["failure_chance"]==.20, n
    if risk:
        selectors={}
        for n in recipes:
            data=json.loads(z.read(n));selector=data["extraction"]["ingredients"][1]["item"]
            selectors.setdefault(selector,set()).add(data["enchantment"])
        assert len(selectors)==44 and all(len(enchantments)==1 for enchantments in selectors.values()), selectors
    assert "AnvilResultMixin" in json.loads(z.read("inscribed.mixins.json"))["mixins"]
    if config:
        assert "ConfigCraftingRecipesMixin" in json.loads(z.read("inscribed.mixins.json"))["mixins"]
        defaults=json.loads(z.read("inscribed/default-recipes.json"))
        assert defaults["format_version"]==1 and len(defaults["enchantment_recipes"])==131
        for n in recipes:
            key="inscribed:"+n.split("enchantment_recipes/")[1][:-5]
            record=dict(defaults["enchantment_recipes"][key]);assert record.pop("enabled") is True
            assert record==json.loads(z.read(n)),key
    metadata = z.read("META-INF/neoforge.mods.toml").decode()
    assert f'version="{version}"' in metadata.replace(" ", "")
    assert not re.search(r'modId\s*=\s*"(?:lsbc|create)"', metadata)
    assert not any(n.startswith("dev/lsbc/") for n in entries)

log = project/("validation-022-final.log" if config else "validation-021-server.log" if risk else "validation-02-server.log")
server_text = log.read_text(encoding="utf-16")
counts = [int(n) for n in re.findall(r"All (\d+) required tests passed", server_text)]
assert counts == ([64,64] if config else [60,60] if risk else [56,56]), counts
assert "BUILD SUCCESSFUL" in server_text
client_log = project/("validation-022-final.log" if config else "validation-021-client.log" if risk else "validation-02-client.log")
assert "BUILD SUCCESSFUL" in client_log.read_text(encoding="utf-16")
build_log = project/("validation-022-final.log" if config else "validation-021-build.log" if risk else "validation-02-build.log")
assert "BUILD SUCCESSFUL" in build_log.read_text(encoding="utf-16")
junit = 0
for f in (project/"build/test-results/test").glob("TEST-*.xml"):
    root = ET.parse(f).getroot()
    assert all(int(root.attrib.get(k, "0")) == 0 for k in ("failures", "errors", "skipped"))
    junit += int(root.attrib["tests"])
assert junit == 4
folder = project/("docs/validation/ritual022-alone" if config else "docs/validation/ritual021-alone" if risk else "docs/validation/ritual02-alone")
client = json.loads((folder/"results.json").read_text(encoding="utf-8"))
assert client["status"] == "PASS" and "lsbc" not in client["mods"]
assert all(client.get(k) == "PASS" for k in (("extractionPhysicalClickNoSelectionScreen","extractionIngredientsSelectFortune","extractionPhysicalBooksAndClientResult") if risk else ("ritualSelectionRealPacketScreen", "extractionChoiceRealServerPacket", "extractionPhysicalBooksAndClientResult")))
assert len(client["captures"]) == 8 and all((folder/n).is_file() for n in client["captures"])
configuration={}
if config:
    import tomllib
    assert client.get("configurationCatalogExported")=="PASS"
    recipe_config=workspace/"config/inscribed/recipes.json"
    assert json.loads(recipe_config.read_text(encoding="utf-8"))==defaults
    options=workspace/"config/inscribed-server.toml"
    old_options=tomllib.loads((backup/"inscribed-server.toml").read_text(encoding="utf-8"))
    new_options=tomllib.loads(options.read_text(encoding="utf-8"))
    for section,values in old_options.items():
        for key,value in values.items():assert new_options[section][key]==value,(section,key)
    assert all(k in new_options for k in ("altar","ritual","anvil","inspection","visuals","sounds"))
    configuration={"recipes":sha(recipe_config),"options":sha(options),"existingOptionsPreserved":True,"authorizedChanges":sorted(authorized)}
installed = workspace/"mods"/jar.name
if installed.exists():
    assert sha(installed) == sha(jar)
    assert sorted(f.name for f in (workspace/"mods").glob("inscribed-*.jar")) == [jar.name]
source_changes = []
for f in [*project.joinpath("src").rglob("*"), *project.joinpath("tools").glob("*.py"), *project.joinpath("docs").glob("*.md"), project/"README.md", project/"build.gradle", project/"gradle.properties"]:
    if not f.is_file(): continue
    relative = f.relative_to(project)
    old = backup/relative
    if not old.is_file() or sha(old) != sha(f): source_changes.append(str(relative).replace("\\", "/"))
manifest = {
    "release": f"Inscribed {version} modpack recipe/config authoring" if config else f"Inscribed {version} ingredient-selected extraction" if risk else "Inscribed 02 Ritual Selection + Extraction + Anvil Rules 0.2.0", "date": "2026-10-13",
    "jar": {"path": str(jar.relative_to(workspace)).replace("\\", "/"), "bytes": jar.stat().st_size, "sha256": sha(jar), "installed": installed.exists()},
    "tests": {"junit": junit, "gameTestsPerRuntime": counts, "runtimes": ["Inscribed alone", "Inscribed + Create 6.0.10 + LSBC 0.21.0"], "nativeClient": client},
    "coverage": {"totalRecipes": 131, "vanilla": 125, "create": 6, "infusion": 131, "extraction": 131},
    "protectedFiles": {"count": len(before), "changed": changed, "snapshot": str((backup/"protected-before-install.json").relative_to(workspace)).replace("\\", "/")},
    "backupJarSha256": sha(old_jar), "changedProjectFiles": sorted(source_changes),
    "configuration":configuration,
    "evidence": {str(f.relative_to(project)).replace("\\", "/"): sha(f) for f in [log, client_log, build_log, *folder.iterdir()] if f.is_file()},
}
(project/"docs/INSCRIBED_RELEASE_MANIFEST.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2)+"\n", encoding="utf-8")
print(json.dumps({"jar": manifest["jar"], "gameTests": counts, "junit": junit, "nativeClient": client["status"], "protectedFiles": len(before), "changed": changed}, indent=2))
