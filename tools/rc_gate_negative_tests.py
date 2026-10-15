"""Mutation tests for the release audit; modifies temporary copies only."""
import contextlib,io,json,shutil,tempfile
from pathlib import Path
import rc_recipe_audit as a
original=a.PACK
results=[]
def run(name,change):
    with tempfile.TemporaryDirectory(prefix="inscribed-rc-gate-",dir=a.P/"build") as folder:
        a.PACK=Path(folder)/"pack";shutil.copytree(original,a.PACK)
        f=a.PACK/"data/inscribed/inscribed/enchantment_recipes/test_sharpness_1.json"
        data=json.loads(f.read_text());change(data,f);f.write_text(json.dumps(data))
        with contextlib.redirect_stdout(io.StringIO()) as output: status=a.audit(False)
        details=json.loads(output.getvalue());assert status==1,name+" was accepted"
        results.append({"mutation":name,"status":"PASS","diagnostic":details["errors"][0]})
try:
    run("duplicate logical recipe",lambda d,f: (f.parent/"duplicate.json").write_text(json.dumps(d)))
    run("unknown enchantment",lambda d,f:d.update(enchantment="minecraft:no_such_enchantment"))
    run("invalid level",lambda d,f:d.update(level=99))
    run("nine ingredients",lambda d,f:d["infusion"].update(ingredients=[{"item":"minecraft:iron_ingot"}]*9))
    run("multi-count pedestal",lambda d,f:d["infusion"]["ingredients"][0].update(count=2))
    run("missing extraction",lambda d,f:d.pop("extraction"))
    run("invalid probability",lambda d,f:d["extraction"].update(failure_chance=1.1))
    run("unknown ingredient",lambda d,f:d["infusion"]["ingredients"][0].update(item="minecraft:no_such_item"))
finally: a.PACK=original
(a.OUT/"NEGATIVE_GATE_TESTS.json").write_text(json.dumps({"status":"PASS","tests":results},indent=2))
import zipfile,warnings
from rc_artifact_audit import validate_archive
with tempfile.TemporaryDirectory(prefix="inscribed-duplicate-id-",dir=a.P/"build") as folder:
    file=Path(folder)/"duplicate.zip";name="data/inscribed/inscribed/enchantment_recipes/same_id.json"
    with warnings.catch_warnings():
        warnings.simplefilter("ignore",UserWarning)
        with zipfile.ZipFile(file,"w") as z:z.writestr(name,"{}");z.writestr(name,"{}")
    try:validate_archive(file)
    except AssertionError as e:results.append({"mutation":"duplicate archive recipe_id","status":"PASS","diagnostic":str(e)})
    else:raise AssertionError("duplicate archive recipe_id accepted")
(a.OUT/"NEGATIVE_GATE_TESTS.json").write_text(json.dumps({"status":"PASS","tests":results},indent=2))
print("PASS:",len(results),"audit rejection tests")
