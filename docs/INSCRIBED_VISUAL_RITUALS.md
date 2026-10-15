> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Ritual visual language — Inscribed 0.4.0

The altar is a physical circuit of knowledge: target first, table second, directional transfer third, participating pedestals and reserved bookshelves fourth. Vanilla particles and configurable vanilla sound IDs serve as placeholders. No custom world lighting, shader dependency, temporary light blocks or renderer replacement is introduced.

| Phase | Timeline | Presentation |
| --- | --- | --- |
| Validation | Start / first 3% | Quiet page confirmation and restrained glyphs; inputs remain untouched |
| Awakening | 3–15% | Target rises, selected pedestals become active, distinct start pitch |
| Resonance | 15–32% | Pedestal motion/pulses and converging reagent paths |
| Transfer | 32–82% | Reserved bookshelf and target paths show direction of knowledge |
| Crescendo | 82–100% | Stronger target pulse, denser focus and faster sound pacing |
| Completion | Successful commit | Brief target burst and success sound; extraction highlights result shelf |
| Dissipation | Next 20 ticks | Item pulse and emissive intensity fade to idle |

**Infusion:** pedestal/book source → table core → target. **Extraction:** reagents still support the table, but knowledge visibly follows target → core → reserved empty-book shelf. Marching particle points interpolate along a controlled arcing path; vanilla glyphs supplement those paths. Only actual reserved slots participate, with at most three distinct bookshelf paths.

`AltarState` synchronizes start game time, duration, mode and completion end time in the existing attachment. Pedestals synchronize corresponding timing through block-entity update tags. Clients derive animation from server time; no per-tick animation packet or client decision about success/consumption is used. Those fields are deliberately not saved, so an interrupted session cannot restart visual effects after load. Cancellation clears them immediately, without a completion fade or new particle stream; already emitted short-lived vanilla particles expire normally.

The floating target rises by at most 0.12 block plus bobbing, with a 3.5% scale pulse at maximum strength. Rotation is a continuous oscillating resonance layered over idle rotation, avoiding discontinuous angle jumps when phases change. Catalyst has a modest extra pulse. Empty consumed pedestals cannot render nonexistent ingredients, but the table target provides completion/dissipation feedback.

`visuals.phase_ritual_effects` controls phase motion/pacing refinements. Existing `render_items`, `particles`, `particle_interval_ticks`, scales/heights and motion settings remain. `dynamic_light` retains its safe meaning: interpolated packed item light only, not illumination of surrounding blocks. It remains false by default to preserve previous configuration; the native visual fixture enables it to inspect the emissive curve. Vanilla full-bright particles still provide subtle glow when it is off.

Sounds retain all prior start/running/completion/failure IDs and add `validation`, `invalid_ritual`, `extraction_transfer`. Infusion pitch rises; extraction page/pull pitch falls. Running interval shortens during crescendo but never goes below eight ticks. Error feedback is a quiet private sound plus localized action bar; no chat flood. Volume zero mutes every ritual sound. Unknown registered sound IDs fall back to vanilla.

Running particles obey the configured interval, emit from at most two rotating reserved pedestal sources and three bookshelf positions per step and never scan chunks each frame. Small glyphs replace long-lived white trails to keep the table readable. Spatial altar scans remain start/debug/commit operations; running validation uses captured references. No GPU or multi-altar benchmark claim is made; client validation captures real rendering and cleanup, while server tests cover bounded lifecycle state.

Read-only `RitualDiagnostics` chooses the closest physically eligible candidate for compact missing-ingredient/Catalyst/book/empty-book/power feedback. It does not teach names or grant discovery. `/inscribed debug altar errors [pos]` lists detailed operator diagnostics. Unrecognized or incompatible targets retain a generic no-valid-ritual message.
