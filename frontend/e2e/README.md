# Diretório `e2e`

Este diretório contém todos os testes _end-to-end_ (E2E) da aplicação, que são executados com o Playwright. O objetivo desses testes é simular o comportamento real do usuário, verificando os fluxos da aplicação de ponta a ponta, desde a interação na interface até a comunicação com o backend (geralmente mockado).

## Estrutura de Pastas

### `cdu/`

Abreviação de "Casos de Uso". Este diretório armazena os testes E2E que validam os principais fluxos de negócio da aplicação, conforme especificado nos documentos de caso de uso. Cada arquivo de teste aqui deve corresponder a um CDU específico (ex: `CDU01-Login.spec.ts`, `CDU08-GerenciarConhecimentos.spec.ts`).

### `geral/`

Contém testes E2E para funcionalidades gerais e componentes transversais que não se encaixam em um único caso de uso. Exemplos incluem:
-   Testes de navegação básica (menus, links).
-   Validação de componentes genéricos (modais de confirmação, notificações).
-   Verificação de páginas institucionais ou de ajuda.

### `support/`

Armazena código de suporte para os testes, como comandos customizados, configurações e _hooks_ globais. O objetivo é evitar a repetição de código nos arquivos de teste e centralizar a lógica de setup. Por exemplo, pode conter um comando para realizar o login do usuário que é reutilizado em vários testes.

### `utils/`

Inclui funções utilitárias específicas para o ambiente de testes E2E. Diferente de `support/`, que contém comandos que interagem com a aplicação (ex: `cy.login()`), `utils/` contém lógica pura que auxilia na criação de dados de teste, manipulação de strings/números ou outras tarefas auxiliares.

### `visual/`

Contém os testes de regressão visual. Estes testes tiram _screenshots_ (capturas de tela) de páginas e componentes e os comparam com versões de referência previamente aprovadas. Qualquer diferença visual não esperada (ex: um botão que mudou de cor, um alinhamento quebrado) fará o teste falhar, ajudando a detectar regressões de UI de forma automatizada.

## Arquivos de Configuração

-   **`playwright.config.ts`**: Arquivo de configuração principal do Playwright, onde são definidos os navegadores a serem usados, a URL base, timeouts, e outras configurações globais para a execução dos testes.
-   **`global.d.ts`**: Arquivo de declaração de tipos globais para TypeScript, estendendo a funcionalidade do Playwright com tipos customizados, se necessário.