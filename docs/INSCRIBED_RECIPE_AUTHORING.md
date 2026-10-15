# Recipe authoring - current reference

The canonical v1 documentation is [Inscribed datapack authoring](INSCRIBED_DATAPACK_AUTHORING.md), with [recipe schema](schema/inscribed_enchantment_recipe_v1.md), [JSON Schema](../schemas/inscribed_enchantment_recipe.schema.json) and [templates](../templates/recipe_basic.json).

Use at most eight expanded unit costs and one item per pedestal. New files should repeat ingredient entries; legacy `count` remains accepted only within that total. Registry IDs and output levels must be valid. Guide/JEI visibility never replaces physical books. Config overlays take precedence over same-ID datapacks.

Historical implementation and balance reports describe earlier stages; consult the canonical references above when authoring for 0.7.0.
