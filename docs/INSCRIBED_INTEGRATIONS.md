# Integrations and resource replacement

## JEI

Optional client JEI 19.57.0.451 adds Infusion/Extraction categories, target previews, ingredients, physical book, power, duration and chances. Visibility follows the local player's knowledge. Learn/forget and reload refresh the categories. Identical recipes reuse registrations.

JEI's public API can add/hide/unhide, but cannot remove registered recipes. Changed structural variants remain hidden until runtime restart. Global enchanted-book ingredient search may reveal vanilla names; it is not a knowledge privacy boundary.

## LSBC

Optional LSBC 0.22.1 adds KnownWords rendering/learning, Codex Enchantments and magnifying-glass inspection. Acquire a book or extract successfully to learn; viewing a shelf does not grant discovery. Unknown words may render as glyphs. Styled/literal description Components are preserved.

Shift + right click with the magnifying glass and keep looking during analysis to inspect target, ingredients, library and power. Without LSBC, Book and Quill records analysis; Inscribed's Guide and rituals still work.

## Create

Optional Create 6.0.10 supplies Capacity and Potato Recovery I?III for six conditional default recipes. Arbitrary registered modded enchantments can use datapacks without Java changes. Pedestals are non-movable by pistons/Create contraptions. They expose no ItemHandler capability for automated transfer.

## Multiplayer

Inscribed is required on client and server. Gameplay datapacks/configuration live on the server. JEI is client-only. LSBC/Create must be installed consistently where their own content/networking requires them. Resource replacements live on clients or ordinary server resource-pack distribution. Knowledge remains per player and server authoritative.

## Assets and sounds

Standard resource-pack replacement paths:

- `assets/inscribed/textures/item/guide_book_placeholder_v2.png`
- `assets/inscribed/models/item/guide_book.json`
- `assets/inscribed/models/item/enchantment_catalyst.json`
- `assets/inscribed/models/block/infusion_pedestal.json`
- `assets/inscribed/blockstates/infusion_pedestal.json`
- `assets/inscribed/guide/en_us.json`, `pt_br.json` (whole localized chapter array)
- `assets/inscribed/lang/en_us.json`, `pt_br.json`

Pedestal textures reference vanilla resources. Particles use vanilla registered particle types. No custom Inscribed sound event/OGG is shipped. Config defaults use beacon activate/ambient, enchantment-table use, amethyst chime, player level-up, book page-turn, fire extinguish and note-block bass; exact IDs are listed under `sounds.*` in [configuration](INSCRIBED_CONFIGURATION.md). Resource packs can override vanilla sounds normally; a newly registered sound requires a provider mod before configuring its ID.

Codex replacement paths are `assets/lsbc/models/item/codex.json` and `assets/lsbc/textures/item/codex_placeholder_v2.png`; these belong to LSBC. Guide chapters are client resources, not datapack files or arbitrary-namespace chapter aggregation. See [authoring](INSCRIBED_DATAPACK_AUTHORING.md) for localization and visibility.

## Create Ponder tutorial (0.7.1)

Hover the Enchanting Table or Infusion Pedestal item and hold W with Create/Ponder installed. The localized scene demonstrates the default physical-library Sharpness I setup. See [Ponder tutorial](INSCRIBED_CREATE_PONDER.md). No real inventory or knowledge is changed. Modpack overrides still require checking the actual Guide/JEI recipes.
