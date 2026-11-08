# Inscribed 0.7.4 — recursos visuais

O Pedestal de Infusão passa a usar a perspectiva de inventário em 3/4, com rotação `[28, 225, 0]`, seguindo a direção visual da Mesa de Pesquisas do LSBC. A alteração está no modelo de item; o bloco colocado mantém a geometria anterior.

O Catalisador de Encantamento recebe um sprite temporário próprio, plano, RGBA 64×64:

`src/main/resources/assets/inscribed/textures/item/enchantment_catalyst.png`

Arte gerada com a skill `imagegen`, ferramenta integrada e fundo transparente. O original está em `docs/evidence/instrument-visuals/art/enchantment_catalyst-original.png`. A normalização para o atlas usou somente amostragem de vizinho mais próximo e preservou o canal alpha. Veja o [registro de procedência](publication/PROVENANCE.md).

Prompt: "Use case: stylized-concept. Asset type: Minecraft temporary 2D item sprite, enchantment catalyst for Inscribed mod. ONE magical amethyst crystal held in a small obsidian and gold setting with a tiny warm gold rune-like gleam, clearly a handheld ritual catalyst. Classic vanilla Minecraft 32x32 logical pixel grid sprite style, hard square pixel clusters, simple silhouette, restrained purple violet gold black palette, strong dark outline, diagonal inventory icon. Centered fills 85 percent of square, genuinely transparent background. No text or readable lettering, no pedestal, no scene, no cast shadow, no extra objects. Square power-of-two PNG."

Build, quatro testes unitários e a auditoria existente do catálogo passaram. A comparação dos JARs confirma que as classes Java e todas as receitas, configurações embarcadas e definições de rituais permanecem idênticas às de 0.7.3. As únicas entradas anteriores alteradas são os dois modelos de item e a versão no manifesto; foi adicionada somente a textura do catalisador.

A [preparação de publicação 0.8.0](publication/README.md) inclui esta alteração visual e registra a validação standalone atual.
