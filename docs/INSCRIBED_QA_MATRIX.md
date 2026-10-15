# QA matrix - Inscribed 0.7.2-rc.1

All RC runtime processes load production from the real candidate JAR through a separate development-only QA module. The final clean-built JAR is byte-identical to the tested artifact. The release-only startup uses an empty source set and contains no QA module. [Structured evidence](rc08/RESULTS.json).

| Case | Optional mods | Dedicated GameTests | Native Guide/integration |
| --- | --- | --- | --- |
| A | Inscribed only | PASS (100) | PASS |
| B | + JEI | PASS (100) | PASS |
| C | + LSBC | PASS (100) | PASS |
| D | + LSBC + JEI | PASS (100) | PASS |
| E | + Create | PASS (100) | PASS |
| F | + Create + JEI | PASS (100) | PASS |
| G | + Create + LSBC + JEI | PASS (100) | PASS |

Minecraft 1.21.1, NeoForge 21.1.255, Java 21, JEI 19.57.0.451, LSBC 0.22.1, Create 6.0.10 and its Ponder 1.0.82+mc1.21.1. Seven cases total 700 GameTest executions; these are 100 test cases repeated across the matrix, not 700 distinct tests. Four JUnit tests also passed.

## Additional actual RC checks

- Real two-client multiplayer: concurrent start/invalid selection attempt, actor disconnect/reconnect, one exact-level output, preserved cancelled inputs and private knowledge. PASS.
- Fresh release-only dedicated startup with only the RC JAR: 125 vanilla recipes, 13 pools, new normal world and clean save/stop. PASS. The earlier launch that included QA was replaced by this verified execution.
- Native fresh-world smoke through actual survival item-use packets: Guide, physical infusion/extraction, JEI lookup, save, reload and a separate JVM restart. PASS. Positive extraction temporarily used zero risk in that isolated profile and restored the shipped 20% before restart; deterministic failure remains covered by GameTests.
- Active save/restart in a separate JVM: safely idle after restart, unchanged unenchanted target and unit reagents. PASS.
- Prior-build world upgrade in native A-G clients: knowledge/guide/default overlay compatibility, shared Codex/Guide geometry, partial KnownWords, optional Create names, recipe visibility and live JEI. PASS. No schema migration or gameplay source change.
- Full stage-07 example datapack, actual reload/infusion and advanced/Create extraction on dedicated processes. Native example-pack edit/reload updates the JEI recipe duration and hides obsolete variants. PASS.
- Invalid unknown enchantment, nine ingredients and invalid probability: entries rejected with ID/field diagnostics, server continues and tests pass. Expected invalid-fixture and deliberate rollback errors are confined to QA runs.
- Optional toggles: JEI disable/hide/restore, LSBC bridge disable/restore and loot/catalyst/book switches. PASS in their applicable runtime tests.
- Native ritual/models/audio: infusion and reversed extraction, completion, cancellation fade, ambient cleanup, Catalyst and multiple item models. PASS.
- Ponder EN/PT: both registered catalysts, localized captions, actual demonstration output and replay restoration. PASS.
- 24 aspect samples: 1280x720, 3440x1440 and 640x480; GUI 2/3/4/auto, shared book leaves and selection bounds. PASS.
- 20 simultaneous altars: exact outputs, unit reagent consumption and no cross-altar state. Start/scan total 56.26 ms, maximum reported average server tick 33.71 ms, observed 56.8 FPS; 4 audible loops, zero after completion. PASS. Hidden native client/isolated world; not a full-pack or hardware-independent benchmark.

## Scope and remaining issues

No known BLOCKER/HIGH remains. The source audit confirms unchanged production Java and unchanged default gameplay values/IDs. Existing lower-severity limitations are documented in [known limitations](INSCRIBED_KNOWN_LIMITATIONS.md). Human playtesting of the full Rogue Tides pack is recommended and is not falsely claimed by automated native tests. These cases do not claim compatibility with every installed third-party mod or shader.

The [independent tester checklist](qa06/MANUAL_TEST_CHECKLIST.md) remains available. Historical qa06 and Ponder reports retain older evidence; current RC results are in `docs/rc08`.
