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

4. **Mapper (`mappers/`)**:
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

## 6. Testes

> [!IMPORTANT]
> Consulte o [Guia de Testes Frontend](frontend-testes.md) para padrões detalhados de testes unitários e de integração
> com Vitest.

## 7. Padrões de Implementação Detalhados

### 7.1. Stores (Pinia) Setup Store Pattern

**Estrutura Padrão:**

```typescript
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Processo } from '@/types/tipos';
import { normalizeError, type NormalizedError } from '@/utils/apiError';
import * as processoService from '@/services/processoService';

export const useProcessosStore = defineStore('processos', () => {
    // Estado reativo
    const processos = ref<Processo[]>([]);
    const isLoading = ref(false);
    const lastError = ref<NormalizedError | null>(null);
    
    // Getters (computed)
    const processosAtivos = computed(() => 
        processos.value.filter(p => p.situacao === 'EM_ANDAMENTO')
    );
    
    // Actions (functions)
    async function buscarProcessos() {
        lastError.value = null;
        isLoading.value = true;
        try {
            processos.value = await processoService.listar();
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }
    
    function clearError() {
        lastError.value = null;
    }
    
    // Retorna estado, getters e actions
    return {
        processos,
        isLoading,
        lastError,
        processosAtivos,
        buscarProcessos,
        clearError
    };
});
```

**Convenções:**

- Use `ref()` para estado reativo
- Use `computed()` para getters derivados
- Funções assíncronas para actions que chamam API
- Sempre gerenciar `isLoading` e `lastError`
- Nomenclatura: `use{Entidade}Store` (plural)
- ID da store: string minúscula (ex: "processos")

### 7.2. Services - Módulos de Funções Puras

**Estrutura Padrão:**

```typescript
import type { Processo, CriarProcessoRequest } from '@/types/tipos';
import apiClient from '@/axios-setup';

export async function listar(): Promise<Processo[]> {
    const response = await apiClient.get<Processo[]>('/processos');
    return response.data;
}

export async function buscarPorCodigo(codigo: number): Promise<Processo> {
    const response = await apiClient.get<Processo>(`/processos/${codigo}`);
    return response.data;
}

export async function criar(request: CriarProcessoRequest): Promise<Processo> {
    const response = await apiClient.post<Processo>('/processos', request);
    return response.data;
}

export async function atualizar(codigo: number, request: AtualizarProcessoRequest): Promise<Processo> {
    const response = await apiClient.post<Processo>(`/processos/${codigo}/atualizar`, request);
    return response.data;
}

export async function excluir(codigo: number): Promise<void> {
    await apiClient.post(`/processos/${codigo}/excluir`);
}
```

**Convenções:**

- Exportar funções nomeadas (não default export)
- Usar `async/await`
- Tipagem explícita dos retornos
- Importar `apiClient` de `@/axios-setup`
- Nomenclatura de arquivo: `{entidade}Service.ts` (camelCase)
- Funções CRUD: `listar`, `buscarPorCodigo`, `criar`, `atualizar`, `excluir`

### 7.3. Mappers - Funções de Transformação

**Estrutura Padrão:**

```typescript
import type { Processo, ProcessoResumo } from '@/types/tipos';

// Idealmente, crie uma interface para o DTO do backend
interface ProcessoBackendDto {
    codigo: number;
    descricao: string;
    // ... outros campos
}

export function mapProcessoDtoToFrontend(dto: ProcessoBackendDto): Processo {
    return {
        ...dto,
        // Transformações específicas se necessário
    };
}

export function mapProcessoToRequest(processo: Processo): CriarProcessoRequest {
    return {
        descricao: processo.descricao,
        dataLimite: processo.dataLimite,
        tipo: processo.tipo
    };
}

// Mapper recursivo para estruturas aninhadas
export function mapUnidadeParticipanteDtoToFrontend(dto: UnidadeParticipanteDto): UnidadeParticipante {
    return {
        ...dto,
        codUnidade: dto.codigo,
        filhos: dto.filhos 
            ? dto.filhos.map(mapUnidadeParticipanteDtoToFrontend) 
            : []
    };
}
```

**Convenções:**

- Funções puras (sem side effects)
- Nomenclatura: `map{Source}To{Target}`
- **Importante:** Evite usar `any` - crie interfaces para os DTOs do backend em `@/types/`
- Uso opcional (quando há transformação real)
- Suporte a estruturas recursivas/aninhadas

### 7.4. Components - Componentes Apresentacionais

**Estrutura Padrão:**

```vue
<script setup lang="ts">
import { computed } from 'vue';

// Props
interface Props {
    processo: Processo;
    mostrarDetalhes?: boolean;
}

const props = defineProps<Props>();

// Emits
interface Emits {
    (e: 'iniciar', codigo: number): void;
    (e: 'editar', codigo: number): void;
}

const emit = defineEmits<Emits>();

// Computed
const badgeClass = computed(() => {
    switch (props.processo.situacao) {
        case 'EM_ANDAMENTO': return 'badge-success';
        case 'FINALIZADO': return 'badge-secondary';
        default: return 'badge-warning';
    }
});

// Methods
function handleIniciar() {
    emit('iniciar', props.processo.codigo);
}
</script>

<template>
    <BCard data-testid="processo-card">
        <BCardTitle>{{ processo.descricao }}</BCardTitle>
        <span :class="badgeClass">{{ processo.situacao }}</span>
        <BButton 
            @click="handleIniciar"
            data-testid="btn-iniciar"
        >
            Iniciar
        </BButton>
    </BCard>
</template>

<style scoped>
.badge-success { background-color: #28a745; }
</style>
```

**Convenções:**

- Use `<script setup lang="ts">`
- Defina interfaces para Props e Emits
- Props são readonly, use `emit` para comunicação
- Use `computed` para lógica derivada
- Sempre adicione `data-testid` para testes
- Prefira componentes BootstrapVueNext
- Style: `scoped` quando necessário

### 7.5. Views - Páginas Inteligentes

**Estrutura Padrão:**

```vue
<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useProcessosStore } from '@/stores/processos';
import ProcessoCard from '@/components/ProcessoCard.vue';

const store = useProcessosStore();

onMounted(async () => {
    await store.buscarProcessos();
});

const processos = computed(() => store.processos);
const isLoading = computed(() => store.isLoading);

async function handleIniciar(codigo: number) {
    await store.iniciarProcesso(codigo);
}
</script>

<template>
    <div class="container">
        <h1>Processos</h1>
        
        <div v-if="isLoading">Carregando...</div>
        
        <div v-else>
            <ProcessoCard
                v-for="processo in processos"
                :key="processo.codigo"
                :processo="processo"
                @iniciar="handleIniciar"
            />
        </div>
    </div>
</template>
```

**Convenções:**

- Carrega dados no `onMounted`
- Usa stores via composables
- Usa `computed` para reatividade
- Delega apresentação para componentes
- Trata eventos de componentes filhos

## 8. Axios Setup e Interceptors

### 8.1. Configuração Centralizada

```typescript
import axios from 'axios';
import router from './router';
import { useFeedbackStore } from '@/stores/feedback';
import { normalizeError, shouldNotifyGlobally, notifyError } from '@/utils/apiError';

const apiClient = axios.create({
    baseURL: 'http://localhost:10000/api',
    headers: {
        'Content-type': 'application/json'
    }
});

// Interceptor de REQUEST: Adiciona JWT token
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Interceptor de RESPONSE: Trata erros globalmente
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        const normalized = normalizeError(error);
        
        // 401: Redireciona para login
        if (normalized.kind === 'unauthorized') {
            const feedbackStore = useFeedbackStore();
            feedbackStore.show('Sessão Expirada', 'Faça login novamente', 'danger');
            router.push('/login');
        }
        
        // Notificação global para erros inesperados
        if (shouldNotifyGlobally(normalized)) {
            notifyError(normalized);
        }
        
        return Promise.reject(error);
    }
);

export default apiClient;
```

**Benefícios:**

- Configuração única para todas as requisições
- JWT automático em todas as chamadas
- Tratamento de erro centralizado
- Logout automático em 401

## 9. Tratamento de Erros

### 9.1. Normalização de Erros

**Arquivo:** `@/utils/apiError.ts`

```typescript
export type NormalizedError = {
    kind: 'network' | 'unauthorized' | 'validation' | 'business' | 'unknown';
    message: string;
    details?: string[];
};

export function normalizeError(error: any): NormalizedError {
    if (!error.response) {
        return { kind: 'network', message: 'Erro de conexão com o servidor' };
    }
    
    const status = error.response.status;
    
    if (status === 401) {
        return { kind: 'unauthorized', message: 'Não autorizado' };
    }
    
    if (status === 400 || status === 422) {
        return {
            kind: 'validation',
            message: error.response.data.mensagem || 'Erro de validação',
            details: error.response.data.erros
        };
    }
    
    if (status === 409) {
        return {
            kind: 'business',
            message: error.response.data.mensagem || 'Erro de negócio'
        };
    }
    
    return {
        kind: 'unknown',
        message: 'Erro inesperado'
    };
}

export function shouldNotifyGlobally(error: NormalizedError): boolean {
    return error.kind === 'network' || error.kind === 'unknown';
}

export function notifyError(error: NormalizedError) {
    const feedbackStore = useFeedbackStore();
    feedbackStore.show('Erro', error.message, 'danger');
}
```

**Padrão de Uso:**

**Na Store:**

```typescript
async function buscar() {
    lastError.value = null;
    try {
        // chamada API
    } catch (error) {
        lastError.value = normalizeError(error);
        throw error; // Re-throw para tratamento local se necessário
    }
}
```

**No Componente:**

```vue
<script setup lang="ts">
const store = useProcessosStore();

async function handleBuscar() {
    await store.buscar();
    
    // Exibir erro inline se houver
    if (store.lastError && store.lastError.kind === 'validation') {
        // Mostrar BAlert inline
    }
}
</script>

<template>
    <BAlert v-if="store.lastError?.kind === 'validation'" variant="danger">
        {{ store.lastError.message }}
    </BAlert>
</template>
```

## 10. Roteamento

### 10.1. Estrutura Modular

**Arquivo:** `@/router/index.ts`

```typescript
import { createRouter, createWebHistory } from 'vue-router';
import { usePerfilStore } from '@/stores/perfil';
import processoRoutes from './processo.routes';
import unidadeRoutes from './unidade.routes';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/login'
        },
        {
            path: '/login',
            name: 'Login',
            component: () => import('@/views/LoginView.vue')
        },
        ...processoRoutes,
        ...unidadeRoutes
    ]
});

// Guard global de autenticação
router.beforeEach((to, from, next) => {
    const perfilStore = usePerfilStore();
    const requiresAuth = to.meta.requiresAuth !== false;
    
    if (requiresAuth && !perfilStore.isAuthenticated) {
        next('/login');
    } else {
        next();
    }
});

export default router;
```

**Módulo de Rotas:** `@/router/processo.routes.ts`

```typescript
export default [
    {
        path: '/processos',
        name: 'Processos',
        component: () => import('@/views/ProcessosView.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/processos/:id',
        name: 'ProcessoDetalhes',
        component: () => import('@/views/ProcessoDetalhesView.vue'),
        meta: { requiresAuth: true }
    }
];
```

**Convenções:**

- Lazy loading com `import()`
- Guards globais para autenticação
- Meta dados para controle de acesso
- Rotas organizadas por domínio

## 11. TypeScript - Tipos e Interfaces

### 11.1. Definição de Tipos

**Arquivo:** `@/types/tipos.ts`

```typescript
export interface Processo {
    codigo: number;
    descricao: string;
    dataCriacao: string;
    dataLimite: string;
    situacao: SituacaoProcesso;
    tipo: TipoProcesso;
}

export type SituacaoProcesso = 
    | 'CRIADO' 
    | 'EM_ANDAMENTO' 
    | 'FINALIZADO';

export type TipoProcesso = 
    | 'MAPEAMENTO' 
    | 'REVISAO' 
    | 'DIAGNOSTICO';

export interface CriarProcessoRequest {
    descricao: string;
    dataLimite: string;
    tipo: TipoProcesso;
}
```

**Convenções:**

- Interfaces para objetos complexos
- Type aliases para unions
- PascalCase para tipos
- Enums como union types (não `enum`)

## 12. Boas Práticas Identificadas

1. **Separation of Concerns:** Camadas bem definidas (View → Store → Service → API)
2. **Single Responsibility:** Cada componente tem responsabilidade única
3. **Composition API:** Uso consistente de `<script setup>`
4. **TypeScript:** Tipagem estrita em todo o código
5. **Error Handling:** Normalização e tratamento centralizado
6. **Reactive State:** Uso correto de `ref` e `computed`
7. **Lazy Loading:** Componentes e rotas carregados sob demanda
8. **Testing:** data-testid em elementos interativos
9. **Accessibility:** Uso de componentes semânticos
10. **Performance:** Memoization com `computed`

## 13. Referências

- **Vue.js 3 Docs:** <https://vuejs.org/>
- **Pinia Docs:** <https://pinia.vuejs.org/>
- **Vue Router Docs:** <https://router.vuejs.org/>
- **Vite Docs:** <https://vitejs.dev/>
- **TypeScript Docs:** <https://www.typescriptlang.org/>
- **BootstrapVueNext:** <https://bootstrap-vue-next.github.io/>
- **Vitest Docs:** <https://vitest.dev/>

### 9.2. Tratamento de Erros de Formulário

Para simplificar o mapeamento de erros de validação (API 422) para campos de formulário, utilize o composable
`useFormErrors`.

**Arquivo:** `@/composables/useFormErrors.ts`

**Exemplo de Uso:**

```typescript
import { useFormErrors } from '@/composables/useFormErrors';

// 1. Inicialize com os nomes dos campos que podem ter erro
const { errors, setFromNormalizedError, clearErrors } = useFormErrors([
  'descricao',
  'dataLimite',
  'atividades'
]);

async function salvar() {
  clearErrors();
  try {
    await store.salvar(dados);
  } catch (error) {
    // 2. Mapeie automaticamente os erros do backend
    setFromNormalizedError(store.lastError);
  }
}
```

**Template:**

```vue
<BFormInput
  v-model="form.descricao"
  :state="errors.descricao ? false : null"
/>
<BFormInvalidFeedback :state="errors.descricao ? false : null">
  {{ errors.descricao }}
</BFormInvalidFeedback>
```