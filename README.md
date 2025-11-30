# SGC - Sistema de Gestão de Competências

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5.x-green.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9.x-blue.svg)](https://www.typescriptlang.org/)

Sistema para gerenciar sistematicamente as competências técnicas das unidades organizacionais do TRE-PE, incluindo mapeamento, revisão e diagnóstico de competências.

---

## 📋 Visão Geral

O SGC permite:

- **Mapeamento de Competências**: Coleta sistemática de atividades e conhecimentos de cada unidade operacional
- **Revisão Periódica**: Atualização dos mapas de competencias
- **Diagnóstico**: Avaliação de importância e domínio das competências, identificando gaps de capacitação
- **Gestão de Processos**: Workflow completo com máquina de estados e trilha de auditoria
- **Notificações**: Alertas visuais e notificações por e-mail sobre evolucoes nos processos e subprocessos

---

## 🏗️ Arquitetura

### Stack Tecnológico

**Backend:**

- Java 21
- Spring Boot 3.5.7
- JPA/Hibernate
- Lombok e MapStruct
- PostgreSQL (produção) / H2 (desenvolvimento e testes)
- Arquitetura: Em camadas, estruturada por domínio

**Frontend:**

- Vue.js 3.5 + TypeScript
- Vite (build)
- Pinia (estado)
- Vue Router (rotas)
- Bootstrap 5
- Axios (cliente http)

**Testes:**

- JUnit 5 (testes unitários do backend)
- Vitest (testes unitários do frontend)

### Estrutura do Projeto

```
sgc/
├── backend/              # API REST baseada em Spring Boot
│   ├── src/main/java/sgc/
│   │   ├── processo/     # Orquestrador dos fluxos de negócio (Mapeamento, Revisão, Diagnóstico)
│   │   ├── subprocesso/  # Máquina de estados para o workflow de cada unidade
│   │   ├── mapa/         # Gestão dos mapas de competências (criação, versionamento)
│   │   ├── atividade/    # CRUD de atividades e conhecimentos
│   │   ├── analise/      # Trilha de auditoria imutável
│   │   ├── notificacao/  # Envio de notificações por e-mail
│   │   ├── alerta/       # Alertas exibidos na interface
│   │   ├── painel/       # Endpoints para os dashboards
│   │   ├── sgrh/         # Integração com SGRH (usuários, perfis, unidades)
│   │   ├── unidade/      # Representação da estrutura organizacional
│   │   ├── util/         # Classes utilitárias
│   │   └── comum/        # Componentes compartilhados (DTOs, exceções)
│   └── src/main/resources/
│       ├── application.yml         # Config padrão (PostgreSQL)
│       └── data.sql             # Dados iniciais para testes
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
│   ├── ...               # Ao toodo, 21 casos de uso documentados
│   └── _informacoes-gerais.md
│
├── build.gradle.kts      # Build raiz (multi-projeto)
└── AGENTS.md             # Guia para agentes de IA
```

---

## 🚀 Quick Start

### Pré-requisitos

- **Java 21** (OpenJDK ou Oracle JDK)
- **Node.js 18+** e npm
- **PostgreSQL 14+** (apenas para produção)

### 1. Clone o Repositório

```bash
git clone https://github.com/lgalvao/sgc.git
cd sgc
```

## 🧪 Testes

### Testes Unitários Backend (JUnit)

```bash
./gradlew :backend:test
```

- Usa perfil `test` automaticamente
- Banco H2 em memória (limpo a cada teste)

### Testes Unitários Frontend (Vitest)

```bash
cd frontend
npm run test:unit
```

## 📊 Perfis Spring

---

## 📐 Domínios de Negócio

### 1. Processo (Orquestrador)

Gerencia o ciclo de vida dos processos de alto nível (Mapeamento, Revisão, Diagnóstico). Publica eventos de domínio para desacoplar módulos.

### 2. Subprocesso (Máquina de Estados)

Gerencia o workflow detalhado de cada unidade organizacional com transições de estado e histórico imutável de movimentações.

### 3. Mapa de Competências

Orquestra criação, cópia e análise de impacto dos mapas. Cada mapa está vinculado a uma unidade e pode ter diferentes situações (ATIVO, ARQUIVADO, etc.).

### 4. Competências, Atividades e Conhecimentos

- **Competência**: Elemento sintetizante (ex: "Desenvolvimento de Software")
- **Atividade**: Ação específica (ex: "Desenvolver APIs REST")
- **Conhecimento**: Saber técnico necessário (ex: "Spring Boot")

### 5. Notificações e Alertas (Reativos)

Sistema orientado a eventos que reage aos eventos de domínio:

- **Alertas**: Visíveis na interface do usuário
- **Notificações**: E-mails assíncronos

---

## 📚 Documentação Adicional

- **[AGENTS.md](AGENTS.md)**: Guia para agentes de IA trabalhando no projeto
- **[backend/README.md](backend/README.md)**: Arquitetura detalhada do backend com diagramas Mermaid
- **[reqs/](reqs/)**: 21 casos de uso documentados (CDU-01 a CDU-21)

### Swagger API

```
http://localhost:10000/swagger-ui.html
http://localhost:10000/api-docs
```
