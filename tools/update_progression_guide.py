"""Add player-facing progression notes to the existing shared nine chapters."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/inscribed/guide'
notes={
 'en_us':{
  'home':'Strong rituals need stronger libraries, rarer reagents and sometimes an Enchantment Catalyst. Finding a powerful book early does not replace those requirements.',
  'power':'Stronger rituals need stronger libraries. Expand ordinary bookshelf power and inspect the altar before risking a rare reference book. Chiseled shelves provide physical knowledge rather than power.',
  'recipes':'Look for aquatic knowledge in ocean exploration, tool knowledge underground, combat knowledge in hostile structures, and rarer knowledge in ancient cities, bastions and end cities. Create knowledge also appears in configured mineshaft/stronghold pools. Books may be consumed during infusion, so duplicate books remain valuable. Extraction selectors remain deterministic, with the existing 20% failure risk shown per recipe. Modpacks can change power, reagents, risk and distribution in config/inscribed/recipes.json and config/inscribed/loot.json; /inscribed reload applies them.',
  'pedestals':'Each pedestal holds exactly one item; at most eight pedestal ingredients per ritual. Use separate pedestals for repeated materials. Retrieve and split legacy stacks. Enchantment Catalyst is used by stronger rituals and is found rarely in intermediate structures, more often in dangerous treasure sources. Late-game rituals also need advanced materials. Basic rituals do not require Catalyst.',
 },
 'pt_br':{
  'home':'Rituais fortes exigem bibliotecas melhores, reagentes raros e, às vezes, um Catalisador de Encantamento. Encontrar um livro poderoso cedo não substitui esses requisitos.',
  'power':'Rituais fortes exigem bibliotecas melhores. Amplie a potência das estantes comuns e inspecione o altar antes de arriscar um livro raro. Estantes esculpidas fornecem conhecimento físico, não potência.',
  'recipes':'Procure conhecimento aquático no oceano, ferramentas no subsolo, combate em estruturas hostis e conhecimento raro em cidades ancestrais, bastiões e cidades do End. Livros Create também aparecem nos pools configurados de minas e fortalezas. Como a infusão pode consumir o livro, duplicatas continuam valiosas. Os ingredientes escolhem a extração, mantendo o risco de falha de 20% exibido na receita. Modpacks podem alterar potência, reagentes, risco e distribuição em config/inscribed/recipes.json e config/inscribed/loot.json; /inscribed reload aplica as mudanças.',
  'pedestals':'Cada pedestal aceita um item; cada ritual usa no máximo oito ingredientes em pedestais separados. Retire e separe pilhas antigas. Rituais fortes usam Catalisador de Encantamento, encontrado raramente em estruturas intermediárias e com maior frequência em fontes perigosas de tesouro. Rituais tardios também exigem materiais avançados. Rituais básicos não exigem Catalyst.',
 }
}
for locale,patch in notes.items():
    path=root/(locale+'.json');rows=json.loads(path.read_text(encoding='utf-8'))
    for row in rows:
        if row['id'] in patch:row['body']=patch[row['id']]
    assert len(rows)==9
    path.write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
