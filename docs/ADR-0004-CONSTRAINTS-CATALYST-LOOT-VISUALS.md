# ADR-0004 — Physical constraints, progression and phase visuals

Status: accepted for Inscribed 0.4.0.

1. Inscribed recipes are constrained to a maximum of eight pedestal ingredients and exactly one item per pedestal. Counts greater than one are represented by repeated entries, not multi-count pedestal stacks. Legacy saved stacks remain retrievable without truncation.
2. Enchantment Catalyst is a reusable progression/gating concept for higher-tier rituals, sourced initially from configurable structure loot and available to future merchant integrations. The item is consumed when a recipe requires it. Trade overhauls and fragments are excluded.
3. Loot injection is additive, data-driven and independent of LSBC. Exact queried loot-table IDs serve as structure-pool filters, with server configuration overrides and bounded weighted rolls.
4. Ritual visuals are intentionally phase-based and distinguish infusion from extraction through directional transfer of knowledge. Server authority determines lifecycle and timing; client rendering has no authority over commits. Emissive item lighting provides a safe alternative to world relighting.
5. Existing catalog IDs and non-ingredient rules remain stable. Rebalance changes are recorded individually. Customized config recipes are preserved rather than silently rewritten; invalid costs receive loader diagnostics.
