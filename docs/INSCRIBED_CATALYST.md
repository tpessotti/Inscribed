# Enchantment Catalyst — Inscribed 0.7.2-rc.1

`inscribed:enchantment_catalyst` / **Enchantment Catalyst** / **Catalisador de Encantamento** is a consumed ritual reagent, not a permanent tool. The progression concept can be reused by recipes and future merchant integrations. It has a vanilla-compatible layered echo/amethyst placeholder model, a creative-tab entry and the tag `inscribed:ritual_catalysts`.

The catalog uses one catalyst in infusion and extraction of tiers 3–5 and the intermediate Channeling recipe, totaling 64 recipe IDs. Basic rituals never use it; curses and Create I/II are intermediate without a universal Catalyst requirement. Exact recipe IDs and levels are listed in [the balance matrix](INSCRIBED_BALANCE_MATRIX.md). Existing thematic extraction selectors still distinguish enchantments; Catalyst does not replace them. Duration and 20% extraction failure remain unchanged; power and infusion risk now follow per-level progression.

Default acquisition is configurable structure loot: a 7% extra roll in intermediate sources, 14% in advanced sources and 22% in treasure sources. Treasure wins yield one unit 95% of the time and two 5%; other defaults yield one. Loot is additive and separately switchable. No crafting recipe or villager-trade rewrite was added. Modpack makers can add crafting through `crafting_recipes` or use the stable item/tag in external merchant integrations.

The guide, shared Codex adapter and JEI receive the same actual costs. Missing Catalyst has a localized action-bar message. Active Catalyst motion has a modest extra pulse; final art and fragments are outside this stage.


Authoring: add `{"item":"inscribed:enchantment_catalyst"}` as a unit ingredient. Loot pools can supply the same item with weights/chance. Future merchants are a possible external source, not an implemented merchant system. See [datapack authoring](INSCRIBED_DATAPACK_AUTHORING.md) and [configuration](INSCRIBED_CONFIGURATION.md).
