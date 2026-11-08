# License audit — Inscribed 0.8.0

- The owner-selected root `LICENSE` is MIT and remains byte-identical to the previously committed license.
- Production `META-INF/neoforge.mods.toml` declares `license="MIT"`; the build embeds the root LICENSE in the release JAR.
- The official default datapack's LICENSE is MIT. Example content uses the repository's MIT license.
- Original project code/assets are covered by that project license; it does not relicense Minecraft, NeoForge or optional integrations. Generative-AI use is documented separately as provenance, not concealed as third-party hand-made artwork.
- The release audit rejects bundled QA classes, optional-provider binaries/classes, local credentials, logs, worlds and caches.
- Gradle wrapper/build infrastructure retains upstream licensing. [Third-party notices](../../THIRD_PARTY_NOTICES.md) and [credits](../../CREDITS.md) remain part of the source and publication kit.

This records the actual repository/artifact declarations and packaging checks. It does not promise approval by a hosting platform or change any separately installed mod's license.
