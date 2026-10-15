> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# JEI — Inscribed 0.3.0

Implementação compilada contra o JAR realmente instalado: **JEI 19.57.0.451 para NeoForge / Minecraft 1.21.1**. A dependência é `compileOnly`, e a metadata declara integração opcional no cliente. Não há cópia das classes JEI dentro do JAR do Inscribed nem referência à API nos pacotes comuns.

`InscribedJeiPlugin`, descoberto por `@JeiPlugin`, registra `inscribed:infusion` e `inscribed:extraction`. São receitas de apresentação, derivadas dos mesmos snapshots filtrados usados pelo guia; não são novas receitas de crafting vanilla.

Infusão exibe alvo compatível, livro encantado de referência, ingredientes e resultado encantado. Extração exibe alvo encantado, livros vazios, ingredientes e livro resultante. Ambos mostram potência, duração e risco; a extração informa a perda do encantamento em caso de falha. Os stacks usam os componentes modernos por meio de `ItemStack.enchant`, incluindo `STORED_ENCHANTMENTS` dos livros. Tooltips das entradas encantadas substituem os nomes vanilla pela apresentação autorizada.

Catalysts: mesa de encantamentos, pedestal de infusão, estante esculpida e estante comum. Exemplos de alvos são descobertos dinamicamente pelo suporte do item ao encantamento: até 12 por encantamento, sem lista gigante de itens codificada manualmente. Alternativas de ingredientes são resolvidas pelo servidor, incluindo tags; até 64 exemplos por ingrediente são apresentados. Quantidades maiores que um stack são indicadas pelo total exigido no tooltip.

## Atualização por jogador

Registro inicial não publica o catálogo global. `onRuntimeAvailable` e updates de `ClientKnowledge` alimentam `IRecipeManager.addRecipes` e `hideRecipes`. Descobrir/esquecer, conceder/revogar receita explícita, trocar palavras conhecidas e recarregar receitas atualizam a mesma visão. Alterações na geração de receitas escondem as linhas antigas antes de adicionar as novas. Logout esconde a sessão anterior; o próximo jogador recebe seu próprio snapshot.

As chamadas usam apenas APIs públicas. Não se alteram classes internas, campos privados ou o matcher do altar. A API desta versão não possui remoção definitiva de receitas em runtime: linhas removidas permanecem ocultas internamente até o JEI reconstruir seu runtime. Muitas recargas podem aumentar esse cache durante a sessão.

## Limitação de pesquisa

O JEI mantém seu próprio índice global de ingredientes vanilla, incluindo subtipos de livros encantados. Esse índice pode localizar nomes completos de encantamentos independentemente da descoberta Inscribed ou de KnownWords. A integração não substitui o índice/renderer global nem esconde itens globais que outros mods usam.

Isso **não publica receitas Inscribed ocultas**: elas não são enviadas nem registradas no cliente. Contudo, não há garantia de sigilo linguístico do índice global de livros ou de receitas publicadas por outros plugins. O Guide Book e o adaptador do Codex possuem pesquisa sem esse vazamento. EMI/REI continuam fora do escopo.
