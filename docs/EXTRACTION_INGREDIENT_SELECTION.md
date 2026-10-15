> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Extração por ingredientes — Inscribed 0.2.1

Extração não abre UI. O encantamento é definido pelos reagentes presentes nos pedestais. Shift + clique direito inicia diretamente a única extração válida. Se os reagentes habilitarem mais de uma, o altar informa a ambiguidade e pede ajuste, sem iniciar, consumir recursos ou escolher por ordem de ID. Quando existe uma extração válida, ela tem prioridade sobre infusões; a tela de seleção permanece disponível somente para múltiplas infusões.

Cada encantamento do catálogo possui um reagente seletor distinto, além das ametistas. O nível precisa corresponder exatamente ao nível presente no alvo. Quantidades excedentes continuam permitidas, por isso é importante não colocar reagentes de vários encantamentos simultaneamente.

Exemplos de extração do catálogo:

| Encantamento | Ingredientes | Potência | Duração | Falha |
| --- | --- | --- | --- | --- |
| Fortuna III | 6 ametistas + 1 esmeralda | 10 | 180 ticks | 20% |
| Eficiência IV | 8 ametistas + 1 redstone | 14 | 220 ticks | 20% |
| Inquebrável III | 6 ametistas + 1 obsidiana | 10 | 180 ticks | 20% |
| Afiação I | 1 ametista + 1 sílex | 2 | 80 ticks | 20% |

Todos esses exemplos exigem um livro comum físico em Chiseled Bookshelf. Consulte ENCHANTMENT_RECIPE_CATALOG.md para as 131 receitas vanilla/Create, incluindo os seletores sem colisões. Custos de infusão permanecem iguais.

## Chance de falha

Novo campo opcional dentro de `extraction`:

```json
"extraction": {
  "ingredients": [
    {"item": "minecraft:amethyst_shard", "count": 6},
    {"item": "minecraft:emerald", "count": 1}
  ],
  "duration_ticks": 180,
  "empty_books_required": 1,
  "failure_chance": 0.20
}
```

Aceita número finito entre 0 e 1: zero garante sucesso; um garante falha. Receitas existentes sem o campo continuam com chance zero, preservando compatibilidade dos datapacks. O catálogo distribuído usa inicialmente 20%, ajustável por receita.

A chance é avaliada uma única vez, no commit do servidor, após revalidar o altar. Tanto sucesso quanto falha removem apenas o encantamento escolhido e consomem as quantidades exigidas de ingredientes/livros. No sucesso, surge o livro encantado no slot reservado. Na falha, não surge livro; o conhecimento é perdido. Os outros encantamentos, nome, dano, tipo e componentes do alvo permanecem. Falha usa fumaça, som vanilla de extinção e mensagem específica.

Cancelar por alteração de livros, ingredientes, alvo, potência, chunks ou reload continua sem consumo dos recursos restantes. Cancelamento não é falha de extração e não faz o sorteio. Reservas e rollback existentes continuam compartilhados com infusão.

## Validação e instalação

Quatro testes adicionais verificam interação física sem UI, rejeição de múltiplas extrações, falha garantida com consumo exato e preservação dos demais dados, e schema com valores inválidos/default compatível. Testes de sucesso usam recipes determinísticas com chance zero; testes de falha usam chance um, sem depender de sorte no CI. A seleção e proteção de pacotes continuam testadas para infusão.

Resultado: 60 GameTests standalone, 60 com LSBC/Create e quatro JUnit aprovados; cliente nativo confirmou Shift + clique, extração por ingredientes sem tela, sincronização e livro físico. Evidências em docs/validation/ritual021-alone; logs validation-021-server.log e validation-021-client.log. Auditoria verifica 44 seletores distintos para os 44 encantamentos e os recursos exatos das 131 receitas.

Instalação: inscribed-0.2.1.jar. Backup da versão anterior em CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-extraction-risk. Reinicie Minecraft para carregar a atualização. Detalhes de hash e arquivos alterados no INSCRIBED_RELEASE_MANIFEST.json.
