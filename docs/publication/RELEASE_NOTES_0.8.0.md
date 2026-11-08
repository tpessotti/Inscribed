# Inscribed 0.8.0

Minecraft 1.21.1 · NeoForge 21.1.255+ · Java 21 · MIT

- Add a configurable, guaranteed extra Enchantment Catalyst offer for qualified Librarians. Defaults: Expert level or higher, 24 emeralds, one catalyst and four uses per restock.
- Keep catalyst structure loot independently configurable through `loot.enable_catalyst_loot`, with the existing additive loot pools and global loot switch.
- Preserve saved Librarian offers, order, prices and uses while trading is disabled. Pending normal/shift-click purchases are blocked; re-enabling restores availability without rerolling the villager.
- Recognize the Librarian source in progression diagnostics.
- Include the 0.7.4 temporary 2D Catalyst sprite and the pedestal's three-quarter inventory view.
- Add a detailed English/Portuguese presentation, real in-game screenshots and a complete, validated enchantment-recipe example and authoring tutorial.
- Preserve the canonical 131 ritual recipes, 13 loot pools, reagent costs and default 20% extraction failure.

Install the JAR on client and server. Customized JSON overlays are preserved. Price/stock configuration changes apply to newly created offers; existing offers keep their saved values. Reopen a trade window after a configuration change to refresh its displayed availability.

This release uses Codex-assisted implementation/documentation and some AI-generated bundled artwork. The publication kit records provenance and the unresolved Modrinth public-listing eligibility requirement. CurseForge and Modrinth submissions are separate from this GitHub draft.
