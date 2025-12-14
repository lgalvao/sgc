# SGC - Sistema de Gestão de Competências

## Visão Geral

O SGC permite:

- **Mapeamento de Competências**: Coleta sistemática de atividades e conhecimentos de cada unidade operacional
- **Revisão Periódica de Competências**: Atualização dos mapas de competências
- **Diagnóstico de Competências e Ocupações Críticas**: Avaliação de domínio das competências, identificando gaps

---

## Arquitetura

### Stack Tecnológico

**Backend:**

- Java 25
- Spring Boot 4
- Hibernate 7
- Lombok e MapStruct
- PostgreSQL (produção) / H2 (desenvolvimento e testes)
- Arquitetura: Modular Monolith em camadas, estruturada por domínio

**Frontend:**

- Vue.js 3.5 + TypeScript
- Vite (build)
- Pinia (estado com Setup Stores)
- Vue Router (rotas modulares)
- BootstrapVueNext
- Axios (cliente HTTP com interceptors)

**Testes:**

- JUnit 5 + Mockito (testes unitários do backend)
- Vitest (testes unitários do frontend)
- Playwright (testes end-to-end)

### Design Patterns Utilizados

O SGC utiliza diversos design patterns consolidados para garantir manutenibilidade e escalabilidade:

**Backend:**
- **Service Facade Pattern:** Cada módulo tem um serviço principal como ponto de entrada único
- **Repository Pattern:** Abstração de persistência com Spring Data JPA (22 repositórios)
- **DTO Pattern + MapStruct:** Separação entre entidades JPA e objetos de transferência (10 mappers)
- **Event-Driven Architecture:** 23 eventos de domínio para comunicação assíncrona entre módulos
- **Layered Architecture:** Separação clara (Controller → Service → Repository → Entity)
- **Exception Hierarchy:** Tratamento centralizado com `RestExceptionHandler`

**Frontend:**
- **Setup Store Pattern (Pinia):** Gerenciamento de estado reativo com Composition API (12 stores)
- **Service Layer Pattern:** Encapsulamento de chamadas HTTP (12 services)
- **Presentational Components:** Componentes burros com props/emits (24 componentes)
- **Smart Views:** Orquestração de dados e componentes (18 views)
- **Mapper Functions:** Transformação de DTOs (7 mappers)
- **Interceptor Pattern:** Axios interceptors para JWT e tratamento de erros
- **Modular Routing:** Rotas organizadas por domínio

Para detalhes completos sobre os padrões arquiteturais, consulte:
- **Backend:** [`/regras/backend-padroes.md`](regras/backend-padroes.md)
- **Frontend:** [`/regras/frontend-padroes.md`](regras/frontend-padroes.md)
- **Guia para Agentes:** [`AGENTS.md`](AGENTS.md)

### Estrutura do Projeto

```text
sgc/
├── backend/            # API REST (Spring Boot) - código-fonte em backend/src
├── frontend/           # Aplicação Vue.js (frontend/src)
├── e2e/                # Testes end-to-end (Playwright), seed e lifecycle
├── build/              # Artefatos de build locais
├── gradle/             # Scripts e wrapper do Gradle
├── scripts/            # Scripts utilitários do repositório
├── reqs/               # Documentação de requisitos (CDUs)
├── test-results/       # Resultados de testes e logs (E2E)
├── build.gradle.kts    # Build raiz (multi-projeto)
```

### Estrutura Detalhada dos Módulos

**Backend - 15 Módulos:**

```text
sgc/backend/src/main/java/sgc/
├── processo/     # Orquestrador dos fluxos de negócio (Mapeamento, Revisão, Diagnóstico)
├── subprocesso/  # Workflow de cada unidade com máquina de estados
├── mapa/         # Gestão dos mapas de competências
├── atividade/    # CRUD de atividades e conhecimentos
├── diagnostico/  # Diagnóstico de competências e ocupações críticas
├── analise/      # Trilha de auditoria
├── notificacao/  # Envio de notificações por e-mail (reativo)
├── alerta/       # Alertas exibidos na interface (reativo)
├── painel/       # Endpoints para os dashboards
├── sgrh/         # Integração com SGRH (usuários, perfis)
├── unidade/      # Representação da estrutura organizacional
├── comum/        # Componentes compartilhados (exceções, config, base entities)
├── config/       # Configurações específicas (OpenAPI/Swagger)
└── e2e/          # Suporte para testes end-to-end
```

**Comunicação Entre Módulos:**
- **Síncrona:** Chamadas diretas via Service Facades
- **Assíncrona:** 23 eventos de domínio (Spring Events) para desacoplamento

**Frontend - 12 Diretórios:**

```text
sgc/frontend/src/
├── views/        # 18 páginas (componentes inteligentes associados a rotas)
├── components/   # 24 componentes reutilizáveis (apresentacionais)
├── stores/       # 12 stores Pinia (gerenciamento de estado)
├── services/     # 12 services (comunicação com API)
├── router/       # Configuração de rotas modulares
├── mappers/      # 7 mappers (transformação de DTOs)
├── types/        # 50+ tipos e interfaces TypeScript
├── composables/  # Hooks customizados da Composition API
├── utils/        # Funções utilitárias (apiError, formatadores)
├── constants/    # Constantes e enums
└── test-utils/   # Utilitários para testes
```

**Arquitetura Frontend:**
```
View → Store (Pinia) → Service (Axios) → Backend API
  ↑        ↓
Component  Estado Reativo
```

---

## 🧪 Testes

### Testes Unitários Backend (JUnit)

```bash
./gradlew :backend:test
```

- Banco H2 em memória (limpo a cada teste)

### Testes Unitários Frontend (Vitest)

```bash
cd frontend
npm run test:unit
```

### Testes End-to-End (Playwright)

```bash
npm run test:e2e
```

### Captura de Telas para Refinamento de UI

O projeto possui uma suite especial de testes Playwright dedicada a capturar screenshots de todas as telas do sistema
para análise manual e refinamento de UI.

**Quick Start:**
```bash
# 1. Capturar todas as telas
./scripts/capturar-telas.sh

# 2. Visualizar as capturas
./scripts/visualizar-telas.sh
```

**Recursos:**
- 🖼️ 50+ screenshots automáticas organizadas em 8 categorias
- 🔍 Visualizador HTML interativo
- 📱 Capturas em múltiplas resoluções
- 🎯 Scripts para captura por categoria

**Documentação:**
- [screenshots/README.md](screenshots/README.md) - Guia rápido
- [docs/GUIA-CAPTURA-TELAS.md](docs/GUIA-CAPTURA-TELAS.md) - Guia completo

## 🛡️ Verificações de Qualidade

O projeto possui um sistema unificado de verificação de qualidade de código que engloba análise estática, linting e
cobertura de testes para Backend e Frontend.

### Execução Rápida (Recomendado)

Utilize o script wrapper na raiz do projeto:

```bash
./quality-check.sh
```

### Execução via Gradle

Você também pode executar tarefas específicas via Gradle:

- **Tudo (Backend + Frontend)**:

  ```bash
  ./gradlew qualityCheckAll
  ```

- **Apenas Backend** (Checkstyle, PMD, SpotBugs, JaCoCo, Testes):

  ```bash
  ./gradlew qualityCheck
  ```

- **Apenas Frontend** (ESLint, Type Check, Vitest):

  ```bash
  ./gradlew frontendQualityCheck
  ```

### Ferramentas e Relatórios

Os relatórios são gerados em `backend/build/reports/`:

- **Checkstyle**: Estilo de código (Google Checks).
- **PMD**: Boas práticas e código morto.
- **JaCoCo**: Cobertura de testes (`backend/build/reports/jacoco/test/html/index.html`).
- **Frontend**: Relatórios de cobertura em `frontend/coverage/`.

## Domínios de Negócio

### 1. Processo

Gerencia o ciclo de vida dos processos de alto nível (Mapeamento, Revisão, Diagnóstico). Publica eventos de domínio para
desacoplar módulos.

### 2. Subprocesso

Gerencia o workflow detalhado de cada unidade organizacional com transições de estado e histórico de movimentações.

### 3. Mapa de Competências

Cada mapa está vinculado a uma unidade e pode ter diferentes situações.

### 4. Competências, Atividades e Conhecimentos

- **Competência**: Elemento sintetizante (ex: "Desenvolvimento de software administrativo")
- **Atividade**: Ação específica (ex: "Desenvolver APIs REST")
- **Conhecimento**: Conhecimento técnico necessário (ex: "Spring Boot")

### 5. Notificações e Alertas

Serviços orientados a eventos que reage aos eventos de domínio:

- **Alertas**: Visíveis na interface do usuário
- **Notificações**: E-mails assíncronos
- **Movimentaçõees**: Registro de mudanças (auditoria)

---

## 📚 Documentação Adicional

### Documentação Geral e Padrões Arquiteturais

- **[AGENTS.md](AGENTS.md)**: Guia completo para agentes de IA trabalhando no projeto
  - Design Patterns detalhados (Backend e Frontend)
  - Convenções de nomenclatura consolidadas
  - Exemplos de código para cada pattern
  - Princípios arquiteturais do sistema

- **[regras/backend-padroes.md](regras/backend-padroes.md)**: Padrões de Arquitetura e Desenvolvimento Backend
  - Service Facade Pattern
  - Event-Driven Architecture (23 eventos de domínio)
  - Repository Pattern (22 repositórios)
  - DTO + Mapper Pattern (MapStruct - 10 mappers)
  - Hierarquia de Exceções
  - Arquitetura em Camadas
  - Padrões de Persistência JPA
  - Organização de Módulos (15 módulos)
  - Segurança e Sanitização

- **[regras/frontend-padroes.md](regras/frontend-padroes.md)**: Padrões de Arquitetura e Desenvolvimento Frontend
  - Setup Store Pattern (Pinia - 12 stores)
  - Service Layer Pattern (12 services)
  - Component Pattern (24 componentes)
  - Smart Views Pattern (18 views)
  - Mapper Pattern (7 mappers)
  - Axios Interceptors
  - Tratamento de Erros Normalizado
  - Roteamento Modular
  - TypeScript - Tipos e Interfaces (50+ tipos)

- **[regras/e2e_regras.md](regras/e2e_regras.md)**: Regras para testes end-to-end

### Documentação de Arquitetura

- **[backend/README.md](backend/README.md)**: Arquitetura detalhada do backend com diagramas Mermaid
- **[frontend/README.md](frontend/README.md)**: Arquitetura detalhada do frontend com diagramas Mermaid
- **[reqs/](reqs/)**: 21 casos de uso documentados (CDU-01 a CDU-21)

### Documentação de Módulos Backend

Cada módulo backend possui um README.md detalhado em `backend/src/main/java/sgc/<módulo>/`:

- **[processo](backend/src/main/java/sgc/processo/README.md)**: Orquestrador dos fluxos de negócio
- **[subprocesso](backend/src/main/java/sgc/subprocesso/README.md)**: Máquina de estados e workflow
- **[mapa](backend/src/main/java/sgc/mapa/README.md)**: Gestão de mapas de competências
- **[atividade](backend/src/main/java/sgc/atividade/README.md)**: CRUD de atividades e conhecimentos
- **[diagnostico](backend/src/main/java/sgc/diagnostico/README.md)**: Diagnóstico e ocupações críticas
- **[analise](backend/src/main/java/sgc/analise/README.md)**: Trilha de auditoria
- **[notificacao](backend/src/main/java/sgc/notificacao/README.md)**: Sistema de notificações por e-mail
- **[alerta](backend/src/main/java/sgc/alerta/README.md)**: Alertas da interface
- **[painel](backend/src/main/java/sgc/painel/README.md)**: Dashboards
- **[sgrh](backend/src/main/java/sgc/sgrh/README.md)**: Integração com sistema de RH
- **[unidade](backend/src/main/java/sgc/unidade/README.md)**: Estrutura organizacional
- **[comum](backend/src/main/java/sgc/comum/README.md)**: Componentes compartilhados
- **[config](backend/src/main/java/sgc/config/README.md)**: Configurações (OpenAPI)
- **[e2e](backend/src/main/java/sgc/e2e/README.md)**: Suporte para testes E2E

### Documentação de Diretórios Frontend

Cada diretório frontend possui um README.md detalhado em `frontend/src/<diretório>/`:

- **[components](frontend/src/components/README.md)**: Componentes Vue reutilizáveis
- **[views](frontend/src/views/README.md)**: Páginas da aplicação
- **[stores](frontend/src/stores/README.md)**: Gerenciamento de estado (Pinia)
- **[services](frontend/src/services/README.md)**: Comunicação com a API
- **[router](frontend/src/router/README.md)**: Configuração de rotas
- **[composables](frontend/src/composables/README.md)**: Composition API helpers
- **[mappers](frontend/src/mappers/README.md)**: Mapeamento de DTOs
- **[utils](frontend/src/utils/README.md)**: Funções utilitárias
- **[types](frontend/src/types/README.md)**: Tipos e interfaces TypeScript
- **[constants](frontend/src/constants/README.md)**: Constantes da aplicação
- **[test-utils](frontend/src/test-utils/README.md)**: Utilitários para testes

### Especificação OpenAPI e Swagger

- <http://localhost:10000/swagger-ui.html>
- <http://localhost:10000/api-docs>
---

## 📋 Convenções de Código

### Idioma

**Português Brasileiro** é o idioma oficial do projeto. Todo o código (variáveis, métodos, classes, comentários, documentação) deve estar em português, com exceção de termos técnicos consagrados e sufixos de padrões (Controller, Service, etc.).

### Nomenclatura Backend

| Elemento | Convenção | Exemplo |
|----------|-----------|---------|
| Classes | PascalCase | `UsuarioService`, `ProcessoController` |
| Métodos e Variáveis | camelCase | `buscarPorCodigo`, `dataCriacao` |
| Pacotes | lowercase | `sgc.processo`, `sgc.mapa` |
| Exceções | Prefixo `Erro` | `ErroEntidadeNaoEncontrada` |
| Controllers | Sufixo `Controller` | `ProcessoController` |
| Services | Sufixo `Service` | `MapaService` |
| Repositórios | Sufixo `Repo` | `ProcessoRepo` |
| Mappers | Sufixo `Mapper` | `ProcessoMapper` |
| DTOs | Sufixo `Dto`, `Req`, `Resp` | `ProcessoDto`, `CriarProcessoReq` |
| Testes | Sufixo `Test` | `MapaServiceTest` |
| Entidades JPA | Campo PK | `codigo` (não `id`) |

### Nomenclatura Frontend

| Elemento | Convenção | Exemplo |
|----------|-----------|---------|
| Componentes Vue | PascalCase | `ProcessoCard.vue`, `SubprocessoHeader.vue` |
| Arquivos TS | camelCase | `processoService.ts`, `apiError.ts` |
| Stores | `use{Entidade}Store` | `useProcessosStore`, `usePerfilStore` |
| Services | `{entidade}Service.ts` | `processoService.ts` |
| Tipos/Interfaces | PascalCase | `Processo`, `UnidadeParticipante` |
| Diretórios | kebab-case/lowercase | `test-utils`, `components`, `stores` |
| Funções mapper | `map{Source}To{Target}` | `mapProcessoDtoToFrontend` |

### Padrões de Testes

**Backend (JUnit 5):**
```java
@Test
void deveCriarProcessoComSucesso() { ... }

@Test
void deveLancarErroQuandoProcessoNaoEncontrado() { ... }
```

**Frontend (Vitest):**
```typescript
it('deve buscar processos com sucesso', async () => { ... })

it('deve tratar erro ao buscar processos', async () => { ... })
```

### Contadores do Sistema

| Categoria | Quantidade |
|-----------|-----------|
| **Backend** | |
| Módulos | 15 |
| Controllers | 14 |
| Services | 30+ |
| Repositórios | 22 |
| Mappers (MapStruct) | 10 |
| Eventos de Domínio | 23 |
| Exceções Customizadas | 9+ |
| Entidades JPA | 20+ |
| DTOs | 50+ |
| **Frontend** | |
| Stores (Pinia) | 12 |
| Services | 12 |
| Mappers | 7 |
| Components | 24 |
| Views | 18 |
| Types/Interfaces | 50+ |

---

## 🎯 Princípios Arquiteturais

1. **Separation of Concerns:** Cada camada tem responsabilidade única e bem definida
2. **Single Responsibility:** Classes/componentes fazem uma coisa bem feita
3. **DRY (Don't Repeat Yourself):** Código compartilhado em módulos `comum` (backend) ou `utils` (frontend)
4. **KISS (Keep It Simple):** Soluções simples e diretas
5. **Dependency Injection:** Spring IoC (backend), Pinia Stores (frontend)
6. **Event-Driven:** Desacoplamento via eventos de domínio
7. **Fail Fast:** Validações early, exceções específicas
8. **Immutability:** Records para DTOs (backend), computed para getters (frontend)

Para detalhes completos sobre os padrões e práticas, consulte:
- [`AGENTS.md`](AGENTS.md) - Guia completo para desenvolvedores
- [`regras/backend-padroes.md`](regras/backend-padroes.md) - Padrões Backend
- [`regras/frontend-padroes.md`](regras/frontend-padroes.md) - Padrões Frontend
