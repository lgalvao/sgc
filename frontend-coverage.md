# Relatório de Cobertura de Testes Frontend

Este documento apresenta o estado atual da cobertura de testes unitários do frontend e detalha o plano de ação para atingir 100% de cobertura.

## Estado Atual

**Resumo Geral:**
- **Statements:** 91.97%
- **Branches:** 82.89%
- **Functions:** 89.69%
- **Lines:** 93.2%

A cobertura geral é alta, mas existem lacunas específicas, principalmente em componentes Vue complexos, tratamento de erros em serviços e stores, e alguns utilitários.

## Arquivos Prioritários (Cobertura < 80% em algum critério)

### 1. Componentes (`src/components`)
*   `SeletorUnidades.vue` (Branches: 74.39%): Lógica de seleção e filtragem precisa de mais cenários.
*   `AdicionarMapaModal.vue` (Geral: ~77%): Modal de adição de mapas tem lógica não testada, possivelmente relacionada a validações ou submissão.
*   `Confirmacao.vue` (Stmts: 73.68%, Funcs: 60%): Componente simples, mas provavelmente métodos de emissão de eventos ou slots não estão cobertos.
*   `ProcessoCards.vue` (Geral: ~80%): Exibição condicional de cards e ações associadas.
*   `TabelaAlertas.vue` (Branches: 75%, Funcs: 75%): Lógica de renderização de alertas e ações.
*   `TreeRowItem.vue` (Funcs: 66.66%): Lógica de expansão/colapso ou seleção na árvore.

### 2. Mappers (`src/mappers`)
*   `processos.ts` (Branches: 66.66%): Conversão de DTOs com campos opcionais ou nulos.
*   `usuarios.ts` (Branches: 78.26%): Mapeamento de perfis ou dados aninhados.
*   `unidades.ts` (Branches: 78.78%): Estruturas hierárquicas podem não estar sendo totalmente exercitadas.

### 3. Router (`src/router`)
*   `index.ts` (Branches: 75%): Guards globais (`beforeEach`) e redirecionamentos.

### 4. Stores (`src/stores`)
*   `alertas.ts` (Branches: 50%): Ações de busca ou marcação de lido com falhas de rede.
*   `atribuicoes.ts` (Branches: 50%): Lógica de erro ou estados intermediários.
*   `processos.ts` (Branches: 64.28%): Filtros complexos ou tratamento de erros em ações assíncronas.

### 5. Views (`src/views`)
*   `CadAtividades.vue` (Branches: 68.21%): Formulário complexo com muitas validações e estados.
*   `NotificacoesView.vue` (Funcs: 56.52%): Lista de notificações, filtros e ações em massa.
*   `ValidarAtividades.vue` (Branches: 68.69%): Fluxo de validação e rejeição de atividades.

## Plano de Ação para 100% de Cobertura

O trabalho deve ser dividido em iterações focadas por tipo de arquivo.

### Passo 1: Stores e Services (Base Lógica)
Garantir que a camada de dados esteja blindada facilita o teste dos componentes visuais.
1.  **`src/stores/alertas.ts`, `atribuicoes.ts`, `processos.ts`**:
    *   Simular erros de API (`axios` mockado retornando erro).
    *   Testar todos os branches de `if/else` em getters e actions.
2.  **`src/services/*.ts`**:
    *   Verificar arquivos com cobertura < 100% (`mapaService.ts`, `painelService.ts`, `usuarioService.ts`).
    *   Cobrir tratamento de respostas vazias ou malformadas.

### Passo 2: Mappers e Utils (Funções Puras)
São os testes mais fáceis e rápidos de corrigir.
1.  **`src/mappers/*.ts`**:
    *   Criar casos de teste com objetos de entrada variados (nulos, arrays vazios, dados parciais) para cobrir todos os branches de mapeamento.
2.  **`src/utils/index.ts`**:
    *   Identificar funções utilitárias não utilizadas e removê-las ou testar os branches de borda.

### Passo 3: Componentes UI (Complexidade Média)
Focar em interações do usuário e estados visuais.
1.  **`AdicionarMapaModal.vue`, `Confirmacao.vue`, `SeletorUnidades.vue`**:
    *   Simular props de entrada variadas.
    *   Disparar eventos de clique e verificar `emitted()`.
    *   Testar slots vazios vs. preenchidos.

### Passo 4: Views (Alta Complexidade)
Estas views possuem muita lógica de integração.
1.  **`CadAtividades.vue` e `ValidarAtividades.vue`**:
    *   Mockar `useRouter`, `useRoute` e stores do Pinia.
    *   Testar fluxos de sucesso (preenchimento correto) e erro (validação falhando).
    *   Verificar o comportamento ao carregar dados iniciais (hooks `onMounted`).
2.  **`NotificacoesView.vue`**:
    *   Testar paginação e filtros.
    *   Verificar renderização de listas vazias.

### Passo 5: Router e Configurações Globais
1.  **`src/router/index.ts`**:
    *   Testar navigation guards (`beforeEach`) simulando usuários autenticados e não autenticados.
    *   Verificar redirecionamentos 404.

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
