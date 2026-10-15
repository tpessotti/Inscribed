# Release notes - Inscribed 0.7.2-rc.1

Status: local release candidate. The existing version scheme is preserved; stable 0.7.2 is a later promotion after review. Nothing has been published. No new gameplay feature was added.

## Frozen core

Physical enchanted-book libraries, ritual Infusion/Extraction, one-item reagent pedestals, Bookshelf Power, Catalyst progression and configurable structure loot. Infusion applies the recipe level, requires a physical reference book and uses no XP/lapis. Extraction chooses by ingredients and removes only the selected enchantment. The approved default failure chance remains 20%; failure loses that enchantment, reagents and empty book. Cancellation consumes nothing.

The default anvil rules block book-to-item transfer, enchantment merge and upgrades while permitting repair/rename that preserve the first input's enchantments. Grindstone removal remains destructive vanilla behavior, including vanilla curse preservation; it never recovers an enchanted book.

## Build and dependencies

Minecraft 1.21.1; NeoForge 21.1.255; Java 21 (tested Oracle 21.0.9). Official Mojang mappings via NeoForm 1.21.1-20240808.144430; ModDevGradle 2.0.148; Gradle 9.2.1.

| Dependency | Classification | Tested version | Metadata range/side |
| --- | --- | --- | --- |
| Minecraft | REQUIRED | 1.21.1 | [1.21.1], both |
| NeoForge | REQUIRED | 21.1.255 | [21.1.255,), both |
| JEI | OPTIONAL | 19.57.0.451 | [19.57.0,), client |
| LSBC | OPTIONAL | 0.22.1 | [0.21.0,), both |
| Create | OPTIONAL | 6.0.10 | [6.0.10,), both |
| Ponder | OPTIONAL | 1.0.82+mc1.21.1 | [1.0.82,), client; supplied by tested Create |
| Flywheel / Registrate | Create-owned | 1.0.6 / bundled provider | No direct Inscribed dependency or redistribution |
| JUnit / jsonschema | DEV ONLY | 5.11.4 / installed validation tool | Not bundled |

The declared ranges are loader constraints, not a claim that every newer provider version was tested. See [compatibility](INSCRIBED_COMPATIBILITY.md), [QA matrix](INSCRIBED_QA_MATRIX.md) and [third-party notices](../THIRD_PARTY_NOTICES.md).

## Canonical content

`content/inscribed_default_datapack` is the single production source. It contains 131 recipe files (262 ritual definitions), all levels of 42 vanilla and two Create enchantments, 13 loot pools targeting 24 structure tables, crafting, tags and integration data. No supported level is missing; no intentional exclusions or alternative defaults remain. Create definitions are conditional and safely absent without the provider. The JAR embeds identical data; editable default config mirrors and the official ZIP are generated from that source.

`inscribed:test_sharpness_1` is a retained historical production ID, not a leaked QA recipe. Examples and development fixtures are separate and absent from the JAR. See [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

## Upgrade and installation

Back up saves and `config/inscribed` before upgrading. Replace the old Inscribed JAR with this candidate on client and server, keeping one Inscribed version. Item/block IDs, recipe IDs, knowledge attachment format, v1 JSON schema, config keys and gameplay defaults are preserved. Customized overlays are not overwritten. Explicit JSON config entries override same-ID datapacks; remove an entry deliberately before overriding it through a pack. Installing the official ZIP is optional because its data is already embedded in the mod; it replaces the same IDs rather than adding duplicates.

Active rituals safely cancel on restart, preserving uncommitted target and ingredients. Knowledge, idle target and pedestal inventories persist. Unknown enchantment components can be discarded by Minecraft when their provider is removed; restore the original backup before reintroducing providers.

## Artifacts

- Candidate: `inscribed-0.7.2-rc.1.jar`, 1020799 bytes, SHA-256 `2fd597457a755a1000458992c62c96485a7f8cb8d10fbdd2fc8ee9d16b7ca6a5`.
- Official pack: `inscribed-vanilla-create-0.7.2-rc.1.zip`, 131964 bytes, SHA-256 `221fe2ac6cb24b9d5c5e4f4616b1fd850ae29269a7aecfa737236c582ac9de33`.
- Local staging: `CODEX LSBC/INSCRIBED/releases/0.7.2-rc.1` in the workspace.

The final clean build produced byte-identical JAR content to the candidate used by the runtime matrix. MIT LICENSE was confirmed unchanged.

## Limits and review

See [known limitations](INSCRIBED_KNOWN_LIMITATIONS.md). There is no stable addon Java API or custom public ritual event. JEI can hide obsolete variants but cannot unregister them through its public API. ID argument completion is limited to command syntax/player suggestions. Full-bright items do not propagate world lighting; pedestal automation is intentionally blocked.

The native smoke is automated through actual Minecraft item-use packets, not a claim of human playtesting. The seven-case matrix tests a dependency subset, not every mod in Rogue Tides. Human playtesting of the complete pack remains recommended before stable promotion. New ideas belong to [future backlog](FUTURE_IDEAS.md).
