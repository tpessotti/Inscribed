# Exploration loot - current reference

Use [loot schema v1](schema/inscribed_loot_v1.md), [datapack authoring](INSCRIBED_DATAPACK_AUTHORING.md), [configuration](INSCRIBED_CONFIGURATION.md) and the [loot template](../templates/loot_entry.json).

Pools live at `data/<namespace>/inscribed/loot_injections/`. Config overlays at `config/inscribed/loot.json` take precedence by ID. Different IDs add independent rolls, with at most eight added results per generation. Disabled reward types skip the selected reward rather than redistributing its weight. Levels/counts are inclusive uniform ranges; weights select relative probability within a pool. `chance` is evaluated per roll after global multipliers.

The default [distribution](INSCRIBED_LOOT_DISTRIBUTION.md) contains 13 thematic pools targeting 24 tables. Installing the example pack adds pools and deliberately changes rarity. Generation alone does not grant knowledge; acquiring the book does. Previously opened containers are not repopulated by reload.
