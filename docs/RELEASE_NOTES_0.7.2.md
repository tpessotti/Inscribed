# Inscribed 0.7.2

Stable promotion of the validated 0.7.2-rc.1, approved by the owner on 2026-10-14. Minecraft 1.21.1, NeoForge 21.1.255 and Java 21. MIT license unchanged.

Gameplay classes, assets and the canonical 131 recipe definitions/13 loot pools are unchanged from the RC. Default extraction failure remains 20%; ingredient selection remains deterministic. The promotion changes version/repository metadata and development/repository packaging only.

Optional tested providers: JEI 19.57.0.451, LSBC 0.22.1, Create 6.0.10 and its Ponder 1.0.82+mc1.21.1. No provider binaries are bundled. Inscribed remains fully functional without the optional integrations.

Back up saves/configs when upgrading and keep only one Inscribed JAR in mods. Existing customized configs are preserved; same-ID JSON config entries take precedence over datapacks. The official Vanilla + Create datapack is already embedded; installing its separate ZIP is optional.

The RC matrix and limitations remain applicable: [RC report](INSCRIBED08_REPORT.md), [compatibility](INSCRIBED_COMPATIBILITY.md), [known limitations](INSCRIBED_KNOWN_LIMITATIONS.md), [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md). Stable promotion validation and checksums are recorded in [stable results](stable-0.7.2/RESULTS.json). See [standalone build instructions](INSCRIBED_DEV_SETUP.md).

Repository: https://github.com/tpessotti/Inscribed (private, owner-selected). Source and release assets are published only there; no CurseForge/Modrinth publication is part of this promotion.
