# Save and configuration migration

Back up the world and `config/inscribed` before upgrading or removing content mods. 0.7.2-rc.1 keeps existing item/block IDs, recipe IDs, knowledge attachment structure and JSON `format_version:1`. No config key was removed or renamed. Existing customized overlays retain precedence and are not overwritten by the release installation.

Knowledge stores enchantment IDs and explicit recipe IDs per player and copies on death. Removing a recipe ID leaves a stale discovery reference; keep the ID stable, retain an alias recipe during transition, or grant the new ID and forget the old one with administrator knowledge commands. Missing provider IDs are filtered from active snapshots rather than causing missing-holder failures.

Legacy pedestal item stacks are preserved on load for retrieval. They must be split into one item per pedestal before paying a ritual. Saved Running/visual timing is not an active session and is cleared after load. Actual transient network update tags retain client animation timing. Target stacks, names, damage and components continue to use Minecraft ItemStack serialization.

Ritual sessions are in memory. Chunk unload, player departure, structure/input changes, recipe generation changes and server shutdown cancel sessions before commit. Restart does not resume rituals. Reserved resources are released; cancelled inputs remain physical. There is no claim of atomic multi-chunk disk persistence through a process crash.

Removing Create/another provider can make Minecraft discard unknown item or enchantment components. ID-based Inscribed knowledge survives and becomes visible again when the provider returns, but physical content recovery requires the original backup. Test removal/reinstallation only on a copied world.

Upgrade verification uses worlds copied from 0.6.1 client fixtures; fresh dedicated environments generate current defaults. See [QA evidence](INSCRIBED_QA_MATRIX.md) for actual provider-removal/reinstallation results.
