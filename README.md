# SGC - Sistema de Gestão de Competências

## Visão Geral

O **SGC (Sistema de Gestão de Competências)** é uma aplicação corporativa para mapeamento, revisão e diagnóstico de
competências organizacionais. O sistema permite que unidades mapeiem suas atividades e conhecimentos necessários,
identifiquem gaps de competência e gerenciem o desenvolvimento de suas equipes.

---

## 🏗️ Arquitetura e Stack Tecnológico

O projeto segue uma arquitetura **Modular Monolith** no backend e **Component-Based** no frontend.

### Stack Principal

| Camada       | Tecnologias Principais                                                        |
|--------------|-------------------------------------------------------------------------------|
| **Backend**  | Java 21, Spring Boot 4, Hibernate 7, MapStruct, H2 (testes)/Oracle (produção) |
| **Frontend** | Vue.js 3.5, TypeScript, Vite, Pinia, BootstrapVueNext                         |
| **Testes**   | JUnit, Mockito, Vitest, Playwright                                            |

**Documentação Essencial:**

* **[AGENTS.md](AGENTS.md)**: **Leitura obrigatória** para desenvolvedores e agentes de IA. Contém convenções de código,
  padrões de projeto e regras fundamentais.
* **[backend/README.md](backend/README.md)**: Arquitetura detalhada do backend, módulos e comunicação.
* **[frontend/README.md](frontend/README.md)**: Arquitetura do frontend, estrutura de pastas e componentes.

---

## 📂 Estrutura do Repositório

```text
sgc/
├── backend/            # Código da API REST (Spring Boot 4)
├── frontend/           # Código do frontend Web (Vue.js 3.5)
├── e2e/                # Testes End-to-End (Playwright)
├── etc/                # Configurações, requisitos e scripts globais
    ├── reqs/           # Especificações de requisitos (Casos de Uso)
    └── scripts/        # Scripts utilitários
```

---

## 🚀 Como Executar

### Pré-requisitos

* JDK 21
* Node.js 25+

### Desenvolvimento

1. **Backend (Homologação):**

    ```bash
    cd backend
    ./gradlew bootRun -PENV=hom
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

O projeto possui uma suite abrangente de testes e verificações de qualidade.

### Execução de Testes

| Tipo                   | Comando                              | Descrição                                        |
|------------------------|--------------------------------------|--------------------------------------------------|
| **Todos Backend**      | `./gradlew :backend:test`            | Executa suite completa (Unitários + Integração). |
| **Unitários Backend**  | `./gradlew :backend:unitTest`        | Executa apenas testes isolados (Rápido).         |
| **Integração Backend** | `./gradlew :backend:integrationTest` | Executa apenas fluxos completos (Mais lento).    |
| **Unitários Frontend** | `cd frontend && npm run test:unit`   | Vitest para componentes e lógica.                |
| **End-to-End (E2E)**   | `npm run test:e2e`                   | Playwright simulando fluxos reais.               |
| **Type Check **        | `npm run typecheck`                  | Verificação de erros de tipos.                   |

### Verificação de Qualidade (Quality Gate)

Para rodar todas as verificações (Checkstyle, PMD, SpotBugs, Testes, Lint, Typecheck) de uma só vez:

```bash
./quality-check.sh
```

Os relatórios são gerados em:

* Backend: `backend/build/reports/`
* Frontend: `frontend/coverage/`

## 📚 Documentação de Negócio

Os requisitos do sistema estão documentados em casos de uso (CDUs) no diretório `etc/reqs/`.

* **Processo de Mapeamento**: Criação e definição de mapas de competências.
* **Revisão**: Fluxo de aprovação e ajuste de mapas.
* **Diagnóstico**: Avaliação de proficiência e identificação de gaps.
