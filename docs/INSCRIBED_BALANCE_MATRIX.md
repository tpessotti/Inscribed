> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Balance matrix — Inscribed 0.5.0

131 existing recipe IDs, per-level tiers. Ordinary bookshelf power is additive in the configured cylinder, with no vanilla cap of 15. Catalyst and rare materials complement this infrastructure gate.

| Enchantment | Level | Tier | Power | Catalyst | Ingredients | Book Risk | Loot Sources |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `create:capacity` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `create:copper_sheet` | 20% | industrial_books, industrial_advanced_books |
| `create:capacity` | 2 | 2 | 10 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:iron_ingot`, 2× `create:copper_sheet` | 23% | industrial_books, industrial_advanced_books |
| `create:capacity` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `create:copper_sheet`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | industrial_advanced_books |
| `create:potato_recovery` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:potato` | 20% | industrial_books, industrial_advanced_books |
| `create:potato_recovery` | 2 | 2 | 10 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:iron_ingot`, 2× `minecraft:potato` | 23% | industrial_books, industrial_advanced_books |
| `create:potato_recovery` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:potato`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | industrial_advanced_books |
| `minecraft:aqua_affinity` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:prismarine_shard` | 20% | ocean_books |
| `minecraft:bane_of_arthropods` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:spider_eye` | 10% | combat_books, combat_advanced_books |
| `minecraft:bane_of_arthropods` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:spider_eye` | 23% | combat_books, combat_advanced_books |
| `minecraft:bane_of_arthropods` | 3 | 2 | 12 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:spider_eye` | 26% | combat_books, combat_advanced_books |
| `minecraft:bane_of_arthropods` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:spider_eye`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books |
| `minecraft:bane_of_arthropods` | 5 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:netherite_ingot`, 3× `minecraft:spider_eye`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 47% | combat_advanced_books |
| `minecraft:binding_curse` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:cobweb` | 20% | combat_books |
| `minecraft:blast_protection` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:gunpowder` | 10% | combat_books, combat_advanced_books |
| `minecraft:blast_protection` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:gunpowder` | 23% | combat_books, combat_advanced_books |
| `minecraft:blast_protection` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:gunpowder`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | combat_advanced_books |
| `minecraft:blast_protection` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:gunpowder`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books |
| `minecraft:breach` | 1 | 3 | 18 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:breeze_rod`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 35% | combat_advanced_books, late_game_books |
| `minecraft:breach` | 2 | 3 | 18 | yes | 2× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:breeze_rod`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 35% | combat_advanced_books, late_game_books |
| `minecraft:breach` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:breeze_rod`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | combat_advanced_books, late_game_books |
| `minecraft:breach` | 4 | 5 | 35 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:breeze_rod`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:netherite_ingot` | 68% | late_game_books |
| `minecraft:channeling` | 1 | 2 | 8 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:lightning_rod`, 1× `inscribed:enchantment_catalyst` | 20% | ocean_books |
| `minecraft:density` | 1 | 3 | 18 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:heavy_core`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 35% | combat_advanced_books, late_game_books |
| `minecraft:density` | 2 | 3 | 18 | yes | 3× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 1× `minecraft:heavy_core`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 35% | combat_advanced_books, late_game_books |
| `minecraft:density` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 1× `minecraft:heavy_core`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | combat_advanced_books, late_game_books |
| `minecraft:density` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 1× `minecraft:heavy_core`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books, late_game_books |
| `minecraft:density` | 5 | 5 | 36 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:netherite_ingot`, 1× `minecraft:heavy_core`, 1× `inscribed:enchantment_catalyst` | 71% | late_game_books |
| `minecraft:depth_strider` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:prismarine_crystals` | 20% | ocean_books, exploration_books |
| `minecraft:depth_strider` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:prismarine_crystals` | 23% | ocean_books, exploration_books |
| `minecraft:depth_strider` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:prismarine_crystals`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | exploration_books |
| `minecraft:efficiency` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:redstone` | 10% | mining_books, tools_advanced_books, late_game_books |
| `minecraft:efficiency` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:redstone` | 23% | mining_books, tools_advanced_books, late_game_books |
| `minecraft:efficiency` | 3 | 2 | 12 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:redstone` | 26% | mining_books, tools_advanced_books, late_game_books |
| `minecraft:efficiency` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:redstone`, 1× `inscribed:enchantment_catalyst` | 43% | tools_advanced_books, late_game_books |
| `minecraft:efficiency` | 5 | 5 | 36 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:netherite_ingot`, 3× `minecraft:redstone`, 1× `inscribed:enchantment_catalyst` | 71% | late_game_books |
| `minecraft:feather_falling` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:feather` | 10% | exploration_books |
| `minecraft:feather_falling` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:feather` | 23% | exploration_books |
| `minecraft:feather_falling` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:feather`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | exploration_books |
| `minecraft:feather_falling` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:feather`, 1× `inscribed:enchantment_catalyst` | 43% | exploration_books |
| `minecraft:fire_aspect` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:blaze_powder` | 10% | combat_books |
| `minecraft:fire_aspect` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:blaze_powder` | 23% | combat_books |
| `minecraft:fire_protection` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:magma_cream` | 10% | combat_books, combat_advanced_books |
| `minecraft:fire_protection` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:magma_cream` | 23% | combat_books, combat_advanced_books |
| `minecraft:fire_protection` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:magma_cream`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | combat_advanced_books |
| `minecraft:fire_protection` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:magma_cream`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books |
| `minecraft:flame` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:blaze_rod` | 20% | combat_books |
| `minecraft:fortune` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:emerald` | 20% | mining_books, tools_advanced_books |
| `minecraft:fortune` | 2 | 3 | 18 | yes | 2× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:emerald`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 35% | tools_advanced_books |
| `minecraft:fortune` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:emerald`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | tools_advanced_books |
| `minecraft:frost_walker` | 1 | 4 | 26 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:blue_ice`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 50% | treasure_books |
| `minecraft:frost_walker` | 2 | 4 | 27 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:iron_ingot`, 2× `minecraft:blue_ice`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 53% | treasure_books |
| `minecraft:impaling` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:prismarine_shard` | 10% | ocean_books, exploration_books |
| `minecraft:impaling` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:prismarine_shard` | 23% | ocean_books, exploration_books |
| `minecraft:impaling` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:prismarine_shard`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | exploration_books |
| `minecraft:impaling` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:prismarine_shard`, 1× `inscribed:enchantment_catalyst` | 43% | exploration_books |
| `minecraft:impaling` | 5 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:netherite_ingot`, 3× `minecraft:prismarine_shard`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 47% | exploration_books |
| `minecraft:infinity` | 1 | 3 | 18 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:ender_eye`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 35% | exploration_books |
| `minecraft:knockback` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:slime_ball` | 10% | combat_books |
| `minecraft:knockback` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:slime_ball` | 23% | combat_books |
| `minecraft:looting` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:ender_pearl` | 20% | combat_books, combat_advanced_books |
| `minecraft:looting` | 2 | 3 | 18 | yes | 2× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:ender_pearl`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 35% | combat_advanced_books |
| `minecraft:looting` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:ender_pearl`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | combat_advanced_books |
| `minecraft:loyalty` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:nautilus_shell` | 20% | ocean_books, exploration_books |
| `minecraft:loyalty` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:nautilus_shell` | 23% | ocean_books, exploration_books |
| `minecraft:loyalty` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:nautilus_shell`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | exploration_books |
| `minecraft:luck_of_the_sea` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:nautilus_shell` | 10% | ocean_books, exploration_books |
| `minecraft:luck_of_the_sea` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:nautilus_shell` | 23% | ocean_books, exploration_books |
| `minecraft:luck_of_the_sea` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:nautilus_shell`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | exploration_books |
| `minecraft:lure` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:tropical_fish` | 10% | ocean_books, exploration_books |
| `minecraft:lure` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:tropical_fish` | 23% | ocean_books, exploration_books |
| `minecraft:lure` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:tropical_fish`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | exploration_books |
| `minecraft:mending` | 1 | 4 | 26 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:echo_shard`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 50% | treasure_books |
| `minecraft:multishot` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:arrow` | 20% | combat_books |
| `minecraft:piercing` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:flint` | 10% | combat_books, combat_advanced_books |
| `minecraft:piercing` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:flint` | 23% | combat_books, combat_advanced_books |
| `minecraft:piercing` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:flint`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | combat_advanced_books |
| `minecraft:piercing` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:flint`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books |
| `minecraft:power` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:string` | 10% | combat_books, combat_advanced_books, late_game_books |
| `minecraft:power` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:string` | 23% | combat_books, combat_advanced_books, late_game_books |
| `minecraft:power` | 3 | 2 | 12 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:string` | 26% | combat_books, combat_advanced_books, late_game_books |
| `minecraft:power` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:string`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books, late_game_books |
| `minecraft:power` | 5 | 5 | 36 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:netherite_ingot`, 3× `minecraft:string`, 1× `inscribed:enchantment_catalyst` | 71% | late_game_books |
| `minecraft:projectile_protection` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:shield` | 10% | combat_books, combat_advanced_books |
| `minecraft:projectile_protection` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 1× `minecraft:shield` | 23% | combat_books, combat_advanced_books |
| `minecraft:projectile_protection` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 1× `minecraft:shield`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | combat_advanced_books |
| `minecraft:projectile_protection` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 1× `minecraft:shield`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books |
| `minecraft:protection` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:leather` | 10% | mining_books, combat_books, combat_advanced_books |
| `minecraft:protection` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:leather` | 23% | mining_books, combat_books, combat_advanced_books |
| `minecraft:protection` | 3 | 2 | 12 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:leather` | 26% | mining_books, combat_books, combat_advanced_books |
| `minecraft:protection` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:leather`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books |
| `minecraft:punch` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:rabbit_foot` | 10% | combat_books |
| `minecraft:punch` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:rabbit_foot` | 23% | combat_books |
| `minecraft:quick_charge` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:redstone` | 10% | combat_books, combat_advanced_books |
| `minecraft:quick_charge` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:redstone` | 23% | combat_books, combat_advanced_books |
| `minecraft:quick_charge` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:redstone`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | combat_advanced_books |
| `minecraft:respiration` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:pufferfish` | 20% | ocean_books, exploration_books |
| `minecraft:respiration` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:pufferfish` | 23% | ocean_books, exploration_books |
| `minecraft:respiration` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:pufferfish`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | exploration_books |
| `minecraft:riptide` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:heart_of_the_sea` | 20% | ocean_books, exploration_books |
| `minecraft:riptide` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:heart_of_the_sea` | 23% | ocean_books, exploration_books |
| `minecraft:riptide` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:heart_of_the_sea`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | exploration_books |
| `minecraft:sharpness` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:copper_ingot`, 2× `minecraft:flint` | 23% | combat_books, combat_advanced_books, late_game_books |
| `minecraft:sharpness` | 3 | 2 | 12 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:flint` | 26% | combat_books, combat_advanced_books, late_game_books |
| `minecraft:sharpness` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:flint`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books, late_game_books |
| `minecraft:sharpness` | 5 | 5 | 36 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:netherite_ingot`, 3× `minecraft:flint`, 1× `inscribed:enchantment_catalyst` | 71% | late_game_books |
| `minecraft:silk_touch` | 1 | 4 | 26 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:honeycomb`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 50% | treasure_books |
| `minecraft:smite` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:rotten_flesh` | 10% | combat_books, combat_advanced_books |
| `minecraft:smite` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:rotten_flesh` | 23% | combat_books, combat_advanced_books |
| `minecraft:smite` | 3 | 2 | 12 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:rotten_flesh` | 26% | combat_books, combat_advanced_books |
| `minecraft:smite` | 4 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:diamond`, 3× `minecraft:rotten_flesh`, 1× `inscribed:enchantment_catalyst` | 43% | combat_advanced_books |
| `minecraft:smite` | 5 | 3 | 22 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:netherite_ingot`, 3× `minecraft:rotten_flesh`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 47% | combat_advanced_books |
| `minecraft:soul_speed` | 1 | 4 | 26 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:soul_sand`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 50% | treasure_books |
| `minecraft:soul_speed` | 2 | 4 | 27 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:iron_ingot`, 2× `minecraft:soul_sand`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 53% | treasure_books |
| `minecraft:soul_speed` | 3 | 4 | 28 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:soul_sand`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 56% | treasure_books |
| `minecraft:sweeping_edge` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:quartz` | 10% | exploration_books |
| `minecraft:sweeping_edge` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:quartz` | 23% | exploration_books |
| `minecraft:sweeping_edge` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:quartz`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | exploration_books |
| `minecraft:swift_sneak` | 1 | 4 | 26 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:sculk`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 50% | treasure_books |
| `minecraft:swift_sneak` | 2 | 4 | 27 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:iron_ingot`, 2× `minecraft:sculk`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 53% | treasure_books |
| `minecraft:swift_sneak` | 3 | 4 | 28 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:sculk`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 56% | treasure_books |
| `minecraft:thorns` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:cactus` | 10% | combat_books, combat_advanced_books |
| `minecraft:thorns` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:cactus` | 23% | combat_books, combat_advanced_books |
| `minecraft:thorns` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:cactus`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | combat_advanced_books |
| `minecraft:unbreaking` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:obsidian` | 10% | ocean_books, mining_books, tools_advanced_books |
| `minecraft:unbreaking` | 2 | 2 | 10 | no | 4× `minecraft:amethyst_shard`, 2× `minecraft:iron_ingot`, 2× `minecraft:obsidian` | 23% | ocean_books, mining_books, tools_advanced_books |
| `minecraft:unbreaking` | 3 | 3 | 20 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:obsidian`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 39% | tools_advanced_books |
| `minecraft:vanishing_curse` | 1 | 2 | 8 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:phantom_membrane` | 20% | combat_books |
| `minecraft:wind_burst` | 1 | 4 | 26 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:copper_ingot`, 1× `minecraft:breeze_rod`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 50% | treasure_books, late_game_books |
| `minecraft:wind_burst` | 2 | 4 | 27 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:iron_ingot`, 2× `minecraft:breeze_rod`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:diamond` | 53% | treasure_books, late_game_books |
| `minecraft:wind_burst` | 3 | 5 | 34 | yes | 2× `minecraft:amethyst_shard`, 1× `minecraft:gold_ingot`, 3× `minecraft:breeze_rod`, 1× `inscribed:enchantment_catalyst`, 1× `minecraft:netherite_ingot` | 65% | late_game_books |
| `minecraft:sharpness` | 1 | 1 | 2 | no | 2× `minecraft:amethyst_shard`, 1× `minecraft:iron_ingot` | 10% | combat_books, combat_advanced_books, late_game_books |
