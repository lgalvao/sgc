# SGC - Sistema de Gestão de Competências

Última atualização: 2025-12-04 14:18:38Z

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5.x-green.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9.x-blue.svg)](https://www.typescriptlang.org/)

Sistema para gerenciar sistematicamente as competências técnicas das unidades organizacionais do TRE-PE, incluindo
mapeamento, revisão e diagnóstico de competências.

---

## Visão Geral

O SGC permite:

- **Mapeamento de Competências**: Coleta sistemática de atividades e conhecimentos de cada unidade operacional
- **Revisão Periódica de Competências**: Atualização dos mapas de competencias
- **Diagnóstico de Competências e Ocupações Críticas**: Avaliação de importância e domínio das competências,
  identificando gaps

---

## Arquitetura

### Stack Tecnológico

**Backend:**

- Java 25
- Spring Boot 3.5.x
- JPA/Hibernate
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
├── regras/             # Regras de negócio / políticas
├── node_modules/       # Dependências do frontend (não versionar alterações)
├── test-results/       # Resultados de testes e logs (E2E)
├── .idea/ .vscode/     # Configs de IDE (opcionais)
├── build.gradle.kts    # Build raiz (multi-projeto)
└── AGENTS.md           # Guia para agentes de IA
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

## Detalhamento técnico (gerado em 2025-12-04T14:22:48Z)

Resumo detalhado dos artefatos, comandos e observações técnicas gerado automaticamente.
