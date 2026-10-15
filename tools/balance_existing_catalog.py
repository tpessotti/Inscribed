"""Inscribed 05: classify and rebalance existing IDs, then audit thematic sources."""
from pathlib import Path
from collections import Counter
import json,zipfile

P=Path(__file__).resolve().parents[1]
R=P/'src/main/resources'
CAT=R/'data/inscribed/inscribed/enchantment_recipes'
TIER_NAMES=['UNCLASSIFIED','TIER_1_BASIC','TIER_2_INTERMEDIATE','TIER_3_ADVANCED','TIER_4_TREASURE','TIER_5_LATE_GAME']
TREASURE={'mending','silk_touch','soul_speed','frost_walker','swift_sneak','wind_burst'}
OCEAN={'respiration','aqua_affinity','depth_strider','frost_walker','luck_of_the_sea','lure','riptide','loyalty','impaling','channeling'}
TOOLS={'efficiency','fortune','unbreaking','silk_touch','mending'}
COMBAT={'sharpness','smite','bane_of_arthropods','power','punch','knockback','looting','fire_aspect','flame','piercing','quick_charge','multishot','protection','blast_protection','projectile_protection','fire_protection','thorns','density','breach','wind_burst','binding_curse','vanishing_curse'}

def tier(d):
    ns,name=d['enchantment'].split(':');level=d['level']
    if ns=='create':return 3 if level==3 else 2
    if name=='wind_burst':return 5 if level==3 else 4
    if name in TREASURE:return 4
    if name in {'binding_curse','vanishing_curse'}:return 2
    if name in {'density','breach'}:return 5 if (name=='density' and level==5 or name=='breach' and level==4) else 3
    if name in {'efficiency','sharpness','power'} and level==5:return 5
    if name in {'fortune','looting'}:return 3 if level>=2 else 2
    if name=='infinity':return 3
    if name in {'efficiency','sharpness','power','protection','smite','bane_of_arthropods'}:return 3 if level>=4 else 2 if level>=2 else 1
    if level>=3:return 3
    if level==2 or name in {'aqua_affinity','respiration','depth_strider','loyalty','riptide','channeling','multishot','flame'}:return 2
    return 1

def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def costs(d,mode,t):
    previous_catalyst=next((i for i,x in enumerate(d[mode]['ingredients']) if x.get('item')=='inscribed:enchantment_catalyst'),None)
    entries=[dict(x,count=1) for x in d[mode]['ingredients'] if x.get('item')!='inscribed:enchantment_catalyst']
    catalyst=t>=3 or t==2 and d['enchantment']=='minecraft:channeling'
    if catalyst:entries.insert(len(entries) if previous_catalyst is None else min(previous_catalyst,len(entries)),{'item':'inscribed:enchantment_catalyst','count':1})
    # Expensive heavy cores retain thematic identity without several cores per ritual.
    seen_core=False;kept=[]
    for e in entries:
        if e.get('item')=='minecraft:heavy_core':
            if seen_core:continue
            seen_core=True
        kept.append(e)
    entries=kept
    material='minecraft:netherite_ingot' if t==5 else 'minecraft:diamond' if t>=3 else None
    if material and not any(e.get('item')==material for e in entries):entries.append({'item':material,'count':1})
    while len(entries)>8:
        amethyst=next((i for i,e in enumerate(entries) if e.get('item')=='minecraft:amethyst_shard'),None)
        if amethyst is None:raise ValueError(('No safe unit reduction',d['enchantment'],mode))
        entries.pop(amethyst)
    assert len(entries)<=8 and all(e['count']==1 for e in entries)
    return entries,catalyst

def main():
    changes=[];rows=[]
    for f in sorted(CAT.rglob('*.json')):
        old=json.loads(f.read_text(encoding='utf-8'));d=json.loads(json.dumps(old));t=tier(d);n=d['level']
        d['progression_tier']=TIER_NAMES[t]
        d['minimum_bookshelf_power']={1:2,2:8+min(4,2*(n-1)),3:18+min(4,2*max(0,n-2)),4:26+min(2,n-1),5:32+min(4,n-1)}[t]
        d['infusion']['book_consumption_chance']=round({1:.10,2:.20+.03*min(3,n-1),3:.35+.04*min(3,max(0,n-2)),4:.50+.03*min(3,n-1),5:.65+.03*min(2,max(0,n-3))}[t],2)
        for mode in ('infusion','extraction'):d[mode]['ingredients'],_=costs(d,mode,t)
        # Preserve the user's explicitly confirmed existing extraction risk.
        assert d['extraction']['failure_chance']==old['extraction']['failure_chance']==.20
        write(f,d);recipe_id='inscribed:'+f.relative_to(CAT).as_posix()[:-5]
        rows.append(dict(recipe_id=recipe_id,**d))
        if old!=d:changes.append({'recipe_id':recipe_id,'before':old,'after':d,'rationale':'Per-level impact tier; graduated library power, bounded thematic costs and coordinated book risk. Existing extraction failure retained.'})

    pool_specs=[
      ('ocean','OCEAN',2,['shipwreck_supply','shipwreck_treasure','buried_treasure','underwater_ruin_small','underwater_ruin_big'],.14,lambda d:d['enchantment'].split(':')[1] in OCEAN|{'unbreaking'} and tier(d)<=2),
      ('mining','UNDERGROUND',2,['abandoned_mineshaft','simple_dungeon'],.14,lambda d:d['enchantment'].split(':')[1] in TOOLS|{'protection'} and tier(d)<=2),
      ('combat','COMBAT',2,['pillager_outpost','jungle_temple','desert_pyramid'],.12,lambda d:d['enchantment'].split(':')[1] in COMBAT and tier(d)<=2),
      ('tools_advanced','UNDERGROUND',3,['stronghold_library','stronghold_corridor'],.18,lambda d:d['enchantment'].split(':')[1] in TOOLS and tier(d)==3),
      ('combat_advanced','COMBAT',3,['woodland_mansion','bastion_other','trial_chambers/reward','trial_chambers/reward_rare','trial_chambers/reward_unique'],.18,lambda d:d['enchantment'].split(':')[1] in COMBAT and tier(d)==3),
      ('exploration','EXPLORATION',3,['stronghold_crossing','woodland_mansion'],.16,lambda d:d['enchantment'].split(':')[1] in OCEAN|{'feather_falling','sweeping_edge','infinity'} and tier(d)<=3 and (tier(d)==3 or d['enchantment'].split(':')[1] in {'feather_falling','sweeping_edge','infinity'})),
      ('treasure','TREASURE',4,['ancient_city','end_city_treasure','bastion_treasure'],.18,lambda d:tier(d)==4),
      ('late_game','ANCIENT',5,['ancient_city','end_city_treasure','bastion_treasure','trial_chambers/reward_ominous','trial_chambers/reward_ominous_rare','trial_chambers/reward_ominous_unique'],.14,lambda d:tier(d)==5),
      ('industrial','CREATE_INDUSTRIAL',2,['abandoned_mineshaft','stronghold_corridor'],.10,lambda d:d['enchantment'].startswith('create:') and tier(d)==2),
      ('industrial_advanced','CREATE_INDUSTRIAL',3,['stronghold_library','woodland_mansion'],.14,lambda d:d['enchantment'].startswith('create:') and tier(d)==3),
    ]
    pools={}
    for name,theme,host,tables,chance,predicate in pool_specs:
        entries=[]
        for d in rows:
            if not predicate(d):continue
            t=tier(d);weight={1:16,2:10,3:8,4:5,5:5}[t]
            if d['enchantment']=='minecraft:mending':weight=4
            # Individual exact levels allow explicit weights without mixing tiers.
            entry={'enchantment':d['enchantment'],'min_level':d['level'],'max_level':d['level'],'weight':weight}
            if d['enchantment'].startswith('create:'):entry['required_mod']='create'
            entries.append(entry)
        assert 0<len(entries)<=64,(name,len(entries))
        pools['inscribed:'+name+'_books']={'enabled':True,'theme':theme,'structure_tier':host,'treasure':host>=4,'require_recipe':True,
          'loot_tables':['minecraft:chests/'+x for x in tables],'chance':chance,'min_rolls':1,'max_rolls':1,'entries':entries}
    catalyst_groups=[
      ('mid','EXPLORATION',2,['shipwreck_treasure','buried_treasure','abandoned_mineshaft','simple_dungeon','pillager_outpost','jungle_temple','desert_pyramid'],.07),
      ('advanced','ARCANE',3,['stronghold_library','stronghold_corridor','stronghold_crossing','woodland_mansion','trial_chambers/reward','trial_chambers/reward_rare','trial_chambers/reward_unique'],.14),
      ('treasure','TREASURE',5,['ancient_city','end_city_treasure','bastion_treasure','trial_chambers/reward_ominous','trial_chambers/reward_ominous_rare','trial_chambers/reward_ominous_unique'],.22),
    ]
    for name,theme,host,tables,chance in catalyst_groups:
        entries=[{'item':'inscribed:enchantment_catalyst','weight':19,'min_count':1,'max_count':1}]
        if host==5:entries.append({'item':'inscribed:enchantment_catalyst','weight':1,'min_count':2,'max_count':2})
        pools['inscribed:'+name+'_catalyst']={'enabled':True,'theme':theme,'structure_tier':host,'treasure':host>=4,'require_recipe':False,
          'loot_tables':['minecraft:chests/'+x for x in tables],'chance':chance,'min_rolls':1,'max_rolls':1,'entries':entries}
    vanilla=P.parents[2]/'CODEX LSBC/prototypes/lsbc_core/.gradle-user-home/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
    with zipfile.ZipFile(vanilla) as z:
        tables={n[len('data/minecraft/loot_table/'):-5] for n in z.namelist() if n.startswith('data/minecraft/loot_table/') and n.endswith('.json')}
    for pool in pools.values():
        for table in pool['loot_tables']:assert table.split(':')[1] in tables,table
    directory=R/'data/inscribed/inscribed/loot_injections'
    # Remove only this project's previous shipped pool resources, after resolving
    # every exact file within the intended directory. No game config is deleted.
    for f in directory.glob('*.json'):
        assert f.resolve().parent==directory.resolve();f.unlink()
    for key,pool in pools.items():write(directory/(key.split(':')[1]+'.json'),pool)
    write(R/'inscribed/default-loot.json',{'format_version':1,'include_datapack_pools':True,'pools':pools})
    defaults=json.loads((R/'inscribed/default-recipes.json').read_text(encoding='utf-8'))
    for d in rows:defaults['enchantment_recipes'][d['recipe_id']]={'enabled':True,**{k:v for k,v in d.items() if k!='recipe_id'}}
    write(R/'inscribed/default-recipes.json',defaults)
    catalog=json.loads((P/'docs/ENCHANTMENT_RECIPE_CATALOG.json').read_text(encoding='utf-8'))
    catalog.update(version='0.5.0',recipes=[{'file':d['recipe_id'].split(':')[1]+'.json',**{k:v for k,v in d.items() if k!='recipe_id'}} for d in rows])
    write(P/'docs/ENCHANTMENT_RECIPE_CATALOG.json',catalog)
    before_report=P/'docs/05-recipe-rebalance.json'
    if not before_report.exists():write(before_report,changes)
    warnings=[];matrix=[]
    for d in rows:
        sources=[key for key,pool in pools.items() if any(e.get('enchantment')==d['enchantment'] and e.get('max_level',0)>=d['level'] for e in pool['entries'])]
        if not sources:warnings.append('NO_BOOK_SOURCE '+d['recipe_id'])
        catalyst=any(e.get('item')=='inscribed:enchantment_catalyst' for e in d['infusion']['ingredients'])
        matrix.append({'recipe_id':d['recipe_id'],'enchantment':d['enchantment'],'level':d['level'],'tier':d['progression_tier'],
          'bookshelf_power':d['minimum_bookshelf_power'],'catalyst_required':catalyst,'ingredient_count':len(d['infusion']['ingredients']),
          'extraction_ingredient_count':len(d['extraction']['ingredients']),'book_consumption_chance':d['infusion']['book_consumption_chance'],
          'extraction_failure_chance':d['extraction']['failure_chance'],'loot_sources':sources,'notes':'Create III is advanced utility, not automatically endgame; curses intermediate; physical book and actual power still mandatory.'})
    for key,pool in pools.items():
        for e in pool['entries']:
            if 'enchantment' not in e:continue
            recipe=next((r for r in rows if r['enchantment']==e['enchantment'] and r['level']==e['min_level']),None)
            if recipe is None:warnings.append('BOOK_WITHOUT_RECIPE '+key)
            elif tier(recipe)>pool['structure_tier']:warnings.append('HIGH_TIER_IN_LOW_STRUCTURE '+recipe['recipe_id']+' '+key)
    assert not warnings,warnings
    write(P/'docs/INSCRIBED_BALANCE_MATRIX.json',matrix)
    write(P/'docs/05-consistency-audit.json',{'recipes':len(rows),'pools':len(pools),'verifiedLootTables':len({t for p in pools.values() for t in p['loot_tables']}),
      'tiers':dict(Counter(d['progression_tier'] for d in rows)),'warnings':warnings,'extractionFailurePreserved':True})
    lines=['# Balance matrix — Inscribed 0.5.0','','131 existing recipe IDs, per-level tiers. Ordinary bookshelf power is additive in the configured cylinder, with no vanilla cap of 15. Catalyst and rare materials complement this infrastructure gate.','',
      '| Enchantment | Level | Tier | Power | Catalyst | Ingredients | Book Risk | Loot Sources |','| --- | --- | --- | --- | --- | --- | --- | --- |']
    for d,m in zip(rows,matrix):
        count=Counter(e.get('item',e.get('tag','')) for e in d['infusion']['ingredients']);cost=', '.join(f'{n}× `{i}`' for i,n in count.items())
        lines.append(f"| `{d['enchantment']}` | {d['level']} | {tier(d)} | {d['minimum_bookshelf_power']} | {'yes' if m['catalyst_required'] else 'no'} | {cost} | {m['book_consumption_chance']:.0%} | {', '.join(s.split(':')[1] for s in m['loot_sources'])} |")
    (P/'docs/INSCRIBED_BALANCE_MATRIX.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    (P/'docs/ENCHANTMENT_RECIPE_CATALOG.md').write_text('# Catalog — Inscribed 0.5.0\n\nThe existing 125 vanilla and six Create IDs are preserved. See [balance matrix](INSCRIBED_BALANCE_MATRIX.md) for infusion costs/power/risk/sources and [catalog JSON](ENCHANTMENT_RECIPE_CATALOG.json) for both modes. Each entry is one physical item; at most eight per mode. Extraction failure remains 20%, as explicitly confirmed by the user.\n',encoding='utf-8')
    loot_lines=['# Thematic loot distribution — Inscribed 0.5.0','','Vanilla table IDs are verified against the installed Minecraft 1.21.1 JAR. Additive rewards supplement existing loot; they do not replace vanilla books or trades.','',
      '| Pool | Theme / host tier | Tables | Book entries / Catalyst | Chance | Rationale |','| --- | --- | --- | --- | --- | --- |']
    for key,pool in pools.items():
        rewards=', '.join(f"{e['enchantment']} {e['min_level']} (w{e['weight']})" if 'enchantment' in e else f"Catalyst {e['min_count']} (w{e['weight']})" for e in pool['entries'])
        loot_lines.append(f"| `{key}` | {pool['theme']} / {pool['structure_tier']} | {', '.join(t.split(':')[1] for t in pool['loot_tables'])} | {rewards} | {pool['chance']:.0%} | Stronger sources exclude basic-tier domination; theme and tier determine entries. |")
    loot_lines += ['', 'Create pools require Create per entry and use vanilla industrial/exploration proxies: mineshafts and strongholds, then stronghold libraries/mansions. No Create structures are required. Catalyst: 7% mid, 14% advanced, 22% treasure; treasure wins yield one unit 95% of the time and two units 5%. Duplicates remain useful because reference books can be consumed. Multipliers change roll chance, not entry weight or book risk.']
    (P/'docs/INSCRIBED_LOOT_DISTRIBUTION.md').write_text('\n'.join(loot_lines)+'\n',encoding='utf-8')
    print(json.dumps({'recipes':len(rows),'tiers':dict(Counter(d['progression_tier'] for d in rows)),'pools':len(pools),'warnings':warnings}))

if __name__=='__main__':main()
