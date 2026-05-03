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
- [x] **P1 - Refatoração de `ProcessoFormFields.vue`**: Componente decomposto em `ProcessoBasicFields`, `ProcessoUnidadesField` e `ProcessoDeadlineField`. Lógica de foco simplificada.
- [x] **P1 - Consolidação de Helpers de Status**: Criado `utils/statusHelpers.ts` centralizando variantes e labels de Processos e Notificações.
- [x] **P1 - Estabilização de Stubs**: Atualizados stubs de testes em `CadProcesso.spec.ts` para refletir a nova interface do formulário.

## Pendentes Prioritários (P1)

## Pendentes de Qualidade (P2-P4)
- [ ] **P2 - Extração da Árvore**: Decompor `ArvoreUnidades.vue` em subcomponentes menores (Nó, Pesquisa, Toggle).
- [ ] **P2 - Refatoração de `NotificacoesAdminView.vue`**: Extrair lógica de listagem e filtros para componentes dedicados.
- [ ] **P3 - Ratcheting de Waivers**: Reduzir os limites em `frontend-cruft-waivers.json` após as limpezas.vos que foram limpos.

## Achados Recentes (Varredura de 2026-05-03)
- [ ] **P3: useErrorHandler.ts** - Possui redundância na captura de erros de rede vs erros de negócio. Padronizar via `normalizarErro`.
