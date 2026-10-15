> SUPERSEDED: historical 0.7.1 evidence. Current RC Ponder tests are recorded in [RC results](../rc08/RESULTS.json).

# Inscribed 0.7.1 - optional Create/Ponder

The Enchanting Table and Infusion Pedestal now register the `inscribed:enchanting_setup` storyboard when Create and Ponder are present on the client. The 725-tick tutorial demonstrates default Sharpness I: two ordinary bookshelves, one physical library book, two separate amethysts and one iron, target placement, Shift + right-click, five-second Infusion and empty-hand retrieval.

Texts are localized in English and Portuguese; the original 7x7 schematic is resource-pack replaceable. This is a visual default-recipe example and does not change real inventories, grant knowledge or invoke server rituals. It explicitly directs modpack users to their actual Guide/JEI recipes because overrides can change costs and chances.

Registration uses a client-only guarded adapter. The optional Ponder plugin/scenes are resolved only after Create and Ponder are detected. No Ponder/Create classes are bundled. Existing recipes, loot, 57 config options, LSBC behavior and 20% default extraction failure are unchanged. MIT remains the Inscribed license.

Validation covers native scene registration for both items, physical book placement, individual pedestal materials, running visuals, exact Sharpness I result, input consumption inside the scene and replay restoration. Caption numbering is checked against Ponder's own generated English defaults; every PT/EN caption is checked for a real localized entry. Translation generation uses a world-less storyboard, so registry-dependent item enchantments are deferred to scene instructions.

The native test fixture is itself behind an optional-provider adapter; it is excluded from the production JAR. Screenshots were checked to ensure that the Chiseled Bookshelf shows its occupied front face and the captions match the current demonstration step.

See [usage and resource paths](../INSCRIBED_CREATE_PONDER.md). Final test results and release hash are recorded in `RESULTS.json` beside this report. Backups are in `CODEX LSBC/INSCRIBED/backups/2026-10-14-pre-ponder`; the 0.7.0 release archive remains available.

Final results: build PASS, 4 JUnit tests, 100 dedicated GameTests without optional mods and 100 with Create/LSBC/JEI. Native client without Create/Ponder PASS; native Ponder EN/PT PASS. JAR isolation/license/resource checks PASS. The 0.7.1 JAR is installed; user configs and LSBC 0.22.1 hashes were preserved.
