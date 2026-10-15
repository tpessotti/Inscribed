> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Guide Book — Inscribed 0.3.0

`inscribed:guide_book` aparece na aba criativa do Inscribed. A receita padrão combina um livro e uma ametista; ela pode ser substituída ou desativada em `config/inscribed/recipes.json`, chave `crafting_recipes.inscribed:guide_book`. Clique direito abre o guia, sem consumir o item.

Há três seções: Guia, Encantamentos e Receitas. Encantamentos contém somente IDs descobertos que ainda existem no registro. Receitas oferece modos separados de Infusão e Extração, paginação, pesquisa e detalhes de custos, alternativas, potência, duração, livros e riscos. Os detalhes permitem navegar entre um encantamento descoberto e suas receitas. A página é rolável com a roda do mouse.

`GuideContentProvider` lê `assets/inscribed/guide/<idioma>.json`; usa `en_us` como fallback. Os nove capítulos são Início, Altar, Infusão, Extração, Biblioteca, Pedestais, Potência, Receitas e Bigorna. Resource packs podem substituir esses documentos. O Guide Book e a quinta aba do Codex usam exatamente esse provider e a mesma tela, evitando cópias divergentes da documentação.

O servidor envia somente o catálogo autorizado para aquele jogador. Pesquisa de nomes usa exclusivamente o componente `searchable`, nunca `getString()` do nome com glifos. IDs e receitas ocultas não entram no catálogo de pesquisa. A narração substitui trechos desconhecidos por “palavra desconhecida”.

A interface reutiliza a textura vanilla de livro; arte final continua fora do escopo. As listas mostram até 12 exemplos de alvos por encantamento e até 64 exemplos de alternativas por ingrediente. Os rituais continuam aceitando o conjunto completo dos ingredientes/tags, independentemente desses limites de apresentação.
