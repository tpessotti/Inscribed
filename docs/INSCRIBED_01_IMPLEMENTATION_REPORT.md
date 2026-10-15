> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Inscribed 01 — Core + Altar MVP: relatório de implementação

Data: 2026-10-13. Release: **Inscribed 0.1.0**.

**Registro histórico do Core + Altar 0.1.0.** O catálogo foi ampliado e a instalação atual é 0.1.1; consulte [catálogo](ENCHANTMENT_RECIPE_CATALOG.md) e [relatório do incremento](ENCHANTMENT_CATALOG_IMPLEMENTATION_REPORT.md). Os testes e hashes abaixo pertencem à release original, arquivada no backup.

O pipeline foi validado em mundos isolados: colocar fisicamente target/reagentes por cliques do cliente, iniciar com Shift, animar, aplicar enchantment, consumir ingredientes e o livro selecionado, sincronizar e retirar o resultado. Nenhum mundo do usuário foi aberto ou alterado para testes.

## 1. Estrutura criada

Projeto independente `CODEX LSBC/prototypes/inscribed`, com 25 classes main organizadas em registry, config, altar, pedestal, recipe, ritual, client, command e mixin. Fontes de testes separadas em gameTest e test. Nenhuma classe LSBC é importada.

## 2. Versões detectadas

Minecraft 1.21.1, NeoForge 21.1.255, Java 21.0.9, Gradle 9.2.1, ModDevGradle 2.0.148, Mojmap/NeoForm 20240808.144430 e FML 4.0.45. A versão da instância também foi conferida em `minecraftinstance.json`.

## 3. Extensão da Enchanting Table

Attachment persistente e sincronizado `inscribed:altar_target` na block entity vanilla. Evento RightClickBlock para inserção/retirada/início; um hook não cancelável de remoção devolve o target antes de perder a mesa, inclusive em substituição. Nenhuma troca de bloco, block entity ou renderer. A interação do menu vanilla passa a ser a interação física do altar.

A decisão, alternativas examinadas e confirmação das APIs locais constam em [arquitetura](INSCRIBED_ALTAR_ARCHITECTURE.md). A documentação de [attachments NeoForge](https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/) orientou o storage, e o helper de sync foi confirmado nas fontes locais da patch 21.1.255 e no runtime.

## 4. Pedestal

`inscribed:infusion_pedestal`: bloco com modelo geométrico placeholder, block entity, uma pilha, merge até o limite, retirada com mão vazia, save/load, update tag/packet e devolução ao quebrar. Os getters/setters copiam ItemStacks; não há transferência pela mão secundária.

## 5. Renderers

BER exclusivo para o pedestal e render adicional do target após os block entities. Flutuação discreta e rotação lenta, aceleradas durante ritual. O cliente nativo confirmou `EnchantTableRenderer` vanilla ainda registrado e desenhando o livro ao lado do target.

## 6. AltarScanner

Scan central sob demanda, com snapshot de target, pedestais, pilhas, potência, fontes, biblioteca, livros, posição/slot e chunks. Não modifica ou consome itens. Rejeita região parcialmente descarregada e excesso de pedestais, sem tickets nem chunk loading forçado.

## 7. Bookshelf Power

Soma dos bônus positivos finitos de `getEnchantPowerBonus` no raio horizontal configurado, Y ± 2, excluindo explicitamente Chiseled Bookshelves. Estante vanilla = 1. Não há cap 15 ou corredor de ar: a fórmula usa a API vanilla/NeoForge com geometria livre. Isso permite receitas de power 20+.

## 8. Chiseled Bookshelf scan

Lê os seis slots das estantes talhadas nos raios. Retorna Enchanted Books e livros comuns separadamente, com identidade da block entity, posição e slot para revalidar/commit. Baús e inventários não contam. Livros vazios ficam disponíveis para a extração futura.

## 9. Recipe loader

Registry customizado no diretório `data/<namespace>/inscribed/enchantment_recipes`. Valida IDs dinâmicos, níveis, potência, chance, ingredientes, duração, visibilidade e blocos infusion/extraction. Arquivos inválidos são logados e excluídos individualmente; reload substitui o snapshot e cancela sessões antigas.

## 10. Matcher

Retorna 0..N matches. Não inicia quando há zero ou múltiplos. Confirma suporte ao enchantment pela API NeoForge, exclusividade vanilla, nível já aplicado, nível do livro e custos. Ingredientes usam Ingredient e fluxo máximo inteiro, permitindo tags sobrepostas, split stacks e ordem livre de pedestais sem contagem dupla.

## 11. RitualController

Estados IDLE, VALIDATING, RUNNING, COMPLETING e CANCELLED. Duração server-authoritative, reservas lógicas entre mesas e cancelamento por mudanças em recursos, power, mesa, chunks, config ou reload. Não persiste sessões incompletas, nem consome insumos durante a animação.

## 12. Partículas, sons e iluminação

Placeholders vanilla: ENCHANT, END_ROD, ENCHANTMENT_TABLE_USE, AMETHYST_BLOCK_CHIME e PLAYER_LEVELUP. Partículas ligam pedestais e livro à mesa. `dynamic_light=false` por default; quando habilitado, fornece FULL_BRIGHT aos itens ativos, sem iluminação real do mundo. Assets finais e luz dinâmica real permanecem para outra etapa.

## 13. Commit

Revalida todos os inputs e a estrutura completa antes de alterar qualquer pilha. Constrói resultado, faz roll final, aplica um enchantment, desconta ingredientes exatos e opcionalmente retira o livro selecionado, tudo na thread do servidor. Atualiza visual/ocupação da estante e sincroniza. Rollback por snapshots em exceções durante mutações, com log da falha.

## 14. Book consumption

Chance definida por receita. 0 e 1 são garantidos, outros valores fazem um roll no final. Seleção determinística por distância da mesa, X, Y, Z e slot. Só o livro reservado participa; exemplares equivalentes não são trocados durante o ritual. Livro de nível N autoriza receitas até N; extensão opcional `exact_book_level=true` exige igualdade.

## 15. Debug commands

`/inscribed debug altar scan|recipes|start|cancel`, permissão >= 2. Aceita mesa mirada ou coordenadas explícitas carregadas. Mostra target, power, estantes, livros/slots, pedestais, matches e estado. Console pode usar scan/recipes com coordenadas; start exige jogador.

## 16. Test recipe

Distribuída: `inscribed:test_sharpness_1`: Afiação I, power 2, 2 ametistas, 1 ferro, 100 ticks, chance 25% de consumir o livro. Extraction futura é apenas parseada. Pedestal também tem crafting simples e loot próprios. As receitas de consumo 0/1, tags, ambiguidade e cliente nativo ficam somente em gameTest.

## 17. Arquivos alterados

Novo projeto completo e documentação, README de navegação em `CODEX LSBC/INSCRIBED`, prompt recebido arquivado e preflight de hashes. O manifest lista todos os arquivos de projeto com SHA256 e as capturas nativas. Nenhuma fonte, recurso ou JAR LSBC foi alterado; os demais JARs e configs existentes também foram preservados.

Inscribed não existia antes desta etapa; não houve substituição de versão anterior. O preflight contém **664 arquivos protegidos** e foi conferido antes/depois da instalação. Os mundos e configs criados por runs são exclusivamente os diretórios de desenvolvimento do novo projeto.

## 18. Documentação

- [INSCRIBED_DEV_SETUP.md](INSCRIBED_DEV_SETUP.md): versões, build, runs, validação, config e comandos.
- [INSCRIBED_ALTAR_ARCHITECTURE.md](INSCRIBED_ALTAR_ARCHITECTURE.md): decisões, fórmula, scan, render, reservas e commit.
- [INSCRIBED_RECIPE_AUTHORING.md](INSCRIBED_RECIPE_AUTHORING.md): schema, exemplos, reload e limitações.
- [README](../README.md): montagem rápida para Afiação I.
- [Manifest de release](INSCRIBED_RELEASE_MANIFEST.json): hashes do artefato, fontes, documentação e evidências.

## 19. Testes

- **4 JUnit**, sem falhas, incluindo comparação contra oracle exaustivo em 600 inventários pequenos.
- **27 GameTests com Inscribed sozinho**, todos aprovados.
- **27 GameTests com LSBC 0.21.0**, todos aprovados. É coexistência, não integração de KnownWords.
- **3 clientes nativos**: Inscribed sozinho, restart em nova JVM e Inscribed + LSBC, todos PASS; **15 capturas** arquivadas em `docs/validation`.
- O cliente envia cliques de target/pedestais e Shift ao servidor e confirma resultado, quantidades, livro consumido, retirada/reinserção sem cópia, renderer vanilla e sync.
- Restart exige o target encantado, remainders dos pedestais e slot consumido salvos pela execução anterior; PASS.
- GameTests incluem cancelamentos de target/ingredient/book/power, remoção da mesa/pedestal, lease entre duas mesas, reload real, config, limite de pedestais no commit, exact book level e scan sem force loading.

Logs finais: `validation-matrix.log`, `validation-client-restart.log`, `validation-client-lsbc.log`. As duas recipes inválidas que aparecem nos logs são fixtures intencionais excluídas do JAR final. Logs de tentativas anteriores não são evidência de aprovação.

## 20. Build e distribuição

`build` concluído com sucesso. JAR `inscribed-0.1.0.jar`, **83.489 bytes**, SHA256:

```text
5e4c3442cc62218a6f0fb9aec781d477c066f8f294cd3e37ad58259effdfb48e
```

Artefato auditado para conter somente a receita distribuída e classes main, sem fixtures, LSBC ou dependências embutidas. Instalado na pasta `mods` da instância com o mesmo hash; cópia de release arquivada em `CODEX LSBC/INSCRIBED/backups/2026-10-13-core-altar-release`. Requer reiniciar a instância para carregar o novo mod. Clientes e servidor devem ter Inscribed compatível; o LSBC existente continua 0.21.0.

## 21. Limitações atuais

Extração não executa; bigorna e outras fontes vanilla de encantamento permanecem com suas regras atuais. Sem guide book, UI de seleção, KnownWords, Codex, recipe discovery, loot/trades de livros, assets custom ou luz dinâmica real. Visibilidade da receita é armazenada para o futuro e não bloqueia a infusão física. Pedestais não têm automação de hopper/capability neste MVP.

Persiste itens, não sessões em andamento: cancelamento/unload/shutdown normal preserva insumos. Não se promete atomicidade de gravação entre chunks contra interrupção abrupta durante save; nem recuperação de corrupção de disco/hooks de terceiros com falha no rollback. O hook de chunk unload foi exercitado por evento no teste, e o restart real foi validado; simulação de corrupção/crash durante escrita não foi feita.

Capturas e automações confirmam o percurso gráfico/técnico; ouvir os sons, avaliar sensação/balanceamento e testar shaders/resource packs/full pack ou dois clientes humanos permanece validação manual. Compatibilidade com outros enchantments usa registry/API, mas os fixtures executados usam enchantments vanilla.

## 22. Próximo prompt recomendado

**Inscribed 02 — Seleção explícita de receita + Extraction MVP.** Usar os matches já retornados para uma seleção pequena, implementar extração transacional para um slot de livro vazio e ampliar testes de cancelamento/commit. Adiar bigorna, guia final e LSBC até consolidar esse segundo fluxo.
