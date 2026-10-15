"""Write reviewed exploration pools and shared guide notes for Inscribed 04."""
from pathlib import Path
import json,zipfile

P=Path(__file__).resolve().parents[1]
if 'mod_version=0.4.0' not in (P/'gradle.properties').read_text(encoding='utf-8'):
    raise SystemExit('Historical stage-04 resource generator: use the current thematic loot data and update_progression_guide.py instead.')
R=P/'src/main/resources'
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def book(name,weight=10,minimum=1,maximum=1,mod=None):
    row={'enchantment':name if ':' in name else 'minecraft:'+name,'weight':weight,'min_level':minimum,'max_level':maximum}
    if mod:row['required_mod']=mod
    return row

structures={
 'coastal':['shipwreck_supply','shipwreck_treasure','buried_treasure'],
 'underground':['abandoned_mineshaft','simple_dungeon','stronghold_library','stronghold_corridor','stronghold_crossing'],
 'temples':['jungle_temple','desert_pyramid'],
 'dangerous':['ancient_city','woodland_mansion'],
 'trials':['trial_chambers/reward','trial_chambers/reward_rare','trial_chambers/reward_unique','trial_chambers/reward_ominous','trial_chambers/reward_ominous_rare','trial_chambers/reward_ominous_unique'],
}
common=[book('unbreaking',18,1,2),book('efficiency',18,1,2),book('protection',15,1,2),book('sharpness',15,1,2),book('feather_falling',12,1,2)]
themes={
 'coastal':common+[book('respiration',12,1,2),book('aqua_affinity',12),book('luck_of_the_sea',8,1,2)],
 'underground':common+[book('fortune',8,1,2),book('silk_touch',2),book('mending',1)],
 'temples':common+[book('looting',6,1,2),book('infinity',2),book('silk_touch',2)],
 'dangerous':[book('unbreaking',20,2,3),book('efficiency',18,3,5),book('protection',18,3,4),book('fortune',10,2,3),book('mending',3),book('swift_sneak',3,1,3),book('create:capacity',4,1,3,'create'),book('create:potato_recovery',4,1,3,'create')],
 'trials':[book('unbreaking',20,2,3),book('density',15,1,3),book('breach',12,1,2),book('wind_burst',2),book('mending',2)],
}
vanilla=P.parents[2]/'CODEX LSBC/prototypes/lsbc_core/.gradle-user-home/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
with zipfile.ZipFile(vanilla) as z:
    vanilla_tables={n[len('data/minecraft/loot_table/'):-5] for n in z.namelist() if n.startswith('data/minecraft/loot_table/') and n.endswith('.json')}
pools={}
for group,names in structures.items():
    tables=['minecraft:chests/'+n for n in names]
    for table in tables:assert table.split(':',1)[1] in vanilla_tables,table
    for kind in ('books','catalyst'):
        advanced=group in ('dangerous','trials')
        pool={'enabled':True,'loot_tables':tables,'chance':(.2 if advanced else .12) if kind=='books' else (.16 if advanced else .06),
              'min_rolls':1,'max_rolls':1,'entries':themes[group] if kind=='books' else [{'item':'inscribed:enchantment_catalyst','weight':1,'min_count':1,'max_count':1}]}
        key='inscribed:'+group+'_'+kind
        pools[key]=pool
        write(R/'data/inscribed/inscribed/loot_injections'/f'{group}_{kind}.json',pool)
write(R/'inscribed/default-loot.json',{'format_version':1,'include_datapack_pools':True,'pools':pools})

translations={
 'en_us':{
  'item.inscribed.enchantment_catalyst':'Enchantment Catalyst',
  'guide.inscribed.pedestal_constraints':'Up to 8 pedestals; one item on each.',
  'message.inscribed.pedestal_occupied':'One item per pedestal. Remove its item first.',
  'message.inscribed.legacy_pedestal_stack':'Split saved pedestal stacks: one item per pedestal.',
  'message.inscribed.missing_catalyst':'Missing Enchantment Catalyst',
  'message.inscribed.missing_ingredients':'Missing pedestal ingredients',
  'message.inscribed.missing_enchanted_book':'Missing matching enchanted book',
  'message.inscribed.missing_empty_book':'Missing empty book',
  'message.inscribed.no_empty_book_slot':'Split empty books into bookshelf slots for the result.',
  'message.inscribed.insufficient_power':'Insufficient Bookshelf Power',
  'message.inscribed.no_recipe':'No valid ritual found',
 },
 'pt_br':{
  'item.inscribed.enchantment_catalyst':'Catalisador de Encantamento',
  'guide.inscribed.pedestal_constraints':'Até 8 pedestais; um item em cada.',
  'guide.inscribed.infusion':'Infusão',
  'guide.inscribed.extraction':'Extração',
  'guide.inscribed.max_level':'Nível máximo: %s',
  'guide.inscribed.power':'Potência das estantes: %s',
  'guide.inscribed.duration':'Duração: %s ticks (%s s)',
  'guide.inscribed.failure':'Falha na extração: %s%%; custos e encantamento serão perdidos.',
  'guide.inscribed.books':'Livros vazios necessários: %s',
  'guide.inscribed.book_exact':'Livro encantado físico: exige o nível exato.',
  'guide.inscribed.book_minimum':'Livro encantado físico: este nível ou superior.',
  'guide.inscribed.targets':'Exemplos de alvos compatíveis:',
  'guide.inscribed.quantity':'Total necessário: %s',
  'message.inscribed.pedestal_occupied':'Um item por pedestal. Retire o item primeiro.',
  'message.inscribed.legacy_pedestal_stack':'Separe as pilhas antigas: um item por pedestal.',
  'message.inscribed.missing_catalyst':'Falta o Catalisador de Encantamento',
  'message.inscribed.missing_ingredients':'Faltam ingredientes nos pedestais',
  'message.inscribed.missing_enchanted_book':'Falta um livro encantado compatível',
  'message.inscribed.missing_empty_book':'Falta um livro vazio',
  'message.inscribed.no_empty_book_slot':'Separe os livros vazios nos slots da estante para receber o resultado.',
  'message.inscribed.insufficient_power':'Potência de estantes insuficiente',
  'message.inscribed.no_recipe':'Nenhum ritual válido encontrado',
 }
}
for locale,patch in translations.items():
    path=R/'assets/inscribed/lang'/f'{locale}.json'
    existing=json.loads(path.read_text(encoding='utf-8'))
    # Repair older double-encoded localized strings without changing valid UTF-8.
    if locale=='pt_br':
        for key,value in existing.items():
            for _ in range(2):
                if 'Ã' not in value and 'â€' not in value:break
                try:value=value.encode('cp1252').decode('utf-8')
                except (UnicodeEncodeError,UnicodeDecodeError):break
            existing[key]=value
    existing.update(patch);write(path,existing)

pt=[
 ('home','Inscribed','Descubra encantamentos obtendo livros encantados. Este guia explica o altar. Conhecer uma receita não substitui os livros e ingredientes físicos. Com LSBC, palavras desconhecidas podem continuar em glifos.'),
 ('altar','Altar','Coloque o alvo numa mesa de encantamentos, ingredientes nos pedestais próximos e livros de referência ou vazios em estantes esculpidas. Use shift + clique direito para iniciar. Analise com a lupa ou com livro e pena quando LSBC estiver ausente. Infusão converge para o alvo; extração flui do alvo para o livro.'),
 ('infusion','Infusão','Um livro encantado físico autoriza seu encantamento no nível exigido ou superior, salvo receitas que exigem nível exato. Forneça os ingredientes e a potência necessária. Se várias infusões corresponderem, escolha uma na tela. A chance configurada pode consumir o livro inteiro de referência, inclusive seus outros encantamentos.'),
 ('extraction','Extração','Os ingredientes nos pedestais selecionam o encantamento extraído, sem tela de seleção. Forneça livros vazios nas estantes e potência suficiente. A falha consome ingredientes e livros vazios e remove o encantamento selecionado sem gerar livro. Os outros encantamentos permanecem. Ajuste os ingredientes se houver várias correspondências.'),
 ('library','Biblioteca física','Estantes esculpidas guardam livros encantados de referência e livros vazios. Eles devem permanecer no lugar durante o ritual. Ver um livro numa estante ou num contêiner não ensina o encantamento. Na extração, reserve um slot que ficará vazio para receber o livro resultante.'),
 ('pedestals','Pedestais e Catalyst','Cada pedestal aceita exatamente um item. Cada receita usa até oito entradas unitárias; itens repetidos ocupam pedestais separados. Retire e separe pilhas salvas por versões antigas. O Catalisador de Encantamento participa de rituais avançados, tesouros e certos encantamentos Create; não é obrigatório em todos os rituais.'),
 ('power','Potência de estantes','Estantes comuns fornecem potência conforme o raio configurado do altar. Estantes esculpidas guardam os livros usados no ritual. Use a inspeção para conferir a potência disponível. Os requisitos continuam físicos, mesmo após descobrir uma receita.'),
 ('recipes','Conhecimento e configuração','Obter um livro encantado ou concluir uma extração ensina seus encantamentos. Guia, Codex e receitas Inscribed no JEI respeitam descoberta. A exploração fornece livros e Catalyst conforme o modpack. Edite config/inscribed/recipes.json, config/inscribed/loot.json e inscribed-server.toml. /inscribed reload aplica receitas e loot. O índice global de livros do JEI pode revelar nomes. Nenhuma troca de aldeão é alterada.'),
 ('anvil','Bigorna','A bigorna preserva os encantamentos do primeiro item. Use o altar para aplicar ou extrair encantamentos. Reparos e nomes continuam disponíveis quando preservam os encantamentos. A regra pode ser configurada pelo modpack.'),
]
write(R/'assets/inscribed/guide/pt_br.json',[dict(id=i,title=t,body=b) for i,t,b in pt])
en_path=R/'assets/inscribed/guide/en_us.json'
en=json.loads(en_path.read_text(encoding='utf-8'))
for row in en:
    if row['id']=='pedestals':row['body']='Each pedestal holds exactly one item. Ritual recipes use at most eight unit entries; repeated materials need separate pedestals. Retrieve and split legacy stacks without losing their contents. Enchantment Catalyst gates advanced, treasure and selected Create rituals; basic rituals do not universally require it.'
    if row['id']=='configuration':row['body']='Edit config/inscribed/recipes.json for rituals/crafting, config/inscribed/loot.json for structure tables/chance/weights/rolls, and inscribed-server.toml for options. /inscribed reload applies recipe and loot changes. Books and catalysts have separate loot switches. Villager trades are unchanged.'
    if row['id']=='recipes' and 'config/inscribed/loot.json' not in row['body']:row['body']+=' Exploration may yield enchanted books and catalysts from configured vanilla structure loot. Configure exact tables, chances, weights and rolls in config/inscribed/loot.json. Separate server options disable book or catalyst injection. Viewing loot does not teach enchantments until acquisition. Villager trades are unchanged.'
write(en_path,en)
print(json.dumps({'lootPools':len(pools),'verifiedVanillaLootTables':len({t for p in pools.values() for t in p['loot_tables']})}))
