> SUPERSEDED: historical implementation evidence. Current candidate: [release notes](RELEASE_NOTES_0.7.2-rc.1.md) and [checklist](INSCRIBED_RELEASE_CHECKLIST.md).

# Inscribed 05 — relatório de implementação

A versão 0.5.0 reúne as etapas 04 e 05. O usuário confirmou que a extração preserva 20% de falha, mantendo determinística apenas a seleção por ingredientes. A base 04 validada e seu JAR estão preservados no backup pré-05.

**Instalado:** `mods/inscribed-0.5.0.jar`, 394733 bytes, SHA-256 `abe407606187719db9d433fa80276afca657540a6314361017b8dd92a318efb2`. A versão 0.3.0 foi retirada da pasta ativa e preservada no backup pré-04. As 131 entradas ainda iguais aos defaults antigos foram migradas; os 46 valores TOML anteriores foram preservados e nove opções acrescentadas. `config/inscribed/loot.json` foi criado com os defaults atuais. Evidência: [migração](05-installed-config-migration.json) e [manifest](INSCRIBED_RELEASE_MANIFEST.json).

1. **Tiers:** `TIER_1_BASIC`, `TIER_2_INTERMEDIATE`, `TIER_3_ADVANCED`, `TIER_4_TREASURE`, `TIER_5_LATE_GAME`, além de `UNCLASSIFIED` para receitas customizadas antigas.
2. **Critérios:** impacto por receita/nível, valor estratégico, biblioteca, materiais e raridade do livro. Origem modded e nível não determinam o tier sozinhos. Create I/II é utilidade intermediária; III é avançado.
3. **Classificação:** 131 IDs preservados; 21 básicos, 47 intermediários, 45 avançados, 12 tesouros e seis tardios.
4. **Power:** 110 receitas ajustadas. Faixas efetivas 2, 8–12, 18–22, 26–28 e 34–36. Usa a fórmula real aditiva do altar, sem cap 15; tier não cria bloqueio oculto.
5. **Catalyst:** 64 receitas exigem o item, versus 67 na base 04. Nunca básico, excepcional no intermediário, obrigatório nos tiers 3–5.
6. **Ingredientes:** 74 receitas ajustadas; reagentes temáticos, seletor de extração e materiais de nível preservados. Avançados/tesouros exigem diamante e tardios netherita. Repetições excessivas de ametista e heavy core reduzidas. Até oito pedestais, um item cada.
7. **Consumo:** 119 probabilidades ajustadas; 10%, 20–29%, 35–47%, 50–56%, 65–71% por tier. Livros raros não são quase sempre descartados; duplicatas mantêm valor.
8. **Extração:** custo e potência escalam junto à receita, Catalyst nos tiers superiores, duração e risco existente preservados. Seleção por ingredientes, sem UI, com perda somente do encantamento escolhido em caso de falha.
9. **Loot:** Global Loot Modifier público NeoForge, aditivo; 13 pools em dados/config, com tabelas exatas, tema, host tier, pesos, níveis e rolls. Índice por tabela e peso total calculados no reload.
10. **Temas:** OCEAN, UNDERGROUND, COMBAT, EXPLORATION, ANCIENT, CREATE_INDUSTRIAL, TREASURE e ARCANE para fontes de Catalyst.
11. **Livros:** 24 tabelas vanilla verificadas; mar, minas, templos/outposts, fortalezas, mansões, bastiões, cidades ancestrais/End e recompensas de trials. Livros acima do tier da fonte não entram nos pools básicos. Tabelas Create próprias não são exigidas.
12. **Catalyst:** 7% em fontes intermediárias, 14% nas avançadas e 22% em tesouros. Tesouros geram uma unidade em 95% dos resultados e duas em 5%; duplicatas de livros são intencionais.
13. **Consistência:** auditoria estática sem warnings; fonte para cada receita, nível/ID válido, receita para cada livro, compatibilidade com tier da fonte e fontes de Catalyst. Auditoria runtime inclui switches/multipliers desativados.
14. **JEI/Guide/Codex:** recebem potência, ingredientes, Catalyst e riscos atualizados pelo snapshot existente. Os nove capítulos compartilhados explicam progressão e exploração sem expor fórmulas de tier ao jogador. KnownWords e descoberta permanecem independentes.
15. **Arquivos:** enum/metadata/parser de tier, loader/índice/auditoria de loot, dois multiplicadores, debug, 131 receitas/defaults, 13 pools/defaults, guia, ferramentas, testes e docs. Lista completa em `INSCRIBED05_CHANGED_FILES.json`; mudanças da etapa 04 ficam no relatório próprio.
16. **Balance report:** [INSCRIBED_BALANCE_MATRIX.md](INSCRIBED_BALANCE_MATRIX.md) e JSON com ID, encantamento, nível, tier, potência, Catalyst, quantidade de entradas, risco e fontes.
17. **Loot report:** [INSCRIBED_LOOT_DISTRIBUTION.md](INSCRIBED_LOOT_DISTRIBUTION.md), com todas as tabelas, entradas/pesos, chances e justificativas. Schema atualizado em `INSCRIBED_LOOT_INJECTION.md`.
18. **Recipe audit:** [INSCRIBED_RECIPE_AUDIT.md](INSCRIBED_RECIPE_AUDIT.md), antes/depois em `05-recipe-rebalance.json`, consistência em `05-consistency-audit.json` e ADR-0005.
19. **Testes:** 88 GameTests em cada runtime, quatro JUnit e cliente nativo de rituais. Cobrem gates físicos dos cinco tiers, temas de loot gerado, toggles/multipliers, entrada inválida isolada, commands/permissions, fonte desativada, Catalyst, Create, persistência, conhecimento, cancelamento e reload. Matriz/capturas finais registradas no manifest da release.
20. **Build:** Java 21.0.9, Minecraft 1.21.1, NeoForge 21.1.255; LSBC 0.21.0, Create 6.0.10 e JEI 19.57.0.451 opcionais. JAR sem fixtures ou classes embutidas das dependências.
21. **Limitações:** defaults são balanceamento inicial para playtest, não uma campanha survival validada. Nenhum overhaul de loot vanilla/trades/worldgen; livros vanilla fortes ainda podem aparecer cedo. Sem benchmark de muitas estruturas/altares ou dois clientes remotos. Limitação da busca global JEI continua documentada. Arte/som placeholders.
22. **Próximo passo:** playtest de progressão early/mid/late no modpack, coletando frequência real de livros/Catalyst, tempo para bibliotecas e perdas de livros; depois ajustar dados e validar servidor com dois clientes reais antes de acrescentar merchants ou conteúdo narrativo.

## Evidências e preservação da instância

Os quatro clientes passaram: A standalone e B com JEI reabrem saves existentes sem LSBC; C com LSBC usa português; D com ambos valida Codex/JEI e Catalyst. Capturas e reports estão em `docs/validation/guide05-A` a `guide05-D` e `docs/validation/polish05`. A extração bem-sucedida na fixture visual usa risco zero somente na configuração isolada de teste; as receitas distribuídas e instaladas mantêm 20%. Os GameTests também cobrem a falha e a transação física.

A auditoria compara o JAR com as classes compiladas, verifica isolamento opcional, exclui fixtures, confere as 131 receitas e os 13 pools, confirma os testes e exige somente um JAR Inscribed ativo com hash exato. Os JARs externos e o código LSBC protegido permanecem iguais à linha de base.

O usuário executou a instância principal durante o trabalho. O inventário de proteção observou oito arquivos de configuração/histórico externos com hashes diferentes, preservados sem escrita por esta tarefa; detalhes em [05-external-config-observations.json](05-external-config-observations.json). Não se afirma que o conteúdo semântico anterior desses oito arquivos permaneceu igual. A instalação alterou somente o JAR e as configurações próprias do Inscribed.
