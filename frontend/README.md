# Frontend SGC

Este diretório contém o código-fonte do frontend da aplicação SGC (Sistema de Gestão de Competências). Este documento serve como um guia para desenvolvedores, detalhando a arquitetura, as tecnologias e as convenções do projeto.

## Tecnologias

-   **Framework:** [Vue.js](https://vuejs.org/) 3 (usando a Composition API)
-   **Linguagem:** [TypeScript](https://www.typescriptlang.org/)
-   **Build Tool:** [Vite](https://vitejs.dev/)
-   **Gerenciador de Estado:** [Pinia](https://pinia.vuejs.org/)
-   **Roteamento:** [Vue Router](https://router.vuejs.org/)
-   **Estilização:** [BootstrapVueNext](https://bootstrap-vue-next.github.io/bootstrap-vue-next/) (baseado em [Bootstrap](https://getbootstrap.com/) 5) e [Bootstrap Icons](https://icons.getbootstrap.com/)
-   **Testes:** [Vitest](https://vitest.dev/)
-   **Cliente HTTP:** [Axios](https://axios-http.com/)

## Primeiros Passos

### Pré-requisitos

-   [Node.js](https://nodejs.org/) (versão 20 ou superior)
-   [npm](https://www.npmjs.com/)

### Instalação

1.  A partir do diretório `frontend`, instale as dependências:
    ```bash
    npm install
    ```

2.  **Configuração do Ambiente:**
    Crie um arquivo `.env` na raiz do diretório `frontend`. Este arquivo é necessário para configurar a URL da API do backend.
    ```env
    VITE_SGC_API_URL=http://localhost:8080
    ```

### Executando a Aplicação

Para iniciar o servidor de desenvolvimento com hot-reload, execute:

```bash
npm run dev
```

A aplicação estará disponível em `http://localhost:5173`.

## Arquitetura

O frontend segue uma arquitetura de camadas bem definida, projetada para separação de responsabilidades e manutenibilidade.

O fluxo de dados e lógica é o seguinte:

1.  **Views (`src/views`):** São os componentes de página, ativados pelo Vue Router. Elas orquestram a exibição de componentes de UI e disparam ações.
2.  **Stores (`src/stores`):** As `Views` interagem com as stores do Pinia para obter estado e invocar ações de negócio (ex: `buscarProcessos()`). As stores são a fonte única de verdade para o estado da aplicação.
3.  **Services (`src/services`):** As ações nas `stores` não fazem chamadas de API diretamente. Em vez disso, elas delegam essa responsabilidade para os `services`. Cada `service` agrupa um conjunto de chamadas de API relacionadas a uma entidade (ex: `processoService`).
4.  **API (Axios):** Os `services` utilizam uma instância configurada do Axios (`src/axios-setup.ts`) para realizar as requisições HTTP para o backend.

### Autenticação e Gerenciamento de Sessão

A autenticação é gerenciada via token JWT. O fluxo é o seguinte:
-   Após o login, o perfil do usuário e o token JWT são armazenados no `localStorage`.
-   O arquivo `axios-setup.ts` configura um **interceptor de requisição** do Axios que anexa automaticamente o token (`Authorization: Bearer ...`) a cada chamada para a API.
-   Um **interceptor de resposta** do Axios verifica se a API retorna um erro `401 Unauthorized`. Se isso acontecer, ele limpa o `localStorage` e redireciona o usuário para a página de login, efetivamente encerrando a sessão.

## Estrutura do Projeto

```
frontend/
├── src/
│   ├── assets/         # Imagens, fontes e outros arquivos estáticos
│   ├── components/     # Componentes Vue reutilizáveis e sem estado direto
│   ├── composables/    # Funções "composables" da Vue para lógica reutilizável
│   ├── constants/      # Constantes da aplicação (ex: chaves de localStorage)
│   ├── mappers/        # Funções para transformar dados entre a API e o frontend
│   ├── router/         # Configuração de rotas (Vue Router)
│   ├── services/       # Camada de serviço para comunicação com a API
│   ├── stores/         # Módulos de estado (Pinia)
│   ├── types/          # Definições de tipos e interfaces TypeScript
│   ├── utils/          # Funções utilitárias genéricas
│   ├── views/          # Componentes de página (associados às rotas)
│   ├── App.vue         # Componente raiz da aplicação
│   ├── axios-setup.ts  # Configuração central do Axios (interceptors)
│   └── main.ts         # Ponto de entrada da aplicação
├── tests/              # Mocks e configurações de teste
└── ...
```

## Gerenciamento de Estado (Pinia)

-   O estado da aplicação é gerenciado pelo Pinia.
-   As `stores` são modulares e organizadas por entidade (ex: `useProcessosStore`, `usePerfilStore`).
-   A `usePerfilStore` é responsável por persistir e ler os dados do usuário do `localStorage`, garantindo que a sessão do usuário seja mantida entre recarregamentos da página.

## Roteamento (Vue Router)

-   As rotas são definidas no diretório `src/router`.
-   A configuração é modular, com rotas separadas em arquivos por funcionalidade (ex: `processo.routes.ts`).
-   Um **guarda de navegação global** (`router.beforeEach` em `src/router/index.ts`) protege as rotas que exigem autenticação, redirecionando usuários não autenticados para a página de login.

## Testes

O projeto utiliza [Vitest](https://vitest.dev/) para testes. A estratégia de testes é dividida em duas categorias:

1.  **Testes de Unidade:**
    -   **Arquivos:** `*.test.ts` ou `*.spec.ts` dentro de `src/`.
    -   **Objetivo:** Testar componentes, funções e stores de forma isolada.
    -   **Comando:** `npm run test:unit`

2.  **Testes de Integração:**
    -   **Arquivos:** `*.integration.test.ts` ou `*.integration.spec.ts`.
    -   **Objetivo:** Testar a interação entre componentes, stores e serviços, simulando fluxos de usuário.
    -   **Comando:** `npm run test:integration`

O arquivo `vitest.setup.ts` configura o ambiente de teste, incluindo mocks globais para `localStorage` e componentes do Vue Router (`RouterLink`, `RouterView`) para garantir um ambiente de teste consistente.

## Estilo de Código e Linting

-   O projeto utiliza [ESLint](https://eslint.org/) com `typescript-eslint` e `eslint-plugin-vue` para garantir um estilo de código consistente.
-   A configuração pode ser encontrada em `eslint.config.js`.
-   Para verificar e corrigir problemas de estilo, execute:
    ```bash
    npm run lint
    ```

## Scripts Disponíveis

-   `npm run dev`: Inicia o servidor de desenvolvimento.
-   `npm run build`: Compila e minifica o código para produção.
-   `npm run preview`: Inicia um servidor local para visualizar a build de produção.
-   `npm run lint`: Executa o linter para análise e correção de código.
-   `npm run typecheck`: Verifica a tipagem do código com TypeScript.
-   `npm run test:unit`: Executa os testes de unidade.
-   `npm run test:integration`: Executa os testes de integração.
-   `npm run coverage`: Gera o relatório de cobertura de testes de unidade.