# ADR-0005 — Per-recipe tiers and thematic source distribution

Status: accepted for Inscribed 0.5.0.

Tier is optional author-controlled recipe metadata, independent of player knowledge, KnownWords and enchantment family identity. Missing metadata defaults to `UNCLASSIFIED`; it never imposes an inferred execution lock on a custom recipe. The shipped catalog receives explicit tiers per level.

Progression uses several physical axes: book availability, summed library power, thematic reagents, Catalyst/rare materials and infusion book-consumption risk. Higher levels and modded origin alone do not determine strategic value. Create utility is assessed from the installed definitions.

The existing maximum of eight unit pedestal entries remains mandatory. Extraction keeps the user-confirmed 20% failure risk; only its selector is deterministic. No new RNG penalty is introduced in stage 05.

Loot remains additive and configurable. Pools carry theme, host tier, optional treasure flag and optional recipe-existence policy. Invalid individual reward entries are omitted without discarding other valid entries. Table indexes and aggregate weights are built on reload, not during every roll.

Global and treasure multipliers affect chance, clamped to one, while weights stay relative within a pool. Duplicate books remain valuable because infusion can consume a reference book. Existing vanilla loot and trades are not rewritten. Modpack makers retain control through config and datapacks; no LSBC tier model or worldgen dependency is introduced.
