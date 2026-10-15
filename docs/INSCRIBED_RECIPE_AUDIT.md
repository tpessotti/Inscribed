> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Recipe and progression audit — Inscribed 0.5.0

The existing **131 IDs** remain: 125 vanilla and six Create. The pack was classified and rebalanced, not replaced. Every mode still uses at most eight unit pedestal entries. Book rules, enchantment identities/levels, visibility, optional Create conditions and ritual durations are retained. The user explicitly confirmed that extraction keeps its existing **20% failure risk**; ingredient selection remains deterministic.

Compared with the validated 04 base: all 131 recipes gain tier metadata; 110 change bookshelf power, 119 change infusion book-consumption probability, and 74 change ingredient lists. Catalyst appears in 64 recipes, versus 67 under the preliminary 04 heuristic. Complete before/after objects and rationale are in [05-recipe-rebalance.json](05-recipe-rebalance.json); final costs and sources are in [INSCRIBED_BALANCE_MATRIX.md](INSCRIBED_BALANCE_MATRIX.md) and its JSON counterpart.

| Tier | Recipes | Power in catalog | Infusion book risk | Catalyst / material gate |
| --- | --- | --- | --- | --- |
| 1 Basic | 21 | 2 | 10% | Never Catalyst; existing basic/thematic materials |
| 2 Intermediate | 47 | 8–12 | 20–29% | Usually absent; Channeling uses one |
| 3 Advanced | 45 | 18–22 | 35–47% | Catalyst and a diamond; existing thematic/level material retained |
| 4 Treasure | 12 | 26–28 | 50–56% | Catalyst and a diamond; rare physical knowledge |
| 5 Late game | 6 | 34–36 | 65–71% | Catalyst and netherite; high-value book and strong library |

Tier is per recipe/level: Efficiency I is basic, III intermediate, IV advanced, V late game. Fortune/Looting II and III are advanced. Silk Touch/Mending and selected mobility treasures are tier 4. Wind Burst III, Breach IV and Density V are late game. Curses are intermediate rather than automatically valuable because they are rare. Create I/II are intermediate utility, III advanced; being modded does not automatically imply endgame.

Create classification uses the installed 6.0.10 definitions: Capacity supports pressurized air sources/backtanks; Potato Recovery supports the potato cannon and its ammunition-recovery behavior. No speculative addon enchantments or Create structures are added. Generic registry matching, physical infusion/extraction, optional source entries and JEI remain shared implementations.

Ingredients preserve thematic identity and avoid eight identical expensive materials. Higher tiers gain one diamond/netherite where absent. Over-budget lists reduce repeated amethyst rather than dropping the thematic extraction selector. Heavy-core repetition is reduced to one core per ritual, retaining the mace association without consuming several exceptionally rare cores. All repeated entries still require separate pedestals.

Power is the actual sum of ordinary bookshelf enchantment-power bonuses in the configured cylinder, excluding chiseled bookshelves. There is no cap of 15 and no rigid vanilla corridor, so a 26–36 shelf library is achievable inside default radius/vertical bounds. Knowing a book early does not satisfy power, Catalyst or materials. The tier label itself adds no hidden execution lock.

The static consistency audit reports **zero warnings** for shipped defaults: all recipes have an injected book source, every injected enchantment/level has a recipe, no book exceeds its pool's structure tier, and Catalyst has viable sources. Tables are verified against the local Minecraft JAR. Runtime `/inscribed debug progression report` also considers disabled loot and multipliers and reports missing sources without blocking reload. Vanilla loot, trades and enchanted equipment can provide additional sources outside this injection audit.

Loot and consumption were coordinated: rare tier-4 books use 50–56% risk rather than near-certain loss, while dangerous sources supply repeat opportunities and catalysts. Duplicates are intentionally useful. Default injection supplements existing vanilla loot, so it cannot guarantee that a player never finds an unusually strong vanilla book early; physical ritual infrastructure still prevents trivial application.

Validation covers early/mid/late physical power gates, thematic generated rewards, valid tiers/levels/IDs, missing Catalyst, Create transactions, multipliers, partial-invalid pool entries, operator commands, discovery isolation, actual GLM execution and reload restoration. Multi-hour survival balance and generation of whole world structures remain playtest work; automated tests use actual registered loot tables and seeded injection contexts instead of claiming a survival playthrough.

Save compatibility: original recipe/enchantment/item IDs and persisted altar/knowledge formats stay stable. Custom recipes without `progression_tier` become `UNCLASSIFIED` metadata and continue to use their authored physical requirements. The installer updates only unchanged shipped config entries, preserving customized ones. Custom over-budget costs receive explicit loader errors.
