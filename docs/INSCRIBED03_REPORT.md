> SUPERSEDED: historical implementation evidence. Current candidate: [release notes](RELEASE_NOTES_0.7.2-rc.1.md) and [checklist](INSCRIBED_RELEASE_CHECKLIST.md).

# Inscribed 03 — relatório de implementação e instalação

Inscribed **0.3.0** instalado em `mods/inscribed-0.3.0.jar` em 13/10/2026. A versão 0.2.2 foi removida da pasta ativa após o fechamento do Minecraft e permanece no backup `CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-inscribed-03`. O JAR instalado tem 340464 bytes e SHA-256 `7e6b3ae7189c75049f4594b85e96ac306576b5e967ee90404d70e9bef64a3df0`.

## Os 30 pontos solicitados

1. **Knowledge model.** Attachment privado `inscribed:knowledge`, com conjuntos imutáveis de IDs `known_enchantments` e `known_recipes`. O servidor concede e remove conhecimento; o cliente recebe somente sua visão permitida.

2. **Persistence.** Codec serializado nos dados do jogador, `copyOnDeath()`, sincronização no login, respawn e mudança de dimensão. Campos ausentes e IDs de conteúdo removido são tolerados. Conhecimento Inscribed sobrevive à remoção do LSBC.

3. **Acquisition triggers.** Pickup concluído, troca concluída, aumento de livros encantados no inventário pessoal, extração bem-sucedida e comandos administrativos. Um livro com vários encantamentos ensina todos. Consultar estantes, contêineres, guia ou JEI não ensina. Os gatilhos e o intervalo de observação são configuráveis.

4. **Recipe visibility.** `RecipeVisibilityService` centraliza `ALWAYS`, `ON_ENCHANTMENT_DISCOVERY` e `EXPLICIT_DISCOVERY` para guia, Codex e JEI. As 131 receitas padrão usam descoberta do encantamento. A descoberta não altera os requisitos nem bloqueia a execução física de um ritual válido.

5. **Explicit discovery.** Concessão independente por ID da receita; não ensina automaticamente seu encantamento. Comandos implementados e testados. Manuscritos, loot e trocas especiais ficam para uma etapa posterior.

6. **Guide Book.** `inscribed:guide_book`, disponível na aba criativa e fabricado com livro e ametista. Clique direito abre o guia standalone com documentação, encantamentos descobertos e receitas permitidas. Receita de fabricação editável na configuração do mod.

7. **Shared documentation provider.** `GuideContentProvider` lê os mesmos nove capítulos dos recursos `guide/en_us.json` e `guide/pt_br.json`, com fallback inglês. Guia standalone e integração Codex usam o mesmo provedor e a mesma tela.

8. **Search.** Busca usa somente texto semanticamente permitido, sem pesquisar IDs ocultos ou interpretar glifos como nomes. Navegação entre receita e encantamento respeita o conhecimento do jogador. A busca não concede descoberta.

9. **LSBC compat.** Integração opcional isolada com LSBC 0.21.0; fallback `NoKnowledgeIntegration` usa nomes normais. Nenhum JAR ou código LSBC foi alterado. A integração pode ser desativada pela configuração.

10. **KnownWords rendering.** Usa `ObservationLexicon` e `UnknownTextRenderer` do LSBC, preservando a identidade literal e permitindo nomes parcialmente conhecidos. O nível romano permanece legível. Tooltips do altar e seleção recebem os mesmos nomes protegidos.

11. **Word learning.** Aquisição real usa `WordKnowledgeService.learnObservedWords` com tokens localizados vanilla/Create. Reaquisição pode reaprender palavras esquecidas sem duplicar descoberta. Erosão de palavras LSBC não apaga os IDs conhecidos pelo Inscribed.

12. **Codex tab/subtabs.** Quinta aba **Encantamentos**, com **Guia**, **Encantamentos** e **Receitas**. O adaptador público de inicialização de tela abre o `GuideScreen` compartilhado com o Codex como tela anterior; fechar retorna ao Codex. A seção Receitas alterna Infusão/Extração.

13. **JEI version/API detectada.** JEI NeoForge **19.57.0.451**, Minecraft 1.21.1. Compilação e validação usam o JAR realmente instalado e sua API pública: `IModPlugin`, categorias, registro de receitas, runtime, `IRecipeManager` e consultas de receitas.

14. **JEI plugin.** `InscribedJeiPlugin`, descoberto por `@JeiPlugin`, registra categorias e atualiza receitas a partir do snapshot privado. Inicialmente não publica receitas dependentes de descoberta. Classes JEI ficam isoladas no pacote de compatibilidade.

15. **Infusion category.** Exibe exemplos de alvo compatível, livro encantado de referência, ingredientes e quantidades, resultado, nível, potência, duração e regras de livros/match exato.

16. **Extraction category.** Exibe alvo encantado, livros vazios, ingredientes seletivos, livro resultante, potência, duração e chance de falha. A extração física continua selecionada pelos ingredientes nos pedestais, sem interface de seleção.

17. **Catalysts.** Enchanting table, pedestal, chiseled bookshelf e bookshelf permitem acesso às categorias relevantes no JEI.

18. **Target representation.** Exemplos são derivados da compatibilidade real do encantamento. A visualização limita exemplos a 12 alvos e 64 alternativas por ingrediente; o matcher físico mantém os conjuntos completos.

19. **Dynamic/per-player visibility.** O servidor filtra por jogador antes de enviar. JEI adiciona apenas linhas autorizadas e oculta linhas revogadas por sua API pública. Alterações de conhecimento atualizam guia, Codex e JEI sem relogin.

20. **JEI search limitations.** O índice global de ingredientes/livros vanilla do JEI ainda pode pesquisar nomes reais dos encantamentos. Filtrar as receitas Inscribed não controla esse índice. Não há promessa de proteção integral contra spoilers na busca global do JEI.

21. **Refresh behavior.** Aprender/esquecer, login, mudanças de KnownWords, reload e alteração dos controles de integração atualizam snapshots. Receitas removidas deixam de aparecer no guia e nas consultas visíveis JEI. Sem API pública de remoção, linhas antigas permanecem ocultas no cache interno JEI até reconstrução do runtime. Logout limpa a visão local.

22. **Multiplayer.** Attachment sem sincronização automática para observadores; envio privado ao próprio jogador. GameTest com dois jogadores valida conhecimentos e snapshots diferentes. Não foi executada uma sessão com dois clientes remotos reais; essa validação continua recomendada antes de publicar para servidores.

23. **Optional dependency safety.** LSBC e JEI declarados opcionais e carregados somente por adaptadores separados. Servidor dedicado funciona sem ambos. Nenhuma classe das dependências opcionais ou fixture de validação foi embutida no JAR. A matriz inclui abertura de save após remoção dos mods opcionais.

24. **Commands.** Todos exigem permissão 2:

    ```text
    /inscribed knowledge status <player>
    /inscribed knowledge learn enchantment <player> <id>
    /inscribed knowledge forget enchantment <player> <id>
    /inscribed knowledge learn recipe <player> <id>
    /inscribed knowledge forget recipe <player> <id>
    /inscribed knowledge clear <player>
    /inscribed debug recipes visible <player>
    ```

25. **Arquivos alterados.** Lista completa dos arquivos de implementação/documentação em [INSCRIBED03_CHANGED_FILES.json](INSCRIBED03_CHANGED_FILES.json). Principais grupos: conhecimento/rede, guia, adaptadores LSBC/JEI, eventos/registro/inspeção/ritual, traduções e lexicons, 131 receitas, configuração e validação. Na instância: novo JAR e somente as configurações próprias `config/inscribed-server.toml` e `config/inscribed/recipes.json`. A migração preservou os 39 valores TOML anteriores, acrescentou sete opções e atualizou apenas receitas idênticas aos padrões antigos; foram 131 nesta instalação. Evidência em [03-installed-config-migration.json](03-installed-config-migration.json).

26. **Documentação.** [Guide System](INSCRIBED_GUIDE_SYSTEM.md), [Knowledge System](INSCRIBED_KNOWLEDGE_SYSTEM.md), [JEI Integration](INSCRIBED_JEI_INTEGRATION.md), [LSBC Integration](INSCRIBED_LSBC_INTEGRATION.md), [Recipe Authoring](INSCRIBED_RECIPE_AUTHORING.md) e [ADR-0003](ADR-0003-KNOWLEDGE-GUIDE-INTEGRATIONS.md), além do catálogo e README atualizados.

27. **Testes.** Quatro testes JUnit e 73 GameTests aprovados em cada um dos dois runtimes de servidor. Incluem persistência NBT, morte, pickup multiencantamento, troca real, inventário sem reaprendizado indevido, isolamento entre jogadores, visibilidade e catálogo de 5000 encantamentos com fragmentação de rede. Clientes nativos validam guia, Codex, KnownWords parcial, busca, comandos, reload, JEI público e remoção de dependências. Extração física real valida sucesso, preservação dos outros encantamentos e descoberta após commit. Evidências e 36 capturas em `docs/validation/guide03-*`.

28. **Build matrix.** Java 21.0.9, Gradle 9.2.1, Minecraft 1.21.1, NeoForge 21.1.255. Todos os builds finais e validações abaixo aprovados:

    | Runtime | Configuração | Resultado |
    | --- | --- | --- |
    | Servidor 1 | Inscribed sem opcionais | 73/73 GameTests |
    | Servidor 2 | Inscribed + Create 6.0.10 + LSBC 0.21.0 | 73/73 GameTests |
    | Cliente A | Inscribed standalone; save previamente usado com opcionais | PASS |
    | Cliente B | Inscribed + JEI; save após remover LSBC | PASS |
    | Cliente C | Inscribed + LSBC, português | PASS |
    | Cliente D | Inscribed + LSBC + JEI, inglês | PASS |
    | Cliente extração | Ritual físico sem opcionais | PASS |

29. **Limitações.** Busca global JEI, cache de receitas ocultas e ausência de dois clientes remotos conforme itens 20–22. Aquisição via fallback de inventário exige delta positivo observável; substituição simultânea de livros idênticos sem delta pode não ser identificada. Pickup e troca possuem eventos próprios. Snapshot excepcionalmente grande é fragmentado; uma única linha superior ao limite conservador de 750 KB é omitida com erro no log. Arte final, manuscritos, loot, trocas especiais e compartilhamento de equipe permanecem fora do escopo.

30. **Próximo prompt recomendado.** Inscribed 04: conteúdo de descoberta explícita por manuscritos/loot configuráveis e validação com dois clientes remotos em servidor dedicado, antes de ampliar integrações ou compartilhar conhecimento entre equipes.

## Auditoria da instalação

[INSCRIBED_RELEASE_MANIFEST.json](INSCRIBED_RELEASE_MANIFEST.json) registra SHA-256, instalação, testes, matriz e dependências. A auditoria confronta o JAR com as classes compiladas, exclui fixtures e classes opcionais embutidas, verifica regras físicas preservadas e confirma um único JAR Inscribed ativo.

O inventário de proteção cobre 661 arquivos externos. Durante o trabalho o usuário abriu esta instância do Minecraft: três arquivos Properties tiveram somente o comentário automático de data alterado, confirmado por reconstrução do hash original, e o histórico de pesquisa JEI do mundo `New World (1)` mudou por atividade do jogo. Também houve alteração externa de `config/relics.yaml`: somente `enabledExtendedConfigs` passou de `false` para `true`, confirmado pela reconstrução exata do hash anterior. Esses arquivos não foram escritos nem restaurados pelo trabalho. Demais arquivos protegidos e JARs de outros mods permaneceram iguais ao backup.
