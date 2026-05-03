# Backlog de Limpezas e Refatorações Frontend

## [CONCLUÍDO - 2026-05-03]
- [x] **P0: NotificacoesAdminView.vue** - Extraída lógica de status para `notificacaoService.ts`.
- [x] **P0: useFeedback.ts** - Removido `usuarioEmail` morto e corrigidos testes (spy logger).
- [x] **P0: useFluxoSubprocesso.ts** - Removida flag booleana `isRevisao` em favor de funções nomeadas explícitas.
- [x] **P0: AtribuicaoTemporariaView.vue** - Removido `defineExpose` e refatorados testes para interação via DOM.
- [x] **P3: useFluxoSubprocesso.ts** - Refatorados imports para usar namespace (`import * as`).

## Pendentes Prioritários (P1)
- [ ] **P1: apiError/normalizer.ts** - Alta complexidade (CRAP 380). Necessário criar suíte de testes unitários robusta antes de refatorar os 19 caminhos de execução.
- [ ] **P1: axios-setup.ts** - Refatorar `handleResponseError` para reduzir acoplamento e complexidade (CRAP 156).
- [ ] **P1: ProcessoCadastroView.vue** - Decompor `handleApiErrors` e mover lógica de validação/mapeamento para um helper ou service.

## Pendentes de Qualidade (P2-P4)
- [ ] **P2: ProcessoFormFields.vue** - Refatorar `focarPrimeiroErro` (CRAP 156).
- [ ] **P3: ArvoreUnidades.vue** - Componente muito grande (800+ linhas). Iniciar extração de subcomponentes (`ArvoreUnidadeItem.vue`).
- [ ] **P4: Waivers Ratchet** - Revisar `frontend-cruft-waivers.json` para reduzir limites de arquivos que foram limpos.

## Achados Recentes (Varredura de 2026-05-03)
- [ ] **P2: BuscadorUsuarios.vue** - Emite eventos desnecessários quando o valor já é o mesmo. Adicionar guard de igualdade.
- [ ] **P3: useErrorHandler.ts** - Possui redundância na captura de erros de rede vs erros de negócio. Padronizar via `normalizarErro`.
