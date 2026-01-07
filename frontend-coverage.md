# Relatório de Cobertura de Testes Frontend

Este documento apresenta o estado atual da cobertura de testes unitários do frontend e detalha o plano de ação para atingir 100% de cobertura.

## Estado Atual

**Resumo Geral (Execução Recente):**
- **Statements:** 92.5% (Anterior: 91.97%)
- **Branches:** 83.31% (Anterior: 82.89%)
- **Functions:** 90.16% (Anterior: 89.69%)
- **Lines:** 93.77% (Anterior: 93.2%)

A cobertura geral é alta, com avanços significativos em Services e Stores. As lacunas remanescentes estão concentradas em componentes Vue complexos, tratamento de erros em mappers, e algumas stores com lógica residual não coberta.

## Arquivos Prioritários (Cobertura < 80% em algum critério)

### 1. Componentes (`src/components`)
*   `ConfirmacaoDisponibilizacaoModal.vue` (Branches: 65.38%): Baixa cobertura em branches condicionais.
*   `ModalConfirmacao.vue` (Stmts: 73.68%, Funcs: 60%): Componente simples, mas com métodos de emissão de eventos ou slots não cobertos.
*   `SubprocessoCards.vue` (Stmts: 77.77%, Funcs: 72%): Exibição condicional de cards e ações associadas.
*   `SeletorUnidades.vue` (Branches: 74.39%): Lógica de seleção e filtragem precisa de mais cenários (obs: nome inferido de `...eUnidades.vue`).
*   `TabelaAlertas.vue` (Branches: 75%, Funcs: 75%): Lógica de renderização de alertas e ações.
*   `TreeRowItem.vue` (Funcs: 66.66%): Lógica de expansão/colapso ou seleção na árvore.

### 2. Mappers (`src/mappers`)
*   `processos.ts` (Branches: 66.66%): Conversão de DTOs com campos opcionais ou nulos.
*   `usuarios.ts` (Branches: 78.26%): Mapeamento de perfis ou dados aninhados.
*   `unidades.ts` (Branches: 78.78%): Estruturas hierárquicas podem não estar sendo totalmente exercitadas.

### 3. Router (`src/router`)
*   `index.ts` (Branches: 75%): Guards globais (`beforeEach`) e redirecionamentos.

### 4. Stores (`src/stores`)
*   `analises.ts` (Branches: 80%): Lógica de tratamento de erro ou estados intermediários.
*   `usuarios.ts` (Stmts: 92.3%): Pequenos trechos não cobertos.

### 5. Views (`src/views`)
*   `CadAtividades.vue` (Branches: 68.21%): Formulário complexo com muitas validações e estados.
*   `NotificacoesView.vue` (Funcs: 56.52%): Lista de notificações, filtros e ações em massa.
*   `ValidarAtividades.vue` (Branches: 68.69%): Fluxo de validação e rejeição de atividades.
*   `CadMapa.vue` (Branches: 73.21%): Formulário de cadastro de mapa.
*   `PainelView.vue` (Branches: 76.92%): Lógica de exibição do painel.

## Plano de Ação para 100% de Cobertura

O trabalho deve ser dividido em iterações focadas por tipo de arquivo.

### Passo 1: Stores e Services (Base Lógica) - **CONCLUÍDO (Maioria)**
A camada de dados está robusta.
1.  **Status:** Services estão com 100% de cobertura (exceto `painelService` com 95% em branch). Stores principais como `alertas`, `atribuicoes`, `processos`, `mapas` estão com 100%.

### Passo 2: Mappers e Utils (Funções Puras)
São os testes mais fáceis e rápidos de corrigir.
1.  **`src/mappers/*.ts`**:
    *   Criar casos de teste com objetos de entrada variados (nulos, arrays vazios, dados parciais) para cobrir todos os branches de mapeamento em `processos.ts` e `usuarios.ts`.
2.  **`src/utils/index.ts`**:
    *   Testar branches de borda em funções utilitárias.

### Passo 3: Componentes UI (Complexidade Média)
Focar em interações do usuário e estados visuais.
1.  **Modais (`ConfirmacaoDisponibilizacaoModal.vue`, `ModalConfirmacao.vue`)**:
    *   Simular props de entrada variadas.
    *   Disparar eventos de clique e verificar `emitted()`.
2.  **`SubprocessoCards.vue`**:
    *   Testar a renderização condicional dos cards baseado no estado do subprocesso.

### Passo 4: Views (Alta Complexidade)
Estas views possuem muita lógica de integração.
1.  **`CadAtividades.vue` e `ValidarAtividades.vue`**:
    *   Mockar `useRouter`, `useRoute` e stores do Pinia.
    *   Testar fluxos de sucesso (preenchimento correto) e erro (validação falhando).
2.  **`NotificacoesView.vue`**:
    *   Testar paginação e filtros.
    *   Verificar renderização de listas vazias.

### Passo 5: Router e Configurações Globais
1.  **`src/router/index.ts`**:
    *   Testar navigation guards (`beforeEach`) simulando usuários autenticados e não autenticados.

## Dicas Técnicas
*   Use `vitest --ui` localmente para visualizar quais linhas exatas não estão cobertas.
*   Para testar branches de erro em stores/services:
    ```typescript
    vi.mock('@/services/api', () => ({
      default: { get: vi.fn().mockRejectedValue(new Error('Erro simulado')) }
    }))
    ```
*   Para componentes, use `mount` do `@vue/test-utils` e verifique alterações no DOM ou snapshots.

## Critérios de Aceite
*   Executar `npm run coverage:unit` deve resultar em 100% em todas as colunas (Statements, Branches, Functions, Lines).
*   Não deve haver testes ignorados (`test.skip`) sem justificativa documentada.
