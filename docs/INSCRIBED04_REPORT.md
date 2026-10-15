> SUPERSEDED: historical implementation evidence. Current candidate: [release notes](RELEASE_NOTES_0.7.2-rc.1.md) and [checklist](INSCRIBED_RELEASE_CHECKLIST.md).

# Inscribed 04 — implementação

Etapa 04 concluída como base de desenvolvimento 0.4.0. O usuário enviou a etapa 05 durante a validação; a instalação final reúne ambas, evitando uma migração intermediária da instância. O JAR 0.4.0 e o código correspondente são preservados no backup pré-05.

1. **Auditoria:** 131 receitas existentes, 125 vanilla e seis Create. Antes da normalização, 73 definições de modo excediam oito ingredientes efetivos; máximo 20.
2. **Correções/rebalanceamento:** todas as receitas usam entradas unitárias. Custos acima do orçamento foram reduzidos, preservando IDs, materiais temáticos, materiais específicos do nível e regras não relacionadas aos ingredientes. Antes/depois em `04-recipe-rebalance.json`.
3. **Enforcement:** parser expande counts antigos somente até oito unidades; loader omite receitas inválidas com ID/razão; ambos os matchers verificam custos unitários e cada pedestal fornece no máximo uma unidade. Inserção transfere um item e recusa merge. Pilhas antigas salvas permanecem recuperáveis sem truncamento.
4. **Catalyst:** item `inscribed:enchantment_catalyst`, nome localizado, aba criativa, modelo placeholder vanilla e tag `inscribed:ritual_catalysts`.
5. **Uso:** 67 receitas na base 04, incluindo tesouros/maldições, Silk Touch, Infinity, nível III+ e Create II+. A classificação é refinada no balanceamento 05.
6. **Loot:** Global Loot Modifier NeoForge aditivo, pools de dados e overrides em `config/inscribed/loot.json`. Não altera loot existente nem trades.
7. **Config:** três switches de loot, efeitos por fase e três novos IDs de som. Chances, pesos, rolls, níveis, itens e tabelas são editáveis por pool.
8. **Fases:** Validation, Awakening, Resonance, Transfer, Crescendo, Completion e fade de 20 ticks. Timing sincronizado, sem pacote por tick.
9. **Distinção:** infusão converge de pedestais/livro para mesa/alvo; extração flui do alvo para mesa/estante do livro vazio.
10. **Sons:** páginas, chimes e assinatura final; pitch ascendente na infusão e descendente na extração. Configuráveis e com volume global.
11. **Partículas:** pequenos glifos e pontos roxos direcionais; duas fontes de pedestal alternadas por emissão, até três estantes reservadas. Densidade reduzida após inspeção nativa para manter a mesa visível.
12. **Iluminação:** curva de luz emissiva apenas no item quando `dynamic_light` está habilitado; nenhum relight, bloco temporário ou dependência de shader.
13. **Erros:** action bar localizada para Catalyst, ingredientes, livros, potência, pilhas antigas e alvo inválido, com som privado discreto. Diagnóstico de operador sem consumo ou concessão de conhecimento.
14. **Guide/JEI/Codex:** custos unitários e Catalyst vêm do snapshot compartilhado. Guia explica limite e loot; JEI/guia mostram a regra de um item por pedestal. Português corrigido.
15. **Arquivos:** conhecimento/integrações existentes reaproveitados; alterações concentradas em parser/matchers, pedestal, estado/visuais/renderers, loot, registro/config/debug, recursos, catálogo, ferramentas e testes. Lista em `INSCRIBED04_CHANGED_FILES.json`, comparando a base validada 04 com o backup da versão 0.3.0.
16. **Documentação:** `INSCRIBED_VISUAL_RITUALS.md`, `INSCRIBED_LOOT_INJECTION.md`, `INSCRIBED_CATALYST.md`, `INSCRIBED_RECIPE_CONSTRAINTS.md`, authoring/config/README, catálogo e ADR-0004.
17. **Testes:** baseline anterior 73 GameTests aprovado; base 04 final com 81 GameTests por runtime e quatro JUnit. Inclui Catalyst físico, pilhas antigas, codec visual, cancelamento, GLM via tabela vanilla real, raridade com seeds, toggles e reload/fallback/restauração do JSON de loot. Cliente nativo valida infusão, extração, resultado físico, dissipação, cancelamento e modelo, com nove capturas.
18. **Build:** Java 21.0.9, Minecraft 1.21.1, NeoForge 21.1.255; servidor standalone e servidor com Create 6.0.10/LSBC 0.21.0 aprovados. Cliente visual com LSBC e JEI 19.57.0.451 aprovado.
19. **Limitações:** placeholders de arte/som; emissão vanilla já existente expira naturalmente após cancelamento; item emissivo não ilumina paredes. Busca global JEI continua com a limitação da etapa 03. Sem benchmark GPU/muitos altares nem dois clientes remotos. Loot filtra tabelas exatas e não reabastece baús já gerados.
20. **Próximo passo:** solicitado pelo usuário: etapa 05, classificação por tier e distribuição temática, preservando as constraints e o risco de extração de 20% confirmado novamente pelo usuário.

A validação Guide/Codex/JEI da base 04 também passou, incluindo Catalyst e custos unitários; evidências em `docs/validation/guide04-D`. O resultado instalado é 0.5.0, com [relatório da etapa 05](INSCRIBED05_REPORT.md) e manifest final combinado. O snapshot do JAR 0.4.0 tem SHA-256 `18b8aedc80d5b06f3c1c2db3c88e9bd4e861c28ad61a7a58108c5e788898452f`.
