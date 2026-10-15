# Enchantment recipe schema v1

Machine-readable [JSON Schema](../../schemas/inscribed_enchantment_recipe.schema.json). No `type` discriminator or `format_version` is required in an individual recipe file.

| Field | Requirement / default | Meaning |
| --- | --- | --- |
| `enabled` | optional, true | false disables the ID |
| `enchantment` | required | registered resource ID |
| `level` | required, 1..registered maximum | exact target/result level |
| `minimum_bookshelf_power` | required, 0..1,000,000 | minimum altar power |
| `recipe_visibility` | optional, ALWAYS | ALWAYS / ON_ENCHANTMENT_DISCOVERY / EXPLICIT_DISCOVERY |
| `exact_book_level` | optional, false | require exact reference level instead of >= |
| `progression_tier` | optional, UNCLASSIFIED | TIER_1_BASIC, TIER_2_INTERMEDIATE, TIER_3_ADVANCED, TIER_4_TREASURE, TIER_5_LATE_GAME |
| `infusion` | one or both ritual bodies required | fields below |
| `extraction` | one or both ritual bodies required | fields below |
| `neoforge:conditions` | optional | NeoForge conditional loading |

Infusion requires `ingredients`, `duration_ticks` (1..72000), and `book_consumption_chance` (0..1). Extraction requires `ingredients`, `duration_ticks` and `empty_books_required` (1..64); optional `failure_chance` (0..1, default 0). Each ingredients array expands to at most eight unit costs. Empty arrays are accepted by the loader, allowing recipes without pedestal costs; normal progression recipes should provide meaningful costs.

Ingredient objects accept registered item IDs, item tags, or registered NeoForge custom ingredient codecs. Legacy `count` (1..8) expands into repeated unit costs; the total remains at most eight. New files should repeat entries. Count does not change the one-item pedestal invariant.

The disabled shorthand is `{"enabled":false}`. Runtime validation checks registries, Ingredient decoding, exact numbers and expanded counts. IDE schemas do not replace those checks. See [authoring](../INSCRIBED_DATAPACK_AUTHORING.md).
