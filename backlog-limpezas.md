# Backlog de Limpezas e Refatorações Frontend

## [CONCLUÍDO - 2026-05-03]
- [x] **P0: NotificacoesAdminView.vue** - Extraída lógica de status para `notificacaoService.ts`.
- [x] **P0: useFeedback.ts** - Removido `usuarioEmail` morto e corrigidos testes (spy logger).
- [x] **P0: useFluxoSubprocesso.ts** - Removida flag booleana `isRevisao` em favor de funções nomeadas explícitas.
- [x] **P0: AtribuicaoTemporariaView.vue** - Removido `defineExpose` e refatorados testes para interação via DOM.
- [x] **P1: apiError/normalizer.ts** - Refatorado para reduzir complexidade (ciclomática reduzida). Criada suíte de testes unitários robusta.
- [x] **P1: axios-setup.ts** - Refatorado `handleResponseError` para decompor responsabilidades (log, 401, erro global).
- [x] **P1: ProcessoCadastroView.vue** - Simplificado `handleApiErrors` e movida lógica de mapeamento para `useProcessoForm.ts`.
- [x] **P2: BuscadorUsuarios.vue** - Adicionados guards de igualdade e limpeza automática reativa (removido `defineExpose`).
- [x] **P3: useFluxoSubprocesso.ts** - Refatorados imports para usar namespace (`import * as`).

## Pendentes Prioritários (P1)
- [ ] **P1: ProcessoFormFields.vue** - Refatorar `focarPrimeiroErro` (CRAP 156). Lógica de busca no DOM acoplada ao componente.
- [ ] **P1: Consolidar Notification Labels** - Centralizar mapeamentos de Badge + Label em `utils/statusHelpers.ts`.

## Pendentes de Qualidade (P2-P4)
- [ ] **P3: ArvoreUnidades.vue** - Componente muito grande (800+ linhas). Iniciar extração de subcomponentes (`ArvoreUnidadeItem.vue`).
- [ ] **P4: Waivers Ratchet** - Revisar `frontend-cruft-waivers.json` para reduzir limites de arquivos que foram limpos.

## Achados Recentes (Varredura de 2026-05-03)
- [ ] **P3: useErrorHandler.ts** - Possui redundância na captura de erros de rede vs erros de negócio. Padronizar via `normalizarErro`.
