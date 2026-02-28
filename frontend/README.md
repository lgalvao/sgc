# Frontend SGC

Este diretório contém o código-fonte do frontend da aplicação SGC (Sistema de Gestão de Competências).

## 🚀 Como Executar

Para iniciar o servidor de desenvolvimento:

```bash
cd frontend
npm install
npm run dev
```

A aplicação estará disponível em `http://localhost:5173`.

## Arquitetura e Tecnologias

O frontend utiliza uma arquitetura baseada em componentes com **Vue.js 3** e **TypeScript**.

* **Framework:** Vue.js 3.5 (Composition API, `<script setup>`)
* **Linguagem:** TypeScript
* **Estado:** Pinia (Setup Stores)
* **Roteamento:** Vue Router (Modularizado)
* **UI:** BootstrapVueNext + Bootstrap 5
* **Build:** Vite
* **HTTP:** Axios (com interceptors para JWT)
* **Testes:** Vitest (Unitários) + Playwright (E2E)
* **Qualidade:** ESLint + TypeScript (typecheck)

### Fluxo de Dados

1. **Views (`src/views`)**: Componentes de página. Disparam ações.
2. **Stores (`src/stores`)**: Gerenciam o estado reativo (Pinia). Chamam os services.
3. **Services (`src/services`)**: Camada de abstração da API. Fazem requisições HTTP.
4. **Backend**: API REST Spring Boot.

## Estrutura de Pastas Principais

* **`src/components/`**: Componentes reutilizáveis, organizados por funcionalidade.
* **`src/composables/`**: Lógica de estado reutilizável (Composition API).
* **`src/mappers/`**: Transformação de dados entre API e View.
* **`src/services/`**: Encapsulamento de chamadas HTTP.
* **`src/stores/`**: Gerenciamento de estado global (Pinia).
* **`src/views/`**: Telas principais da aplicação.
* **`src/utils/`**: Funções utilitárias e auxiliares.
* **`src/types/`**: Definições de tipos e DTOs.
* **`etc/`**: Scripts de automação e documentação adicional.

## Testes e Qualidade

### Unitários (Vitest)

```bash
npm test
```

### Type Check

```bash
npm run typecheck
```

### Linting

```bash
npm run lint
```

### E2E (Playwright)

```bash
npm run test:e2e
```

## Autenticação

A autenticação é feita via **JWT**. O token é armazenado no `localStorage` e injetado automaticamente pelo
`axios-setup.ts`.

## Documentação Adicional

Consulte a pasta `etc/docs/` para guias detalhados sobre:

- [Diretrizes de Design](etc/docs/design-guidelines.md)
- [Padrões do Frontend](etc/docs/frontend-padroes.md)
- [Testes no Frontend](etc/docs/frontend-testes.md)

Para convenções de código, veja o arquivo **[AGENTS.md](../AGENTS.md)** na raiz do projeto.
