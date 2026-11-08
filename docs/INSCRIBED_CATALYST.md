# Enchantment Catalyst — Inscribed 0.8.0

`inscribed:enchantment_catalyst` is a consumed reagent. It has its own temporary 2D sprite, a creative-tab entry and the item tag `inscribed:ritual_catalysts`. A recipe decides whether it requires a catalyst; basic rituals do not.

## Structure loot

The existing additive pools remain unchanged: 7% additional roll in intermediate structures, 14% in advanced structures and 22% in treasure sources. Intermediate/advanced wins give one. Treasure wins give one 95% of the time and two 5% of the time. Exact loot-table IDs are in `content/inscribed_default_datapack/data/inscribed/inscribed/loot_injections/*_catalyst.json`.

`loot.enable_catalyst_loot=false` disables catalyst injections. `loot.enable_loot_injection=false` disables all Inscribed loot injection, including books. Multipliers and JSON pool overlays remain supported. Already generated chests are not refilled and existing custom overlays are not overwritten.

## Librarians

Qualified Librarians receive one additional catalyst offer without replacing their ordinary trades. Defaults: minimum level **4 / Expert**, **24 emeralds** for **one catalyst**, **four uses per restock**, 15 villager XP and vanilla price multiplier 0.05. Discounts and demand apply normally.

```toml
[trades]
enable_catalyst_trade = true
catalyst_librarian_level = 4
catalyst_emerald_cost = 24
catalyst_max_uses = 4
```

The level is 1..5, price 1..64 and stock 1..64. Price and stock settings affect newly created offers. Existing offers retain saved price, demand, discounts and uses.

Disabling the feature makes existing Librarian catalyst offers unavailable without deleting or reordering them. Normal pickup and shift-click recheck permission even for a menu already open. Reopen the window to refresh client presentation after a setting changes. Re-enabling restores availability; saving while disabled does not erase stock history. Ordinary Librarian trades and other professions are unaffected.

Loot and trade switches are independent. Progression diagnostics recognize either enabled acquisition route. A server can deliberately disable both.

## Datapack authors

Use `{"item":"inscribed:enchantment_catalyst"}` as an ordinary one-item pedestal cost. Add custom loot with the [loot schema](schema/inscribed_loot_v1.md). The item is not hardcoded as a requirement for every high-level enchantment; the recipe remains authoritative.

See [configuration](INSCRIBED_CONFIGURATION.md), [authoring](ADDING_ENCHANTMENTS.md) and the [balance matrix](INSCRIBED_BALANCE_MATRIX.md). Recipe/loot IDs and default failure chances are unchanged in 0.8.0.
