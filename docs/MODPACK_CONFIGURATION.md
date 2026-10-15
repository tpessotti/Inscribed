# Modpack configuration - current reference

Use [configuration](INSCRIBED_CONFIGURATION.md), [quickstart](INSCRIBED_MODPACK_QUICKSTART.md), [datapack authoring](INSCRIBED_DATAPACK_AUTHORING.md) and [troubleshooting](INSCRIBED_TROUBLESHOOTING.md) for Inscribed 0.7.0.

Global rules live in the synchronized SERVER TOML. Recipe/crafting and loot JSON overlays remain supported; explicit config entries win over datapacks. New content should use stable own-namespace datapack IDs. No key was removed or renamed in this pass.

Crafting overlay entries are complete vanilla/NeoForge recipe bodies, keyed by resource ID; `enabled:false` disables an entry. Use Minecraft 1.21.1's result format (`id`, optional `count`) and `data/<namespace>/recipe/` for vanilla datapack crafting files. This is distinct from Inscribed's custom ritual directory. Back up overlays before replacing them with empty examples.

Legacy ingredient counts expand into separate pedestals and must total at most eight. Examples exceeding eight are invalid. The one-item pedestal invariant cannot be disabled. Default extraction remains 20% failure; duration, risks, ingredients, visibility and Catalyst costs are per-recipe content.
