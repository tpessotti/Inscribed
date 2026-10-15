> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Extraction - Inscribed 0.2.1

Place the enchanted target on the enchanting table, extraction reagents on pedestals and ordinary minecraft:book in Chiseled Bookshelf slots. Supply minimum bookshelf power and Shift/right-click with an empty hand. A valid extraction starts directly without UI and takes priority over infusion. Multiple extraction matches reject the attempt and require adjusting pedestal reagents. Infusion alone may still use its selector. Inventory books, normal bookshelves, generic containers and Book and Quill are not empty-book receptacles.

A match requires an extraction recipe, registered enchantment holder, exact target enchantment level, sufficient power, reagent allocation and empty books. Curses/treasures have no hardcoded exemption: they work only with a recipe. The current catalog includes vanilla curses/treasures and optional Create holders. Other registered mod/datapack enchantments use the same API; no per-enchantment Java branches.

## Empty books and deterministic output

Scan order is distance, X/Y/Z, then slot. Allocate exactly empty_books_required without double-counting. At least one allocated slot must become completely empty. On success, the first such slot receives one enchanted book containing only the selected enchantment at its original level. On failed extraction, the consumed slots remain empty. Additional required empty books are consumed. Pedestal excess remains. No ground drop or player-inventory insertion is needed.

Vanilla has one book per slot. Modded stack sizes require the chosen greedy allocation to free a slot; this MVP does not search all alternative allocations. Custom receptacle/tag support is a future extension, not implemented.

## Target, cancellation and commit

Copy the target with all components, remove only the selected enchantment holder, preserve all others, custom name, damage and prior-work cost. A Fortune III / Efficiency IV / Unbreaking III pickaxe loses only the chosen enchantment. An enchanted-book target remains that item type when its final enchantment is removed.

During animation nothing is consumed. Shared leases prevent concurrent rituals from consuming the same slots/pedestals. Target/book/reagent changes, removed pedestal/table, insufficient power, changed config, unloaded chunks or datapack generation changes cancel safely. Inputs removed by the player are not recreated; untouched resources remain intact.

Completion rescans and commits synchronously on the server thread. Exact ingredients/books are consumed and the target loses the selected enchantment. On success a book is created; on failure the knowledge is lost. The optional failure_chance defaults to zero for old datapacks; catalog recipes use 0.20. RuntimeException attempts snapshot rollback. This is not multi-chunk disk crash recovery; process/disk failure or third-party rollback-hook failures remain outside the guarantee. Active sessions are not persisted; physical items and existing save schema are preserved.

## Visuals and sounds

Pedestal particles converge on the table. During the second half, knowledge travels from table to the reserved book, with REVERSE_PORTAL on the target. ENCHANTMENT_TABLE_USE starts at lower pitch; AMETHYST_BLOCK_CHIME descends during progress; BOOK_PAGE_TURN marks completion with a bookshelf particle burst. Infusion keeps its previous direction and sounds. All placeholders are vanilla IDs centralized in RitualVisuals. No LSBC dependency or new audio/texture assets.


See EXTRACTION_INGREDIENT_SELECTION.md for unique selectors, failure semantics and current validation evidence.
