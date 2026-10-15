# Inscribed datapack authoring - v1

## Introduction and file structure

Use your own namespace and stable IDs. Minecraft 1.21.1 uses datapack `pack_format: 48`. Install on the server in `<world>/datapacks`, or in the single-player world's datapacks folder.

```
mypack/pack.mcmeta
mypack/data/mypack/inscribed/enchantment_recipes/fortune_3.json
mypack/data/mypack/inscribed/loot_injections/mineshaft.json
```

These files become `mypack:fortune_3` and `mypack:mineshaft`. See [minimal example](../examples/minimal/README.md), [complete example](../examples/inscribed_example_datapack/README.md), [recipe schema](schema/inscribed_enchantment_recipe_v1.md), and [loot schema](schema/inscribed_loot_v1.md).

## Recipes, ingredients, Infusion and Extraction

Copy [basic](../templates/recipe_basic.json), [advanced](../templates/recipe_advanced.json), or [conditional Create](../templates/recipe_modded_create.json). A recipe declares the registered enchantment, exact output level, minimum Bookshelf Power and one or both ritual bodies. Durations are server ticks (20 ticks per second at normal TPS).

One expanded ingredient entry requires one pedestal and one item. Maximum eight entries after expanding legacy `count`. Prefer repeated entries for repeated materials:

```json
[{"item":"minecraft:diamond"},{"item":"minecraft:diamond"},{"item":"minecraft:diamond"}]
```

`{"tag":"c:gems/emerald"}` uses an item tag. Tags must exist and contain a matching item. NeoForge custom ingredient objects with a registered `type` are passed to the Ingredient codec. An ingredient entry must be an object; array-style alternatives are not accepted. Legacy `count` remains supported for compatibility, but does not allow stacks on a single pedestal.

Infusion requires a matching physical enchanted book in a nearby Chiseled Bookshelf. A higher book level is accepted unless `exact_book_level: true`. A book with several enchantments remains intact except when the entire reference book is consumed by `book_consumption_chance`. The target receives the recipe's exact level, never the reference book's higher level. Invalid, incompatible, same-level and downgrade operations are rejected.

Extraction matches the target's existing enchantment and the pedestal ingredients. It requires `empty_books_required` ordinary books. `failure_chance` defaults to zero in authored recipes; specify `0.2` to retain the default catalog's 20% risk. Failure removes the selected enchantment and consumes inputs, producing no book. Successful extraction preserves other enchantments, name, damage and components. Cancellation does not consume inputs.

## Catalyst and visibility

Catalyst is an ordinary ingredient: `{"item":"inscribed:enchantment_catalyst"}`. Its use is determined by recipes, not hardcoded Java. See [Catalyst](INSCRIBED_CATALYST.md).

| Visibility | Guide and JEI visibility |
| --- | --- |
| `ALWAYS` | Visible without discovery |
| `ON_ENCHANTMENT_DISCOVERY` | Visible after that enchantment is learned |
| `EXPLICIT_DISCOVERY` | Visible after that recipe ID is learned |

Visibility controls presentation, not permission to execute a matching physical ritual. Knowledge unlock is separate from physical access. Changing recipe IDs can leave old `known_recipes` references; retain IDs or grant the replacement ID with administrator commands.

## Loot and modded enchantments

Copy [loot template](../templates/loot_entry.json). Pools target exact loot table IDs, such as `minecraft:chests/shipwreck_treasure`; they add results rather than replacing vanilla loot. Different pool IDs targeting one table each roll independently, subject to the global eight-result cap. Example packs therefore change loot balance when installed.

No Java change is needed for a registered modded enchantment. Use its exact registry ID, valid level and item/tag ingredients. Add a root condition:

```json
"neoforge:conditions": [{"type":"neoforge:mod_loaded","modid":"create"}]
```

Without the provider, conditional recipes are skipped. Without the condition, an unknown enchantment rejects that recipe with an error. A recipe cannot register a new enchantment by itself.

## Overrides, priority and reload

A higher-priority datapack replaces a whole file with the same namespace and path. Distinct IDs coexist; there is no field-by-field merge. Namespace is identity, not priority. JSON config overlays are applied afterward and win for the same ID. The generated overlays contain default entries: remove the corresponding entry before overriding it through a datapack, or deliberately switch to the empty overlay examples after making a backup. `include_datapack_recipes: false` excludes datapack recipes entirely.

Disable a recipe with a same-ID `{"enabled":false}` file, provided the config overlay does not re-enable that ID. An invalid replacement is omitted rather than falling back to the lower-priority file. An invalid whole config file keeps the last valid config in that server session; a fresh session without a valid config fails closed.

`/reload` reloads ritual recipes, loot pools and visibility metadata, invalidates pending selections and active ritual generations, and updates client snapshots/JEI. `/inscribed reload` also applies the recipe/config reload path. Guide chapters and translations are client resources: reload resource packs with F3+T. Registered enchantments and mods require the appropriate game/server restart.

## Testing and troubleshooting

1. Install the minimal pack and run `/datapack list enabled` and `/reload`.
2. Check `latest.log` for `Loaded ... Inscribed enchantment recipes` and any `Invalid Inscribed recipe <id>` errors.
3. Open the Guide or JEI. Grant explicit discovery when needed; see [commands](INSCRIBED_TROUBLESHOOTING.md).
4. Supply the physical book, target, materials and Bookshelf Power. Execute and retrieve the result.
5. Edit the enchantment to another registered, compatible enchantment, reload, and repeat with its physical book.

JSON Schemas help IDE validation but cannot check registry membership, tag contents, enchantment compatibility, expanded counts or cross-field ranges. The runtime loader remains authoritative. See [quickstart](INSCRIBED_MODPACK_QUICKSTART.md) and [troubleshooting](INSCRIBED_TROUBLESHOOTING.md).

## VS Code

Associate local schemas in `.vscode/settings.json` (adjust paths to your checkout):

```json
{"json.schemas":[
 {"fileMatch":["**/inscribed/enchantment_recipes/*.json"],"url":"./schemas/inscribed_enchantment_recipe.schema.json"},
 {"fileMatch":["**/inscribed/loot_injections/*.json"],"url":"./schemas/inscribed_loot.schema.json"}
]}
```

## Localization, Guide and resource packs

Custom enchantment names come from their enchantment description Component, usually `enchantment.<namespace>.<name>` in `assets/<namespace>/lang/<locale>.json`. Inscribed has no independent custom recipe-title field. Missing translations receive a readable ID-based fallback; authored styled/literal descriptions are preserved.

Guide chapters are a localized array at `assets/inscribed/guide/en_us.json` or `pt_br.json`. A resource pack may replace the whole array; chapters are not aggregated from arbitrary namespaces or loaded as datapack files. Inspect the shipped array for the actual fields. Standard models, textures and language files are resource-pack overridable. See [integrations and asset paths](INSCRIBED_INTEGRATIONS.md).
