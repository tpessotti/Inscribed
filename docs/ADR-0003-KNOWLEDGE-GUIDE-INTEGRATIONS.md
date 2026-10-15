# ADR 0003 — Descoberta, linguagem e apresentações opcionais

Status: aceito para Inscribed 0.3.0.

1. Descoberta de encantamentos e receitas pertence ao Inscribed, é persistida por jogador e funciona sem LSBC.
2. LSBC complementa esse estado com KnownWords. Conhecer um encantamento e compreender suas palavras são permissões distintas; erosão linguística não remove descoberta.
3. Guide Book e Codex compartilham um único provider de documentação e snapshots autorizados. O Codex expõe esse provider por sua quinta posição de ribbon, preservando o LSBC instalado.
4. JEI recebe apenas receitas permitidas ao jogador atual e usa sua API pública para adicionar/ocultar em runtime. Limitações do índice global de ingredientes e ausência de remoção definitiva são documentadas, sem patches de classes internas.
5. Descoberta modifica apresentação, nunca a autorização física dos rituais. Livros em estantes não ensinam só por serem vistos; ingredientes continuam selecionando a extração e sua chance de falha permanece configurável.
