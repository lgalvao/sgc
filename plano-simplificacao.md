# Plano de Simplificação Detalhado

Este documento detalha as ações específicas para reduzir a complexidade do projeto SGC, focando na eliminação de camadas redundantes e consolidação de lógica.

## 1. Consolidação do Backend (Módulo Processo)

O objetivo é reduzir de 7 arquivos para apenas 2 na camada de serviço/orquestração do domínio `Processo`.

### Ações Específicas:
- **Fusão de Serviços:** Unificar os 5 serviços abaixo em um único `ProcessoService.java`:
  - `ProcessoConsultaService` (Métodos: `buscarProcessoCodigo`, `subprocessosElegiveis`)
  - `ProcessoManutencaoService` (Métodos: `criar`, `atualizar`, `apagar`)
  - `ProcessoWorkflowService` (Métodos: `iniciar`, `finalizar`)
  - `ProcessoValidacaoService` (Lógica de permissões e validação de unidades)
  - `ProcessoNotificacaoService` (Envio de e-mails/lembretes)
- **Eliminação da `ProcessoFacade`:** Os métodos de roteamento da `ProcessoFacade` (ex: `obterContextoCompleto`, `executarAcaoEmBloco`) serão movidos diretamente para o `ProcessoController` ou incorporados como métodos de orquestração no novo `ProcessoService`.
- **Refatoração do Builder:** O `ProcessoDetalheBuilder` será removido. Sua lógica de construção de DTO será migrada para um método `converterParaDetalheDto` dentro do `ProcessoService`.
- **Simplificação de DTOs:** Remover o mapeamento manual em métodos que retornam `Processo` e usar `@JsonView(ProcessoViews.Publica.class)` no `ProcessoController` para controlar a serialização via Jackson.

## 2. Simplificação do Frontend (Módulo Processo)

Migrar da arquitetura baseada em Stores globais para Composables baseados em contexto.

### Ações Específicas:
- **Exclusão da Store:** Apagar `src/stores/processos.ts`.
- **Criação do Composable:** Criar `src/composables/useProcessos.ts` contendo:
  - Estado local reativo: `processos`, `processoAtivo`, `loading`.
  - Funções encapsuladas: `loadProcessos()`, `loadDetalhes(id)`, `executarAcao(id, payload)`.
- **Refatoração de Views:** 
  - Em `ProcessoView.vue` e `ProcessoDetalheView.vue`, substituir `useProcessosStore()` por `useProcessos()`.
  - Remover a sincronização reativa global entre telas que não é necessária para este fluxo.

## 3. Cronograma de Execução

1. **Fase 1:** Criar `ProcessoService` e migrar logicamente `Consulta` e `Manutencao`.
2. **Fase 2:** Migrar `Workflow`, `Validacao` e `Notificacao` para o novo serviço.
3. **Fase 3:** Refatorar `ProcessoController` para remover a `ProcessoFacade`.
4. **Fase 4:** Criar o Composable no frontend e remover a Store de processos.
