# Padrões de Arquitetura e Desenvolvimento - Frontend SGC

Este documento consolida os padrões de arquitetura, convenções de código e melhores práticas identificadas no código
fonte do frontend do sistema SGC. O objetivo é servir como guia para manter a consistência e qualidade do código.

## 1. Visão Geral e Tecnologias

O frontend do SGC é uma Single Page Application (SPA) construída com tecnologias modernas do ecossistema Vue.js.

- **Framework Principal:** [Vue.js 3.5](https://vuejs.org/) (Composition API via `<script setup>`)
- **Linguagem:** [TypeScript](https://www.typescriptlang.org/)
- **Build Tool:** [Vite](https://vitejs.dev/)
- **Gerenciamento de Estado:** [Pinia](https://pinia.vuejs.org/)
- **Roteamento:** [Vue Router](https://router.vuejs.org/)
- **Componentes UI:** [BootstrapVueNext](https://bootstrap-vue-next.github.io/) (baseado em Bootstrap 5)
- **Comunicação HTTP:** [Axios](https://axios-http.com/)
- **Testes:** [Vitest](https://vitest.dev/) (Unitários)

## 2. Estrutura de Diretórios

A estrutura do projeto (`frontend/src/`) segue uma organização por responsabilidade técnica:

| Diretório      | Responsabilidade                                                                                                                                    |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `views/`       | **Páginas** completas da aplicação. São componentes "inteligentes" associados a rotas. Orquestram o carregamento de dados e a interação do usuário. |
| `components/`  | **Componentes de UI** reutilizáveis e "burros" (agnósticos). Recebem dados via props e comunicam ações via eventos.                                 |
| `stores/`      | **Gerenciamento de Estado**. Contém as stores do Pinia, separadas por domínio (ex: `processos.ts`, `usuarios.ts`).                                  |
| `services/`    | **Camada de Serviço**. Encapsula as chamadas HTTP para o backend.                                                                                   |
| `router/`      | **Configuração de Rotas**. Definições modulares das rotas da aplicação.                                                                             |
| `mappers/`     | **Transformação de Dados**. Funções puras que convertem DTOs da API para interfaces do frontend e vice-versa.                                       |
| `types/`       | **Definições de Tipos**. Interfaces TypeScript que definem os contratos de dados (ex: `Processo`, `Unidade`).                                       |
| `composables/` | **Lógica Reutilizável**. Hooks customizados da Composition API (ex: lógica compartilhada entre componentes).                                        |
| `utils/`       | **Utilitários**. Funções auxiliares genéricas.                                                                                                      |

## 3. Arquitetura e Fluxo de Dados

A arquitetura segue um fluxo unidirecional e em camadas para garantir a separação de responsabilidades.

```mermaid
flowchart LR
    View[View (Página)] -->|Lê Estado/Dispara Ações| Store[Store (Pinia)]
    Store -->|Solicita Dados| Service[Service (Axios)]
    Service -->|Requisicao HTTP| API[Backend API]
    API -->|Resposta DTO| Service
    Service -->|Dados| Store
    Store -->|Estado Atualizado| View
    View -->|Props| Component[Componente UI]
    Component -->|Eventos (emit)| View
```

### Detalhamento das Camadas

1. **View (`views/`)**:
    - Ponto de entrada da rota.
    - Acessa a Store para buscar dados (`onMounted`).
    - Passa dados para componentes filhos via `props`.
    - Ouve eventos de componentes filhos para disparar ações na Store.

2. **Store (`stores/`)**:
    - Fonte única de verdade (Single Source of Truth) para o estado da aplicação.
    - Contém a lógica de negócio do frontend.
    - Gerencia o estado de carregamento (`isLoading`) e erros.

3. **Service (`services/`)**:
    - Abstração sobre o Axios.
    - Não contém estado, apenas métodos assíncronos que retornam Promessas.
    - Usa a instância configurada `apiClient` (`axios-setup.ts`) que gerencia tokens JWT.

4. **Mapper (`mappers/`)** (Opcional, mas recomendado):
    - Usado dentro das Stores ou Services para transformar os dados brutos da API em objetos tipados e formatados para a
      UI.

## 4. Padrões de Implementação

### 4.1. Componentes e Views

- **Nomenclatura:** PascalCase (ex: `ProcessoDetalhes.vue`).
- **Sintaxe:** `<script setup lang="ts">`.
- **Estilo:** Uso de componentes `BootstrapVueNext` (ex: `BCard`, `BButton`, `BModal`) em vez de HTML puro com classes
  Bootstrap, sempre que possível.
- **Testes:** Devem usar `data-testid` para seletores estáveis.

### 4.2. Stores (Pinia)

- **Estilo:** O projeto utiliza o estilo **Setup Stores** (função de setup que retorna o estado, getters e actions).
    - *Exemplo:*
      `export const useProcessosStore = defineStore("processos", () => { const state = ref(...); function action() {...}; return { state, action }; });`
- **Modularidade:** Uma store por domínio/entidade (ex: `useProcessosStore`, `useUnidadesStore`).

### 4.3. Services

- **Padrão:** Módulos que exportam funções assíncronas individuais.
- **Nomenclatura de Arquivo:** camelCase com sufixo `Service` (ex: `processoService.ts`).
- **Cliente HTTP:** Importar `apiClient` de `@/axios-setup`.
- **Tipagem:** Retornos devem ser tipados explicitamente com `Promise<Tipo>`.

### 4.4. Roteamento

- **Modularização:** As rotas não ficam todas em um único arquivo. Elas são divididas por domínio em
  `frontend/src/router/` (ex: `processo.routes.ts`, `unidade.routes.ts`) e importadas no `index.ts`.
- **Lazy Loading:** Views devem ser importadas dinamicamente para otimizar o bundle (`component: () => import(...)`).

## 5. Convenções de Código

### Nomenclatura

- **Diretórios:** kebab-case ou lowercase simples (`components`, `test-utils`).
- **Componentes Vue:** PascalCase (`SubprocessoHeader.vue`).
- **Arquivos TypeScript (stores, services, utils):** camelCase (`processoService.ts`, `formatadores.ts`).
- **Interfaces/Tipos:** PascalCase (`Processo`, `Unidade`).

### TypeScript

- **Tipagem Estrita:** Evitar `any`. Usar interfaces definidas em `frontend/src/types/`.
- **Props de Componentes:** Usar interface genérica com `defineProps<Props>()`.

### 6. Testes Unitários e de Integração (Vitest)

- **Localização:** Arquivos `*.spec.ts` ou `*.test.ts` próximos ao código fonte ou em diretórios `__tests__`.
- **Escopo:**
    - **Unitários:** Testar funções isoladas (utils, mappers) e componentes simples.
    - **Integração:** Testar Stores e Views (montando o componente e mockando serviços/stores).
- **Execução:** `npm run test:unit`.