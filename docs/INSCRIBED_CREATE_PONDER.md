# Optional Create / Ponder tutorial

Inscribed 0.7.1 registers an animated Ponder scene for `minecraft:enchanting_table` and `inscribed:infusion_pedestal` when **Create and Ponder are both installed on the client**. Create 6.0.10 includes Ponder 1.0.82 for Minecraft 1.21.1. Neither is required by Inscribed.

Hover the Enchanting Table item in your inventory or JEI and hold **W**, the default Ponder key. The pedestal item opens the same tutorial. Use the normal Ponder controls to pause, inspect, replay and jump between steps. This does not change the table's world interactions.

The scene demonstrates the shipped Sharpness I recipe:

1. Enchanting Table at the center.
2. Two ordinary bookshelves supplying 2 Bookshelf Power.
3. Chiseled Bookshelf containing a physical Sharpness I-or-higher book.
4. Three separate pedestals: two amethyst shards and one iron ingot.
5. Right-click with an iron sword to place the target.
6. Shift + right-click to start a five-second Infusion without XP/lapis.
7. Reagents consumed and Sharpness I applied; the reference book's default consumption chance is 10%.
8. Empty-main-hand right-click to retrieve the result.

English and Portuguese captions describe the default scan limits, one-item pedestal rule and the distinction between learned knowledge and physical books. This is a **visual tutorial of the default recipe**, not a live recipe simulation. Modpacks can override ingredients, power, durations and chances; consult the actual Guide/JEI recipe in that pack. The tutorial does not consume real inventory, grant knowledge or run server rituals.

Resource packs can replace `assets/inscribed/ponder/enchanting_setup.nbt` and the `inscribed.ponder.enchanting_setup.*` language keys. The original schematic is included in the Inscribed JAR. Ponder/Create classes and JARs are not bundled. Registration is isolated behind a client-only optional-mod check; no Ponder classes load on the dedicated server or absent-provider clients.

Development compiles against the Ponder JAR extracted from the workspace's installed Create into `libs/ponder-neoforge-1.0.82+mc1.21.1.jar` as `compileOnly`. Runtime uses Create's bundled Ponder. The integration uses the public plugin/storyboard API shown in [Create's Ponder plugin](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/foundation/ponder/CreatePonderPlugin.java).

Validation evidence: [Ponder integration report](ponder/REPORT.md).

Caption keys are `header` and `text_1` through `text_8`. Language JSON uses Minecraft formatting: escape a literal percent as `%%`.
