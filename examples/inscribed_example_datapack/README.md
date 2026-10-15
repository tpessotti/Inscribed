# Example pack

Minecraft 1.21.1, Inscribed 0.7.0. Copy this folder into `<world>/datapacks/`, then `/reload`. Namespaces are examples, not changes to the default balance. Create recipe is conditional.

- `example:sharpness_basic`: ALWAYS, no Catalyst.
- `example:fortune_advanced`: EXPLICIT_DISCOVERY; grant with `/inscribed knowledge learn recipe <player> example:fortune_advanced`.
- `example:create_capacity`: ON_ENCHANTMENT_DISCOVERY, requires Create.
- Three additive loot pools. Installing these alongside defaults adds extra rolls; override existing IDs to replace distribution.

Physical books, power and ingredients remain required. See ../../docs/INSCRIBED_DATAPACK_AUTHORING.md.
