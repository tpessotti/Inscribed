> SUPERSEDED: historical design/implementation evidence. Current behavior and coverage: [RC notes](RELEASE_NOTES_0.7.2-rc.1.md), [configuration](INSCRIBED_CONFIGURATION.md) and [coverage](INSCRIBED_RECIPE_COVERAGE_FINAL.md).

# Inscribed 0.1.2 — Som, alvo ampliado e análise com lupa

O ritual reproduz um toque de ametista a cada 20 ticks, com altura crescente ao longo da infusão. O som inicial e o som de conclusão continuam presentes. Os toques pertencem ao servidor, são posicionais e param quando o ritual termina ou é cancelado.

A escala do alvo flutuante sobre a mesa foi dobrada: de 0,55 para 1,10 antes da transformação vanilla do item. A ampliação mantém o centro acima do livro da mesa e não amplia os itens dos pedestais.

## Uso da lupa

Com `lsbc:magnifying_glass` na mão principal ou secundária, pressione Shift + botão direito na mesa de encantamentos. Mantenha o botão direito pressionado e o olhar na mesa durante 40 ticks, aproximadamente dois segundos a 20 TPS. O alcance da análise é de quatro blocos, igual ao alcance da lupa LSBC.

Depois da análise abre-se um painel com:

- o item colocado na mesa, com ícone e tooltip;
- a potência real disponível, calculada pelo scanner do altar;
- os ingredientes e quantidades presentes nos pedestais dentro do alcance;
- os livros encantados nas estantes talhadas, incluindo cada encantamento e seu nível;
- a quantidade de livros comuns disponíveis.

Use a roda do mouse para percorrer a lista e Esc ou Concluído para fechar. Os dados são um retrato do momento da análise; repita a análise depois de modificar o altar. As mensagens de área incompleta ou excesso de pedestais aparecem no painel. Bibliotecas excepcionalmente grandes ou com componentes enormes recebem um aviso explícito se ultrapassarem o limite de transmissão.

A análise não insere a lupa na mesa, inicia um ritual, reserva recursos ou consome itens. Soltar o botão, afastar-se, desviar o olhar, trocar a lupa, remover a mesa, morrer ou mudar de dimensão cancela a análise. Não é necessário manter Shift depois de iniciar.

## Integração e validação

A integração permanece dentro do Inscribed e identifica a lupa pelo ID de registro. LSBC continua opcional e seu JAR não é alterado. Nenhuma classe LSBC é importada. O protocolo próprio transmite somente uma visualização privada do servidor para o jogador que analisou a mesa. A interface não envia comandos de modificação de inventário.

Os 34 GameTests passaram no cenário Inscribed sozinho e no cenário Inscribed + Create 6.0.10 + LSBC 0.21.0, além de quatro testes JUnit. Os testes de servidor validam a transmissão dos componentes dos livros, a conservação dos inventários, o clique com a lupa real LSBC nas duas mãos, a duração e o cancelamento ao desviar o olhar. Os testes exclusivos da lupa são exercitados no cenário com LSBC.

O cliente de teste separado registra a abertura da interface por pacotes reais e capturas de tela em `runs/loupeClient/screenshots/inscribed-altar`. Logs: `validation-inspection-final.log` para a matriz de servidor e `validation-inspection-client-final.log` para a apresentação final. A validação do cliente também completa o ritual, verifica o resultado sincronizado, os custos e a retirada do item.

Backup do código e do JAR anterior: `CODEX LSBC/INSCRIBED/backups/2026-10-13-pre-loupe-inspection`.
