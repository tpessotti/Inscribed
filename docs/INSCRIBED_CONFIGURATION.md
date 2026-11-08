# Configuration reference - 0.8.0

`config/inscribed-server.toml` controls global behavior. All options are NeoForge SERVER scope, synchronized to clients; visuals currently share that scope. Edit the active file used by your instance (NeoForge can use a world's serverconfig directory), preferably with the server stopped, then restart for complete synchronization.

`config/inscribed/recipes.json` and `config/inscribed/loot.json` are compatible v1 content overlays. New content is best authored through datapacks. No existing key was removed or renamed, and no customized overlay is rewritten during upgrade.

**Explicit JSON config entries win over same-ID datapack files.** Generated overlays include the default catalog. Remove the specific entry before overriding through a datapack, or back up your overlay and intentionally use the empty [recipe](../examples/config_overlay/recipes.json) and [loot](../examples/config_overlay/loot.json) overlays. Different IDs coexist.

Run `/reload` or `/inscribed reload` for recipe/loot JSON changes. Client resources use F3+T. Changing mods or registered enchantments requires restart. NeoForge corrects invalid TOML types/ranges to safe defaults with warnings. Invalid recipe/pool fields reject their entry. Invalid whole JSON config files retain the last valid document within that server session; without one, loading fails closed.

One item per pedestal and at most eight expanded costs are fixed design rules. `max_pedestals` may lower the accepted count (1..8), but cannot exceed eight. Excess populated pedestals reject the altar rather than selecting a subset.

## Catalyst acquisition switches

`loot.enable_catalyst_loot` controls catalyst loot independently of `trades.enable_catalyst_trade`. The global `loot.enable_loot_injection` also controls all injected loot. Both catalyst routes default to enabled. Existing custom JSON overlays are preserved.

| Trade key | Default | Range / effect |
| --- | --- | --- |
| `trades.enable_catalyst_trade` | `true` | Enable the extra Librarian offer; disabled existing offers are unavailable, not deleted |
| `trades.catalyst_librarian_level` | `4` | Minimum level, 1 novice through 5 master |
| `trades.catalyst_emerald_cost` | `24` | Base price for new offers, 1..64; vanilla discounts/demand apply |
| `trades.catalyst_max_uses` | `4` | Uses per restock for new offers, 1..64 |

Existing price/stock history is preserved. Pending normal/shift-click purchases cannot bypass a disabled trade. Reopen a trade window after changing the switch to refresh displayed availability. See [Catalyst](INSCRIBED_CATALYST.md).

## Inventory

All keys below are used, SERVER scope and not deprecated. The [machine-readable inventory](INSCRIBED_CONFIG_INVENTORY.json) contains field names and defaults exported from the running spec. Numeric ranges are also documented by generated TOML comments. Radius is horizontal Euclidean distance with a separate vertical limit.

| Key | Type | Default | Effect |
| --- | --- | --- | --- |
| `altar.bookshelf_radius` | Integer | `5` | Horizontal ordinary Bookshelf Power scan radius; range 1.0..16.0 |
| `altar.chiseled_bookshelf_radius` | Integer | `5` | Horizontal physical library scan radius; range 1.0..16.0 |
| `altar.max_pedestals` | Integer | `8` | Maximum populated pedestals; excess rejects the altar; range 1.0..8.0 |
| `altar.pedestal_radius` | Integer | `4` | Horizontal reagent pedestal scan radius; range 1.0..16.0 |
| `altar.vertical_radius` | Integer | `2` | Vertical scan distance above and below the table; range 0.0..16.0 |
| `anvil.restrict_enchantment_changes` | Boolean | `true` | Registered sound event for restrict enchantment changes; unknown ID falls back to vanilla |
| `inspection.analysis_ticks` | Integer | `40` | Inspection analysis duration in ticks; range 1.0..1200.0 |
| `inspection.book_cooldown_ticks` | Integer | `20` | Book inspection cooldown; range 0.0..1200.0 |
| `inspection.enable_book_fallback` | Boolean | `true` | Enable Book and Quill inspection notes |
| `inspection.enable_lsbc_lens` | Boolean | `true` | Enable optional magnifying-glass inspection |
| `inspection.reach` | Double | `4.0` | Maximum altar inspection distance; range 1.0..16.0 |
| `knowledge.inventory_scan_interval_ticks` | Integer | `10` | Inventory acquisition polling interval; range 1.0..1200.0 |
| `knowledge.jei_recipe_integration` | Boolean | `true` | Enable optional JEI recipe synchronization |
| `knowledge.learn_on_inventory_acquisition` | Boolean | `true` | Discover newly acquired inventory books |
| `knowledge.learn_on_pickup` | Boolean | `true` | Discover acquired enchanted books on pickup |
| `knowledge.learn_on_successful_extraction` | Boolean | `true` | Discover successful extraction results |
| `knowledge.learn_on_trade` | Boolean | `true` | Discover purchased enchanted books |
| `knowledge.lsbc_word_integration` | Boolean | `true` | Enable optional KnownWords learning and rendering |
| `loot.enable_book_loot_injection` | Boolean | `true` | Allow enchanted-book results from pools |
| `loot.enable_catalyst_loot` | Boolean | `true` | Allow Catalyst results from pools |
| `loot.enable_loot_injection` | Boolean | `true` | Master switch for added exploration rewards |
| `loot.loot_multiplier` | Double | `1.0` | Multiply each pool chance, then clamp effective chance to one; range 0.0..10.0 |
| `loot.treasure_loot_multiplier` | Double | `1.0` | Additional chance multiplier for treasure=true pools; range 0.0..10.0 |
| `ritual.enable_extraction` | Boolean | `true` | Enable Extraction execution |
| `ritual.enable_infusion` | Boolean | `true` | Enable Infusion execution |
| `ritual.require_shift_right_click` | Boolean | `true` | Require sneaking to start rituals |
| `ritual.selection_reach` | Double | `8.0` | Maximum selection validation distance; range 1.0..32.0 |
| `ritual.selection_timeout_ticks` | Integer | `1200` | Pending infusion selection expiry in server ticks; range 20.0..72000.0 |
| `sounds.enabled` | Boolean | `true` | Enable ritual audio |
| `sounds.extraction_complete` | String | `"minecraft:item.book.page_turn"` | Registered sound event for extraction complete; unknown ID falls back to vanilla |
| `sounds.extraction_failure` | String | `"minecraft:block.fire.extinguish"` | Registered sound event for extraction failure; unknown ID falls back to vanilla |
| `sounds.extraction_start` | String | `"minecraft:block.enchantment_table.use"` | Registered sound event for extraction start; unknown ID falls back to vanilla |
| `sounds.extraction_transfer` | String | `"minecraft:item.book.page_turn"` | Registered sound event for extraction transfer; unknown ID falls back to vanilla |
| `sounds.infusion_ambient` | String | `"minecraft:block.beacon.ambient"` | Registered sound event for infusion ambient; unknown ID falls back to vanilla |
| `sounds.infusion_complete` | String | `"minecraft:entity.player.levelup"` | Registered sound event for infusion complete; unknown ID falls back to vanilla |
| `sounds.infusion_start` | String | `"minecraft:block.beacon.activate"` | Registered sound event for infusion start; unknown ID falls back to vanilla |
| `sounds.invalid_ritual` | String | `"minecraft:block.note_block.bass"` | Registered sound event for invalid ritual; unknown ID falls back to vanilla |
| `sounds.running` | String | `"minecraft:block.amethyst_block.chime"` | Registered sound event for running; unknown ID falls back to vanilla |
| `sounds.running_interval_ticks` | Integer | `20` | Ticks between running sound events; range 1.0..1200.0 |
| `sounds.validation` | String | `"minecraft:item.book.page_turn"` | Registered sound event for validation; unknown ID falls back to vanilla |
| `sounds.volume_multiplier` | Double | `1.0` | Global ritual volume multiplier; zero mutes; range 0.0..4.0 |
| `visuals.bob_speed` | Double | `0.08` | Vertical oscillation speed; range 0.0..2.0 |
| `visuals.dynamic_light` | Boolean | `false` | Full-bright ritual item rendering; no world light |
| `visuals.idle_bob_height` | Double | `0.035` | Idle vertical oscillation amplitude; range 0.0..1.0 |
| `visuals.idle_rotation_degrees_per_tick` | Double | `0.8` | Idle item angular speed; range 0.0..30.0 |
| `visuals.item_scale` | Double | `0.55` | Base table item size; range 0.05..4.0 |
| `visuals.particle_interval_ticks` | Integer | `5` | Ticks between particle emissions; range 1.0..100.0 |
| `visuals.particles` | Boolean | `true` | Emit ritual particles |
| `visuals.pedestal_height` | Double | `1.15` | Pedestal item rendering height; range 0.0..5.0 |
| `visuals.pedestal_item_scale` | Double | `0.8` | Independent pedestal item size; range 0.05..4.0 |
| `visuals.phase_ritual_effects` | Boolean | `true` | Enable phase motion and directional transfer/fade |
| `visuals.render_items` | Boolean | `true` | Render stored table and pedestal items |
| `visuals.running_bob_height` | Double | `0.065` | Active vertical oscillation amplitude; range 0.0..1.0 |
| `visuals.running_rotation_degrees_per_tick` | Double | `3.0` | Active item angular speed; range 0.0..30.0 |
| `visuals.target_height` | Double | `1.65` | Table target rendering height; range 0.0..5.0 |
| `visuals.target_render_distance` | Double | `64.0` | Item rendering cutoff in blocks; range 8.0..256.0 |
| `visuals.target_scale` | Double | `2.0` | Table target multiplier applied to item_scale; range 0.1..8.0 |

## Overlay layouts

Recipes: `{ "format_version": 1, "include_datapack_recipes": true, "enchantment_recipes": {}, "crafting_recipes": {} }`. Each map is keyed by resource ID and contains complete bodies. Ritual bodies follow the [v1 schema](schema/inscribed_enchantment_recipe_v1.md); crafting bodies use vanilla/NeoForge formats. `enabled:false` disables the ID. `include_datapack_recipes:false` excludes datapacks.

Loot: `{ "format_version": 1, "include_datapack_pools": true, "pools": {} }`. Entries are complete [loot pool bodies](schema/inscribed_loot_v1.md). `include_datapack_pools:false` excludes datapack pools. Limit: 4 MB and 256 config pools.

There is no XP/lapis cost option. Dynamic light means full-bright items, not emitted world light. Table target size uses `item_scale * target_scale`; `pedestal_item_scale` is independent. Probabilities, duration, Catalyst costs and visibility belong to recipe content; distributions belong to loot content. Global loot multipliers adjust pool probability.
