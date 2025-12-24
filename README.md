# SGC - Sistema de Gestão de Competências

## Visão Geral

O **SGC (Sistema de Gestão de Competências)** é uma aplicação corporativa para mapeamento, revisão e diagnóstico de competências organizacionais. O sistema permite que unidades mapeiem suas atividades e conhecimentos necessários, identifiquem gaps de competência e gerenciem o desenvolvimento de suas equipes.

---

## 🏗️ Arquitetura e Stack Tecnológico

O projeto segue uma arquitetura **Modular Monolith** no backend (com **Spring Modulith 2.0.1**) e **Component-Based** no frontend.

### Stack Principal

| Camada | Tecnologias Principais |
|--------|------------------------|
| **Backend** | Java 21, Spring Boot 4.0.1, Spring Modulith 2.0.1, Hibernate, H2/Oracle |
| **Frontend** | Vue.js 3.5, TypeScript, Vite, Pinia, BootstrapVueNext |
| **Testes** | JUnit 5, Mockito, Vitest, Playwright, ArchUnit, PITest |

### Documentação Detalhada

A documentação técnica foi desacoplada deste README para facilitar a manutenção e evitar duplicação. Consulte os documentos abaixo para detalhes sobre padrões, arquitetura e regras:

* **[AGENTS.md](AGENTS.md)**: **Leitura obrigatória** para desenvolvedores e agentes de IA. Contém convenções de código, padrões de projeto e regras fundamentais.
* **[backend/README.md](backend/README.md)**: Arquitetura detalhada do backend, módulos e comunicação.
* **[frontend/README.md](frontend/README.md)**: Arquitetura do frontend, estrutura de pastas e componentes.
* **[regras/](regras/)**: Diretório contendo guias específicos de padrões (backend, frontend, E2E).

---

## 📂 Estrutura do Repositório

```text
sgc/
├── backend/            # Código-fonte da API REST (Spring Boot)
├── frontend/           # Código-fonte da aplicação Web (Vue.js)
├── e2e/                # Testes End-to-End (Playwright)
├── reqs/               # Documentação de requisitos (Casos de Uso)
├── regras/             # Guias de padrões e convenções
├── scripts/            # Scripts utilitários (captura de telas, quality checks)
└── build.gradle.kts    # Configuração de build raiz
```

---

## 🚀 Como Executar

### Pré-requisitos

* JDK 21
* Node.js 22+

### Desenvolvimento

1. **Backend:**

    ```bash
    ./gradlew bootRun
    ```

    A API estará disponível em `http://localhost:10000`.

2. **Frontend:**

    ```bash
    cd frontend
    npm install
    npm run dev
    ```

    Acesse em `http://localhost:5173`.

---

## 🧪 Testes e Qualidade

O projeto possui uma suite abrangente de testes e verificações de qualidade, incluindo **Mutation Testing** para avaliar a eficácia dos testes.

### Execução de Testes

| Tipo | Comando | Descrição |
|------|---------|-----------|
| **Unitários Backend** | `./gradlew :backend:test` | JUnit 5 com banco em memória (H2). |
| **Unitários Frontend** | `cd frontend && npm run test:unit` | Vitest para componentes e lógica. |
| **End-to-End (E2E)** | `npm run test:e2e` | Playwright simulando fluxos reais. |
| **Type Check (Front)**| `cd frontend && npm run typecheck` | Verificação estática de tipos TypeScript. |
| **Mutation Testing** | `./scripts/run-mutation-tests.sh` | PITest para avaliar qualidade dos testes. |

### Verificação de Qualidade (Quality Gate)

Para rodar todas as verificações (Checkstyle, PMD, SpotBugs, Testes, Lint, Typecheck) de uma só vez:

```bash
./quality-check.sh
```

Os relatórios são gerados em:

* Backend: `backend/build/reports/`
* Frontend: `frontend/coverage/`

### Mutation Testing (PITest)

O projeto utiliza **PITest** para avaliar a qualidade dos testes através de **mutation-based testing**. Esta técnica introduz pequenas mudanças (mutações) no código e verifica se os testes conseguem detectá-las.

#### Executar Mutation Testing

```bash
# Modo rápido (apenas módulos de alta prioridade)
./scripts/run-mutation-tests.sh --quick

# Módulo específico
./scripts/run-mutation-tests.sh --module processo

# Completo (todos os módulos configurados)
./scripts/run-mutation-tests.sh --full
```

#### Relatórios

O relatório HTML é gerado em: `backend/build/reports/pitest/index.html`

**Métricas principais:**

* **Mutation Coverage**: Percentual de mutantes detectados pelos testes (meta: ≥70%)
* **Test Strength**: Eficácia dos testes existentes (meta: ≥0.70)

Para um guia detalhado sobre como interpretar relatórios e matar mutantes, consulte:

* **[MUTATION_TESTING_PLAN.md](MUTATION_TESTING_PLAN.md)**: Plano completo de Mutation Testing

### Guia de Testes JUnit

Para aprender como criar novos testes unitários e de integração seguindo as melhores práticas do projeto, consulte:

* **[guia-testes-junit.md](guia-testes-junit.md)**: Guia completo com recomendações, exemplos e checklist

---

## 📚 Documentação de Negócio

Os requisitos do sistema estão documentados em casos de uso (CDUs) no diretório `reqs/`.

* **Processo de Mapeamento**: Criação e definição de mapas de competências.
* **Revisão**: Fluxo de aprovação e ajuste de mapas.
* **Diagnóstico**: Avaliação de proficiência e identificação de gaps.

---

## 🤝 Convenções de Contribuição

Todo o código, comentários e documentação devem ser escritos em **Português Brasileiro**.

Para detalhes completos sobre nomenclaturas (Classes, Variáveis, Banco de Dados) e padrões de projeto (Facade, DTO, Store, Service), consulte o arquivo **[AGENTS.md](AGENTS.md)**.
