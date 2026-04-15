# SGC - Sistema de Gestão de Competências

## Visão geral

O **SGC (Sistema de Gestão de Competências)** é uma aplicação corporativa para mapeamento, revisão e diagnóstico de competências organizacionais. O sistema permite que unidades mapeiem suas atividades e conhecimentos necessários, identifiquem gaps de competência e gerenciem o desenvolvimento de suas equipes.

---

## Arquitetura e Stack tecnológico

O projeto segue uma arquitetura **Modular monolith** no backend e **Component-Based** no frontend.

### Stack principal

| Camada       | Tecnologias principais                                                  |
|--------------|-------------------------------------------------------------------------|
| **Backend**  | Java 21, Spring Boot 4.0.5, Hibernate 7, H2 (testes)/Oracle (produção)  |
| **Frontend** | Vue.js 3.5.28, TypeScript 5.9.3, Vite 7.3.1, Pinia 3.0.4, BootstrapVueNext 0.44.0 |
| **Testes**   | JUnit 5, Mockito, Vitest 4.0, Playwright 1.58.2, Storybook 10.3.3       |

**Documentação essencial:**

* **[AGENTS.md](AGENTS.md)**: **Leitura obrigatória** para desenvolvedores e agentes de IA. Contém convenções de código, padrões de projeto e regras fundamentais.
* **[backend/README.md](backend/README.md)**: Arquitetura detalhada do backend, módulos (`mapa`, `processo`, `subprocesso`, `organizacao`, `seguranca`, etc) e comunicação.
* **[frontend/README.md](frontend/README.md)**: Arquitetura do frontend, estrutura de pastas e componentes.
* **[etc/docs/](etc/docs/)**: Guias detalhados sobre [regras de acesso](etc/docs/regras-acesso.md), [E2E](etc/docs/regras-e2e.md) e mais.
cenário real.

---

## Estrutura do Repositório

```text
sgc/
├── backend/            # API REST (Spring Boot 4) - Módulos: mapa, processo, organizacao, etc.
├── frontend/           # Frontend Web (Vue.js 3.5) - Componentes, Stores (Pinia), Services.
├── e2e/                # Testes End-to-End (Playwright) e Fixtures.
├── etc/                # Configurações, requisitos e scripts globais.
    ├── reqs/           # Especificações de requisitos (Casos de Uso e Regras de Negócio).
    ├── docs/           # Documentação técnica e guias de processo.
    ├── scripts/        # Scripts utilitários (.sh e .ps1).   
```

---

## Como executar

### Pré-requisitos

* JDK 21+ (Recomendado 25+)
* Node.js 22+ (Recomendado 25+)

### Desenvolvimento

1. **Stack completa com perfil e2e** (Windows/Linux)

   ```bash
   node e2e/lifecycle.js
   ```

2. **Backend (definir perfil com -PENV)**

    ```bash
    cd backend
    ./gradlew bootRun -PENV=e2e
    ```

   A API estará disponível em `http://localhost:10000`.

3. **Frontend:**

    ```bash
    cd frontend
    npm install
    npm run dev
    ```

   Acesse em `http://localhost:5173`.

---

## Testes e qualidade

O projeto possui uma suite abrangente de testes e verificações de qualidade.

### Execução de Testes

| Tipo                    | Comando (Linux/Bash)                 | Comando (Windows/PS)                 | Descrição                                         |
|-------------------------|--------------------------------------|--------------------------------------|---------------------------------------------------|
| **Todos backend**       | `./gradlew :backend:test`            | `./gradlew :backend:test`            | Suite completa (Unitários + Integração).          |
| **Unitários backend**   | `./gradlew :backend:unitTest`        | `./gradlew :backend:unitTest`        | Apenas testes isolados (Rápido).                  |
| **Integração backend**  | `./gradlew :backend:integrationTest` | `./gradlew :backend:integrationTest` | Apenas fluxos completos (Mais lento).             |
| **Mutação (PIT)**       | `./gradlew :backend:mutationTest`    | `./gradlew :backend:mutationTest`    | Valida qualidade dos testes (PITest).             |
| **Unitários frontend**  | `npm run test:unit --prefix frontend`| `npm run test:unit --prefix frontend`| Vitest para componentes e lógica.                 |
| **End-to-End (E2E)**    | `npm run test:e2e`                   | `npm run test:e2e`                   | Playwright simulando fluxos reais.                |
| **Type check**          | `npm run typecheck`                  | `npm run typecheck`                  | Verificação de erros de tipos (Vue + TS).         |
| **Lint (OXC)**          | `npm run lint:ox`                    | `npm run lint:ox`                    | Verificação ultra-rápida com [OXC](https://github.com/oxc-project/oxc). |
| **Lint (Completo)**     | `npm run lint`                       | `npm run lint`                       | OXC + ESLint (regras complexas e fix).            |

### Verificação de qualidade (Batch)

Para um smoke test mínimo de qualidade na raiz do projeto:

* **Linux/macOS:** `./smoke-test.sh`
* **Windows:** `./smoke-test.ps1`

---

## Documentação de Negócio

Os requisitos do sistema estão documentados no diretório `etc/reqs/`.

* **Casos de Uso (CDUs)**: Especificações detalhadas de cada funcionalidade (01 a 36).