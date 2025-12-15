# Guia para Agentes de Desenvolvimento

Este documento estabelece diretrizes e boas práticas para agentes de desenvolvimento que trabalham no projeto SGC. O
objetivo é garantir consistência, eficiência e alinhamento com as convenções do projeto.

## 1. Conhecimento do Projeto

É fundamental que o agente se familiarize com a estrutura e as especificidades de cada módulo do projeto antes de
iniciar qualquer tarefa.

- **Visão Geral:** Consulte o `README.md` na raiz do repositório.
- **Backend:** Cada pacote principal em `backend/src/main/java/sgc/` possui um `README.md` detalhando suas
  responsabilidades, arquitetura e componentes.
- **Frontend:** Cada diretório em `frontend/src/` (components, views, stores, etc.) também contém um `README.md`.

## 2. Regras Gerais de Desenvolvimento

### 2.1. Idioma

- **Português Brasileiro:** Todo o sistema, incluindo nomes de variáveis, métodos, classes, mensagens de erro, logs,
  comentários e documentação, deve estar em Português Brasileiro.

### 2.2. Convenções de Nomenclatura

- **Classes:** PascalCase (ex: `UsuarioService`).
- **Métodos e Variáveis:** camelCase (ex: `buscarPorCodigo`).
- **Exceções:** Prefixo `Erro` (ex: `ErroEntidadeNaoEncontrada`).
- **Repositórios JPA:** Sufixo `Repo` (ex: `SubprocessoRepo`).
- **Serviços:** Sufixo `Service` (ex: `MapaService`).
- **Controladores:** Sufixo `Controller` (ex: `ProcessoController`).
- **Testes:** Sufixo `Test` (ex: `MapaServiceTest`).

## 3. Backend (Java com Spring Boot)

### 3.1. Arquitetura e Design Patterns

O backend segue uma **arquitetura em camadas modular** (Modular Monolith) com 15 módulos de domínio. Para detalhes completos, consulte `/regras/backend-padroes.md`.

#### 3.1.1. Service Facade Pattern

**Descrição:** Cada módulo possui um serviço principal (Fachada) que serve como ponto de entrada único.

**Exemplo:**

```java
@Service
@Transactional
@RequiredArgsConstructor
public class MapaService {
    // Serviços especializados injetados
    private final CopiaMapaService copiaMapaService;
    private final ImpactoMapaService impactoMapaService;
    private final MapaVinculoService mapaVinculoService;
    
    // Método público que orquestra
    public MapaDto criar(CriarMapaReq req) {
        // Orquestra chamadas aos serviços especializados
        Mapa mapa = // criar
        mapaVinculoService.vincular(mapa, req.unidadeCodigo());
        return mapper.toDto(mapa);
    }
}
```

**Regra:** Controller interage APENAS com a fachada, nunca com serviços especializados.

#### 3.1.2. Event-Driven Architecture (Eventos de Domínio)

**Descrição:** Comunicação assíncrona entre módulos via Spring Events para desacoplamento.

**23 Eventos de Domínio identificados** no sistema, incluindo:

- `EventoProcessoIniciado`, `EventoProcessoFinalizado`
- `EventoSubprocessoCadastroAceito`, `EventoSubprocessoCadastroDevolvido`
- `EventoSubprocessoMapaValidado`, `EventoSubprocessoMapaHomologado`

**Exemplo:**

```java
// Publicar evento
@Service
public class ProcessoService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public void iniciar(Long codigo) {
        // lógica de negócio
        eventPublisher.publishEvent(new EventoProcessoIniciado(codigo));
    }
}

// Escutar evento
@Component
public class EventoProcessoListener {
    @EventListener
    @Transactional
    public void onProcessoIniciado(EventoProcessoIniciado evento) {
        alertaService.criar(...);
        notificacaoEmailService.enviar(...);
    }
}
```

**Benefício:** Módulos `notificacao` e `alerta` não são diretamente acoplados ao `processo`.

#### 3.1.3. Repository Pattern (JPA)

**Convenções:**

- Nome: `{Entidade}Repo` (ex: `ProcessoRepo`, `MapaRepo`)
- Extends: `JpaRepository<Entidade, Long>`
- **22 repositórios** identificados no sistema

**Exemplo:**

```java
public interface ProcessoRepo extends JpaRepository<Processo, Long> {
    List<Processo> findBySituacao(SituacaoProcesso situacao);
    
    @Query("SELECT p FROM Processo p WHERE p.dataLimite < :data")
    List<Processo> findProcessosAtrasados(@Param("data") LocalDateTime data);
}
```

#### 3.1.4. DTO Pattern com MapStruct

**Descrição:** Separação entre entidades JPA e objetos de transferência.

**Convenções de Nomenclatura:**

- `{Entidade}Dto`: DTO genérico
- `Criar{Entidade}Req`: Request de criação
- `Atualizar{Entidade}Req`: Request de atualização
- `{Entidade}Resp`: Response específica

**Exemplo de Mapper (MapStruct):**

```java
@Component
@Mapper(componentModel = "spring")
public abstract class ProcessoMapper {
    @Mapping(source = "unidade.codigo", target = "unidadeCodigo")
    public abstract ProcessoDto toDto(Processo processo);
    
    @Mapping(target = "unidade", ignore = true)
    public abstract Processo toEntity(ProcessoDto dto);
}
```

**Regra Absoluta:** NUNCA exponha entidades JPA nos Controllers. Use DTOs.

#### 3.1.5. Hierarquia de Exceções

**Estrutura:**

```
RuntimeException
└── ErroNegocioBase (abstract)
    ├── ErroNegocio (400 Bad Request)
    ├── ErroEntidadeNaoEncontrada (404 Not Found)
    ├── ErroValidacao (400 Bad Request)
    ├── ErroSituacaoInvalida (409 Conflict)
    ├── ErroAccessoNegado (403 Forbidden)
    └── ErroRequisicaoSemCorpo (400 Bad Request)
```

**Uso:**

```java
public Processo buscarPorCodigo(Long codigo) {
    return processoRepo.findById(codigo)
        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));
}
```

**Tratamento:** `RestExceptionHandler` (anotado com `@ControllerAdvice`) converte automaticamente para JSON padronizado.

#### 3.1.6. Camadas da Aplicação

```
Controller → Service Facade → Serviços Especializados → Repository → Entity → Database
    ↓             ↓
   DTO         Mapper
```

**Responsabilidades:**

- **Controller:** Recebe HTTP, valida entrada básica, delega para Service, retorna DTO
- **Service Facade:** Orquestra lógica de negócio, gerencia transações, publica eventos
- **Serviços Especializados:** Lógica de negócio específica
- **Repository:** Acesso a dados
- **Entity:** Modelo de domínio JPA

### 3.2. Padrões de Código Backend

#### 3.2.1. Entidades JPA

**Base Class:**

```java
@MappedSuperclass
public abstract class EntidadeBase implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")  // Note: "codigo" não "id"
    private Long codigo;
}
```

**Convenções:**

- Tabelas: `UPPER_CASE` com schema `sgc`
- Colunas: `snake_case`
- Enums: `@Enumerated(EnumType.STRING)` (legibilidade)
- PK: campo `codigo` (não `id`)

#### 3.2.2. Transações

```java
@Service
@Transactional  // Padrão: read-write para toda a classe
public class ProcessoService {
    
    @Transactional(readOnly = true)  // Otimização para consultas
    public List<Processo> listar() {
        return processoRepo.findAll();
    }
}
```

#### 3.2.3. Logging

```java
@Slf4j
@Service
public class ProcessoService {
    public void iniciar(Long codigo) {
        log.info("Iniciando processo {}", codigo);
        // lógica
        log.debug("Detalhes: {}", detalhes);
    }
}
```

### 3.3. Organização de Módulos

**15 Módulos identificados:**

**Domínio Principal:**

- `processo`: Orquestrador de alto nível
- `subprocesso`: Máquina de estados e workflow
- `mapa`: Gestão de mapas de competências
- `atividade`: CRUD de atividades e conhecimentos
- `diagnostico`: Diagnóstico e ocupações críticas

**Suporte:**

- `analise`: Trilha de auditoria
- `notificacao`: E-mails assíncronos
- `alerta`: Alertas na interface
- `painel`: Dashboards

**Infraestrutura:**

- `comum`: Componentes compartilhados (erros, config, base entities)
- `config`: Configurações (OpenAPI/Swagger)
- `sgrh`: Integração com sistema de RH
- `unidade`: Estrutura organizacional
- `e2e`: Suporte para testes

**Estrutura Interna Padrão:**

```
sgc/{modulo}/
├── {Modulo}Controller.java
├── {Modulo}Service.java
├── README.md
├── dto/
│   ├── {Acao}Req.java
│   └── {Entidade}Dto.java
├── mapper/ (ou mappers em dto/)
│   └── {Entidade}Mapper.java
├── model/
│   ├── {Entidade}.java
│   └── {Entidade}Repo.java
├── service/ (serviços especializados)
├── erros/ (exceções específicas)
└── eventos/ (eventos de domínio)
```

- **DTOs:** Nunca exponha entidades JPA diretamente nos Controllers. Utilize DTOs (`dto/`) e Mappers (`MapStruct`).
- **Pacote Comum:** Utilize o pacote `sgc.comum` para exceções (`ErroApi`), configurações e utilitários compartilhados.

### 3.4. API REST - Convenções Específicas do Projeto

**Importante:** Este projeto usa uma convenção REST não-padrão por escolha de design.

**Verbos HTTP:**

- `GET`: Consultas e listagens (idempotente)
- `POST`: Criação de recursos
- `POST` com sufixo: Atualizações, exclusões e ações de workflow

**Exemplos:**

```
GET    /api/processos              # Listar
GET    /api/processos/{id}         # Buscar um
POST   /api/processos              # Criar
POST   /api/processos/{id}/atualizar    # Atualizar
POST   /api/processos/{id}/excluir      # Excluir
POST   /api/processos/{id}/iniciar      # Ação de workflow
POST   /api/processos/{id}/finalizar    # Ação de workflow
```

**Justificativa:**

- Clareza semântica para ações de workflow
- Facilita auditoria (todas modificações via POST)
- Consistência em toda a API

**Estrutura de Controller:**

```java
@RestController
@RequestMapping("/api/processos")
@RequiredArgsConstructor
public class ProcessoController {
    private final ProcessoService processoService;  // APENAS a fachada
    
    @GetMapping
    public List<ProcessoDto> listar() {
        return processoService.listar();
    }
    
    @PostMapping
    public ResponseEntity<ProcessoDto> criar(@RequestBody CriarProcessoReq req) {
        ProcessoDto dto = processoService.criar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    @PostMapping("/{codigo}/iniciar")
    public void iniciar(@PathVariable Long codigo) {
        processoService.iniciar(codigo);
    }
}
```

**Erros:** Lance exceções da hierarquia de `sgc.comum.erros`. O `RestExceptionHandler` as converterá automaticamente
para respostas JSON padronizadas.

### 3.5. Testes Backend

**Estrutura:**

```java
@SpringBootTest
@Transactional  // Rollback automático após cada teste
class ProcessoServiceTest {
    @Autowired
    private ProcessoService processoService;
    
    @MockBean
    private ProcessoRepo processoRepo;  // Mock de dependências
    
    @Test
    void deveCriarProcessoComSucesso() {
        // Arrange
        CriarProcessoReq req = new CriarProcessoReq("Teste", ...);
        
        // Act
        ProcessoDto resultado = processoService.criar(req);
        
        // Assert
        assertNotNull(resultado.codigo());
        assertEquals(req.descricao(), resultado.descricao());
    }
    
    @Test
    void deveLancarErroQuandoProcessoNaoEncontrado() {
        // Arrange
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(
            ErroEntidadeNaoEncontrada.class,
            () -> processoService.buscarPorCodigo(999L)
        );
    }
}
```

**Convenções:**

- Use **JUnit 5** e **Mockito**
- Banco H2 em memória (automático em testes)
- Nomenclatura: `deve{Acao}Quando{Condicao}`
- `@Transactional` para rollback automático
- Evite criar dados de teste manualmente se puder usar os builders ou factories existentes

**Execução:**

```bash
./gradlew :backend:test
```

## 4. Frontend (Vue.js com TypeScript)

### 4.1. Arquitetura e Fluxo de Dados

O frontend segue uma **arquitetura em camadas unidirecional**. Para detalhes completos, consulte `/regras/frontend-padroes.md`.

**Fluxo:**

```
View → Store (Pinia) → Service (Axios) → API Backend
  ↑        ↓
Component  Estado Reativo
```

**Camadas:**

1. **View (Página):** Componente inteligente associado a rota
   - Carrega dados via Store no `onMounted`
   - Passa dados para componentes via `props`
   - Escuta eventos de componentes filhos

2. **Store (Pinia):** Gerenciamento de estado
   - Fonte única de verdade (Single Source of Truth)
   - Contém lógica de negócio do frontend
   - Gerencia `isLoading` e `lastError`

3. **Service:** Camada de comunicação
   - Funções assíncronas puras
   - Chama API via `apiClient` (axios)
   - Não mantém estado

4. **Mapper (opcional):** Transformação de dados
   - Funções puras de conversão DTO ↔ Model

**Exemplo Completo:**

**Service:**

```typescript
// services/processoService.ts
import type { Processo } from '@/types/tipos';
import apiClient from '@/axios-setup';

export async function listar(): Promise<Processo[]> {
    const response = await apiClient.get<Processo[]>('/processos');
    return response.data;
}

export async function buscarPorCodigo(codigo: number): Promise<Processo> {
    const response = await apiClient.get<Processo>(`/processos/${codigo}`);
    return response.data;
}
```

**Store (Pinia Setup Store):**

```typescript
// stores/processos.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Processo } from '@/types/tipos';
import { normalizeError, type NormalizedError } from '@/utils/apiError';
import * as processoService from '@/services/processoService';

export const useProcessosStore = defineStore('processos', () => {
    // Estado
    const processos = ref<Processo[]>([]);
    const isLoading = ref(false);
    const lastError = ref<NormalizedError | null>(null);
    
    // Getters
    const processosAtivos = computed(() => 
        processos.value.filter(p => p.situacao === 'EM_ANDAMENTO')
    );
    
    // Actions
    async function listar() {
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
    
    return { processos, isLoading, lastError, processosAtivos, listar, clearError };
});
```

**View:**

```vue
<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useProcessosStore } from '@/stores/processos';
import ProcessoCard from '@/components/ProcessoCard.vue';

const store = useProcessosStore();

onMounted(async () => {
    await store.listar();
});

const processos = computed(() => store.processos);
const isLoading = computed(() => store.isLoading);
</script>

<template>
    <div class="container">
        <h1>Processos</h1>
        <div v-if="isLoading">Carregando...</div>
        <div v-else>
            <ProcessoCard 
                v-for="p in processos" 
                :key="p.codigo" 
                :processo="p"
            />
        </div>
    </div>
</template>
```

**Component (Apresentacional):**

```vue
<script setup lang="ts">
import type { Processo } from '@/types/tipos';

interface Props {
    processo: Processo;
}

interface Emits {
    (e: 'iniciar', codigo: number): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>

<template>
    <BCard data-testid="processo-card">
        <BCardTitle>{{ processo.descricao }}</BCardTitle>
        <BButton @click="emit('iniciar', processo.codigo)">
            Iniciar
        </BButton>
    </BCard>
</template>
```

### 4.2. Padrões de Código Frontend

#### 4.2.1. Setup Stores (Pinia)

**Convenção:** Use o estilo "Setup Store" (não Options Store)

```typescript
export const useProcessosStore = defineStore('processos', () => {
    // Estado com ref()
    const processos = ref<Processo[]>([]);
    
    // Getters com computed()
    const total = computed(() => processos.value.length);
    
    // Actions com funções
    async function buscar() { /* ... */ }
    
    // Retornar tudo
    return { processos, total, buscar };
});
```

**Total de Stores:** 12 identificadas

#### 4.2.2. Componentes Vue

**Convenções:**

- Use `<script setup lang="ts">`
- Defina interfaces para Props e Emits
- Props são readonly
- Comunique com pai via `emit`
- Use `computed` para lógica derivada
- Adicione `data-testid` para testes
- Prefira componentes BootstrapVueNext

**Total de Componentes:** 24 identificados

#### 4.2.3. Axios Setup e Interceptors

**Configuração Centralizada:**

```typescript
// axios-setup.ts
import axios from 'axios';
import router from './router';
import { normalizeError } from '@/utils/apiError';

const apiClient = axios.create({
    baseURL: 'http://localhost:10000/api',
    headers: { 'Content-type': 'application/json' }
});

// Interceptor: Adiciona JWT automaticamente
apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Interceptor: Trata erros globalmente
apiClient.interceptors.response.use(
    response => response,
    error => {
        const normalized = normalizeError(error);
        
        // 401: Redireciona para login
        if (normalized.kind === 'unauthorized') {
            router.push('/login');
        }
        
        return Promise.reject(error);
    }
);

export default apiClient;
```

#### 4.2.4. Tratamento de Erros

**Normalização de Erros:**

```typescript
// utils/apiError.ts
export type NormalizedError = {
    kind: 'network' | 'unauthorized' | 'validation' | 'business' | 'unknown';
    message: string;
    details?: string[];
};

export function normalizeError(error: any): NormalizedError {
    if (!error.response) {
        return { kind: 'network', message: 'Erro de conexão' };
    }
    
    const status = error.response.status;
    if (status === 401) return { kind: 'unauthorized', ... };
    if (status === 400) return { kind: 'validation', ... };
    // ...
}
```

**Uso nas Stores:**

- Popule `lastError` em `catch`
- Deixe o componente decidir como exibir (inline vs global)

**Uso nos Componentes:**

```vue
<BAlert v-if="store.lastError?.kind === 'validation'" variant="danger">
    {{ store.lastError.message }}
</BAlert>
```

### 4.3. Roteamento Modular

**Estrutura:**

```typescript
// router/index.ts
import processoRoutes from './processo.routes';
import unidadeRoutes from './unidade.routes';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/login', component: () => import('@/views/LoginView.vue') },
        ...processoRoutes,
        ...unidadeRoutes
    ]
});

// Guard global
router.beforeEach((to, from, next) => {
    const perfilStore = usePerfilStore();
    if (to.meta.requiresAuth && !perfilStore.isAuthenticated) {
        next('/login');
    } else {
        next();
    }
});
```

**Módulo de Rotas:**

```typescript
// router/processo.routes.ts
export default [
    {
        path: '/processos',
        name: 'Processos',
        component: () => import('@/views/ProcessosView.vue'),
        meta: { requiresAuth: true }
    }
];
```

### 4.4. Tecnologias Frontend

- **UI:** Utilize componentes da biblioteca **BootstrapVueNext** (`BButton`, `BModal`, `BCard`, etc.) em vez de HTML/Bootstrap puro quando possível
- **Estado:** Utilize **Pinia** com "Setup Stores" (não Options API)
- **Roteamento:** Vue Router modularizado com lazy loading
- **TypeScript:** Tipagem estrita, evite `any`
- **Composables:** Para lógica reutilizável (`use{Feature}`)

**Total de:**

- Services: 12
- Stores: 12
- Views: 18
- Components: 24
- Mappers: 7

## 5. Testes e Qualidade

### 5.1. Testes Backend

Antes de submeter alterações no **backend**, execute (na raiz):

```bash
./gradlew :backend:test
```

**Convenções:**

- JUnit 5 + Mockito
- Banco H2 em memória
- `@Transactional` para rollback automático
- Nomenclatura: `deve{Acao}Quando{Condicao}`

### 5.2. Testes Frontend

Antes de submeter alterações no **frontend**, execute (dentro de `frontend`):

```bash
npm run typecheck  # Verifica tipagem TypeScript
npm run lint       # Verifica estilo de código
npm run test:unit  # Testes unitários (Vitest)
```

**Convenções para Testes:**

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useProcessosStore } from '@/stores/processos';
import * as processoService from '@/services/processoService';

vi.mock('@/services/processoService');

describe('useProcessosStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });
    
    it('deve buscar processos com sucesso', async () => {
        const mock = [{ codigo: 1, descricao: 'Teste' }];
        vi.mocked(processoService.listar).mockResolvedValue(mock);
        
        const store = useProcessosStore();
        await store.listar();
        
        expect(store.processos).toEqual(mock);
    });
});
```

### 5.3. Tratamento de Erros (Frontend)

- **Normalização:** Ao capturar erros em services/stores, utilize `normalizeError` de `@/utils/apiError` para converter o erro em um formato padrão.
- **Stores:** Evite usar `feedbackStore.show` dentro de blocos `catch` nas Stores. Em vez disso, popule uma variável reativa `lastError` e deixe o componente decidir como exibir o erro (inline vs global).
- **Componentes:** Para erros de validação ou negócio, prefira exibir um `BAlert` inline. Para erros inesperados, o interceptor global já cuidará da notificação via toast.

## 6. Documentação de Referência

### 6.1. Documentação de Arquitetura

Para entendimento profundo dos padrões arquiteturais:

- **Backend Patterns:** `/regras/backend-padroes.md`
  - Design Patterns: Service Facade, Repository, Event-Driven, DTO, Mapper
  - Arquitetura em camadas
  - Padrões de persistência JPA
  - Hierarquia de exceções
  - Organização de módulos
  - Tecnologias e ferramentas

- **Frontend Patterns:** `/regras/frontend-padroes.md`
  - Store Pattern (Pinia Setup Stores)
  - Service Layer
  - Component Pattern (Props/Emits)
  - Tratamento de erros
  - Roteamento modular
  - Axios interceptors

- **E2E Testing:** `/regras/e2e_regras.md`
  - Regras para execução de testes E2E, especialmente para correções do sistema com base nos testes E2E
  - Diagnóstico de falhas
  - Uso de error-context.md

### 6.2. Documentação de Módulos

**Backend:**
Cada módulo em `backend/src/main/java/sgc/{modulo}/` possui um `README.md` detalhado:

- processo, subprocesso, mapa, atividade, diagnostico
- analise, notificacao, alerta, painel
- sgrh, unidade, comum, config, e2e

**Frontend:**
Cada diretório em `frontend/src/` possui um `README.md` detalhado:

- components, views, stores, services
- router, mappers, types, utils, composables
- constants, test-utils

### 6.3. API Documentation

- **Swagger UI:** <http://localhost:10000/swagger-ui.html>
- **OpenAPI Spec:** <http://localhost:10000/api-docs>

## 7. Design Patterns - Resumo Executivo

### 7.1. Backend Patterns

| Pattern | Uso | Exemplo | Benefício |
|---------|-----|---------|-----------|
| **Service Facade** | Ponto de entrada único por módulo | `MapaService` orquestra `CopiaMapaService`, `ImpactoMapaService` | Desacoplamento, testabilidade |
| **Repository** | Abstração de persistência | `ProcessoRepo extends JpaRepository` | Separação de concerns |
| **DTO + Mapper** | Transferência de dados | `ProcessoDto` com `ProcessoMapper (MapStruct)` | Desacoplamento API-Modelo |
| **Event-Driven** | Comunicação assíncrona | `EventoProcessoIniciado` → `EventoProcessoListener` | Desacoplamento entre módulos |
| **Exception Hierarchy** | Tratamento centralizado | `ErroNegocioBase` → subclasses | Respostas consistentes |
| **Layered Architecture** | Separação de responsabilidades | Controller → Service → Repository | Manutenibilidade |

### 7.2. Frontend Patterns

| Pattern | Uso | Exemplo | Benefício |
|---------|-----|---------|-----------|
| **Setup Store** | Gerenciamento de estado | `useProcessosStore` com `ref()`, `computed()` | Composability, reatividade |
| **Service Module** | Comunicação com API | `processoService.listar()` | Separação de concerns |
| **Presentational Components** | UI reutilizável | Props + Emits | Reusabilidade, testabilidade |
| **Smart Views** | Orquestração | View conecta Store e Components | Separação de lógica |
| **Error Normalization** | Tratamento consistente | `normalizeError()` | UX consistente |
| **Axios Interceptors** | Cross-cutting concerns | JWT, error handling | Centralização |
| **Modular Routing** | Organização de rotas | `processo.routes.ts` | Escalabilidade |

### 7.3. Convenções de Nomenclatura

**Backend:**

- Classes: `PascalCase`
- Métodos/Variáveis: `camelCase`
- Pacotes: `lowercase` sem separadores
- Exceções: Prefixo `Erro` (ex: `ErroEntidadeNaoEncontrada`)
- Sufixos: `Controller`, `Service`, `Repo`, `Mapper`, `Test`
- DTOs: `Dto`, `Req`, `Resp`

**Frontend:**

- Components: `PascalCase` (ex: `ProcessoCard.vue`)
- Arquivos TS: `camelCase` (ex: `processoService.ts`)
- Stores: `use{Entidade}Store` (ex: `useProcessosStore`)
- Tipos/Interfaces: `PascalCase`
- Diretórios: `kebab-case` ou `lowercase`
