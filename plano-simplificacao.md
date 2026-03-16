# Pendências do Plano de Simplificação

Este documento lista apenas o que ainda falta concluir no processo de simplificação do projeto SGC.

## 1. Backend (Módulo Processo)

### Pendências
- **Simplificação de DTOs:** revisar os endpoints do módulo `Processo` para eliminar, onde ainda existir, o mapeamento manual desnecessário em retornos de `Processo`.
- **Serialização com Jackson:** aplicar `@JsonView(ProcessoViews.Publica.class)` no `ProcessoController`, caso ainda não tenha sido adotado em todos os endpoints previstos pelo plano.

## 2. Frontend (Módulo Processo)

### Pendências
- **Remover a Store global de processos:** apagar `src/stores/processos.ts`.
- **Desacoplar o composable da Store:** refatorar `src/composables/useProcessos.ts` para que ele deixe de ser um wrapper de `useProcessosStore()` e passe a manter estado local baseado em contexto.
- **Garantir estado local reativo no composable:** manter diretamente no composable os estados:
  - `processos`
  - `processoAtivo`
  - `loading`
- **Concentrar operações no composable:** manter no composable as funções:
  - `loadProcessos()`
  - `loadDetalhes(id)`
  - `executarAcao(id, payload)`
- **Refatorar views restantes:** substituir usos remanescentes de `useProcessosStore()` por `useProcessos()`.
- **Remover sincronização reativa global desnecessária:** eliminar dependências entre telas que ainda assumem compartilhamento global de estado para esse fluxo.

## 3. Próxima fase de execução

1. Concluir a simplificação de DTOs e padronizar a serialização no backend.
2. Refatorar `useProcessos.ts` para funcionar sem Store global.
3. Atualizar as views restantes para usar apenas o composable.
4. Remover definitivamente a Store de processos.
