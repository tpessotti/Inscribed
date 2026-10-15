"""Release gate for the canonical production datapack. No gameplay/data mutation."""
from pathlib import Path
import os
import collections
import hashlib
import json
import math
import sys
import zipfile
from jsonschema import Draft202012Validator

P = Path(__file__).resolve().parents[1]
W = P.parents[2]
PACK = P / 'content/inscribed_default_datapack'
OUT = P / 'docs/rc08'

def read(path):
    return json.loads(path.read_text(encoding='utf-8'))

def audit(write_reports=True):
    errors = []
    expected = {}
    item_models = set()
    sources = {
        'minecraft': P / 'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar',
        'create': Path(os.environ.get('INSCRIBED_CREATE_JAR', str(P/'libs/create-1.21.1-6.0.10.jar' if (P/'libs/create-1.21.1-6.0.10.jar').is_file() else W/'mods/create-1.21.1-6.0.10.jar'))),
    }
    for namespace, jar in sources.items():
        with zipfile.ZipFile(jar) as archive:
            prefix = f'data/{namespace}/enchantment/'
            for name in archive.namelist():
                if name.startswith(prefix) and name.endswith('.json'):
                    key = namespace + ':' + name[len(prefix):-5]
                    expected[key] = json.loads(archive.read(name))['max_level']
                model = f'assets/{namespace}/models/item/'
                if name.startswith(model) and name.endswith('.json'):
                    item_models.add(namespace + ':' + name[len(model):-5])
    item_models.update({'inscribed:enchantment_catalyst'})
    validator = Draft202012Validator(read(P / 'schemas/inscribed_enchantment_recipe.schema.json'))
    loot_validator = Draft202012Validator(read(P / 'schemas/inscribed_loot.schema.json'))
    ids = {}
    logical = {}
    rows = []
    for path in sorted((PACK / 'data').glob('*/inscribed/enchantment_recipes/**/*.json')):
        relative = path.relative_to(PACK / 'data')
        recipe_id = relative.parts[0] + ':' + '/'.join(relative.parts[3:])[:-5]
        name = path.relative_to(P).as_posix()
        try:
            body = read(path)
            validator.validate(body)
            if recipe_id in ids:
                raise ValueError(f'duplicate recipe_id {recipe_id}: {ids[recipe_id]}, {name}')
            ids[recipe_id] = name
            enchantment, level = body['enchantment'], body['level']
            if enchantment not in expected:
                raise ValueError(f'unknown enchantment {enchantment}')
            if not 1 <= level <= expected[enchantment]:
                raise ValueError(f'invalid level {level} for {enchantment}')
            if enchantment.startswith('create:') and body.get('neoforge:conditions') != [{'type':'neoforge:mod_loaded','modid':'create'}]:
                raise ValueError('Create recipe requires its mod_loaded condition')
            for mode in ('infusion','extraction'):
                if mode not in body:
                    raise ValueError(f'missing required {mode}')
                key = (mode,enchantment,level)
                if key in logical:
                    raise ValueError(f'accidental duplicate {key}: {logical[key]}, {name}')
                logical[key] = name
                costs = body[mode]['ingredients']
                if len(costs) > 8 or any(cost.get('count',1) != 1 for cost in costs):
                    raise ValueError(f'{mode}: maximum eight unit ingredients')
                for cost in costs:
                    if 'item' in cost and cost['item'] not in item_models:
                        raise ValueError(f'unknown ingredient item {cost["item"]}; runtime registry validation also required')
                probability = body[mode].get('book_consumption_chance' if mode=='infusion' else 'failure_chance',0)
                if not math.isfinite(probability) or not 0 <= probability <= 1:
                    raise ValueError(f'{mode}: probability outside 0..1')
                if mode=='extraction' and probability != .2:
                    raise ValueError('Default extraction must preserve the approved 20% risk')
            rows.append({'source':'Create' if enchantment.startswith('create:') else 'Vanilla',
                         'enchantment':enchantment,'level':level,'infusion':True,'extraction':True,
                         'recipeId':recipe_id,'status':'PASS','file':name})
        except Exception as exc:
            errors.append(f'{name}: {exc}')
    for enchantment, maximum in expected.items():
        for level in range(1,maximum+1):
            for mode in ('infusion','extraction'):
                if (mode,enchantment,level) not in logical:
                    errors.append(f'Missing coverage: {mode} {enchantment} level {level}')
    if list((P / 'src/main/resources/data').glob('*/inscribed/enchantment_recipes/**/*.json')):
        errors.append('Legacy production recipes remain in src/main/resources/data')
    pools = []
    for path in sorted((PACK/'data').glob('*/inscribed/loot_injections/**/*.json')):
        try:
            body = read(path); loot_validator.validate(body)
            if body['max_rolls'] < body['min_rolls']:
                raise ValueError('max_rolls below min_rolls')
            for entry in body['entries']:
                if 'enchantment' in entry:
                    enchantment = entry['enchantment']
                    if enchantment not in expected or not 1 <= entry['min_level'] <= entry['max_level'] <= expected[enchantment]:
                        raise ValueError(f'invalid loot enchantment/level {enchantment}')
            pools.append(body)
        except Exception as exc:
            errors.append(f'{path.relative_to(P)}: {exc}')
    rows.sort(key=lambda r:(r['source'],r['enchantment'],r['level']))
    report = {'status':'PASS' if not errors else 'FAIL','recipeFiles':len(rows),
              'ritualDefinitions':len(logical),'vanillaEnchantments':sum(k.startswith('minecraft:') for k in expected),
              'createEnchantments':sum(k.startswith('create:') for k in expected),
              'pools':len(pools),'lootTables':len(set(t for pool in pools for t in pool['loot_tables'])),
              'intentionalExclusions':[],'intentionalAlternativeRecipes':[],'errors':errors,'coverage':rows}
    if write_reports:
        OUT.mkdir(parents=True,exist_ok=True)
        (OUT/'RECIPE_AUDIT.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
        table = '\n'.join(f'| {r["source"]} | `{r["enchantment"]}` | {r["level"]} | PASS | PASS | `{r["recipeId"]}` | {r["status"]} | '+('Stable historical production ID' if r['recipeId']=='inscribed:test_sharpness_1' else '')+' |' for r in rows)
        (P/'docs/INSCRIBED_RECIPE_COVERAGE_FINAL.md').write_text(
            '# Final production recipe coverage\n\n'+
            f'Gate: **{report["status"]}**. {len(rows)} files, {len(logical)} ritual definitions, '
            f'{report["vanillaEnchantments"]} Vanilla and {report["createEnchantments"]} Create enchantments. '
            'All supported levels have both Infusion and Extraction. No intentional exclusions or alternate default recipes.\n\n'+
            'Canonical source: `content/inscribed_default_datapack`. The mod JAR and config mirrors are generated from it. '
            'The separately shipped datapack replaces the same IDs, so it does not create extra active recipes. '
            '`inscribed:test_sharpness_1` is intentionally retained for save/knowledge/config compatibility; it is the sole canonical Sharpness I recipe.\n\n'+
            '| Source | Enchantment | Level | Infusion | Extraction | Recipe ID | Status | Notes |\n'+
            '| --- | --- | --- | --- | --- | --- | --- | --- |\n'+table+'\n\n'+
            ('Errors:\n\n'+'\n'.join('- '+error for error in errors) if errors else 'Duplicate IDs/logical recipes: none. Missing levels/rituals: none. All costs use at most eight unit entries. Runtime GameTests validate registered ingredients and actual matching separately.')+'\n',encoding='utf-8')
    print(json.dumps({k:v for k,v in report.items() if k!='coverage'},indent=2))
    return 0 if not errors else 1

if __name__=='__main__':
    raise SystemExit(audit())
