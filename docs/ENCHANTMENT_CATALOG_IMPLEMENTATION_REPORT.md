> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Inscribed 0.1.1 — Catálogo vanilla + Create

Data: 2026-10-13. Incremento autorizado após Core + Altar MVP, antes do prompt 02.

## Resultado

**131 receitas de infusão**, uma por nível legal: **125 para os 42 encantamentos vanilla de Minecraft 1.21.1** e **6 para os dois encantamentos do Create 6.0.10**. São **130 receitas novas**; `inscribed:test_sharpness_1` conserva ID, ingredientes, duração, potência, chance e dados futuros de extraction exatamente como no MVP.

Cobertura inclui todos os níveis de tesouros e maldições, além dos encantamentos da maça (Density, Breach e Wind Burst). O Create base instalado registra somente `create:capacity` e `create:potato_recovery`, ambos I–III. Addons Aeronautics/Submarine não fazem parte do pedido de receitas do Create base.

Os IDs e máximos foram extraídos dos JSONs de enchantment nos JARs locais exatos, não de uma lista memorizada ou versão diferente. O catálogo JSON guarda caminhos e SHA256 desses JARs. As receitas continuam dependendo do livro físico, power, compatibilidade do target e ingredientes nos pedestais.

## Autoria e balanceamento inicial

Arquivos novos em `data/inscribed/inscribed/enchantment_recipes/minecraft/` e `create/`. Cada receita nova usa ametistas, um catalisador de nível e um reagente temático. Catalisadores: cobre I, ferro II, ouro III, diamante IV e netherita V. Afiação I já usava ferro; Afiação II usa cobre para distinguir a montagem dos dois níveis.

Quantidade de ametista = 2 × nível, catalisador = nível, reagente temático = nível; escudo é limitado a 1 por ser item não empilhável. Power comum = 2 + 4 × (nível − 1); tesouros/maldições partem de 10. Duração = 100 + 40 × (nível − 1) ticks. Chance comum = 20% + 5% por nível adicional; tesouros/maldições partem de 35%. A receita original Afiação I preserva seus 25%.

Os custos são provisórios e editáveis por datapack. Um livro superior continua autorizando infusão inferior: o catalisador escolhido permite montar esse nível. Deixar ingredientes e livros extras que habilitem mais de um match continua gerando a mensagem de ambiguidade do MVP.

[Tabela completa de custos, power, duração e risco](ENCHANTMENT_RECIPE_CATALOG.md). [Catálogo JSON e proveniência](ENCHANTMENT_RECIPE_CATALOG.json). Gerador reproduzível: `tools/generate_enchantment_catalog.py`.

## Create opcional

As seis receitas usam `neoforge:conditions` com `neoforge:mod_loaded` / `create`. O reload agora usa `ICondition.conditionsMatched(makeConditionalOps(), json)` antes do parser: condição falsa omite a receita antes de tentar resolver enchantments/ingredientes. Isso mantém Inscribed funcional sem Create e evita mensagens de IDs Create inexistentes.

Nenhuma importação Java do Create/LSBC, dependência obrigatória ou mod de terceiro foi adicionado/alterado. Foi adicionada apenas uma run de teste isolada para o Create instalado; seu JAR inclui as dependências Ponder/Flywheel/Registrate via jar-in-jar.

## Validação atual

- Build 0.1.1 concluído com sucesso; 4 JUnit sem falhas.
- **30 GameTests com Inscribed sozinho**, todos aprovados; as receitas Create são omitidas corretamente.
- **30 GameTests com Inscribed + Create 6.0.10 + LSBC 0.21.0**, todos aprovados.
- O teste de catálogo confere exatamente uma receita para cada nível legal dos registries runtime e monta os ingredientes de todas as receitas distribuídas para conferir o matching físico.
- Os testes Create aplicam Capacity I a Copper Backtank e Potato Recovery I a Potato Cannon, usando livros III, e verificam o consumo dos três pedestais ao concluir.
- Os 27 testes anteriores de altar, consumo, cancelamento, persistência, configurações e reload continuam aprovados.
- A primeira execução detectou receitas de Projectile Protection II–IV que pediam mais de um escudo em uma pilha; foram corrigidas para um escudo e a matriz completa foi reexecutada com sucesso.

Evidência final: `validation-catalog-final.log`, cópia integral do log da matriz aprovada. Dois arquivos inválidos intencionais em gameTest continuam gerando diagnósticos esperados; não são distribuídos. Capturas nativas 0.1.0 foram preservadas como evidência histórica do Core e não são apresentadas como novas capturas 0.1.1.

## Release e preservação

JAR: `mods/inscribed-0.1.1.jar`, substituindo somente o `inscribed-0.1.0.jar` previamente instalado. O 0.1.0 e seus fontes/docs/tools foram arquivados em `CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-enchantment-catalog`. A instalação verifica hashes e mantém uma única versão Inscribed na pasta mods.

O [manifest atual](INSCRIBED_RELEASE_MANIFEST.json) contém hash/tamanho do JAR, cobertura, arquivos adicionados/alterados e log dos testes. JARs de terceiros, LSBC e configs existentes foram conferidos contra o preflight e preservados. Os testes usaram somente mundos em `runs`.

Reinicie cliente/servidor com a nova versão. O schema e o armazenamento de itens continuam iguais; este incremento não adiciona seleção UI, extraction executável, regras de bigorna, guide book ou integração KnownWords/Codex.
