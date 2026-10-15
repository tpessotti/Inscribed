> SUPERSEDED: historical implementation evidence. Current candidate: [release notes](RELEASE_NOTES_0.7.2-rc.1.md) and [checklist](INSCRIBED_RELEASE_CHECKLIST.md).

# Inscribed 07 - final authoring and release report

Version: **0.7.0**. License: **MIT**, explicitly selected by the owner. Nothing has been published. This report consolidates authoring preparation with the preceding QA/hardening work.

## Delivered authoring surface

| Requested deliverable | Result / reference |
| --- | --- |
| Config audit, removed/renamed keys, canonical layout | 57 used SERVER options; none removed/renamed. TOML global rules plus compatible recipe/crafting and loot JSON overlays. [Reference](INSCRIBED_CONFIGURATION.md), [inventory](INSCRIBED_CONFIG_INVENTORY.json) |
| Recipe schema | Official v1 [field reference](schema/inscribed_enchantment_recipe_v1.md) and [JSON Schema](../schemas/inscribed_enchantment_recipe.schema.json) |
| Loot schema | Official v1 [field reference](schema/inscribed_loot_v1.md) and [JSON Schema](../schemas/inscribed_loot.schema.json) |
| Validation improvements | Disabled datapack recipe shorthand; clear enabled/probability/loot-field errors; ID-specific rejected content; missing translation fallback |
| Example recipes | Sharpness I without Catalyst, Fortune III with Catalyst/extraction, conditional real Create Capacity |
| Example loot | Shipwreck Respiration/Aqua Affinity/rare Catalyst; mineshaft Efficiency/Fortune/Unbreaking; ancient-city/stronghold treasure/Catalyst |
| Example/minimal datapacks | [Full example](../examples/inscribed_example_datapack/README.md), [minimal example](../examples/minimal/README.md), [copy/paste templates](../templates/recipe_basic.json) |
| Authoring/config/integration guides | [Authoring](INSCRIBED_DATAPACK_AUTHORING.md), [configuration](INSCRIBED_CONFIGURATION.md), [integrations](INSCRIBED_INTEGRATIONS.md) |
| Quickstart and troubleshooting | [5-10 minute workflow](INSCRIBED_MODPACK_QUICKSTART.md), [troubleshooting and real administrator commands](INSCRIBED_TROUBLESHOOTING.md) |
| Compatibility and limitations | [Tested versions/table](INSCRIBED_COMPATIBILITY.md), [limitations](INSCRIBED_KNOWN_LIMITATIONS.md) |
| README and hosting descriptions | [Player README](../README.md), [prepared CurseForge/Modrinth/GitHub descriptions](INSCRIBED_RELEASE_DESCRIPTIONS.md); not published |
| Changelog and credits | [Changelog](../CHANGELOG.md), [credits](../CREDITS.md), [MIT LICENSE](../LICENSE) |
| Metadata/logo audit | Name Inscribed, modid inscribed, version 0.7.0, MIT; correct required MC/NeoForge and optional JEI/LSBC/Create ranges; valid 1254x1254 PNG logo; no invented homepage/issue URL |
| Translation/resource/sound audit | 89 matching EN/PT keys; standard replaceable resource paths and existing vanilla sound IDs [documented](INSCRIBED_INTEGRATIONS.md) |
| Addon/API/localization extension | [Internal experimental API audit](INSCRIBED_ADDON_API.md); no invented public events. Guide arrays use resource packs, not datapacks; registered modded enchantments use recipes without Java changes |
| Release checklist | [Completed release gates](INSCRIBED_RELEASE_CHECKLIST.md) |
| Files changed | [Machine-readable list](qa06/CHANGED_FILES.json) relative to the pre-QA/authoring backup |

Existing JSON overlays were retained because the earlier modpack requirement explicitly allows all recipes/configuration through config. New content is recommended through datapacks. **Same-ID explicit config entries take precedence**; documented empty overlays allow intentional datapack ownership. No silent user-config migration or default-balance rewrite occurs.

Maximum eight expanded unit ingredients and one item per pedestal remain invariants. Legacy count is supported within that total. The default 131 recipes (125 vanilla + six conditional Create), 13 loot pools, recipe IDs and 20% extraction failure are unchanged. Visibility is presentation; physical books remain required. No new ritual, worldgen, merchant overhaul, EMI/REI or enchantment was added.

## Author experience and validation

Actual example pack tests with and without Create passed **3/3 each**: load recipes/loot/conditions, execute the physical basic recipe, edit its enchantment, reload, execute the edited recipe, restore and reload. Native Guide/JEI checks separately verified visibility, forget/relearn and changed-content reload through the public runtime APIs. The workflow's main confusion was same-ID config precedence; the quickstart uses a new own-namespace ID to avoid it.

Fresh dedicated environments generated defaults and started without optional mods. Prior-build client save copies opened successfully and Guide right-click worked. Removing/reinstalling Create on a copied save retained ID-based knowledge and filtered/restored active views; unknown physical components remain subject to vanilla behavior. [Full QA evidence](INSCRIBED_QA_MATRIX.md).

Final clean build passed, with **4/4 JUnit tests**, **100/100 dedicated GameTests** without optional mods and **100/100** with Create + LSBC + JEI. The full A-G matrix previously passed 100 tests in each dedicated combination and native client checks in all seven. Two real localhost players passed concurrency, disconnect/reconnect, inventory conservation and private knowledge. Native aspect/audio/model checks passed. Native Portuguese Guide/JEI also passed.

The read-only `python tools/release07_audit.py` audit validates examples, templates and default content, checks EN/PT keys, current public-document links, PNG metadata and release JAR contents. It requires Python plus jsonschema for this developer audit only, not to run the mod. No runData task exists; hand-authored files were validated directly. [Audit JSON](qa06/RELEASE07_AUDIT.json).

## Remaining limitations and next step

No reproduced BLOCKER/HIGH is open in the executed scope. JEI retains hidden changed structural variants until its runtime restarts; global book search is not a knowledge privacy boundary. Automated item transfer, contraption movement and actual world dynamic lighting are unsupported. Third-party shader/protection/automation combinations are not certified by the base matrix.

The release support archive contains current documentation, schemas, templates and example datapacks. Examples are not installed into the user's world automatically because their additional recipes/loot intentionally change balance. The local candidate is ready for ordinary human playtesting in the full Rogue Tides pack and review before publication.

## Local installation

Installed `mods/inscribed-0.7.0.jar`; only the previous Inscribed 0.6.1 JAR was replaced. LSBC 0.22.1 and existing TOML/recipe/loot configuration hashes were preserved. Backup: `CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-qa-authoring`. Candidate JAR and support ZIP are in `CODEX LSBC/INSCRIBED/releases/0.7.0`. No release was published.
