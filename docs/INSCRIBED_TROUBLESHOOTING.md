# Troubleshooting

| Symptom | Check |
| --- | --- |
| Recipe missing | Correct `data/<namespace>/inscribed/enchantment_recipes` path, enabled pack, JSON and reload |
| Override unchanged | Explicit same-ID JSON config entry wins; remove intentionally after backup |
| Unknown enchantment | Exact registered ID, provider mod, valid level and optional condition |
| Ritual not starting | Compatible target, physical book, individual costs, no extra populated pedestals, reach and sneaking |
| Insufficient power | Ordinary bookshelves supply power; physical Chiseled Bookshelf storage is separate |
| JEI recipe invisible | Integration enabled, local discovery, reload/snapshot; visibility is per player |
| LSBC glyph name | Acquire the book to learn words; viewing a shelf does not teach |
| Catalyst unavailable | Loot switches/multipliers, pool entry and probabilistic structure loot |
| Loot absent | Exact table, enabled pool, valid entries, newly generated/unopened container and repeated rolls |

Search `latest.log` for `Invalid Inscribed recipe <id>`, `Invalid Inscribed loot pool`, `Invalid entry in Inscribed loot pool`, `config ... rejected` and `Loaded ... Inscribed`. Messages include IDs and field reasons; the logger does not uniformly use literal `[Inscribed]`. Missing target JSON emits a warning. Rejected content does not crash the whole reload.

## Administrator commands

All Inscribed commands require permission level **2**. These are maintenance tools:

```
/inscribed reload
/inscribed debug recipe audit
/inscribed debug altar scan [x y z]
/inscribed debug altar recipes [x y z]
/inscribed debug altar errors [x y z]
/inscribed debug altar start [x y z]
/inscribed debug altar cancel [x y z]
/inscribed debug loot
/inscribed debug loot table <namespace:id>
/inscribed debug loot enchantment <namespace:id>
/inscribed debug balance <recipe_id>
/inscribed debug recipes visible <player>
/inscribed knowledge status <player>
/inscribed knowledge clear <player>
/inscribed knowledge learn enchantment <player> <namespace:id>
/inscribed knowledge forget enchantment <player> <namespace:id>
/inscribed knowledge learn recipe <player> <namespace:id>
/inscribed knowledge forget recipe <player> <namespace:id>
```

Learn checks ID existence; forget can remove stale IDs. Commands never supply physical library books. Test with ordinary `/datapack list enabled`, `/reload`, `/give` and `/loot` in administrator worlds.

Reports should include versions, content IDs, config overlay entries, actual altar inspection and relevant log excerpts. Back up before dependency/save changes. Deliberately invalid fixture data appears only in development tests, not the JAR.
