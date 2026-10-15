# Inscribed 08 - final candidate report

Final release gate: **PASS**. Local version **0.7.2-rc.1**, following the established version scheme. Feature freeze is preserved: no production Java/gameplay change, no balance change, no automatic publication. [Checklist](INSCRIBED_RELEASE_CHECKLIST.md) / [evidence](rc08/RESULTS.json).

| Required final output | Result |
| --- | --- |
| 1. RC version | 0.7.2-rc.1 |
| 2. Commit/base | No Git repository, branch or commit. Base 0.7.1; SHA-256 `d53a3ce52ca29153512d5efb6ebe18bd353242b4a1c10b0e03271ce00203bcbc`; source/build hashes recorded before edits. |
| 3. Minecraft/NeoForge | 1.21.1 / 21.1.255; Java 21; NeoForm official mappings 20240808.144430. |
| 4. Dependency matrix | Required Minecraft/NeoForge; optional JEI/LSBC/Create/Ponder; development-only JUnit/jsonschema. [Ranges/versions](RELEASE_NOTES_0.7.2-rc.1.md). |
| 5. Core checklist | Physical infusion/extraction, exact level, books, unit costs, power, Catalyst, shift interaction, no XP/lapis reroll, restricted anvil and destructive grindstone. PASS. |
| 6. Recipe audit | 131 canonical files, 262 rituals, 42 Vanilla + 2 Create enchantments, every valid level/both modes. PASS. |
| 7. Loot audit | 13 pools, 24 target tables, valid probabilities/weights/levels; runtime modifier, source themes, toggles, multipliers and reload. PASS. Values unchanged. |
| 8. Catalyst | Registration, localization, loot, creative item, model, Guide/JEI cost and actual ritual consumption. PASS. |
| 9. Pedestal | Unit invariant, persistence, interaction/break, renderer, automation/movement blocking and dedicated safety. PASS. |
| 10. Audiovisual | Infusion/reversed extraction/completion/cancel, positional ambient cleanup and models. PASS; full-bright item light only. |
| 11. JEI | Categories, constraints, costs/Catalyst, all visibility modes, hide/reuse, disable/restore, actual example-pack reload. PASS; indexing limits documented. |
| 12. LSBC | KnownWords/partial names, independent knowledge, shared two-page geometry, Codex tab, bridge toggles. PASS. |
| 13. Create | Conditional catalog, real registered item/enchantments, rituals/JEI and EN/PT Ponder/replay; absent-provider startup. PASS. |
| 14. Singleplayer | Seven native compatibility runs, survival packet smoke and audiovisual checks. PASS. |
| 15. Multiplayer | Two actual clients, simultaneous attempt/invalid selection, departure/reconnect, one output/private knowledge. PASS. |
| 16. Dedicated | A-G: 100 tests each; release-only server starts without QA/client-class crash. PASS. |
| 17. Save/load | Idle target/pedestal/knowledge and active save across separate JVMs. Active ritual cancels preserving inputs. PASS. |
| 18. Upgrade | Prior-build native worlds open; IDs/schema/config overlays unchanged; no automatic rewrite. PASS. |
| 19. Fresh install | Release-only startup/default generation and native Guide/infusion/extraction/JEI/restart on new world. PASS. |
| 20. Datapacks | Actual stage-07 examples load, edit/reload/execute/restore; advanced/Create extraction; native JEI updates actual pack edit. PASS. |
| 21. Localization | 98 matching en_us/pt_br keys; native partial names, fallback, captions and GUI. PASS. |
| 22. Resources | Models/own texture references, PNG/logo, lang, pack/meta, original Ponder schematic, borrowed vanilla sounds and no test assets in JAR. PASS. |
| 23. Logging | No temporary production markers/prints/per-tick logs. Expected invalid-content/rollback fixture logs are test-only. Clean release-only startup reviewed. PASS. |
| 24. Performance | 20 simultaneous altars; 56.8 FPS, maximum reported average tick 33.71 ms, total start/scan 56.26 ms; four audible loops and zero remaining. Isolated native test, not full-pack benchmark. |
| 25. Security/dupes | Server authority, reach/spectator/offhand, stale selection/spam, rollback/cancel/reload, automation invariants and actual reconnect/conservation. PASS. |
| 26. Remaining bugs | BLOCKER: none known. HIGH: none known. Existing lower-severity limitations listed below. QA harness interference and clean-server classpath leakage were corrected; neither is bundled. |
| 27. Known limitations | JEI cannot unregister obsolete variants; no stable addon Java API/public ritual events; no ID argument suggestions; manual pedestals; full-bright rendering; third-party provider removal and full-pack/shader scope. |
| 28. Docs | README/changelog/credits/license confirmation, release notes, checklist, configuration/authoring/compatibility/limits, prepared hosting text, notices and backlog. Local links audited. |
| 29. Artifact | `CODEX LSBC/INSCRIBED/releases/0.7.2-rc.1/inscribed-0.7.2-rc.1.jar`, 1020799 bytes. |
| 30. JAR SHA-256 | `2fd597457a755a1000458992c62c96485a7f8cb8d10fbdd2fc8ee9d16b7ca6a5` |
| 31. Official datapack | `content/inscribed_default_datapack`; exported `CODEX LSBC/INSCRIBED/releases/0.7.2-rc.1/inscribed-vanilla-create-0.7.2-rc.1.zip`. MIT LICENSE included. |
| 32. Coverage report | [INSCRIBED_RECIPE_COVERAGE_FINAL.md](INSCRIBED_RECIPE_COVERAGE_FINAL.md) |
| 33. Duplicates | No duplicate file/resource IDs, logical recipes, legacy parallel production files or generated mirror drift. Nine rejection tests include duplicate archive recipe_id. PASS. |
| 34. Final gate | PASS |
| 35. Exact blockers | None known. |
| 36. Recommendation | Review the candidate, conduct independent full-pack playtesting, then promote/tag stable 0.7.2 after approval. No stable tag/public release was created. |

## Evidence and reproducibility

[Build/artifact/source freeze](rc08/ARTIFACT_AUDIT.json), [recipe audit](rc08/RECIPE_AUDIT.json), [negative gates](rc08/NEGATIVE_GATE_TESTS.json), [authoring/resources/docs audit](rc08/RELEASE_AUDIT.json), [native stress](rc08/STRESS.json), [multiplayer](rc08/MULTIPLAYER.json), [smoke](rc08/SMOKE.json) and [all results](rc08/RESULTS.json). Final candidate bytes match the JAR used by runtime tests.

Gradle: `clean test build compileReleaseQaJava`; dedicated: `runReleaseGameTestServer -PrcRunDirectory=runs/rc08/A-server`; native: `runReleaseClient -PrcSmoke=true -PrcRunDirectory=runs/rc08/fresh-client`. The separate releaseQa mod verifies candidate JAR class origin, while `runReleaseServer` uses an empty source set. The test profiles contain explicit QA fixtures; the default smoke disables extraneous fixture recipes and restores its temporary zero-risk override. `runData` and a formatter are not configured, so neither is falsely marked executed.

MIT LICENSE and primary instance recipes/loot/TOML/LSBC hashes remain unchanged. The stable 0.7.1 JAR remains installed in the main instance; only isolated test profiles run the RC. Backups/history are retained outside the distribution. Test worlds, QA screenshots, caches, optional mod binaries and generated scratch outputs are excluded from candidate packaging.

Startup warnings from the isolated environment (offline auth, vanilla command ambiguity, existing shader sampler/mipmap messages) are not missing mod resources. Invalid-entry and deliberate commit-hook exceptions in GameTests are expected test evidence; they are not clean production-startup failures. Automated native tests do not claim human playtesting of the entire modpack.
