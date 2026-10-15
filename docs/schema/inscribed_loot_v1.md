# Loot injection schema v1

Machine-readable [JSON Schema](../../schemas/inscribed_loot.schema.json). Files live in `data/<namespace>/inscribed/loot_injections/`.

| Field | Requirement / default |
| --- | --- |
| `enabled` | optional true; false disables pool |
| `loot_tables` | 1..64 exact table IDs |
| `chance` | required probability 0..1, evaluated per roll |
| `min_rolls`, `max_rolls` | required integers 0..8; max >= min |
| `entries` | 1..64 weighted objects |
| `require_recipe` | optional false; require recipes for every generated enchantment level |
| `theme` | optional UNSPECIFIED, at most 64 characters; descriptive |
| `structure_tier` | optional 5, integer 1..5; descriptive progression metadata |
| `treasure` | optional false; applies treasure loot multiplier |

Each entry requires `weight` (1..1,000,000). Book entries use `enchantment`, `min_level`, `max_level`, and optionally `item: minecraft:enchanted_book`. Levels must fit the registered enchantment and max >= min. Other item entries require `item`; Catalyst uses `inscribed:enchantment_catalyst`. `min_count` defaults to 1, `max_count` defaults to min; both 1..4 and cannot exceed the item's stack size. `required_mod` optionally skips an entry if its provider is absent.

Effective chance is `min(1, chance * loot_multiplier * treasure_loot_multiplier)` for treasure pools; omit the final multiplier otherwise. Disabled book/Catalyst selections yield no replacement roll. Pools add at most eight results combined per loot generation. Existing opened containers are not repopulated. Missing table resources emit warnings; invalid entries are omitted, and a pool with only invalid entries is rejected.

Examples: [shipwreck](../../examples/inscribed_example_datapack/data/example/inscribed/loot_injections/shipwreck.json), [mineshaft](../../examples/inscribed_example_datapack/data/example/inscribed/loot_injections/mineshaft.json), [high tier](../../examples/inscribed_example_datapack/data/example/inscribed/loot_injections/high_tier.json). Use stable own-namespace IDs. Different IDs add independent pools; same IDs replace before config overlay precedence.
