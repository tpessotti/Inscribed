# Inscribed 0.8.0 publication kit

Target: Minecraft **1.21.1**, **NeoForge 21.1.255+**, Java **21**, client and server, **MIT**. The mod works without optional providers.

The release build produces `build/publication/` with the installable JAR, official default datapack, source archive, complete upload kit and SHA-256 checksums. The QA probe is a separate development artifact and is never an uploadable mod dependency.

## Prepared materials

- Main [README](../../README.md), [Portuguese presentation](../README.pt-BR.md), [authoring tutorial](../ADDING_ENCHANTMENTS.md) and [Portuguese tutorial](../ADDING_ENCHANTMENTS.pt-BR.md).
- Actual in-game screenshots in `docs/images/`: altar overview, active infusion, Librarian trade and standalone Guide. `project-icon.png` is a native Minecraft render of the actual pedestal, rather than generated promotional artwork.
- Generated `CURSEFORGE_DESCRIPTION.md` and `MODRINTH_DESCRIPTION.md` use English text and public GitHub image URLs. Review platform media eligibility before submitting.
- [Release notes](RELEASE_NOTES_0.8.0.md), [metadata](PROJECT_METADATA.json), [provenance and disclosure](PROVENANCE.md), [license audit](LICENSE_AUDIT.md).
- `RELEASE_0.8.0_VALIDATION.json` records packaging, test results, checksums and screenshot provenance after final validation.

Validation includes four unit tests, 106 dedicated development tests, the same 106 tests against the actual packaged JAR, and a standalone native client verifying the Librarian purchase and a catalog infusion. Optional-provider compatibility code is preserved; this release does not claim a fresh full provider matrix beyond those stated checks.

## CurseForge submission

1. Create a **Minecraft Java mod** project in the author dashboard. Suggested title: **Inscribed**; confirm slug/name availability there.
2. Use the English summary/description, MIT, source and issue links from the metadata. Relevant categories: Magic and Equipment; do not tag unrelated categories.
3. Select Minecraft 1.21.1 and the **NeoForge** loader. Upload `inscribed-0.8.0.jar` as the main mod file with the prepared release notes.
4. Declare JEI, Create/Ponder and LSBC only as optional integrations where corresponding platform projects exist. NeoForge/Java requirements are installation requirements; do not invent a project ID for an unpublished integration.
5. Upload the native icon and selected in-game screenshots, then review the submission before sending it to moderation.

CurseForge requires English project descriptions and has its own moderation process. [Official project submission guide](https://support.curseforge.com/support/solutions/articles/9000197241-creating-and-submitting-a-project).

## Modrinth submission

1. Review [PROVENANCE.md](PROVENANCE.md) first. This is an actual eligibility condition, not a claim that moderation has approved the project.
2. Prepare project type **mod**, license **MIT**, game version **1.21.1**, loader **NeoForge**, and client/server support required. Add the source and issue links.
3. Apply the platform's **Contains AI-generated content** disclosure accurately for code, bundled assets and written content. Owner design direction and testing do not by themselves establish that the project meets Modrinth's criteria for public listing.
4. Use only media allowed by its current rules. The screenshots are genuine game captures; do not upload the original AI texture-generation outputs as gallery images or a branding icon.
5. Confirm eligibility/visibility with Modrinth before public submission. Supply platform project IDs only after the project exists; this kit does not invent IDs or contain API credentials.

The project has substantial Codex-assisted development and AI-generated bundled artwork. **Public-listing eligibility remains unresolved under Modrinth's AI policy.** The prepared JAR and documentation do not remove that condition. [Official policy](https://support.modrinth.com/en/articles/16551575-disclosure-and-usage-of-ai).

## GitHub

The requested workflow commits the complete reviewed 0.8.0 source, pushes it to `main`, verifies MIT, and changes `tpessotti/Inscribed` to public. A draft release can hold the prepared assets. A draft is not a published CurseForge/Modrinth release.

`python tools/github_publication.py inspect` reads repository metadata using the local Git credential helper. `public` and `draft` are explicit owner/admin operations; never put credentials into command arguments or repository files. The script only targets `tpessotti/Inscribed` and refuses to overwrite a published 0.8.0 release.

## Rebuild

Follow [developer setup](../INSCRIBED_DEV_SETUP.md) for legitimate optional compile API JARs. Then:

```text
./gradlew build test runGameTestServer
python tools/prepare_publication.py
```

Windows uses `gradlew.bat`. Native screenshots can be reproduced with the separate preview utility; it requires a valid local Minecraft/NeoForge installation and creates its own world/profile. It never opens player saves. See [capture_release.py](../../tools/capture_release.py).

Publishing to either mod platform still requires its author account/project and submission. There is no unattended upload workflow, stored API key or automatic platform publication in this repository.
