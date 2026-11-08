# Add an enchantment to Inscribed

This guide targets Inscribed 0.8.0 on Minecraft 1.21.1 / NeoForge. It adds an Inscribed ritual for an **already registered enchantment**. If you want a completely new effect, first register that enchantment through a mod or Minecraft's enchantment datapack registry; see the [NeoForge 1.21.1 enchantment documentation](https://docs.neoforged.net/docs/1.21.1/resources/server/enchantments/).

## 1. Create a datapack

Copy [examples/add_enchantment](../examples/add_enchantment) into `<world>/datapacks`. Keep `pack.mcmeta` directly inside the pack folder or ZIP root:

```text
my_inscribed_pack/
  pack.mcmeta
  data/example/inscribed/enchantment_recipes/sharpness_1.json
```

```json
{"pack":{"pack_format":48,"description":"My Inscribed rituals"}}
```

The file above becomes recipe ID `example:sharpness_1`. Use your own namespace and keep IDs stable after players discover recipes. A new namespace adds a recipe rather than replacing the built-in one.

## 2. Define the ritual

The complete sample is [sharpness_1.json](../examples/add_enchantment/data/example/inscribed/enchantment_recipes/sharpness_1.json):

```json
{
  "enchantment": "minecraft:sharpness",
  "level": 1,
  "minimum_bookshelf_power": 2,
  "recipe_visibility": "ALWAYS",
  "progression_tier": "TIER_1_BASIC",
  "infusion": {
    "ingredients": [
      {"item": "minecraft:flint"},
      {"item": "minecraft:amethyst_shard"},
      {"item": "minecraft:iron_ingot"}
    ],
    "duration_ticks": 100,
    "book_consumption_chance": 0.1
  },
  "extraction": {
    "ingredients": [
      {"item": "minecraft:lapis_lazuli"},
      {"item": "minecraft:amethyst_shard"}
    ],
    "duration_ticks": 120,
    "empty_books_required": 1,
    "failure_chance": 0.2
  }
}
```

| Field | Meaning |
| --- | --- |
| `enchantment` | Registered enchantment ID; use the provider's exact namespace/path |
| `level` | Exact level produced; must be valid for that enchantment |
| `minimum_bookshelf_power` | Required ordinary Bookshelf Power; Chiseled Bookshelves store books instead |
| `ingredients` | One item on one pedestal per entry; at most eight expanded costs |
| `duration_ticks` | Ritual duration, 20 ticks per second at normal server speed |
| `book_consumption_chance` | Infusion chance of consuming the physical reference book: `0.1` means 10% |
| `empty_books_required` | Empty books supplied through the physical library for extraction |
| `failure_chance` | Extraction failure probability: `0.2` means 20%; failure removes the selected enchantment and consumes inputs without producing a book |

A recipe may provide infusion, extraction or both. Infusion still needs a matching physical enchanted book; knowledge/Guide visibility does not replace it. Higher-level books qualify by default; add `"exact_book_level": true` at the root to require an exact match. A multi-enchantment reference book is consumed as a whole if the infusion's consumption roll succeeds.

Use `{"tag":"c:gems/emerald"}` for an item tag. For three diamonds, write three ingredient entries and provide three pedestals. `{"item":"inscribed:enchantment_catalyst"}` is an ordinary unit ingredient. The optional tier label organizes the recipe; it does not create a separate permission gate.

## 3. Use another mod's enchantment

Change `enchantment` to the actual registered ID and choose a supported level. The target item must also support that enchantment; Inscribed does not bypass incompatibilities or supported-item rules.

When the provider is optional, add a root condition:

```json
"neoforge:conditions": [
  {"type":"neoforge:mod_loaded","modid":"the_provider_mod"}
]
```

Replace `the_provider_mod` with the real mod ID. Without the provider, that conditional recipe is skipped. Do not ship fictional enchantment IDs as enabled recipes.

## 4. Load and test

1. Run `/datapack list enabled`, then `/reload` or `/inscribed reload`.
2. Check `latest.log` for the recipe count and any error naming your recipe ID.
3. For this example, put a Sharpness I or stronger book in a nearby Chiseled Bookshelf and provide at least two Bookshelf Power.
4. Put a compatible sword on the table and flint, amethyst shard and iron ingot on three separate pedestals.
5. Start the ritual. The example is an additional Sharpness option; select it if other matching infusions are present.
6. Test extraction with the matching enchanted target, its two extraction reagents and an empty book. Remember the configured failure chance.

Removing the example pack afterward removes its additional recipe; it does not erase the built-in recipe. Restart the world when adding/removing the enchantment registry definitions or provider mods, rather than relying on a recipe reload alone.

## Overrides and discovery

The editable `config/inscribed/recipes.json` overlay takes precedence over same-ID datapack entries. Generated overlays contain default IDs. Use a new namespace for additions; for an intentional built-in override, remove that exact overlay entry first or deliberately switch to the documented empty overlay after making a backup. Do not delete the whole customized config as a shortcut.

`ALWAYS` shows the recipe immediately. `ON_ENCHANTMENT_DISCOVERY` waits for the enchantment to be learned. `EXPLICIT_DISCOVERY` waits for that recipe ID to be learned. These are presentation rules; the physical ritual still needs its book, reagents and power.

The enchantment's display name comes from its description component/translation, not an independent Inscribed recipe-title field. For a new enchantment, supply its name in an appropriate resource pack or provider mod.

## Validation

Use the [recipe JSON schema](../schemas/inscribed_enchantment_recipe.schema.json), [reference](INSCRIBED_DATAPACK_AUTHORING.md) and [templates](../templates). Schema validation cannot verify registry IDs, populated tags or item compatibility; the server parser and an actual ritual remain the final checks.

The release audit validates this exact sample against the schema. It is not embedded as production content in the JAR or official default datapack.
