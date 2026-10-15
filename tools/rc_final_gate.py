"""Final local release gate. No publication or primary-instance mutation."""
from pathlib import Path
import json,subprocess,sys
P=Path(__file__).resolve().parents[1]
steps=[]
for name in ["rc_recipe_audit.py","rc_gate_negative_tests.py","rc_artifact_audit.py","rc_evidence.py","release08_audit.py"]:
    run=subprocess.run([sys.executable,str(P/"tools"/name)],capture_output=True,text=True)
    steps.append({"step":name,"status":"PASS" if run.returncode==0 else "FAIL","output":run.stdout.strip(),"error":run.stderr.strip()})
    if run.returncode:break
passed=len(steps)==5 and all(s["status"]=="PASS" for s in steps)
report={"status":"PASS" if passed else "FAIL","version":"0.7.2-rc.1","steps":steps,"publication":"not performed","stablePromotion":"after review/approval only"}
(P/"docs/rc08/RELEASE_GATE.json").write_text(json.dumps(report,indent=2),encoding="utf-8")
print(report["status"],"final release gate")
if not passed:print(steps[-1]["error"] or steps[-1]["output"])
sys.exit(0 if passed else 1)
