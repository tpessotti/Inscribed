# Compatibility - 0.7.2

Stable promotion of 0.7.2-rc.1, with byte-identical gameplay classes/assets/data. [Stable validation](stable-0.7.2/RESULTS.json).

Minecraft 1.21.1, NeoForge 21.1.255, Java 21.

| Mod | Required? | Tested? | Notes |
| --- | --- | --- | --- |
| NeoForge 21.1.255 | Yes | Yes | Dedicated and native clients |
| JEI 19.57.0.451 | No | Yes | Client categories and live visibility/reload; server JAR unnecessary |
| LSBC 0.22.1 | No | Yes | KnownWords, Codex tab, shared book geometry and inspection |
| Create 6.0.10 | No | Yes | Conditional recipes, actual enchantments and non-movable pedestal |
| Ponder 1.0.82+mc1.21.1 | No | Yes | Optional Create tutorial; EN/PT playback and replay |
| Other modded enchantments | No | Registry support | Add recipes by datapack; provider-specific semantics need testing |
| Shader / dynamic-light mods | No | Not in this matrix | Inscribed's own full-bright items only |
| EMI / REI | No | No integration | Outside scope |

The [QA matrix](INSCRIBED_QA_MATRIX.md) records seven dependency combinations, real two-client multiplayer, reload and migration evidence. Optional classes are isolated/gated. Removing LSBC leaves the Guide and Book and Quill inspection; removing JEI leaves ordinary rituals; absent Create skips its conditional recipes.

Default anvils permit repairs/renaming while preserving the first input's enchantments; enchantment transfer/merge is rejected when `anvil.restrict_enchantment_changes=true`. Setting false deliberately restores vanilla behavior. Grindstones remain destructive vanilla removal with curse preservation. Other mods replacing these mechanics should be checked against these rules.

The 0.7.1 [Ponder integration report](ponder/REPORT.md) records the localized tutorial and absent-provider validation.

The RC tests load production classes from the actual candidate JAR through a separate development-only QA module. Seven dedicated and native cases passed; see [RC evidence](rc08/RESULTS.json). Server-only startup also passed without the QA module. Ponder EN/PT and replay were rerun against the RC. Full target-modpack human playtesting and third-party shaders are outside this matrix.
