# Frontend SGC

Última atualização: 2025-12-14

Este diretório contém o código-fonte do frontend da aplicação SGC (Sistema de Gestão de Competências). Este documento
serve como um guia para desenvolvedores, detalhando a arquitetura, as tecnologias e as convenções do projeto.

## Tecnologias

- **Framework:** [Vue.js](https://vuejs.org/) 3.5 (usando a Composition API)
- **Linguagem:** [TypeScript](https://www.typescriptlang.org/)
- **Builds:** [Vite](https://vitejs.dev/)
- **Gerenciamento de estado:** [Pinia](https://pinia.vuejs.org/)
- **Roteamento:** [Vue Router](https://router.vuejs.org/)
- **Componentes de UI:** [BootstrapVueNext](https://bootstrap-vue-next.github.io/)
- **Ícones:** [Bootstrap Icons](https://icons.getbootstrap.com/)
- **Testes unitários:** [Vitest](https://vitest.dev/)
- **Cliente HTTP:** [Axios](https://axios-http.com/)

### Executando a Aplicação

Para iniciar o servidor de desenvolvimento com hot-reload, execute:

```bash
npm run dev
```

A aplicação estará disponível em `http://localhost:5173`.

## Arquitetura

O frontend segue uma arquitetura de camadas bem definida, projetada para separação de responsabilidades e
manutenibilidade. O fluxo de dados e lógica é o seguinte:

1. **Views (`src/views`):** São os componentes de página, ativados pelo Vue Router. Elas orquestram a exibição de
   componentes de UI e disparam ações.
2. **Stores (`src/stores`):** As `Views` interagem com as stores do Pinia para obter estado e invocar ações de negócio (
   ex: `buscarProcessos()`). As stores são a fonte única de verdade para o estado da aplicação.
3. **Services (`src/services`):** As ações nas `stores` não fazem chamadas de API diretamente. Em vez disso, elas
   delegam essa responsabilidade para os `services`. Cada `service` agrupa um conjunto de chamadas de API relacionadas a
   uma entidade (ex: `processoService`).
4. **API (Axios):** Os `services` utilizam uma instância configurada do Axios (`src/axios-setup.ts`) para realizar as
   requisições HTTP para o backend.

### Autenticação e Gerenciamento de Sessão

A autenticação é gerenciada via token JWT. O fluxo é o seguinte:

- Após o login, o perfil do usuário e o token JWT são armazenados no `localStorage`.
- O arquivo `axios-setup.ts` configura um **interceptor de requisição** do Axios que anexa automaticamente o token (
  `Authorization: Bearer ...`) a cada chamada para a API.
- Um **interceptor de resposta** do Axios verifica se a API retorna um erro `401 Unauthorized`. Se isso acontecer, ele
  limpa o `localStorage` e redireciona o usuário para a página de login, efetivamente encerrando a sessão.

## Estrutura do Projeto

```text
frontend/
├── src/
│   ├── components/     # Componentes Vue reutilizáveis e sem estado direto
│   ├── composables/    # Funções "composables" da Vue para lógica reutilizável
│   ├── constants/      # Constantes da aplicação (ex: chaves de localStorage)
│   ├── mappers/        # Funções para transformar dados entre a API e o frontend
│   ├── router/         # Configuração de rotas (Vue Router)
│   ├── services/       # Camada de serviço para comunicação com a API
│   ├── stores/         # Módulos de estado (Pinia)
│   ├── types/          # Definições de tipos e interfaces TypeScript
│   ├── utils/          # Funções utilitárias gerais
│   ├── views/          # Componentes de página (associados às rotas)
│   ├── App.vue         # Componente raiz da aplicação
│   ├── axios-setup.ts  # Configuração central do Axios (interceptors)
│   └── main.ts         # Ponto de entrada da aplicação
└── ...
```

## Gerenciamento de Estado (Pinia)

- O estado da aplicação é gerenciado pelo Pinia.
- As `stores` são modulares e organizadas por entidade (ex: `useProcessosStore`, `usePerfilStore`).
- A `usePerfilStore` é responsável por persistir e ler os dados do usuário do `localStorage`, garantindo que a sessão do
  usuário seja mantida entre recarregamentos da página.

## Roteamento (Vue Router)

- As rotas são definidas no diretório `src/router`.
- A configuração é modular, com rotas separadas em arquivos por funcionalidade (ex: `processo.routes.ts`).
- Um **guarda de navegação global** (`router.beforeEach` em `src/router/index.ts`) protege as rotas que exigem
  autenticação, redirecionando usuários não autenticados para a página de login.

## Testes

O projeto utiliza [Vitest](https://vitest.dev/) para testes. A estratégia de testes é dividida em duas categorias:

1. **Testes de Unidade:**
    - **Arquivos:** `*.test.ts` ou `*.spec.ts` dentro de `src/`.
    - **Objetivo:** Testar componentes, funções e stores de forma isolada.
    - **Comando:** `npm run test:unit`

## Estilo de Código e Linting

- O projeto utiliza [ESLint](https://eslint.org/) com `typescript-eslint` e `eslint-plugin-vue` para garantir um estilo
  de código consistente.
- A configuração pode ser encontrada em `eslint.config.js`.
- Para verificar e corrigir problemas de estilo, execute:

    ```bash
    npm run lint
    ```

## 🛡️ Verificações de Qualidade

O frontend possui scripts dedicados para garantir a qualidade do código e cobertura de testes.

### Ferramentas

- **ESLint**: Análise estática e formatação.
- **vue-tsc**: Verificação de tipos TypeScript.
- **Vitest Coverage**: Relatório de cobertura de testes.

### Como Executar

Para rodar todas as verificações (sem falhar o build em caso de avisos):

```bash
npm run quality:all
```

Para gerar os relatórios de cobertura (já incluído em `quality:all`):

```bash
npm run coverage:unit
```

Os relatórios de cobertura ficam em `coverage/`.

## Scripts Disponíveis

- `npm run dev`: Inicia o servidor de desenvolvimento.
- `npm run lint`: Executa o linter para análise e correção de código.
- `npm run typecheck`: Verifica a tipagem do código com TypeScript.
- `npm run test:unit`: Executa os testes de unidade.
- `npm run quality:all`: Executa verificações de qualidade (testes, lint, typecheck).

## Detalhamento técnico (gerado em 2025-12-14)

Resumo detalhado dos artefatos, comandos e observações técnicas gerado automaticamente.
