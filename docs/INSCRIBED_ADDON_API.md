# Addon surface audit

There is **no stable public Java addon API** in 0.7.2-rc.1. Supported extension surfaces are documented v1 datapacks and resource packs. Register modded enchantments in their provider, then reference their IDs without compiling Inscribed.

Public implementation classes `InscribedRecipeRegistry`, `KnowledgeService`, `RecipeVisibilityService`, `RitualController` and `AltarStateService` are internal/experimental, not a binary compatibility promise. Their authoritative mutations require the server thread. `KnowledgeIntegration` is an internal optional adapter, not an external registration hook.

No dedicated public knowledge-learned, ritual-start, ritual-complete or extraction-complete events exist. NeoForge lifecycle/acquisition/tick/reload hooks are implementation details. Do not invent addon subscriptions.

Use stable own-namespace IDs. Explicit discovery saves recipe IDs; retain old IDs or grant replacement IDs during migration. Removing a provider can make vanilla discard unknown items/enchantments; Inscribed cannot guarantee their recovery. Back up original saves before removing content mods.
