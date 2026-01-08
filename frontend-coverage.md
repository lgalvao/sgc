# Relatório de Cobertura de Testes Frontend

Este documento apresenta o estado atual da cobertura de testes unitários do frontend e detalha o plano de ação para atingir 100% de cobertura.

## Estado Atual

**Resumo Geral:**
- **Instruções (Statements):** 92.53%
- **Ramos (Branches):** 84.15%
- **Funções (Functions):** 90.16%
- **Linhas (Lines):** 93.77%

A cobertura geral é alta, com a camada de dados (Services e Stores) e agora Mappers e Utils praticamente completa. O esforço restante deve focar em componentes visuais (UI) e views complexas.

## Arquivos Prioritários (Cobertura < 80% em algum critério)

### 1. Componentes (`src/components`)
*   `EmptyState.vue` (Branches: 50%): Componente visual simples, mas com renderização condicional não testada.
*   `ModalConfirmacao.vue` (Funções: 60%, Instruções: 73.68%): Métodos de emissão de eventos ou slots não cobertos.
*   `ConfirmacaoDisponibilizacaoMapaModal.vue` (Branches: 65.38%): Baixa cobertura em fluxos de confirmação/cancelamento.
*   `SubprocessoCards.vue` (Funções: 72%, Instruções: 77.77%): Exibição condicional de cards e ações associadas.
*   `SeletorUnidades.vue` (Branches: 74.39%): Lógica de seleção e filtragem precisa de mais cenários.
*   `TabelaAlertas.vue` (Branches: 75%, Funções: 75%): Lógica de renderização de alertas e ações de leitura.
*   `FiltroProcessos.vue` (Branches: 75%): Lógica de filtragem condicional.
*   `AnaliseModal.vue` (Branches: 75%): Tratamento de estados do modal.
*   `SelecaoUnidadesModal.vue` (Branches: 76.78%): Seleção múltipla e eventos.
*   `TreeRowItem.vue` (Funções: 66.66%): Interatividade da árvore (expandir/colapsar).

### 2. Mappers (`src/mappers`)
*   **Concluído.** (Anteriormente: `processos.ts`, `usuarios.ts`, `unidades.ts` tinham baixa cobertura. Agora estão com 100% de cobertura de branches.)

### 3. Composables (`src/composables`)
*   **Concluído.** (Anteriormente: `useFormErrors.ts` tinha baixa cobertura. Agora está com 100% de cobertura de branches.)

### 4. Router (`src/router`)
*   `index.ts` (Branches: 75%): Guards globais (`beforeEach`) e redirecionamentos condicionais.

### 5. Views (`src/views`)
*   `NotificacoesView.vue` (Funções: 56.52%): Lista de notificações, filtros e ações em massa.
*   `CadAtividades.vue` (Branches: 68.21%): Formulário complexo com muitas validações e estados de UI.
*   `ValidarAtividades.vue` (Branches: 68.69%): Fluxo de validação, aprovação e rejeição de atividades.
*   `CadMapa.vue` (Branches: 73.21%): Formulário de cadastro de mapa.
*   `PainelView.vue` (Branches: 76.92%): Lógica de exibição condicional do painel.
*   `SubprocessoView.vue` (Branches: 78.33%): Detalhes do subprocesso e abas.
*   `UnidadeView.vue` (Branches: 78.46%): Visualização de unidade e hierarquia.
*   `ProcessoView.vue` (Branches: 79.26%): Visualização do processo e seus subprocessos.

## Plano de Ação para 100% de Cobertura

O trabalho deve ser executado na seguinte ordem de prioridade.

### Passo 1: Mappers e Utils (Correções Rápidas)
*   **Status:** Concluído.
*   **Resultados:** `processos.ts`, `usuarios.ts`, `unidades.ts`, `useFormErrors.ts` agora possuem 100% de cobertura de branches.

### Passo 2: Componentes Isolados
*   **Ação:** Testar renderização condicional (v-if/v-else) e emissão de eventos.
*   **Alvo:** `EmptyState.vue`, `ModalConfirmacao.vue`, `FiltroProcessos.vue`.

### Passo 3: Componentes de Negócio
*   **Ação:** Mockar stores e testar interações mais complexas.
*   **Alvo:** `SubprocessoCards.vue`, `SeletorUnidades.vue`, `TabelaAlertas.vue`.

### Passo 4: Views Críticas (Formulários e Fluxos)
*   **Ação:** Mockar `useRouter`, `useRoute` e todas as stores dependentes. Simular fluxos completos de sucesso e erro.
*   **Alvo:** `CadAtividades.vue`, `ValidarAtividades.vue`, `NotificacoesView.vue`.

## Dicas para Aumentar a Cobertura

1.  **Use `vitest --ui`:** A interface gráfica mostra linha a linha o que não foi executado.
2.  **Mock de Erros:** Para cobrir branches de `catch`, force erros nos mocks:
    ```typescript
    vi.mock('@/services/api', () => ({
      default: { get: vi.fn().mockRejectedValue(new Error('Erro simulado')) }
    }))
    ```
3.  **Testar Redirecionamentos:** Em views, verifique se `router.push` foi chamado com os argumentos corretos.
4.  **Snapshots com Cuidado:** Use snapshots para garantir que a estrutura do DOM não mude acidentalmente, mas foque em testar a lógica (v-if, @click).

## Critérios de Aceite
*   Executar `npm run coverage:unit` deve resultar em 100% em todas as colunas.
*   Todo código novo deve vir acompanhado de testes.
*   Não utilizar `/* v8 ignore next */` a menos que seja código inalcançável por definição (ex: tipos restritivos do TS).
