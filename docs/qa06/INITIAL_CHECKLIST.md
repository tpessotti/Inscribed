Historical baseline checklist. Final outcomes: [QA matrix](../INSCRIBED_QA_MATRIX.md) and [release report](../INSCRIBED07_REPORT.md). The license was subsequently set to MIT by the owner.

# Auditoria inicial 06/07

Baseline Inscribed 0.6.1: build PASS, JUnit 4/4, dedicated GameTests 88/88.

Presentes no pack: LSBC 0.22.1; JEI 19.57.0.451; Create 6.0.10. LSBC/JEI/Create s?o opcionais para o core. NeoForge 21.1.255, Minecraft 1.21.1, Java 21.

- [x] Build/testes antes de editar c?digo.
- [x] Muta??o de invent?rio: AltarInteraction, InfusionPedestalBlock, PedestalBlockEntity, RitualController.complete.
- [x] Muta??o de encantamento/commit: RitualController.complete, InfusionMatcher.compatible.
- [x] Packets: SelectionChoice e GuideRequest serverbound; InspectionPayload, SelectionPayload e GuidePayload clientbound.
- [x] Estado de ritual: RitualController, RitualSelection, AltarStateService.
- [x] Reload: InscribedRecipeRegistry, LootInjection, ConfigRecipeStore, ConfigCraftingRecipesMixin; generation invalida sess?es.
- [x] Save/load: PedestalBlockEntity, AltarState.CODEC, KnowledgeData.CODEC e copyOnDeath.
- [x] Optional classloading: KnowledgeIntegrations usa gate/reflection; JEI plugin isolado; LSBCGuideClient gated.
- [x] Determinismo: TreeMap de receitas, dist?ncia/XYZ/slot para invent?rios, alocador max-flow.
- [ ] Matriz A?G dedicada e cliente nativo.
- [ ] Multiplayer de duas conex?es reais.
- [ ] Restart/save e remo??o/reinstala??o de Create.
- [ ] Exploits/cancelamento/no-op/automa??o.
- [ ] Build clean, documenta??o, schemas, exemplos e authoring real.

Warnings esperados: annotations antigas Bus.MOD; avisos de runtime NeoForge/Minecraft; PNGs placeholder com mipmap n?o alinhado. Fixtures deliberadamente inv?lidas geram erros de valida??o; n?o representam receitas da release.

Poss?veis bugs sob investiga??o: sess?o ativa n?o observa sa?da do jogador; canal vanilla EnchantmentMenu acess?vel por mod externo; JEI re-registra variantes id?nticas ap?s reload; fallback de encantamentos modded sem tradu??o.

Config JSON de conte?do existente ser? preservada como overlay de compatibilidade; datapacks ser?o o caminho recomendado para conte?do novo, com preced?ncia documentada. Nenhuma licen?a nova ser? presumida: metadata atual All Rights Reserved, sem LICENSE.
