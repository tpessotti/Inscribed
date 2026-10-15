# Known limitations - 0.7.2

Stable promotion of 0.7.2-rc.1, with byte-identical gameplay classes/assets/data. [Stable validation](stable-0.7.2/RESULTS.json).

- JEI's public API cannot remove obsolete structural recipe variants from the running registry. They are hidden until runtime restart. Identical reload/unlock cycles reuse records. Global enchanted-book search may reveal vanilla names independently of Inscribed knowledge.
- Pedestals support manual one-item interaction only. Hoppers/pipes have no ItemHandler capability. Pistons and Create contraptions cannot move them. Legacy saved stacks remain retrievable, but cannot pay a unit recipe until split.
- Dynamic light is full-bright item rendering, not world light propagation. No third-party shader/lighting mod compatibility is claimed by the base matrix.
- Active rituals and pending selections do not resume after server restart. Inputs are consumed only at commit; a cancelled session preserves inputs. Vanilla/world-save crashes during disk writes are outside a transaction's in-memory rollback guarantee.
- Removing an enchantment provider may cause Minecraft to discard its physical item/enchantment data. Inscribed retains ID-based knowledge and filters absent registry entries, but cannot restore unknown vanilla data. Preserve the original world backup.
- Guide chapter arrays are resource-pack replacements, not additive arbitrary-namespace datapack chapters. No custom recipe title field or stable addon Java API exists.
- The default catalog supports registered vanilla and Create enchantments. Other mods may add restrictions/components that require their own compatibility checks; valid registry IDs alone do not prove every modded item interaction.
- Placeholder artwork is not final art. Non-power-of-two images can produce mipmap warnings without missing textures. No custom sound event/audio is bundled.
- All visual config currently uses the synchronized SERVER spec. There is no independent client config or supported team-wide knowledge.
- No EMI/REI integration, merchant overhaul, worldgen or new enchanting mechanics was added in this pass.

- Administrative command syntax and player arguments complete normally; enchantment/recipe ID arguments currently have no registry-aware suggestion provider. Unknown learn IDs are rejected, and commands require permission level 2.
- RC runtime QA covers the documented optional-mod subset. It does not replace human playtesting of the complete Rogue Tides modpack or guarantee third-party shader behavior.
