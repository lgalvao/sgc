# Relatório de Cobertura de Testes Frontend

## Visão Geral

A suíte de testes unitários do frontend foi executada com sucesso, com todos os testes passando. No entanto, a cobertura global ainda está abaixo dos limites configurados no projeto.

*   **Total de Testes:** 958
*   **Testes Passaram:** 958 (100%)
*   **Testes Falharam:** 0
*   **Cobertura de Linhas:** 88.16% (Meta: 90%)
*   **Cobertura de Funções:** 83.77% (Meta: 89%)
*   **Cobertura de Statements:** 87.03% (Meta: 90%)

## Análise de Falhas Corrigidas

Durante a verificação inicial, foi identificado e corrigido um teste falho em `src/views/__tests__/ProcessoView.spec.ts`.

*   **Problema:** O teste `deve redirecionar para detalhes da unidade ao clicar na tabela (Gestor)` falhava porque a store `perfilStore` não estava sendo configurada corretamente com a lista de perfis, fazendo com que a computada `isGestor` retornasse falso.
*   **Correção:** O teste foi atualizado para injetar a lista de perfis correta (`perfis: [Perfil.GESTOR]`) além do perfil selecionado, garantindo que a lógica de navegação funcione como esperado.

## Áreas com Baixa Cobertura

As seguintes áreas foram identificadas como críticas devido à baixa cobertura de testes (< 80%):

### 1. Stores
*   **`src/stores/configuracoes.ts`** (37.93% linhas): A lógica de gerenciamento de configurações do sistema está pouco testada.
*   **`src/stores/subprocessos.ts`** (68.42% linhas): Uma das stores mais complexas, responsável pela máquina de estados dos subprocessos, precisa de mais testes nos casos de borda e tratamento de erros.

### 2. Views (Componentes de Página)
*   **`src/views/ConfiguracoesView.vue`** (38.88% linhas): A tela de configurações tem a menor cobertura entre as views, deixando funcionalidades administrativas vulneráveis a regressões.
*   **`src/views/ProcessoHeader.vue`** (68.42% linhas): Componente de cabeçalho com lógica condicional de exibição que não está totalmente coberta.
*   **`src/views/ProcessoView.vue`** (74.82% linhas): Apesar da correção recente, a view principal de processos ainda possui caminhos lógicos não testados, especialmente no tratamento de ações em bloco e erros.
*   **`src/views/MeusProcessos.vue`** (74.07% linhas): A listagem de processos do usuário carece de testes para filtros e estados vazios.
*   **`src/views/ProcessoCards.vue`** (76.59% linhas): Componente de visualização em cards.

### 3. Componentes
*   **`src/components/ModalAcaoBloco.vue`** (73.91% linhas): Componente crítico para operações em lote, precisa de testes mais robustos para garantir que os eventos e estados de erro sejam manipulados corretamente.

## Próximos Passos

Para atingir a meta de cobertura e garantir a corretude do frontend, recomenda-se o seguinte plano de ação:

1.  **Prioridade Alta:** Aumentar a cobertura de `src/stores/subprocessos.ts` para > 85%, focando nas actions que manipulam transições de estado complexas.
2.  **Prioridade Média:** Criar testes para `src/views/ConfiguracoesView.vue` para cobrir pelo menos o fluxo básico de salvamento e validação.
3.  **Prioridade Média:** Reforçar os testes de `src/views/ProcessoView.vue`, cobrindo especificamente os casos de erro nas chamadas de serviço e as condicionais de exibição de botões.
4.  **Prioridade Baixa:** Revisar `src/components/ModalAcaoBloco.vue` para garantir que todos os cenários de interação (sucesso, erro, cancelamento) estejam cobertos.
