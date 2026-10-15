# Release checklist - Inscribed 0.7.2-rc.1

Final candidate gate: **PASS**. All entries use PASS, N/A or KNOWN LIMITATION. This is a local candidate, not a stable publication. Evidence: [RC report](INSCRIBED08_REPORT.md), [runtime matrix](INSCRIBED_QA_MATRIX.md), [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md) and [structured results](rc08/RESULTS.json).

| Prompt item | Requirement | Result | Evidence / scope |
| --- | --- | --- | --- |
| 1 | Princípio desta etapa | PASS | RC report, runtime matrix and artifact/source audits. |
| 2 | Objetivo geral | PASS | RC report, runtime matrix and artifact/source audits. |
| 3 | Release state | PASS | No Git repository; baseline hashes recorded. Source/resources/artifact audited; development worlds/fixtures excluded from distribution. |
| 4 | Version freeze | PASS | RC report, runtime matrix and artifact/source audits. |
| 5 | Build metadata | PASS | RC report, runtime matrix and artifact/source audits. |
| 6 | Dependency audit | PASS | RC report, runtime matrix and artifact/source audits. |
| 7 | Core feature freeze checklist | PASS | RC report, runtime matrix and artifact/source audits. |
| 8 | Vanilla enchanting bypass | PASS | RC report, runtime matrix and artifact/source audits. |
| 9 | Grindstone | PASS | RC report, runtime matrix and artifact/source audits. |
| 10 | Recipe pack final audit | PASS | Coverage: 131 files, 262 rituals, 42 Vanilla + 2 Create; audit and nine negative gate tests; no duplicates. |
| 10A | Datapack final obrigatório — Vanilla + Create | PASS | Coverage: 131 files, 262 rituals, 42 Vanilla + 2 Create; audit and nine negative gate tests; no duplicates. |
| 11 | Recipe coverage | PASS | Coverage: 131 files, 262 rituals, 42 Vanilla + 2 Create; audit and nine negative gate tests; no duplicates. |
| 12 | Loot final audit | PASS | RC report, runtime matrix and artifact/source audits. |
| 13 | Catalyst final check | PASS | RC report, runtime matrix and artifact/source audits. |
| 14 | Pedestal final check | PASS | RC report, runtime matrix and artifact/source audits. |
| 15 | Ritual audiovisual final check | PASS | RC report, runtime matrix and artifact/source audits. |
| 16 | Navigator’s Codex final compatibility | PASS | Native Guide/Codex, EN/PT, item models, shared bounds and 24 aspect samples. |
| 17 | Inscribed Guide final check | PASS | Native Guide/Codex, EN/PT, item models, shared bounds and 24 aspect samples. |
| 18 | Placeholder assets | PASS | Native Guide/Codex, EN/PT, item models, shared bounds and 24 aspect samples. |
| 19 | JEI final check | PASS | RC report, runtime matrix and artifact/source audits. |
| 20 | JEI recipe visibility | PASS | RC report, runtime matrix and artifact/source audits. |
| 21 | LSBC final check | PASS | RC report, runtime matrix and artifact/source audits. |
| 22 | Create final check | PASS | RC report, runtime matrix and artifact/source audits. |
| 23 | Dedicated server release test | PASS | Seven native/dedicated cases; real two-player test; fresh/idle/active JVM restart; native packet smoke (automated, not a human playtest claim). |
| 24 | Multiplayer release test | PASS | Seven native/dedicated cases; real two-player test; fresh/idle/active JVM restart; native packet smoke (automated, not a human playtest claim). |
| 25 | Save/load final test | PASS | Seven native/dedicated cases; real two-player test; fresh/idle/active JVM restart; native packet smoke (automated, not a human playtest claim). |
| 26 | Upgrade test | PASS | Seven native/dedicated cases; real two-player test; fresh/idle/active JVM restart; native packet smoke (automated, not a human playtest claim). |
| 27 | Fresh install test | PASS | Seven native/dedicated cases; real two-player test; fresh/idle/active JVM restart; native packet smoke (automated, not a human playtest claim). |
| 28 | Datapack test | PASS | Actual example pack/edit/reload/JEI and extraction; invalid-fixture diagnostics; optional integration/loot toggles. |
| 29 | Invalid datapack test | PASS | Actual example pack/edit/reload/JEI and extraction; invalid-fixture diagnostics; optional integration/loot toggles. |
| 30 | Localization final pass | PASS | Native Guide/Codex, EN/PT, item models, shared bounds and 24 aspect samples. |
| 31 | UI text overflow | PASS | Native Guide/Codex, EN/PT, item models, shared bounds and 24 aspect samples. |
| 32 | Logging final pass | PASS | RC report, runtime matrix and artifact/source audits. |
| 33 | Debug code audit | PASS | RC report, runtime matrix and artifact/source audits. |
| 34 | Dead code | PASS | RC report, runtime matrix and artifact/source audits. |
| 35 | Resource audit | PASS | RC report, runtime matrix and artifact/source audits. |
| 36 | Sound audit | PASS | RC report, runtime matrix and artifact/source audits. |
| 37 | Datagen audit | N/A | No configured datagen or formatter/linter; hand-authored resources validated directly. |
| 38 | Config audit | PASS | RC report, runtime matrix and artifact/source audits. |
| 39 | Server config synchronization | PASS | RC report, runtime matrix and artifact/source audits. |
| 40 | Optional integration toggles | PASS | Actual example pack/edit/reload/JEI and extraction; invalid-fixture diagnostics; optional integration/loot toggles. |
| 41 | Performance release pass | PASS | 20 concurrent native altars; measured frame/tick/scan times, exact outputs and zero orphan loops; bounded scans in source. |
| 42 | Multiple altar stress test | PASS | 20 concurrent native altars; measured frame/tick/scan times, exact outputs and zero orphan loops; bounded scans in source. |
| 43 | Security final pass | PASS | RC report, runtime matrix and artifact/source audits. |
| 44 | Automation interaction | PASS | RC report, runtime matrix and artifact/source audits. |
| 45 | Creative mode | PASS | RC report, runtime matrix and artifact/source audits. |
| 46 | Spectator/adventure | PASS | RC report, runtime matrix and artifact/source audits. |
| 47 | Commands | KNOWN LIMITATION | Permissioned admin commands; ID suggestions absent. No stable Java addon API or custom public events; data formats are supported. |
| 48 | Public API audit | KNOWN LIMITATION | Permissioned admin commands; ID suggestions absent. No stable Java addon API or custom public events; data formats are supported. |
| 49 | Events | KNOWN LIMITATION | Permissioned admin commands; ID suggestions absent. No stable Java addon API or custom public events; data formats are supported. |
| 50 | README final | PASS | RC report, runtime matrix and artifact/source audits. |
| 51 | CurseForge description | PASS | Hosting descriptions prepared; local artifacts staged; no publication. |
| 52 | Modrinth description | PASS | Hosting descriptions prepared; local artifacts staged; no publication. |
| 53 | Changelog | PASS | RC report, runtime matrix and artifact/source audits. |
| 54 | Release notes | PASS | RC report, runtime matrix and artifact/source audits. |
| 55 | Known limitations | PASS | RC report, runtime matrix and artifact/source audits. |
| 56 | Future backlog | PASS | RC report, runtime matrix and artifact/source audits. |
| 57 | Credits | PASS | RC report, runtime matrix and artifact/source audits. |
| 58 | License | PASS | Original MIT LICENSE unchanged; present in JAR/datapack/support package. Third-party binaries not bundled. |
| 59 | Metadata final | PASS | RC report, runtime matrix and artifact/source audits. |
| 60 | Icon/logo | PASS | RC report, runtime matrix and artifact/source audits. |
| 61 | Build output naming | PASS | RC report, runtime matrix and artifact/source audits. |
| 62 | Sources/dev artifacts | PASS | RC report, runtime matrix and artifact/source audits. |
| 63 | Clean build | PASS | RC report, runtime matrix and artifact/source audits. |
| 64 | SHA/checksum | PASS | RC report, runtime matrix and artifact/source audits. |
| 65 | Dependency licenses | PASS | Original MIT LICENSE unchanged; present in JAR/datapack/support package. Third-party binaries not bundled. |
| 66 | Release matrix | PASS | RC report, runtime matrix and artifact/source audits. |
| 66A | Recipe datapack release gate | PASS | Coverage: 131 files, 262 rituals, 42 Vanilla + 2 Create; audit and nine negative gate tests; no duplicates. |
| 67 | Bug gate | PASS | RC report, runtime matrix and artifact/source audits. |
| 68 | RC iteration | N/A | No production BLOCKER/HIGH found requiring rc.2; QA/build isolation corrections were completed. |
| 69 | Release checklist final | PASS | RC report, runtime matrix and artifact/source audits. |
| 70 | Release candidate output | PASS | Hosting descriptions prepared; local artifacts staged; no publication. |
| 71 | Manual smoke test | PASS | Seven native/dedicated cases; real two-player test; fresh/idle/active JVM restart; native packet smoke (automated, not a human playtest claim). |
| 72 | Smoke test with LSBC | PASS | Seven native/dedicated cases; real two-player test; fresh/idle/active JVM restart; native packet smoke (automated, not a human playtest claim). |
| 73 | Smoke test with Create | PASS | Seven native/dedicated cases; real two-player test; fresh/idle/active JVM restart; native packet smoke (automated, not a human playtest claim). |
| 74 | Smoke test loot | PASS | RC report, runtime matrix and artifact/source audits. |
| 75 | Documentation link validation | PASS | RC report, runtime matrix and artifact/source audits. |
| 76 | No superseded docs in root | PASS | RC report, runtime matrix and artifact/source audits. |
| 77 | Code formatting | N/A | No configured datagen or formatter/linter; hand-authored resources validated directly. |
| 78 | Git diff review | PASS | No Git repository; baseline hashes recorded. Source/resources/artifact audited; development worlds/fixtures excluded from distribution. |
| 79 | No local machine paths | PASS | No Git repository; baseline hashes recorded. Source/resources/artifact audited; development worlds/fixtures excluded from distribution. |
| 80 | No temporary assets | PASS | No Git repository; baseline hashes recorded. Source/resources/artifact audited; development worlds/fixtures excluded from distribution. |
| 81 | Test data separation | PASS | No Git repository; baseline hashes recorded. Source/resources/artifact audited; development worlds/fixtures excluded from distribution. |
| 82 | Dev commands | PASS | No Git repository; baseline hashes recorded. Source/resources/artifact audited; development worlds/fixtures excluded from distribution. |
| 83 | Release artifact validation | PASS | No Git repository; baseline hashes recorded. Source/resources/artifact audited; development worlds/fixtures excluded from distribution. |
| 84 | Startup log review | PASS | No Git repository; baseline hashes recorded. Source/resources/artifact audited; development worlds/fixtures excluded from distribution. |
| 85 | Crash test recovery | PASS | RC report, runtime matrix and artifact/source audits. |
| 86 | Save backup note | PASS | RC report, runtime matrix and artifact/source audits. |
| 87 | Compatibility statement | PASS | RC report, runtime matrix and artifact/source audits. |
| 88 | Player-facing feature freeze | PASS | RC report, runtime matrix and artifact/source audits. |
| 89 | v0.1.0 definition | PASS | RC report, runtime matrix and artifact/source audits. |
| 90 | O que NÃO entra no 0.1.0 | PASS | RC report, runtime matrix and artifact/source audits. |
| 91 | Final quality bar | PASS | RC report, runtime matrix and artifact/source audits. |
| 92 | Release blocker criteria | PASS | RC report, runtime matrix and artifact/source audits. |
| 93 | High severity criteria | PASS | RC report, runtime matrix and artifact/source audits. |
| 94 | Medium/Low | PASS | RC report, runtime matrix and artifact/source audits. |
| 95 | Final deliverables | PASS | RC report, runtime matrix and artifact/source audits. |
| 96 | Release artifact | PASS | RC report, runtime matrix and artifact/source audits. |
| 97 | Do not publish automatically | PASS | Hosting descriptions prepared; local artifacts staged; no publication. |
| 98 | Critério de aprovação | PASS | RC report, runtime matrix and artifact/source audits. |
| 99 | Saída final obrigatória | PASS | RC report, runtime matrix and artifact/source audits. |

No known BLOCKER/HIGH remains. Known limitations are documented, not silently marked fixed. Native smoke uses actual Minecraft packets; independent human full-pack playtesting remains recommended before stable promotion. The main instance retains its stable 0.7.1 JAR and customized configs; the RC is installed in isolated test profiles and staged locally.
