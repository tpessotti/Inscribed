# Como adicionar um encantamento ao Inscribed

Este tutorial usa Inscribed 0.8.0, Minecraft 1.21.1 e NeoForge. A receita integra ao ritual um **encantamento que já existe no registro do jogo**. Ela não cria seu efeito. Um encantamento inteiramente novo deve ser definido antes por um mod ou pelo registro de encantamentos de datapacks do Minecraft; consulte a [documentação do NeoForge 1.21.1](https://docs.neoforged.net/docs/1.21.1/resources/server/enchantments/).

## 1. Crie o datapack

Copie [examples/add_enchantment](../examples/add_enchantment) para `<mundo>/datapacks`. O arquivo `pack.mcmeta` fica diretamente na raiz da pasta ou ZIP:

```text
meus_rituais/
  pack.mcmeta
  data/example/inscribed/enchantment_recipes/sharpness_1.json
```

O `pack.mcmeta` usa `pack_format: 48`. O arquivo do exemplo cria a receita `example:sharpness_1`. Use um namespace próprio: isso adiciona uma alternativa e evita sobrescrever as receitas originais.

## 2. Edite a receita completa

Abra [sharpness_1.json](../examples/add_enchantment/data/example/inscribed/enchantment_recipes/sharpness_1.json). O exemplo já inclui infusão e extração:

- `enchantment`: ID registrado, como `minecraft:sharpness` ou o ID real fornecido por outro mod.
- `level`: nível exato aplicado; deve ser válido para o encantamento.
- `minimum_bookshelf_power`: potência mínima de estantes comuns.
- `infusion.ingredients`: um item por pedestal, até oito ingredientes expandidos. O exemplo usa sílex, ametista e ferro em três pedestais.
- `infusion.duration_ticks`: 100 ticks correspondem a cinco segundos em um servidor a 20 TPS.
- `infusion.book_consumption_chance`: `0.1` significa 10% de chance de consumir o livro de referência inteiro.
- `extraction.ingredients`: reagentes que selecionam a extração; no exemplo, lápis-lazúli e ametista.
- `extraction.empty_books_required`: quantidade de livros vazios exigida na biblioteca física.
- `extraction.failure_chance`: `0.2` significa 20% de falha. A falha consome os materiais, remove o encantamento selecionado e não gera livro.

Uma receita pode ter apenas infusão, apenas extração ou ambas. Na infusão, o livro encantado físico continua obrigatório. Um livro mais forte pode autorizar a receita, mas o resultado usa seu nível exato. Para exigir o nível exato do livro, adicione `"exact_book_level": true` na raiz.

Para aceitar uma tag de itens, use `{"tag":"c:gems/emerald"}`. Para três diamantes, repita o ingrediente três vezes e use três pedestais. O catalisador usa `{"item":"inscribed:enchantment_catalyst"}` como qualquer reagente unitário.

## 3. Integre outro mod

Troque o campo `enchantment` pelo ID real registrado pelo mod e escolha um nível permitido. O equipamento também deve aceitar esse encantamento; o Inscribed respeita incompatibilidades e os itens suportados.

Se o mod for opcional, acrescente na raiz:

```json
"neoforge:conditions": [
  {"type":"neoforge:mod_loaded","modid":"id_real_do_mod"}
]
```

Substitua `id_real_do_mod`. A receita será ignorada quando o provedor estiver ausente. Não deixe IDs fictícios em receitas habilitadas.

## 4. Carregue e teste no jogo

1. Execute `/datapack list enabled` e depois `/reload` ou `/inscribed reload`.
2. Confira no `latest.log` a quantidade de receitas e mensagens que mencionem seu ID.
3. Para o exemplo, guarde um livro de Afiação I ou superior na estante esculpida e forneça ao menos dois pontos de potência por estantes comuns.
4. Coloque uma espada compatível na mesa e os três reagentes de infusão em pedestais separados.
5. Inicie o ritual e escolha a receita nova se houver várias opções válidas.
6. Teste a extração com o equipamento encantado, os reagentes correspondentes e um livro vazio. Considere a chance de falha.

Remova o datapack de exemplo depois do teste se não quiser manter essa alternativa adicional. Ao adicionar registros novos de encantamentos ou mods provedores, reinicie o mundo/servidor; apenas recarregar receitas pode não ser suficiente.

## Prioridade, nomes e descoberta

Entradas explícitas em `config/inscribed/recipes.json` têm prioridade sobre datapacks com o mesmo ID. O arquivo gerado contém as receitas padrão. Para adicionar conteúdo, prefira um namespace novo. Para substituir uma receita padrão, remova apenas a entrada correspondente do overlay, após fazer backup. Não apague configurações personalizadas como atalho.

`ALWAYS` exibe a receita imediatamente; `ON_ENCHANTMENT_DISCOVERY` espera a descoberta do encantamento; `EXPLICIT_DISCOVERY` espera a descoberta do ID da receita. Isso controla a apresentação, sem substituir livros, materiais ou potência.

O nome vem da descrição/tradução do encantamento original. O Inscribed não tem um campo separado de título para cada receita. Para um encantamento novo, forneça os nomes no mod ou resource pack apropriado.

O [JSON Schema](../schemas/inscribed_enchantment_recipe.schema.json) ajuda a validar a estrutura. O parser do servidor e o teste real conferem registros, tags e compatibilidade. Consulte também a [referência completa](INSCRIBED_DATAPACK_AUTHORING.md). O exemplo é validado na auditoria da versão e não é instalado como conteúdo padrão.
