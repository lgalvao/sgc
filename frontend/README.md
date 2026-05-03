# Frontend SGC

Este diretório contém o código-fonte do frontend da aplicação SGC (Sistema de Gestão de Competências).

## 🚀 Como executar

Para iniciar o servidor de desenvolvimento:

```bash
cd frontend
pnpm install
pnpm run dev
```

A aplicação estará disponível em `http://localhost:5173`.

## Arquitetura e Tecnologias

O frontend utiliza uma arquitetura baseada em componentes com **Vue.js 3** e **TypeScript**.

* **Framework:** Vue.js 3.5 (Composition API, `<script setup>`)
* **Linguagem:** TypeScript 5.9
* **Estado:** Pinia (Setup stores)
* **Roteamento:** Vue Router (modularizado)
* **UI:** BootstrapVueNext + Bootstrap 5
* **Build:** Vite 7
* **HTTP:** Axios (com interceptors para JWT)
* **Testes:** Vitest (unitários) + Playwright (E2E)
* **Qualidade:** ESLint + OXLint + TypeScript (typecheck)

### Fluxo de Dados

1. **Views (`src/views`)**: Componentes de página. Disparam ações.
2. **Stores (`src/stores`)**: Gerenciam o estado reativo (Pinia). Chamam os services.
3. **Services (`src/services`)**: Camada de abstração da API. Fazem requisições HTTP.
4. **Backend**: API REST Spring Boot.

## Estrutura de Pastas principais

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
pnpm run test:unit
```

### Type check

```bash
pnpm run typecheck
```

### Linting

```bash
pnpm run lint
```

### Qualidade completa

Para executar testes, linting e typecheck de uma vez:

```bash
pnpm run quality:all
```

### E2E (Playwright)

```bash
pnpm run test:e2e
```

## Autenticação

A autenticação é feita via **JWT**. O token é armazenado no `localStorage` e injetado automaticamente pelo `axios-setup.ts`.

## Builds

| Comando | Destino |
|---|---|
| `pnpm run build` | Build padrão (desenvolvimento) |
| `pnpm run build:hom` | Build para homologação (habilita widget de feedback) |
| `pnpm run build:prod` | Build para produção |

Para convenções de código, veja o arquivo **[AGENTS.md](../AGENTS.md)** na raiz do projeto.