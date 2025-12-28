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

## 🏗️ Arquitetura e Tecnologias

O frontend utiliza uma arquitetura baseada em componentes com **Vue.js 3** e **TypeScript**.

*   **Framework:** Vue.js 3 (Composition API, `<script setup>`)
*   **Linguagem:** TypeScript
*   **Estado:** Pinia
*   **Roteamento:** Vue Router
*   **UI:** BootstrapVueNext + Bootstrap 5
*   **Build:** Vite
*   **HTTP:** Axios (com interceptors para JWT)
*   **Testes:** Vitest (Unitários) + Playwright (E2E)

### Fluxo de Dados

1.  **Views (`src/views`)**: Componentes de página. Disparam ações.
2.  **Stores (`src/stores`)**: Gerenciam o estado reativo (Pinia). Chamam os services.
3.  **Services (`src/services`)**: Camada de abstração da API. Fazem requisições HTTP.
4.  **Backend**: API REST Spring Boot.

## 📂 Estrutura de Pastas

*   **`src/stores/`**: Módulos Pinia (ex: `useProcessoStore`). Fonte única da verdade.
*   **`src/services/`**: Encapsulamento de chamadas HTTP (ex: `processoService.ts`).
*   **`src/views/`**: Telas principais da aplicação.
*   **`src/components/`**: Componentes reutilizáveis (botões, modais, cards).
*   **`src/router/`**: Configuração de rotas e guardas de navegação (auth guards).
*   **`src/types/`**: Interfaces e Tipos TypeScript compartilhados.

## 🧪 Testes

### Unitários (Vitest)

Testam componentes, stores e lógica de negócio isolada.

```bash
cd frontend
npm run test:unit
```

### Type Check

Verificação estática de tipos.

```bash
cd frontend
npm run typecheck
```

## 🔒 Autenticação

A autenticação é feita via **JWT**.
*   O token é armazenado no `localStorage`.
*   O arquivo `axios-setup.ts` injeta o token automaticamente em todas as requisições (`Authorization: Bearer ...`).
*   Se a API retornar `401 Unauthorized`, o usuário é redirecionado para o login.

## 🤝 Padrões de Código

Consulte o arquivo **[AGENTS.md](../AGENTS.md)** na raiz para detalhes sobre convenções de nomenclatura e padrões Vue/TypeScript.
