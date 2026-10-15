> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Recipe constraints — Inscribed 0.4.0

A ritual uses at most **eight ingredient entries** and **one item per physical pedestal**. Repeated materials require repeated entries on separate pedestals. Infusion and extraction are validated independently. Empty reagent lists remain legal for modpack authors; books are bookshelf requirements, not pedestal entries.

`RecipeParser` expands legacy `count > 1` into unit entries when the effective total remains at most eight. Larger totals and more than eight explicit entries reject that recipe with its ID and the reason in the reload log; other recipes load normally. Counts remain integers and cannot be zero, fractional or strings. Both matchers independently require unit costs and count each pedestal only once. The existing integer max-flow allocator retains deterministic handling of overlapping tags.

New insertion transfers exactly one item from the hand. An occupied pedestal refuses further items, even of the same type. The setter rejects stacks larger than one before mutation. Old saved multi-item stacks remain intact and can be retrieved or dropped on breaking the block. They cannot fund a ritual until removed and split. No upgrade silently truncates inventory. `altar.max_pedestals` may be reduced by a modpack but cannot exceed eight.

The original 131 Vanilla/Create IDs, enchantments, levels, power, duration, book rules, visibility, conditions and failure probabilities are preserved. The audit found **73 mode definitions above eight effective ingredients**, with a maximum of **20**. All 262 resulting mode definitions fit within eight unit entries. Quantities that already fit were expanded; over-budget infusion costs retain amethyst, level-specific material and thematic reagent with reduced quantities. Over-budget extraction retains its unique selector and caps amethyst at six. Advanced rituals additionally use one Enchantment Catalyst.

See [04-recipe-rebalance.json](04-recipe-rebalance.json) for every original/result pair, [04-recipe-audit.json](04-recipe-audit.json) for catalyst IDs, and [the catalog](ENCHANTMENT_RECIPE_CATALOG.md) for the final physical arrangements. Release installation replaces only config entries exactly matching the old shipped defaults. Customized entries are retained; any over-budget custom recipe is rejected clearly until its author rebalances it.

Examples:

```json
"ingredients": [
  {"item":"minecraft:diamond","count":1},
  {"item":"minecraft:diamond","count":1},
  {"item":"inscribed:enchantment_catalyst","count":1}
]
```

Three pedestals, one item each. A legacy `diamond,count:3` has the same physical requirement after parsing. `/inscribed debug recipe audit` checks loaded recipes; consult reload logs for rejected entries because they are absent from the active registry.
