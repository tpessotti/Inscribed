> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Conhecimento — Inscribed 0.3.0

Cada jogador possui o attachment privado `inscribed:knowledge`: conjuntos imutáveis `known_enchantments` e `known_recipes`, ambos com `ResourceLocation`. Não há nomes traduzidos, níveis nem glifos na identidade persistida. O Codec tolera campos ausentes e IDs de mods/receitas removidos. IDs obsoletos permanecem salvos, mas não geram entradas inválidas na interface.

O attachment é serializado com os dados do jogador e usa `copyOnDeath()`. Trocar de dimensão, desconectar ou reiniciar não reinicializa os conjuntos. Não se usa sincronização automática de attachment para entidades rastreadas: `PacketDistributor.sendToPlayer` envia apenas a visão filtrada ao próprio jogador.

## Aquisição

- `ItemEntityPickupEvent.Post`: aprende todos os encantamentos do livro efetivamente obtido, usando o stack original, mesmo quando o item no chão já ficou vazio.
- `TradeWithVillagerEvent`: aprende o livro resultante de uma troca concluída, inclusive de comerciante ambulante quando aplicável.
- Inventário pessoal: deltas positivos de livros encantados cobrem retirada de contêiner, estante, give e outras entradas efetivas no inventário. O primeiro snapshot da sessão serve de baseline; abrir um menu não conta como aquisição. Livros já presentes e inalterados não desfazem um comando de esquecimento.
- Extração: somente depois do commit bem-sucedido, para o jogador que iniciou o ritual. Falha de extração não concede descoberta. Falha da integração opcional não desfaz a transação física.
- Comandos administrativos.

Aprender novamente é idempotente. Reobter um livro pode reaprender palavras esquecidas pelo LSBC sem duplicar o conhecimento próprio. Os eventos de pickup/comércio e o fallback de inventário são mecanismos independentes: para impedir descoberta por uma dessas fontes, desative também o fallback de inventário se ele continuaria observando a aquisição.

Consultar o guia, JEI, biblioteca, contêiner aberto ou inventário de outro jogador não concede conhecimento. O monitor observa apenas o inventário pessoal, incluindo a mão secundária; não examina slots do menu aberto. Substituição de livros idênticos sem delta observável de quantidade não é distinguível pelo fallback; pickup e comércio possuem eventos próprios.

## Visibilidade

`RecipeVisibilityService` é o ponto único:

| Política | Condição |
| --- | --- |
| `ALWAYS` | Pública |
| `ON_ENCHANTMENT_DISCOVERY` | ID do encantamento em `known_enchantments` |
| `EXPLICIT_DISCOVERY` | ID da receita em `known_recipes` |

As receitas padrão usam `ON_ENCHANTMENT_DISCOVERY`. Uma concessão explícita não ensina automaticamente o encantamento, e descobrir o encantamento não libera receitas explícitas. O matcher físico não consulta descoberta: saber a receita não dispensa livros, ingredientes, potência ou compatibilidade; desconhecê-la não bloqueia um ritual fisicamente válido.

Snapshots são enviados em lotes sequenciais menores que 750 KB e aplicados atomicamente no cliente. Uma linha excepcionalmente maior que esse limite é omitida com erro no log; o ritual e o dado persistido continuam válidos. O cliente descarta seus dados ao desconectar. Requests de leitura têm intervalo mínimo de dez ticks e não contêm comandos para conceder IDs.

## Administração

Todos exigem permissão 2:

```text
/inscribed knowledge status <player>
/inscribed knowledge learn enchantment <player> <id>
/inscribed knowledge forget enchantment <player> <id>
/inscribed knowledge learn recipe <player> <id>
/inscribed knowledge forget recipe <player> <id>
/inscribed knowledge clear <player>
/inscribed debug recipes visible <player>
```

`learn` valida a existência do encantamento/receita; `forget` também aceita um ID obsoleto para permitir limpeza. `clear` afeta somente o conhecimento Inscribed. O diagnóstico mostra ID, resultado e política responsável. Grants explícitos por loot/manuscritos e compartilhamento de equipe continuam fora do escopo.
