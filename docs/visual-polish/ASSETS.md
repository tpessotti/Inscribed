# Inscribed 0.6.0 / LSBC 0.22.0 — visual polish

## Decisões visuais

Os dois livros usam duas folhas da textura vanilla `minecraft:textures/gui/book.png`, com UV espelhado na folha esquerda. A proporção aproximada de 1,62 respeita o Book and Quill, com margens para a capa e a dobra. Não é uma reprodução pixel-perfect das referências.

O Codex mostra progresso/notas recentes ou índice/detalhes em páginas distintas. Os álbuns distribuem seus slots entre as folhas sem colocar itens na dobra. Marcadores laterais usam pergaminho e vermelho para a seleção. Controles adaptam-se às coordenadas escaladas do Minecraft.

O Guide mantém busca e índice na esquerda e detalhes com rolagem limitada na direita. Os marcadores Guide/Enchantments/Recipes compartilham a linguagem do Codex. O fundo do livro não recebe o blur de Screen.render. Componentes estilizados continuam preservando KnownWords; texto desconhecido não se torna pesquisável por seu nome lógico.

Os diagramas usam sprites reais, respeitando resource packs: estante esculpida/pedestal → mesa → espada; espada → mesa → livro encantado. São exemplos conceituais, não receitas que concedem conhecimento.

O pedestal usa obsidian e ouro vanilla, base de 14×14, coluna de 6×6 e topo de 12×12 com inserto de 10×10. Altura total permanece 14/16 de bloco. Seu item recebe escala própria de 0,80, comparada à escala anterior de 0,55, sem aumentar o alvo da mesa.

A infusão recebe uma única instância de som ambiente posicional de beacon por mesa. O loop observa o estado sincronizado do altar e para ao completar/cancelar, descarregar a mesa, sair do mundo, mudar dimensão, afastar-se ou silenciar os sons. Os sons existentes de transferência, conclusão e extração continuam configuráveis. `sounds.infusion_start` passa a beacon.activate por padrão.

Nomes de descobertas usam traduções do mod de origem com fallback legível. Sem tradução, `blue_jellyfish` vira `Blue Jellyfish` e `blank_shark` vira `Blank Shark`. Nomes autorais e acentos permanecem; IDs de persistência e glifos intencionais de KnownWords não mudam. Notas recentes recompõem seus nomes a partir dos registros atuais.

## Assets substituíveis

Paths abaixo são relativos a `CODEX LSBC/prototypes/`:

| Mod | Arquivo | Papel |
| --- | --- | --- |
| LSBC | `lsbc_core/src/main/resources/assets/lsbc/textures/item/codex_placeholder_v2.png` | Livro vermelho/castanho, bússola dourada, fecho turquesa |
| LSBC | `lsbc_core/src/main/resources/assets/lsbc/models/item/codex.json` | Registro do ícone |
| Inscribed | `inscribed/src/main/resources/assets/inscribed/textures/item/guide_book_placeholder_v2.png` | Livro vermelho/castanho, símbolo arcano e marcador roxo |
| Inscribed | `inscribed/src/main/resources/assets/inscribed/models/item/guide_book.json` | Registro do ícone |
| Inscribed | `inscribed/src/main/resources/assets/inscribed/models/block/infusion_pedestal.json` | Geometria e texturas vanilla do pedestal |
| Ambos | `src/main/java/.../OpenBookBackground.java` | Fundo de duas folhas vanilla |
| Inscribed | `inscribed/src/main/java/dev/inscribed/client/RitualDiagram.java` | Diagramas com itens reais |

Para substituir os ícones por resource pack, use os mesmos caminhos `assets/<mod>/textures/item/...png`. Ambos são placeholders transparentes gerados por imagegen, com estética de pixel art. Os PNGs originais são de alta resolução (Codex 1299×1211, Guide 1254×1254); não foram reduzidos nem retocados fora de imagegen. O Minecraft os carrega, mas registra um aviso de alinhamento de mipmaps. Uma futura arte final quadrada em 32×32 ou 64×64 reduzirá o tamanho do atlas.

O inventário completo de alterações está em `changed-source-files.json`. `vanilla-book-reference.png` é apenas uma cópia de referência da textura vanilla local; não entra no JAR.

## Configuração nova

Em `config/inscribed-server.toml`:

- `visuals.pedestal_item_scale = 0.8`: tamanho do item sobre o pedestal.
- `sounds.infusion_ambient = "minecraft:block.beacon.ambient"`: loop da infusão.
- `sounds.infusion_start = "minecraft:block.beacon.activate"`: novo padrão de início.
- `sounds.enabled` e `sounds.volume_multiplier` também controlam o loop.

Receitas, distribuição de loot, progressão e chance de falha de extração de 20% permanecem nas configurações existentes.

## Geração dos ícones

Ferramenta: imagegen built-in, fundo transparente, imagens novas. Referências visuais são orientação; não houve edição manual dos PNGs. Originais identificados pelos nomes de gera??o abaixo; caminhos privados n?o fazem parte da distribui??o:

- Codex: `exec-812db63a-2d82-4b36-b9df-d8f397393625.png`
- Guide: `exec-fac5644f-29fb-470e-8ccf-3f0a9fca09fe.png`

Prompt Codex:

> Use case: stylized-concept. Asset type: a single Minecraft inventory item sprite for Navigator's Codex, to be integrated into a Java mod. Generate one isolated closed book icon, centered, transparent background. Vanilla Minecraft pixel art: an angled reddish brown leather book with thick ivory pages, simple muted gold corners and a crisp gold compass/star navigation emblem on the cover, a small teal clasp, dark outlines. Deliberately low-resolution 32x32-style pixel grid, hard square pixels, no smoothing, no realistic shading, no lettering or text, no background, no glow, no mockup UI. The uploaded reference book artwork is style guidance only; create an original simpler sprite that remains legible at 32 pixels. Output only this single book sprite.

Prompt Guide:

> Use case: stylized-concept. Asset type: a single Minecraft inventory item sprite for the Inscribed Guide. Generate one isolated closed book icon, centered, transparent background. Match the reddish brown leather and ivory pages of a vanilla-friendly Navigator's Codex book, but use a simple gold arcane ring with one purple amethyst star in its center and a purple bookmark as its distinct identity. Angled closed book, chunky dark outlines, muted gold corners, restrained purple accent. Deliberately coarse 32x32-style pixel art, hard square pixels, no smoothing, no photorealistic details, no text or lettering, no glow outside the silhouette, no background or UI. Create original placeholder art inspired by the uploaded references; output only this single guide book sprite.
