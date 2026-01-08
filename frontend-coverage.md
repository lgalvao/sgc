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
*   `ConfirmacaoDisponibilizacaoMapaModal.vue` (Branches: 65.38%): Baixa cobertura em fluxos de confirmação/cancelamento.
*   `AnaliseModal.vue` (Branches: 75%): Tratamento de estados do modal.
*   `SelecaoUnidadesModal.vue` (Branches: 76.78%): Seleção múltipla e eventos.
*   `TreeRowItem.vue` (Funções: 66.66%): Interatividade da árvore (expandir/colapsar).

### 2. Mappers (`src/mappers`)
*   **Concluído.**

### 3. Composables (`src/composables`)
*   **Concluído.**

### 4. Router (`src/router`)
*   `index.ts` (Branches: 75%): Guards globais (`beforeEach`) e redirecionamentos condicionais.

### 5. Views (`src/views`)
*   `NotificacoesView.vue`: **ARQUIVO NÃO ENCONTRADO**. Removido da lista.
*   `CadAtividades.vue` (Branches: 78.8%): Cobertura melhorada, mas ainda há branches a cobrir em validações e getters complexos.
*   `VisAtividades.vue` (ValidarAtividades) (Branches: 68.69%): Fluxo de validação, aprovação e rejeição de atividades.
*   `CadMapa.vue` (Branches: 73.21%): Formulário de cadastro de mapa.
*   `PainelView.vue` (Branches: 76.92%): Lógica de exibição condicional do painel.
*   `SubprocessoView.vue` (Branches: 78.33%): Detalhes do subprocesso e abas.
*   `UnidadeView.vue` (Branches: 78.46%): Visualização de unidade e hierarquia.
*   `ProcessoView.vue` (Branches: 79.26%): Visualização do processo e seus subprocessos.

## Plano de Ação para 100% de Cobertura

O trabalho deve ser executado na seguinte ordem de prioridade.

### Passo 1: Mappers e Utils (Correções Rápidas)
*   **Status:** Concluído.

### Passo 2: Componentes Isolados
*   **Status:** Concluído.

### Passo 3: Componentes de Negócio
*   **Status:** Concluído.

### Passo 4: Views Críticas (Formulários e Fluxos)
*   **Ação:** Mockar `useRouter`, `useRoute` e todas as stores dependentes. Simular fluxos completos de sucesso e erro.
*   **Alvo:** `VisAtividades.vue`, `CadAtividades.vue`.
*   **Em Progresso:** `CadAtividades.vue` melhorado significativamente. `VisAtividades.vue` requer atenção.

## Dicas para Aumentar a Cobertura

1.  **Use `vitest --ui`:** A interface gráfica mostra linha a linha o que não foi executado.
2.  **Mock de Erros:** Para cobrir branches de `catch`, force erros nos mocks.
3.  **Testar Redirecionamentos:** Em views, verifique se `router.push` foi chamado com os argumentos corretos.
4.  **Snapshots com Cuidado:** Use snapshots para garantir que a estrutura do DOM não mude acidentalmente.

## Critérios de Aceite
*   Executar `npm run coverage:unit` deve resultar em 100% em todas as colunas.
