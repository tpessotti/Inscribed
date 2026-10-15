# Consistência dos livros — Inscribed 0.6.1 / LSBC 0.22.1

Os livros usam o mesmo contrato `BookGeometry`: posição, tamanho, proporção, margens das folhas, largura dos marcadores e posição do rodapé. O código é idêntico nos dois projetos, com pacote próprio para manter o Inscribed independente do LSBC. A paleta também é idêntica.

- Busca com borda e fundo de pergaminho em ambos os livros.
- Marcadores com seleção vermelha, indicação de seleção e rótulos completos ajustados à largura.
- Blocks, Items, Creatures e Locations como subabas na lateral direita, abaixo de Catalog. As abas principais seguintes se deslocam para acomodá-las.
- Conteúdo, filtros, cabeçalhos de álbuns e botões respeitam as margens de cada folha e a dobra central.
- Removidas as linhas horizontais que atravessavam o livro.
- Setas anterior/próxima juntas no canto inferior direito, inclusive quando existe apenas uma página. No Guide elas ficam desativadas nos limites; o Codex usa a mesma regra.
- Histórico e retorno permanecem no rodapé da folha esquerda. A paginação continua disponível nos detalhes de coleção e no painel de filtros.

Em telas muito pequenas, a pilha lateral do catálogo ajusta sua posição para permanecer dentro do viewport. Os controles de conteúdo ficam dentro das folhas; os marcadores laterais são a extensão intencional do livro.

## Validação

Os testes nativos do Codex verificam todos os widgets quanto a viewport, sobreposição e limites horizontais das folhas, com exceção dos marcadores laterais. As 108 capturas cobrem inglês/português, escalas 2/3/4/auto, listas, álbuns, filtros, detalhes, rolagem e navegação.

O teste nativo com LSBC e JEI compara diretamente os limites dos dois livros abertos no mesmo viewport. Os testes do Guide verificam os limites das folhas nas quatro escalas. Os testes unitários dos dois projetos também foram executados.

Esta alteração é de interface. Receitas, configs de balanceamento, conhecimento, extração, sons e pedestal permanecem como na versão anterior. O backup está em `CODEX LSBC/INSCRIBED/backups/2026-10-13-book-consistency`.

Evidências finais e hashes de instalação estão em `results.json`. Capturas selecionadas estão nesta pasta.
