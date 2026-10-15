# Modpack quickstart - 5-10 minutes

1. Install Inscribed on Minecraft 1.21.1, NeoForge 21.1.255 and Java 21, on client and server. Start once to generate config. JEI is optional.
2. Copy [minimal](../examples/minimal/README.md) to `<world>/datapacks/my_inscribed_pack`. Its own ID `minimal:sharpness_basic` avoids same-ID config overrides.
3. Copy [shipwreck.json](../examples/inscribed_example_datapack/data/example/inscribed/loot_injections/shipwreck.json) to `data/minimal/inscribed/loot_injections/shipwreck.json` within that pack. This adds an independent pool and intentionally increases loot.
4. Run `/datapack list enabled` and `/reload`. Check loading messages in `latest.log`, then open Guide Recipes or JEI Infusion.
5. Place an Enchanting Table, two ordinary bookshelves within five blocks, a Chiseled Bookshelf within five and three Infusion Pedestals within four, within vertical range two. Supply separate flint, amethyst shard and iron ingot, a Sharpness I or higher physical book and an iron sword target. Shift + right click; wait five seconds and retrieve with an empty main hand.
6. Change the recipe enchantment to `minecraft:unbreaking`, reload, supply its physical book and a compatible fresh target, and repeat. Keep recipe IDs stable.
7. Test newly generated/unopened loot or `/loot give @s loot minecraft:chests/shipwreck_treasure`. A single unsuccessful roll is normal. Raise chance temporarily only in a test world, then restore it.

For modded enchantments, use registered IDs/valid levels and optional `neoforge:mod_loaded` conditions. The [full example](../examples/inscribed_example_datapack/README.md) includes Create, Fortune III, Catalyst and three pools. To override defaults, remove their explicit config entries or intentionally switch to empty overlays after backup. See [configuration](INSCRIBED_CONFIGURATION.md) and [authoring](INSCRIBED_DATAPACK_AUTHORING.md).
