> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Anvil and Grindstone - Inscribed 0.2.0

AnvilResultMixin checks AnvilMenu.createResult at RETURN on client and server, including early returns through AnvilUpdateEvent. AnvilRestrictionService compares both ENCHANTMENTS and STORED_ENCHANTMENTS maps (holder and level) between the first input and proposed output. Tooltip flags are not enchantment levels.

- Book-to-item transfer that adds/changes enchantments: no output.
- Item merge, level upgrade or copying another enchantment: no output.
- Removing an enchantment from the proposed output: no output.
- Rename: allowed under vanilla rules when both maps remain identical.
- Material repair or item repair: allowed when both maps remain identical.
- Enchanted-book combination producing new knowledge: blocked through STORED_ENCHANTMENTS.
- Creative follows the same rule. Vanilla XP costs, too-expensive rule, material quantities and prior-work penalty remain vanilla.

An enchanted second input is allowed when the output still preserves the exact maps of the first input. This guards vanilla and events executed within createResult. A mod replacing the output afterwards may need separate compatibility work. Administrative commands are unchanged.

Grindstone remains vanilla: removes non-curse enchantments, preserves curses, retains vanilla repair/XP behavior and creates no extraction book. No Grindstone Mixin or knowledge-transfer path was added.
