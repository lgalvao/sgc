# Plano de Aumento da Cobertura de Testes - Frontend SGC

Este documento detalha o plano de ação para aumentar a cobertura de testes unitários e de componentes no módulo
`frontend` do projeto SGC, com base na análise do relatório de cobertura gerado.

## Análise Geral da Cobertura

O relatório de cobertura inicial revela que uma parte significativa do código `frontend` possui 0% de cobertura de
testes, especialmente em módulos de rotas, serviços, algumas stores e a maioria das *views* e componentes Vue. Existem
também arquivos com cobertura parcial, que necessitam de mais testes para alcançar um nível adequado.

## Objetivos

* Aumentar a cobertura geral de statements, functions e branches para um mínimo aceitável (ex: 80-90%).
* Garantir que as funcionalidades críticas do sistema estejam cobertas por testes.
* Estabelecer uma cultura de testes no desenvolvimento de novos recursos.

## Plano de Ação Detalhado

A estratégia de aumento da cobertura será dividida em prioridades, começando pelos arquivos mais críticos (0% de
cobertura) e avançando para aqueles com baixa cobertura parcial.

### Prioridade 1: Arquivos com 0% de Cobertura

Estes arquivos não possuem nenhum teste que exercite seu código. A meta é criar testes básicos que cubram as
funcionalidades principais de cada um.

**Módulos de Rotas (`src/router/*.ts` e `src/router/index.ts`)** - **CONCLUÍDO**

* **Arquivos:**
    * `src/router/diagnostico.routes.ts`
    * `src/router/main.routes.ts`
    * `src/router/processo.routes.ts`
    * `src/router/unidade.routes.ts`
    * `src/router/index.ts`
* **Ação:** Criar testes unitários para a configuração das rotas, verificando se os componentes corretos são associados
  a cada caminho e se os metadados das rotas (como `requiresAuth`) estão configurados adequadamente. Testar a
  funcionalidade de guardas de rota (navigation guards), se aplicável.

**Módulos de Serviços (`src/services/*.ts`)**

* **Arquivos:**
    * `src/services/diagnosticoService.ts` - **CONCLUÍDO**
* **Ação:** Escrever testes unitários para cada função do serviço, utilizando mocks para as chamadas HTTP (ex:
  `axios-mock-adapter`). Garantir que os serviços lidam corretamente com sucesso e falha das requisições.

**Componentes e Views Vue (`.vue`)**

* **Arquivos:**
    * `src/components/TabelaMovimentacoes.vue` - **CONCLUÍDO**
    * `src/components/TabelaAlertas.vue` - **CONCLUÍDO**
    * `src/App.vue` - **CONCLUÍDO**
    * `src/views/LoginView.vue` - **CONCLUÍDO**
    * `src/views/PainelView.vue` - **CONCLUÍDO**
    * `src/views/HistoricoView.vue` - **CONCLUÍDO**
    * `src/views/ConfiguracoesView.vue` - **CONCLUÍDO**
    * ... (outros views)
* **Ação:**
    * **Componentes:** Testes de unidade/componente para as `props`, eventos emitidos (`emits`), slots, e a lógica
      interna do script setup.
    * **Views:** Focar em testes de integração (montar o componente e interagir como um usuário) usando
      `vue-test-utils`.

**Utilitários/Mocks (`src/test-utils/mocks/index.ts`, `src/utils/logger.ts`)** - **CONCLUÍDO**

* **Arquivos:**
    * `src/test-utils/mocks/index.ts`
    * `src/utils/logger.ts`
* **Ação:** Criar testes para as funções de mock (se houver lógica complexa) e para o logger, verificando se as
  mensagens são logadas corretamente nos níveis esperados.

### Prioridade 2: Arquivos com Baixa Cobertura Parcial (Média < 70%)

Estes arquivos já possuem algum nível de cobertura, mas ainda há código não testado. O objetivo é complementar os testes
existentes para cobrir as lacunas.

* `src/components/UnidadeTreeItem.vue` (Cobertura Média: 33.33%) - **CONCLUÍDO**
    * **Ação:** Analisar `statementMap` e `branchMap` para identificar linhas e branches não cobertos. Criar testes que
      exercitem esses caminhos de código, especialmente as condições de ramificação.
* `src/mappers/atividades.ts` (Cobertura Média: 54.46%) - **CONCLUÍDO**
    * **Ação:** Garantir que todas as funções de mapeamento estejam cobertas com diferentes cenários de entrada e saída,
      incluindo casos de erro ou dados incompletos.
* `src/components/ImpactoMapaModal.vue` (Cobertura Média: 54.65%) - **CONCLUÍDO**
    * **Ação:** Focar nos fluxos de interação do modal, validação de entrada, e a lógica de processamento do impacto.
* `src/stores/feedback.ts` (Cobertura Média: 65.56%) - **CONCLUÍDO**
    * **Ação:** Escrever testes para todas as actions e getters da store, cobrindo cenários de sucesso, erro e
      diferentes estados.
* `src/services/subprocessoService.ts` (Cobertura Média: 68.18%) - **CONCLUÍDO**
    * **Ação:** Identificar funções não cobertas e criar testes que simulem chamadas a essas funções, mockando as
      dependências.
* `src/components/UnidadeTreeNode.vue` (Cobertura Média: 68.25%) - **CONCLUÍDO**
    * **Ação:** Semelhante ao `UnidadeTreeItem.vue`, focar nas ramificações e na lógica de renderização da árvore de
      nós.
* `src/axios-setup.ts` (Cobertura Média: 70.38%) - **CONCLUÍDO**
    * **Ação:** Testar os interceptors de requisição e resposta, garantindo que a lógica de autenticação (tokens),
      tratamento de erros (ex: redirecionamento para login em 401) e modificação de requisições/respostas esteja
      funcionando conforme o esperado.
* `src/components/TabelaProcessos.vue` (Cobertura Média: 72.51%) - **CONCLUÍDO**
    * **Ação:** Testar a renderização da tabela, ordenação, paginação (se houver), e interações com as linhas ou ações
      dos processos.
* `src/stores/processos.ts` (Cobertura Média: 76.93%) - **CONCLUÍDO**
    * **Ação:** Expandir os testes existentes para cobrir mais actions, getters e estados da store, especialmente
      aqueles que interagem com o `processoService`.

## Estratégias e Ferramentas

* **Framework de Testes:** Vitest
* **Biblioteca de Testes de Componentes Vue:** `@vue/test-utils`
* **Mocks de HTTP:** `axios-mock-adapter` ou `msw` (se já estiver em uso ou for fácil de integrar).
* **Mocks de Stores:** `pinia` (mockar stores diretamente, como feito em `HistoricoAnaliseModal.spec.ts`).
* **Foco em BDD/TDD:** Sempre que possível, adotar uma abordagem de Behavior-Driven Development (BDD) ou Test-Driven
  Development (TDD) para garantir que os testes sejam escritos antes ou em conjunto com o código de produção.
* **Revisão de Código:** Incluir a cobertura de testes como um item de revisão de código para garantir a
  sustentabilidade do esforço.

## Monitoramento

* Manter o relatório de cobertura atualizado e revisá-lo regularmente.
* Integrar checks de cobertura no pipeline de CI/CD para evitar regressões na cobertura.

Este plano servirá como guia para o aumento gradual e sistemático da cobertura de testes, visando a melhoria da
qualidade e robustez do frontend.
