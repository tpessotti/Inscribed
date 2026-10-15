# Validation tools

For stable 0.7.2, use the [standalone setup](../docs/INSCRIBED_DEV_SETUP.md), then `gradlew clean test build`. The portable production datapack audit (`rc_recipe_audit.py`, retaining its historical name) runs automatically during `build` and validates the complete Vanilla + Create catalog. `prepare_dev_dependencies.py` prepares ignored local provider JARs.

`rc_gate_negative_tests.py`, `rc_artifact_audit.py`, `rc_evidence.py`, `rc_final_gate.py`, `release08_audit.py` and `stage_rc_release.py` preserve the original RC workspace's validation/staging procedure. Several require its baseline backup, runtime evidence directories and hardcoded RC artifact; do not treat them as a portable stable-release pipeline. Historical results are in `docs/rc08`; stable promotion results are in `docs/stable-0.7.2`.

Other generators/audits are SUPERSEDED historical implementation tools. Do not run old catalog/balance/resource generators against frozen production data. They are not build inputs. No optional mod binary or QA/world output belongs in version control or the mod JAR.
