> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Inscribed — Arquitetura do Altar MVP

## Separação de responsabilidades

| Package | Responsabilidade |
| --- | --- |
| `dev.inscribed` | Bootstrap e registro de eventos |
| `registry` | Bloco, item, block entity, attachment e aba criativa |
| `config` | Config SERVER validada e snapshot dos raios |
| `altar` | Estado da mesa, interações, scan e biblioteca |
| `pedestal` | Bloco e inventário de uma pilha |
| `recipe` | Parser, reload, matching e alocação de ingredientes |
| `ritual` | Reserva, duração, cancelamento, commit e feedback visual |
| `client` | BER do pedestal e render adicional do alvo |
| `command` | Diagnóstico restrito a operadores |
| `mixin` | Um hook de remoção, sem cancelamento da rotina original |

Guide e compat LSBC serão módulos posteriores. Não existe import `dev.lsbc`, dependência Gradle de LSBC ou estado linguístico no Inscribed.

## Decisão: estender a Enchanting Table

O bloco e a block entity continuam **minecraft:enchanting_table** / **EnchantingTableBlockEntity**. O target usa attachment `inscribed:altar_target`, com codec persistente e `AttachmentType.Builder.sync(StreamCodec)` da versão local 21.1.255. `setData` marca a block entity como alterada e sincroniza watchers; a infraestrutura NeoForge envia attachments existentes ao começar a acompanhar o chunk.

O attachment guarda uma cópia do ItemStack e revisão. O estado `running` é enviado ao cliente e deliberadamente não é serializado para o mundo. O alvo sobrevive a save/load; um ritual interrompido por shutdown/unload não recomeça e não consome insumos. A remoção do Inscribed de um mundo que possui seus blocos/itens não é portável; a futura presença ou ausência do LSBC não modifica o armazenamento do altar.

`RightClickBlock` intercepta a mesa em ambos os lados, executando mutações apenas no servidor e apenas na mão principal. Spectators e jogadores sem `mayInteract` não alteram o altar. O interceptador substitui a interação que abriria o menu de encantamento vanilla, conforme o fluxo físico deste MVP; livro, animação, nome customizado, bloco e renderer permanecem vanilla. Sistemas de outros mods que também substituem o clique da mesa precisam de avaliação específica.

Uma capability é útil para acesso externo a inventários, mas não fornece sozinha persistência, render ou tratamento universal de remoção. Substituir a block entity ou renderer vanilla ampliaria a superfície de incompatibilidade sem benefício aqui. Attachments e eventos resolvem armazenamento/interação. Há um único mixin comum que injeta em HEAD de `BlockBehaviour.BlockStateBase.onRemove`, sem cancelar, para devolver o target quando o estado anterior é a mesa e o novo bloco deixa de ser mesa. Eventos de quebra/drops não cobrem uniformemente survival, criativo, explosões e substituição por `/setblock`; o hook observa a remoção antes de perder a block entity, limpa o attachment e devolve a pilha uma vez. Nenhum mixin de cliente é usado.

Referências consultadas: [attachments NeoForge 1.21.1](https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/) e [block entities 1.21.1](https://docs.neoforged.net/docs/1.21.1/blockentities/). A documentação dessa linha ainda descreve envio manual de attachments; a existência e comportamento do helper de sync foram confirmados diretamente em `AttachmentType`, `AttachmentHolder` e `AttachmentSync` do userdev **21.1.255**, e no cliente nativo.

## Pedestal

Um slot contém uma pilha até `ItemStack.getMaxStackSize()`. Inserir transfere a pilha da mão; nova inserção do mesmo item/components completa até o limite. Item diferente não substitui o conteúdo. Mão vazia retira a pilha para o inventário, com drop do restante se faltar espaço. Quebrar/substituir o pedestal devolve somente seu conteúdo restante.

Não se expõe referência mutável ao inventário: getters e setters copiam ItemStacks. Save/load usa ItemStack com registry lookup e revisão. A sincronização usa update tag/packet vanilla de block entity. `running` entra no update tag, mas não no save. Automação por hopper/capability não faz parte deste MVP.

## Render e iluminação

O BER do pedestal desenha o item centralizado em Y 1.15, escala 0.55, bobbing de 0.035 e rotação de 0.8 grau/tick. Durante o ritual, bobbing 0.065 e rotação 3 graus/tick. O alvo da mesa usa o mesmo helper em Y 1.65 para ficar acima do livro.

`RenderLevelStageEvent.AFTER_BLOCK_ENTITIES` usa `LevelRenderer.iterateVisibleBlockEntities`; só desenha alvos de mesas visíveis a até 64 blocos. Não varre chunks por frame nem registra um renderer substituto para a mesa. Renderers e referências Minecraft client ficam em subscribers `Dist.CLIENT`; o servidor dedicado não os carrega.

`dynamic_light=false` é o default seguro. Partículas ENCHANT/END_ROD dão a ilusão de brilho. Quando true, somente os itens ativos são renderizados em FULL_BRIGHT: paredes/chão não são iluminados. Não há troca de blockstate da mesa, bloco de luz temporário, dependência de shader ou relight de chunks. Luz dinâmica real fica para prompt posterior.

## Scan e potência

O scan percorre um cilindro discreto: `dx² + dz² <= raio²`, verticalmente de `mesa.y − 2` até `mesa.y + 2`, limitado à altura do mundo. Cada categoria usa seu raio; a união do espaço deve estar carregada. Qualquer chunk ausente causa falha, sem `getChunk` forçado ou tickets. Todas as Chiseled Bookshelves nos raios são consideradas, sem exigir orientação ou linha de visão.

O resultado inclui target, revisão, identidade da mesa, pedestais/pilhas/revisões, fontes de power, estantes talhadas, livros/posição/slot e chunks consultados. Ordenação determinística: distância quadrática da mesa, X, Y, Z; em uma estante, slot 0..5. Pedestais vazios também contam no limite. Se houver mais que `max_pedestals`, o altar é inválido: não se escolhe um subconjunto arbitrário.

Fórmula real de Bookshelf Power:

```text
power = soma de max(0, state.getEnchantPowerBonus(level, pos))
        para as fontes no raio, excluindo minecraft:chiseled_bookshelf
```

Valores não finitos são tratados como zero. Uma estante vanilla fornece 1. A extensão NeoForge permite que blocos de outros mods forneçam seus próprios bônus. **Não há cap 15 nem corredor de ar obrigatório**: o layout é livre dentro do raio, permitindo receitas que exijam 20 ou mais. O conceito/API de bônus vanilla é reutilizado; a geometria fixa e o teste de transmissão da mesa vanilla não são usados. Estantes talhadas fornecem apenas conhecimento.

Cada uma de suas seis posições é lida sem retirar itens. `minecraft:enchanted_book` fornece enchantments/níveis armazenados; `minecraft:book` entra como livro vazio para extração. Outras caixas, inventário de jogadores e livros de outras classes não contam neste MVP.

Scan espacial acontece ao iniciar, em comandos de debug e no commit. Durante RUNNING são verificadas referências, pilhas, revisões, chunks e as fontes de power já coletadas, sem varrer o mundo. Mudanças em fontes não utilizadas não obrigam scan por tick; a estrutura completa é revalidada ao concluir.

## Máquina de estados e reserva

```text
IDLE -> VALIDATING -> RUNNING -> COMPLETING -> IDLE
                     |             |
                     +-> CANCELLED <-+ -> IDLE
```

Estados intermediários VALIDATING/COMPLETING/CANCELLED são internos à operação. Não há histórico persistente de estados terminais; consultas após a liberação mostram IDLE. Apenas o contador do servidor controla duração, em ticks, não tempo de frame.

Um match único cria reserva lógica do slot físico selecionado do livro e de cada pedestal usado. Duas mesas não reservam o mesmo slot/pedestal simultaneamente. O target é exclusivo da sua mesa. Fontes de potência podem ser compartilhadas. Inputs continuam fisicamente disponíveis; retirar um deles cancela a sessão. Não há bloqueio de inventário ou cópia para storage oculto.

Alterações de target/pedestal feitas pelo Inscribed cancelam imediatamente. O contador revalida livro e power a cada tick, incluindo identidade da block entity e componentes/quantidade da pilha. A sessão cancela se a mesa é removida, input muda, power fica menor que o mínimo, config muda, datapacks são recarregados ou um chunk da área é descarregado. Um livro equivalente em outro slot não substitui o livro reservado.

## Commit de infusão

No último tick:

1. Revalida target/revisão/identidade, selected book/slot, pedestais/componentes/count, chunks, power, config e geração de recipes.
2. Faz novo scan completo. Excesso de pedestais, estrutura inválida, ausência da receita escolhida cancela sem consumir.
3. Constrói uma cópia do resultado, respeitando `supportsEnchantment`, `Enchantment.areCompatible` e os níveis já presentes. Livro comum como alvo vira Enchanted Book, preservando seus demais componentes.
4. Faz o roll final do livro. Chance 0 nunca consome; chance 1 sempre consome; demais valores usam o RNG do ServerLevel uma vez.
5. Libera a reserva lógica e, no mesmo turno síncrono do servidor, instala o resultado, desconta as quantidades dos pedestais e retira um livro do slot selecionado se o roll indicar consumo.
6. Marca inventários alterados, atualiza slot ocupado da estante, encerra visuais, sincroniza e envia feedback.

O livro é o **primeiro elegível na ordenação de estantes/slots descrita acima**, não uma escolha aleatória. Um livro de nível N autoriza receitas até N, salvo `exact_book_level=true`. Só o exemplar selecionado sofre o roll, mesmo se contiver outros encantamentos.

Uma exceção de RuntimeException durante o commit tenta restaurar os snapshots originais e registra a falha. Não há consumo durante a animação. Nenhum jogador/interação é intercalado entre validação e mutações na thread do servidor. Isto cobre cancelamento, concorrência lógica e shutdown normal; não promete atomicidade de arquivos de múltiplos chunks contra encerramento abrupto do processo durante a gravação, corrupção de disco ou um hook de terceiro que também falhe durante rollback. Journal persistente de crash recovery seria outra etapa.

Pacotes de attachment e update de block entity podem chegar em instantes diferentes. O cliente é visual, nunca decide sucesso/consumo; a validação nativa aguarda a convergência de ambos antes de conferir o estado final.

## Visuais de ritual

Start: ENCHANTMENT_TABLE_USE e partículas ENCHANT discretas. Running: partículas ENCHANT dos pedestais em direção à mesa; na segunda metade, também do livro selecionado, com AMETHYST_BLOCK_CHIME no meio. Completion: PLAYER_LEVELUP e burst de END_ROD. A opção particles controla somente partículas próprias. Nenhum som ou partícula custom foi registrado.

## 02: shared ritual selection and extraction

RitualType identifies INFUSION/EXTRACTION. RitualMatcher preserves InfusionMatcher and adds extraction matching. RitualMatch holds mode, recipe, dynamic enchantment holder, physical BookUse slots/counts and IngredientAllocator pedestal uses. RitualController shares leases, timer, revalidation, cancellation and synchronous commit between modes. Extraction removes only the selected holder; on success its book occupies the first fully consumed reserved slot. On failure no book is created. Other target components remain.

Since 0.2.1, Shift/right-click resolves extraction by pedestal ingredients without UI: one extraction starts, multiple reject without consumption. Extraction takes priority over infusion. With no extraction, multiple infusion matches can open RitualSelectionScreen. LSBC lens and standalone Book and Quill inspection keep interaction priority. Options show mode/name/level, duration, required power, infusion book chance and ingredients in tooltip. This is not a global guide.

RitualSelection stores player, random nonce, table object/revision, recipe generation, offered options and expiry at 1200 server ticks. C2S contains only nonce/index. It checks range (eight blocks), dimension, player state, permissions, table identity/revision, generation, expiry and index; then consumes the session and rescans before starting. Wrong nonce does not invalidate the legitimate session. Replay is rejected. Index -1 closes; disconnect, timeout and shutdown clear sessions. Opening/cancelling consumes nothing and acquires no leases.

All extraction BookUse slots are reserved and checked each tick along with target, reagents, power, chunks, config and recipe generation. Existing physical inventories and saved attachment schema remain unchanged. See INSCRIBED_EXTRACTION.md and INSCRIBED_ANVIL_RULES.md for commit and maintenance rules.


## 0.2.1 extraction risk

Extraction.failureChance comes from optional failure_chance (default zero, catalog .20). The server rolls once only after final validation; both outcomes consume costs and remove the selected holder. Failure skips book creation and uses RitualVisuals.failed. Runtime errors retain the existing rollback. Selection sessions contain only infusions and reject a stale infusion choice if current ingredients now match an extraction. See EXTRACTION_INGREDIENT_SELECTION.md.

## 0.2.2: configuration authority

ConfigRecipeStore reads config/inscribed/recipes.json at resource apply time, exports defaults only when missing, validates its envelope and merges recipe additions/overrides/disables before the existing conditional registry parser. A malformed document retains the last valid document in memory without touching the edited file. ConfigCraftingRecipesMixin merges crafting entries into RecipeManager.apply before vanilla parsing and client synchronization. No parallel recipe registry or per-enchantment code is introduced.

InscribedConfig remains SERVER and adds vertical radius, mode/inspection/anvil switches, inspection timing/reach, selection expiry/reach, renderer parameters and sound IDs/volume/intervals. Captured ritual Settings include geometry and enabled modes, so changes cancel safely. Recipe reload increments the existing generation and invalidates both rituals and selection sessions. See MODPACK_CONFIGURATION.md.
