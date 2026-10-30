# Changelog

## 0.7.3

Update the guide and altar inspection book geometry and theme, responsive content panels, bilingual text and native guide validation. Remove an unused LSBC guide shim from the publication checkout. Preserve ritual behavior, canonical recipes, loot and extraction failure defaults.

## 0.7.2

Promote the validated 0.7.2-rc.1 to stable. Gameplay classes and canonical recipe/loot definitions are unchanged. Preserve 20% default extraction failure. Add portable development dependency preparation and repository build instructions. RC evidence remains explicitly historical.

## 0.7.2-rc.1

### Changed

- Freeze the existing enchanting core for candidate review; no new gameplay features.
- Consolidate the approved Vanilla + Create production content into one canonical datapack. Generate JAR resources, compatible editable config mirrors and the official standalone ZIP from that source.
- Add a failing coverage/deduplication gate, negative audit tests, and validation against the actual release JAR on dedicated servers and native clients.
- Prepare final release documentation, compatibility evidence and candidate packaging. Preserve all recipe IDs, unit costs, progression values and the approved 20% extraction failure.

### Fixed

- Remove redundant unit-count fields from canonical production recipes without changing costs.
- Separate native QA scripts so they cannot mutate the same altar concurrently. QA fixtures and test-only overrides are excluded from the release JAR.

### Known limitations

See [known limitations](docs/INSCRIBED_KNOWN_LIMITATIONS.md). Human playtesting of the complete target modpack remains recommended before stable publication.

## 0.7.1

### Added

Optional Create/Ponder scene for the Enchanting Table and Infusion Pedestal: physical library, Bookshelf Power, unit reagents, target insertion, Infusion and retrieval. English/Portuguese captions, original schematic and replay checkpoints. Client-only registration; no new mandatory dependency or recipe/balance change.

## 0.7.0

### Added

MIT license; configuration, authoring, integration, quickstart and troubleshooting guides; addon audit; recipe/loot JSON Schemas, templates and minimal/full datapacks; hardening and actual authoring reload tests; same-ID `enabled:false` datapack recipe disabling.

### Changed

Pedestals cannot move through pistons/Create contraptions. Vanilla random enchanting menu actions are rejected, including indirectly opened menus. Payload handlers explicitly use the main thread. Guide target previews cache by recipe generation.

### Fixed

Departure cancels owned rituals without consuming inputs. Persisted Running flags no longer resume zombie animations; client updates retain transient animation state. Identical JEI unlocks/reloads reuse records. Description fallbacks preserve authored Components. Missing loot fields produce useful errors. Rejected Guide spam does not extend successful-request cooldown.

### Known issues

JEI cannot publicly remove changed structural variants before runtime restart. Dynamic light is full-bright rendering, not world light. Pedestals have no automated transfer capability. See the release checklist for verification gates. Default balance and 20% extraction failure are preserved.

## 0.6.1

Matching Guide/Codex geometry, bounded pages, catalog sub-tabs and paired bottom-right page navigation.
