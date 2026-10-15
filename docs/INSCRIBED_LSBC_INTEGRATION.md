> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# LSBC — Inscribed 0.3.0

Compatibilidade validada com o LSBC **0.21.0** instalado. O JAR e o código LSBC permanecem intactos. A integração é opcional; sua implementação só é resolvida após `ModList.isLoaded("lsbc")`. Sem LSBC, `NoKnowledgeIntegration` mantém nomes traduzíveis normais, guia e descoberta funcionando.

`KnowledgeIntegration` separa apresentação/aprendizado linguístico do conhecimento do Inscribed. `LSBCKnowledgeIntegration` chama `UnknownTextRenderer.renderForPlayer`, `ObservationLexicon.literal` e `WordKnowledgeService.learnObservedWords`. Não existe um segundo registro de palavras.

Os nomes conservam literalmente a identidade original. Palavras desconhecidas recebem a fonte `lsbc:unknown`, palavras conhecidas permanecem legíveis e o nível é acrescentado separadamente com fonte normal. Pesquisa usa o componente semântico produzido pelo LSBC; narração não fala o texto lógico dos glifos. Seleção de infusão e análise da lupa também passam a usar essa apresentação para nomes de encantamentos.

Adquirir um livro ou concluir uma extração aprende tokens significativos do nome localizado, ignorando stopwords e palavras que o vocabulário LSBC não registra. Recursos `data/inscribed/observation_languages/en_us.json` e `pt_br.json` acrescentam nomes vanilla/Create ao léxico já existente do LSBC. Modpacks podem fornecer outros idiomas ou nomes de mods usando esse mecanismo de dados do LSBC.

`WordKnowledgeChangedEvent` atualiza snapshots e apresentação no JEI/guia. Esquecer palavras ou Memory Erosion no LSBC não apaga `known_enchantments` nem `known_recipes`: conhecimento do encantamento e compreensão do nome são estados distintos. Desativar `knowledge.lsbc_word_integration` restaura a apresentação normal.

O Codex possui cinco posições de ribbon e usava quatro. `ScreenEvent.Init.Post` adiciona **Encantamentos** na quinta posição, sem reflexão em campos privados nem patch do LSBC. A ação abre o adaptador compartilhado com subtabs Guia / Encantamentos / Receitas; fechar retorna ao Codex. Somente encantamentos descobertos e receitas autorizadas aparecem. Esse adaptador reutiliza a tela do Guide Book, em vez de duplicar o conteúdo e controles dentro da implementação privada do Codex.

Um save pode ser reaberto sem LSBC: os dados Inscribed permanecem, o guia volta a apresentar nomes normais e JEI funciona quando presente. Traduções/vocabulário adicional de mods e idiomas não fornecidos são responsabilidade do modpack; não são ensinados tokens inexistentes.
