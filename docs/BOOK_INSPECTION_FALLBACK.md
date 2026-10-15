> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Inscribed 0.1.3 — Livro e Pena como fallback

Quando LSBC não está instalado, segure um Livro e Pena (`minecraft:writable_book`) em qualquer mão e use Shift + clique direito na mesa de encantamentos. O servidor analisa o altar e acrescenta um relatório às páginas existentes. A interface vanilla de edição abre automaticamente para ler o livro. Este fallback é imediato; não precisa manter o botão pressionado.

O relatório registra a posição da mesa, item alvo, potência disponível, ingredientes e quantidades, livros encantados disponíveis com seus níveis e a quantidade de livros comuns. São os mesmos dados da análise com lupa. O retrato permanece no livro depois que o altar muda, permitindo guardar ou compartilhar as anotações.

As páginas antigas, seus filtros e os demais componentes do livro são preservados. O livro continua sendo Livro e Pena, sem assinatura ou conversão para livro escrito. Novas análises acrescentam novos relatórios. Se não houver espaço para o relatório inteiro nas 100 páginas vanilla, a operação é recusada com uma mensagem; nenhuma página é apagada ou parcialmente gravada. A paginação respeita limites conservadores de largura e altura. Um breve cooldown impede relatórios duplicados por cliques repetidos.

Com LSBC instalado, a lupa continua sendo a ferramenta de análise. Livros assinados não são ferramentas do fallback. A análise não inicia o ritual nem consome o alvo, os ingredientes ou a biblioteca.

Os cabeçalhos e nomes vanilla usam traduções locais em inglês e português brasileiro, selecionadas pelo idioma informado pelo cliente. O arquivo português vem do asset oficial local do Minecraft, índice 17, SHA-1 `b2358bd3f17adc3244bc6c811e52ac01f932654e`; o inglês vem de `assets/minecraft/lang/en_us.json` no JAR local 1.21.1. As tabelas filtradas são empacotadas no Inscribed, sem exigir LSBC em runtime. Outros idiomas e nomes de mods usam os nomes disponíveis no servidor.

Validação: quatro testes JUnit e 38 GameTests em cada cenário, Inscribed sozinho e Inscribed + Create + LSBC. Inclui clique nas duas mãos sem LSBC, preservação de anotações e insumos, recusa de livro cheio, tradução e limites de páginas, e desativação do fallback com LSBC. O cliente isolado sem LSBC passou, confirmando a sincronização das páginas e abertura da interface vanilla; evidência em `runs/bookClient/screenshots/inscribed-altar`, log `validation-book-final.log`.

Backup anterior: `CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-book-fallback`.
