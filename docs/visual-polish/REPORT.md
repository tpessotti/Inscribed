# Visual Polish — entrega instalada

Instalados nesta instância: **Inscribed 0.6.0** e **LSBC Core 0.22.0**. Os JARs anteriores foram removidos de `mods` e permanecem no backup `CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-visual-polish`.

## Resultado

- Codex com duas páginas vanilla, marcadores laterais, busca na folha esquerda, detalhes na direita e álbuns que evitam a dobra.
- Guide independente com índice permanente, detalhes roláveis, marcadores e diagramas de infusão/extração com sprites reais.
- Novos ícones transparentes de Codex e Guide, com navegação/arcano como identidades distintas.
- Pedestal de obsidian e ouro, topo menor, mesma altura e itens maiores com escala separada.
- Loop posicional de beacon na infusão, limitado a uma instância por mesa e interrompido pelo estado real do altar.
- Fallback legível para nomes modded sem tradução, preservando nomes autorais, acentos, componentes e KnownWords.

As receitas, dados de progressão, loot e identificadores de persistência não foram alterados. A seleção da extração continua determinada pelos ingredientes; sua chance de falha de 20% foi preservada. A auditoria comparou dados dos projetos com o backup e os JSONs instalados com seus hashes anteriores à instalação. Também confirmou que nenhum JAR de outro mod foi alterado.

## Validação

| Verificação | Resultado |
| --- | --- |
| JUnit Inscribed | 4/4 |
| JUnit LSBC | 112/112 |
| GameTests Inscribed sem LSBC | 88/88 |
| GameTests Inscribed com LSBC 0.22.0 | 88/88 |
| Codex nativo, EN/PT, escalas 2/3/4/auto | 108 capturas, PASS |
| Guide nativo standalone | PASS, incluindo diagramas e quatro escalas |
| Guide com LSBC + JEI | PASS, busca protegida, navegação e atualização de receitas |
| Ritual nativo | PASS, infusão/extração/conclusão/cancelamento e loop único de áudio |

Testes de apresentação usam clientes isolados. O teste de fallback exercita uma tradução ausente de entidade modded e preservação de nomes com acentos; não afirma testar cada tradução de todos os mods da instância. A fixture de ritual usa sucesso garantido para capturar a extração; isso não altera a chance de falha da configuração instalada.

As primeiras capturas encontraram espelhamento invisível e blur sobre o Guide; ambos foram corrigidos e as capturas refeitas. O menor viewport usa paginação e rolagem para manter as páginas dentro da tela.

## Evidências e manutenção

- [Decisões, assets substituíveis e prompts imagegen](ASSETS.md)
- [Manifesto de instalação e hashes](RELEASE_MANIFEST.json)
- [Inventário de código e assets alterados](changed-source-files.json)
- [Auditoria de preservação](preservation-audit.json)
- [Migração das opções visuais](config-migration.json)
- [108 capturas do Codex](codex-layout-results.json)
- [Guide standalone](standalone/results.json)
- [Guide com LSBC e JEI](lsbc-jei/results.json)
- [Áudio e rituais](ritual-results.json)
- [Infusão ilustrada](lsbc-jei/visual-scale3-infusion.png)
- [Extração ilustrada](lsbc-jei/visual-scale3-extraction.png)
- [Pedestal em ritual](03-infusion-transfer.png)

A config ganhou `visuals.pedestal_item_scale` e `sounds.infusion_ambient`. Apenas o valor antigo padrão de `sounds.infusion_start` foi migrado para beacon.activate; outras opções existentes foram preservadas. Os ícones são placeholders de alta resolução; seu aviso de mipmaps e os caminhos para substituir por arte final estão documentados em ASSETS.md.
