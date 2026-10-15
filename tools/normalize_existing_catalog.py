"""Audit and normalize the existing catalog; never generate a replacement pack."""
from pathlib import Path
from collections import Counter
import json

PROJECT = Path(__file__).resolve().parents[1]
VERSION=next(line.split('=',1)[1] for line in (PROJECT/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))
DEST = PROJECT / 'src/main/resources/data/inscribed/inscribed/enchantment_recipes'
TREASURE = {'binding_curse', 'vanishing_curse', 'mending', 'frost_walker', 'soul_speed', 'swift_sneak', 'wind_burst'}

def needs_catalyst(recipe):
    ns, name = recipe['enchantment'].split(':')
    level = recipe['level']
    return name in TREASURE or name in {'silk_touch', 'infinity'} or level >= 3 or ns == 'create' and level >= 2

def normalize(recipe):
    recipe = json.loads(json.dumps(recipe))
    for mode in ('infusion', 'extraction'):
        if mode not in recipe:
            continue
        original = recipe[mode]['ingredients']
        if all(x.get('count', 1) == 1 for x in original) and len(original) <= 8:
            # Already normalized entries are kept verbatim on subsequent runs.
            continue
        costs = json.loads(json.dumps(original))
        catalyst = needs_catalyst(recipe)
        total = sum(x.get('count', 1) for x in costs) + int(catalyst)
        if total > 8:
            if mode == 'infusion':
                # Keep existing level-specific material and thematic identity.
                for i, cost in enumerate(costs):
                    cost['count'] = min(cost.get('count', 1), 2 if i == 0 else 1 if i == 1 else 3)
            else:
                costs[0]['count'] = min(costs[0].get('count', 1), 6)
        if catalyst:
            costs.append({'item': 'inscribed:enchantment_catalyst', 'count': 1})
        units = []
        for cost in costs:
            count = cost.pop('count', 1)
            units.extend(dict(cost, count=1) for _ in range(count))
        assert len(units) <= 8, (recipe['enchantment'], recipe['level'], mode)
        recipe[mode]['ingredients'] = units
    return recipe

def refresh_catalog():
    rows = []
    changes = []
    defaults_path = PROJECT / 'src/main/resources/inscribed/default-recipes.json'
    defaults = json.loads(defaults_path.read_text(encoding='utf-8'))
    for path in sorted(DEST.rglob('*.json')):
        old = json.loads(path.read_text(encoding='utf-8'))
        new = normalize(old)
        rel = path.relative_to(DEST).as_posix()
        if old != new:
            changes.append({'recipe': 'inscribed:' + rel[:-5], 'before': old, 'after': new,
                            'reason': 'Unit pedestal expansion; over-budget costs reduced while retaining thematic reagents and level-specific material; advanced ritual catalyst.'})
            path.write_text(json.dumps(new, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
        rows.append({'file': rel, **new})
        defaults['enchantment_recipes']['inscribed:'+rel[:-5]] = {'enabled': True, **new}
    defaults_path.write_text(json.dumps(defaults, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    catalog_path = PROJECT / 'docs/ENCHANTMENT_RECIPE_CATALOG.json'
    catalog = json.loads(catalog_path.read_text(encoding='utf-8'))
    catalog.update(version=VERSION, recipes=rows)
    catalog_path.write_text(json.dumps(catalog, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    lines = [f'# Existing Vanilla + Create catalog — Inscribed {VERSION}', '',
             'Existing IDs, enchantments, power, duration and risk retained. Each ingredient entry uses one item on its own pedestal; maximum eight. Advanced rituals use Enchantment Catalyst. Physical books remain mandatory.', '']
    for mode in ('infusion', 'extraction'):
        lines += [f'## {mode.title()}', '', '| Recipe | Power | Pedestals | Ingredients | Ticks | Book risk / failure |', '| --- | --- | --- | --- | --- | --- |']
        for row in rows:
            data = row[mode]
            counts = Counter(x.get('item', '#'+x.get('tag','')) for x in data['ingredients'])
            cost = ', '.join(f'{n} × `{item}`' for item,n in counts.items())
            risk = data.get('book_consumption_chance',data.get('failure_chance',0))
            lines.append(f"| `{row['enchantment']}` {row['level']} | {row['minimum_bookshelf_power']} | {len(data['ingredients'])} | {cost} | {data['duration_ticks']} | {risk:.0%} |")
        lines += ['']
    (PROJECT/'docs/ENCHANTMENT_RECIPE_CATALOG.md').write_text('\n'.join(lines),encoding='utf-8')
    # Retain the first before/after audit when rerun, rather than erasing history.
    report = PROJECT/f'docs/{"04" if VERSION=="0.4.0" else "current"}-normalization-rebalance.json'
    if changes or not report.exists():
        report.write_text(json.dumps(changes,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    summary = {'recipes':len(rows), 'vanilla':sum(x['enchantment'].startswith('minecraft:') for x in rows),
               'create':sum(x['enchantment'].startswith('create:') for x in rows),
               'changedRecipes':len(changes), 'maximumPedestals':max(len(x[m]['ingredients']) for x in rows for m in ('infusion','extraction')),
               'catalystRecipes':[('inscribed:'+x['file'][:-5]) for x in rows if any(i.get('item')=='inscribed:enchantment_catalyst' for i in x['infusion']['ingredients'])]}
    (PROJECT/f'docs/{"04" if VERSION=="0.4.0" else "current"}-normalization-audit.json').write_text(json.dumps(summary,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({k:v for k,v in summary.items() if k!='catalystRecipes'}))

if __name__ == '__main__':
    refresh_catalog()
