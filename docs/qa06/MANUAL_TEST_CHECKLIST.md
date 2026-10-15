# Reproducible tester checklist

Use a backed-up test world, record versions and content IDs, and restore temporary probability/config changes afterward. The automated report records what was executed; this checklist also supports independent human playtesting.

| Area | Procedure and expected result |
| --- | --- |
| Core | Insert/retrieve target and pedestal items in survival/creative; repeated clicks conserve unit counts; offhand does not duplicate |
| Infusion | Supply physical book, exact ingredients and power; complete requested level; test higher reference book, incompatible/same/lower target and multi-enchantment reference |
| Extraction | Use selector ingredients and empty books; chosen enchantment only is removed; preserve other components; force risk 0/1 in fixture to verify success/failure; restore 20% default |
| Anvil | Repair/rename preserve enchantments; transfer/merge rejected by default; config false restores vanilla |
| Grindstone | Remove ordinary enchantments, preserve curses, observe vanilla XP behavior |
| JEI | Learn/forget/relearn, change recipe and reload; eligible categories refresh, hidden recipes stay hidden; inspect Catalyst and exact level |
| LSBC | Acquire a book, test partial KnownWords, search and Codex tab; compare Guide/Codex bounds; inspect altar with magnifying glass |
| Create | Infuse/extract real Capacity/Potato Recovery; absence skips recipes; contraption cannot move a pedestal |
| Multiplayer | Two clients use same altar, attempt stale/invalid selection; actor disconnect cancels, reconnect completes; observer knowledge remains private |
| Reload | Edit valid recipe, reload, verify new recipe; disabled/invalid file omitted with useful log; active ritual cancels |
| Save/load | Restart before commit, check inputs and no zombie animation; retain custom components; open copied save with provider absent then restored |
| Visuals | GUI scales 2/3/4/auto; low/windowed/16:9/ultrawide; inspect page margins, arrows, sub-tabs, long names/tooltips; table/pedestal sword/book/block/small/large/modded models |
| Sounds | One infusion ambient loop; distinct extraction feedback; cancellation/unload/disconnect stop audio |
| Loot | Newly generated structure table rolls, exact IDs, disabled switches, rare Catalyst and bounded added results |

Commands: see [troubleshooting](../INSCRIBED_TROUBLESHOOTING.md). Test examples in their own world: adding their loot pools changes default balance and invalidates default-distribution assumptions.
