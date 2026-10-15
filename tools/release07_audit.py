"""Read-only authoring/release audit. Requires Python 3 and jsonschema."""
from pathlib import Path
import hashlib
import json
import re
import struct
import zipfile
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parents[1]

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def audit():
    errors = []
    recipe_schema = json.loads((ROOT / 'schemas/inscribed_enchantment_recipe.schema.json').read_text())
    loot_schema = json.loads((ROOT / 'schemas/inscribed_loot.schema.json').read_text())
    for schema in (recipe_schema, loot_schema):
        Draft202012Validator.check_schema(schema)
    recipe_validator = Draft202012Validator(recipe_schema)
    loot_validator = Draft202012Validator(loot_schema)
    validated = []
    for folder in ('examples', 'templates', 'src/main/resources/data'):
        for path in (ROOT / folder).rglob('*.json'):
            try:
                data = json.loads(path.read_text(encoding='utf-8-sig'))
                if 'enchantment_recipes' in path.parts or path.name.startswith('recipe_'):
                    recipe_validator.validate(data)
                    validated.append(str(path.relative_to(ROOT)))
                    for mode in ('infusion', 'extraction'):
                        costs = data.get(mode, {}).get('ingredients', [])
                        if sum(c.get('count', 1) for c in costs) > 8:
                            raise ValueError('Expanded ingredient count exceeds eight')
                elif 'loot_injections' in path.parts or path.name == 'loot_entry.json':
                    loot_validator.validate(data)
                    validated.append(str(path.relative_to(ROOT)))
                    if data.get('enabled', True):
                        if data['max_rolls'] < data['min_rolls']:
                            raise ValueError('max_rolls below min_rolls')
                        for entry in data['entries']:
                            for low, high in (('min_level', 'max_level'), ('min_count', 'max_count')):
                                if high in entry and entry[high] < entry.get(low, 1):
                                    raise ValueError(f'{high} below {low}')
            except Exception as exc:
                errors.append(f'{path.relative_to(ROOT)}: {exc}')
    en = json.loads((ROOT / 'src/main/resources/assets/inscribed/lang/en_us.json').read_text())
    pt = json.loads((ROOT / 'src/main/resources/assets/inscribed/lang/pt_br.json').read_text())
    if en.keys() != pt.keys():
        errors.append('English/Portuguese translation keys differ')
    # Current public documentation only; old implementation reports retain historical evidence.
    public_docs = [ROOT / 'README.md', ROOT / 'CHANGELOG.md', ROOT / 'CREDITS.md']
    public_docs += [ROOT / 'docs' / name for name in (
        'INSCRIBED_CONFIGURATION.md', 'INSCRIBED_DATAPACK_AUTHORING.md',
        'INSCRIBED_MODPACK_QUICKSTART.md', 'INSCRIBED_INTEGRATIONS.md',
        'INSCRIBED_TROUBLESHOOTING.md', 'INSCRIBED_ADDON_API.md',
        'INSCRIBED_COMPATIBILITY.md', 'INSCRIBED_KNOWN_LIMITATIONS.md',
        'INSCRIBED_SAVE_MIGRATION.md', 'INSCRIBED_RELEASE_CHECKLIST.md',
        'INSCRIBED_QA_MATRIX.md', 'INSCRIBED_CATALYST.md',
        'INSCRIBED_RECIPE_AUTHORING.md', 'MODPACK_CONFIGURATION.md',
        'INSCRIBED_LOOT_INJECTION.md', 'INSCRIBED07_REPORT.md',
        'INSCRIBED_RELEASE_DESCRIPTIONS.md', 'qa06/MANUAL_TEST_CHECKLIST.md')]
    public_docs += list((ROOT / 'docs/schema').glob('*.md'))
    for path in public_docs:
        if not path.exists():
            errors.append(f'Missing document: {path.relative_to(ROOT)}')
            continue
        for link in re.findall(r'\]\(([^)]+)\)', path.read_text(encoding='utf-8')):
            if link.startswith(('https:', 'http:', '#')):
                continue
            target = link.split('#')[0].strip('<>')
            if not (path.parent / target).exists():
                errors.append(f'{path.relative_to(ROOT)}: broken link {link}')
    logo = ROOT / 'src/main/resources/assets/inscribed/textures/item/guide_book_placeholder_v2.png'
    data = logo.read_bytes()
    if data[:8] != b'\x89PNG\r\n\x1a\n':
        errors.append('Guide/logo is not a PNG')
    dimensions = struct.unpack('>II', data[16:24])
    jar = ROOT / 'build/libs/inscribed-0.7.0.jar'
    jar_report = {}
    if jar.exists():
        with zipfile.ZipFile(jar) as archive:
            names = archive.namelist()
            if 'LICENSE' not in names:
                errors.append('LICENSE absent from JAR')
            metadata = archive.read('META-INF/neoforge.mods.toml').decode()
            if 'license="MIT"' not in metadata or 'version="0.7.0"' not in metadata:
                errors.append('Release metadata version/license mismatch')
            leaked = [name for name in names if any(x in name for x in ('GameTests', 'ClientValidation', '06Validation', '06Client', 'qa06_', 'qa_missing', 'inscribed_authoring/')) or name.startswith(('mezz/', 'dev/lsbc/', 'com/simibubi/'))]
            if leaked:
                errors.append(f'Development/third-party classes leaked into JAR: {leaked}')
            jar_report = {'sha256': digest(jar), 'bytes': jar.stat().st_size, 'entries': len(names), 'licenseIncluded': True}
    else:
        errors.append('Release JAR absent')
    report = {'status': 'PASS' if not errors else 'FAIL', 'validatedContentFiles': len(validated),
              'validatedFiles': validated, 'translationKeys': len(en), 'logoDimensions': dimensions,
              'publicDocumentsChecked': len(public_docs), 'jar': jar_report, 'errors': errors}
    output = ROOT / 'docs/qa06/RELEASE07_AUDIT.json'
    output.write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({k: v for k, v in report.items() if k != 'validatedFiles'}, indent=2))
    return 0 if not errors else 1

if __name__ == '__main__':
    raise SystemExit(audit())
