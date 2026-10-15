# Building Inscribed 0.7.2

Use JDK 21, Python 3 with `jsonschema`, and the checked-in Gradle wrapper (9.2.1). Minecraft 1.21.1, NeoForge 21.1.255 and official NeoForm mappings 20240808.144430 are pinned by the build. The first build needs network access for Gradle/NeoForge artifacts.

## Optional-provider compile dependencies

Optional integrations require their APIs while compiling, even though players can run without those providers. Supply your legitimate local JARs: JEI 19.57.0.451, LSBC 0.22.1 and Create 6.0.10 for Minecraft 1.21.1/NeoForge. Create supplies Ponder 1.0.82+mc1.21.1. None of these binaries is checked into this repository or bundled in Inscribed.

```text
python -m pip install -r requirements-dev.txt
python tools/prepare_dev_dependencies.py --jei PATH_TO_JEI.jar --lsbc PATH_TO_LSBC.jar --create PATH_TO_CREATE.jar
```

The helper copies the three providers into ignored `libs/` and extracts Create's nested Ponder. The Create binary is needed by the production-content audit to verify its two registered enchantments. You can instead point the compiler at `-PjeiJar=... -PlsbcJar=... -PponderJar=...` and set `INSCRIBED_CREATE_JAR` for the audit. LSBC's source/JAR must be obtained from its owner; no public Maven artifact is assumed.

```text
./gradlew clean test build
```

Windows: use `gradlew.bat`. Outputs are `build/libs/inscribed-0.7.2.jar` and `build/distributions/inscribed-vanilla-create-0.7.2.zip`. Canonical data is `content/inscribed_default_datapack`; compatible editable defaults are generated from it. Do not hand-edit generated build files.

## Runtime QA

Ordinary development uses `runClient`, `runServer` and `runGameTestServer`. `releaseQa` is a separate development-only module for testing the actual packaged JAR, not production classes from the source set. Place the matching release JAR and desired optional providers in the run directory's `mods/`; invoke `runReleaseGameTestServer -PrcRunDirectory=...` or `runReleaseClient -PvalidateGuide=true -PrcRunDirectory=...`. `runReleaseServer` uses an empty source set and only installed JARs. Runtime test worlds/configs/screenshots and optional binaries are ignored.

The `tools/rc_*` scripts and `docs/rc08` preserve historical RC evidence and include assumptions about the original test workspace. They are not a portable replacement for the documented build. The portable production recipe gate runs automatically with `build`; see `tools/README.md`. No formatter or runData task is configured.
