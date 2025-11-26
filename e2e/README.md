# Testes End-to-End (E2E)

Este diretório contém os testes End-to-End (E2E) da aplicação, que garantem que todas as funcionalidades críticas do sistema funcionem corretamente, simulando a interação de um usuário real com a interface.

## Tecnologias

-   **Framework de Testes:** [Playwright](https://playwright.dev/)
-   **Linguagem:** [TypeScript](https://www.typescriptlang.org/)

## Estrutura do Diretório

A organização dos testes E2E segue uma estrutura lógica para facilitar a manutenção e a localização dos testes:

-   `e2e` (raiz): Contém os arquivos de teste principais, organizados por "Caso de Uso" (CDU). Cada arquivo `.spec.ts` representa um fluxo de usuário ou funcionalidade específica a ser testada.
-   `e2e/helpers/`: Este diretório agrupa funções e dados reutilizáveis para os testes, promovendo a padronização e evitando duplicação de código.
    -   `acoes/`: Funções que realizam ações na interface do usuário (ex: `clicarBotao`, `preencherFormularioLogin`).
    -   `dados/`: Constantes e dados de teste (ex: `USUARIOS`, `SELETORES`, `URLS`).
    -   `navegacao/`: Funções relacionadas à navegação entre páginas.
    -   `verificacoes/`: Funções que realizam asserções e verificações na interface.
-   `e2e/support/`: Contém arquivos de configuração e setup específicos para o ambiente de testes, como extensões do objeto `test` do Playwright.
-   `global.d.ts`: Declarações de tipos globais para o ambiente de testes.

## Como Executar os Testes

### Pré-requisitos

Certifique-se de ter o [Node.js](https://nodejs.org/) e o [npm](https://www.npmjs.com/) instalados.

### Instalação das Dependências

A partir do diretório raiz do projeto, instale as dependências do Playwright:

```bash
npm install
```

### Executando Todos os Testes

Para executar todos os testes E2E em modo headless (sem abrir o navegador):

```bash
npx playwright test
```

### Executando Testes Específicos

Para executar um arquivo de teste específico:

```bash
npx playwright test e2e/cdu/cdu-01.spec.ts
```

Para executar testes que correspondam a um nome específico (case-insensitive):

```bash
npx playwright test --grep "login"
```

### Modo UI do Playwright

Para executar os testes e visualizar a interface do Playwright, que permite inspecionar elementos, depurar e executar testes passo a passo:

```bash
npx playwright test --ui
```

## Convenções de Escrita de Testes

-   **Seletores:** Prefira usar `data-testid` para identificar elementos na interface do usuário, tornando os testes mais robustos a mudanças na estrutura HTML ou CSS.
-   **Organização:** Mantenha os testes organizados por Caso de Uso (`e2e/cdu/`).
-   **Reutilização:** Utilize as funções auxiliares (`helpers/`) para padronizar ações, dados e verificações, evitando a duplicação de código e tornando os testes mais legíveis e fáceis de manter.
-   **`vueTest`:** Utilize o objeto `vueTest` importado de `e2e/support/vue-specific-setup.ts` para testes que necessitem de configurações específicas do ambiente Vue.
