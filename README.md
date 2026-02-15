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

### Documentação Detalhada

A documentação técnica foi desacoplada deste README para facilitar a manutenção e evitar duplicação.

**📚 [DOCUMENTACAO.md](DOCUMENTACAO.md)** - Índice completo de toda a documentação do projeto organizada por categoria.

**Documentação Essencial:**

* **[AGENTS.md](AGENTS.md)**: **Leitura obrigatória** para desenvolvedores e agentes de IA. Contém convenções de código,
  padrões de projeto e regras fundamentais.
* **[backend/README.md](backend/README.md)**: Arquitetura detalhada do backend, módulos e comunicação.
* **[frontend/README.md](frontend/README.md)**: Arquitetura do frontend, estrutura de pastas e componentes.
* **[PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md](PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md)**: Plano consolidado de simplificação e redução de complexidade (incluindo impacto em testes e documentação).

---

## 📂 Estrutura do Repositório

```text
sgc/
├── backend/            # Código-fonte da API REST (Spring Boot)
├── frontend/           # Código-fonte da aplicação Web (Vue.js)
├── e2e/                # Testes End-to-End (Playwright)
├── etc/                # Configurações, requisitos e scripts globais
│   ├── reqs/           # Documentação de requisitos (Casos de Uso)
│   └── scripts/        # Scripts utilitários
└── build.gradle.kts    # Configuração de build raiz
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
| **Type Check (Front)** | `cd frontend && npm run typecheck`   | Verificação estática de tipos TypeScript.        |

### Verificação de Qualidade (Quality Gate)

Para rodar todas as verificações (Checkstyle, PMD, SpotBugs, Testes, Lint, Typecheck) de uma só vez:

```bash
./quality-check.sh
```

Os relatórios são gerados em:

* Backend: `backend/build/reports/`
* Frontend: `frontend/coverage/`

### Guia de Testes JUnit

Para aprender como criar novos testes unitários e de integração seguindo as melhores práticas do projeto, consulte:

* **[guia-testes-junit.md](/backend/etc/regras/guia-testes-junit.md)**: Guia completo com recomendações, exemplos e checklist

---

## 📚 Documentação de Negócio

Os requisitos do sistema estão documentados em casos de uso (CDUs) no diretório `etc/reqs/`.

* **Processo de Mapeamento**: Criação e definição de mapas de competências.
* **Revisão**: Fluxo de aprovação e ajuste de mapas.
* **Diagnóstico**: Avaliação de proficiência e identificação de gaps.

---

## 🤝 Convenções de Contribuição

Todo o código, comentários e documentação devem ser escritos em **Português Brasileiro**.

Para detalhes completos sobre nomenclaturas (Classes, Variáveis, Banco de Dados) e padrões de projeto (Facade, DTO,
Store, Service), consulte o arquivo **[AGENTS.md](AGENTS.md)**.
