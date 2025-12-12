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
- Arquitetura: Em camadas, estruturada por domínio

**Frontend:**

- Vue.js 3.5 + TypeScript
- Vite (build)
- Pinia (estado)
- Vue Router (rotas)
- BootstrapVueNext
- Axios (cliente http)

**Testes:**

- JUnit (testes unitários do backend)
- Vitest (testes unitários do frontend)
- Playwright (testes end-to-end)

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

`
text
sgc/
├── backend/              # API REST baseada em Spring Boot
│   ├── src/main/java/sgc/
│   │   ├── processo/     # Orquestrador dos fluxos de negócio (Mapeamento, Revisão, Diagnóstico)
│   │   ├── subprocesso/  # Workflow de cada unidade dentro de um processo
│   │   ├── mapa/         # Gestão dos mapas de competências
│   │   ├── atividade/    # CRUD de atividades e conhecimentos
│   │   ├── analise/      # Trilha de auditoria
│   │   ├── notificacao/  # Envio de notificações por e-mail
│   │   ├── alerta/       # Alertas exibidos na interface
│   │   ├── painel/       # Endpoints para os dashboards
│   │   ├── sgrh/         # Integração com SGRH (usuários, perfis, unidades)
│   │   ├── unidade/      # Representação da estrutura organizacional
│   │   └── comum/        # Componentes compartilhados (DTOs, exceções)
│   └── src/main/resources/
│       ├── application.yml         # Config padrão (PostgreSQL)
│       └── application-e2e.yml     # Config para testes end-to-end (H2)
│
├── frontend/             # Aplicação Vue.js
│   ├── src/
│      ├── components/   # Componentes reutilizáveis (Vue)
│      ├── views/        # Páginas da aplicação (Vue)
│      ├── stores/       # Módulos de estado (Pinia)
│      ├── services/     # Comunicação com a API (Axios)
│      ├── router/       # Configuração de rotas (Vue Router)
│      ├── composables/  # Funções reutilizáveis (Composition API)
│      ├── mappers/      # Mapeamento de DTOs
│      ├── utils/        # Funções utilitárias
│      ├── constants/    # Constantes e enums
│      ├── types/        # Tipos e interfaces (TypeScript)
│      └── test-utils/   # Utilitários para testes
├── reqs/                 # Documentação de requisitos
│   ├── cdu-01.md         # Caso de uso 01: Login
│   ├── cdu-02.md         # Caso de uso 02: Criar processo
│   ├── ...               # 21 casos de uso documentados
│   └── _intro.md
│
├── build.gradle.kts      # Build raiz (multi-projeto)
└── AGENTS.md             # Guia para agentes de IA
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

- **[AGENTS.md](AGENTS.md)**: Guia para agentes de IA trabalhando no projeto
- **[backend/README.md](backend/README.md)**: Arquitetura detalhada do backend com diagramas Mermaid
- **[frontend/README.md](backend/README.md)**: Arquitetura detalhada do frontend com diagramas Mermaid
- **[reqs/](reqs/)**: 21 casos de uso documentados (CDU-01 a CDU-21)

### Especificação OpenAPI e Swagger

- <http://localhost:10000/swagger-ui.html>
- <http://localhost:10000/api-docs>