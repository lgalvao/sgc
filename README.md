# SGC - Sistema de Gestão de Competências

## Visão geral

O **SGC (Sistema de Gestão de Competências)** é uma aplicação corporativa para mapeamento, revisão e diagnóstico de
competências organizacionais. O sistema permite que unidades mapeiem suas atividades e conhecimentos necessários,
identifiquem gaps de competência e gerenciem o desenvolvimento de suas equipes.

---

## Arquitetura e Stack tecnológico

O projeto segue uma arquitetura **Modular monolith** no backend e **Component-Based** no frontend.

### Stack principal

| Camada       | Tecnologias principais                                              |
|--------------|---------------------------------------------------------------------|
| **Backend**  | Java 21, Spring Boot 4, Hibernate 7, H2 (testes)/Oracle (produção)  |
| **Frontend** | Vue.js 3.5, TypeScript 5.9, Vite 7.3, Pinia 3, BootstrapVueNext 0.4 |
| **Testes**   | JUnit, Mockito, Vitest, Playwright 1.5, Storybook 10                |

**Documentação essencial:**

* **[AGENTS.md](AGENTS.md)**: **Leitura obrigatória** para desenvolvedores e agentes de IA. Contém convenções de código, padrões de projeto e regras fundamentais.
* **[backend/README.md](backend/README.md)**: Arquitetura detalhada do backend, módulos e comunicação.
* **[frontend/README.md](frontend/README.md)**: Arquitetura do frontend, estrutura de pastas e componentes.

---

## Estrutura do Repositório

```text
sgc/
├── backend/            # Código da API REST (Spring Boot 4)
├── frontend/           # Código do frontend Web (Vue.js 3.5)
├── e2e/                # Testes end-to-End (Playwright)
├── etc/                # Configurações, requisitos e scripts globais
    ├── reqs/           # Especificações de requisitos (Casos de Uso)
    └── scripts/        # Scripts utilitários
```

---

## Como executar

### Pré-requisitos

* JDK 21+
* Node.js 25+

### Desenvolvimento

1. **Stack completa com perfil e2e**

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

| Tipo                   | Comando                              | Descrição                                                              |
|------------------------|--------------------------------------|------------------------------------------------------------------------|
| **Todos backend**      | `./gradlew :backend:test`            | Executa suite completa (Unitários + Integração).                       |
| **Unitários backend**  | `./gradlew :backend:unitTest`        | Executa apenas testes isolados (Rápido).                               |
| **Integração backend** | `./gradlew :backend:integrationTest` | Executa apenas fluxos completos (Mais lento).                          |
| **Unitários frontend** | `cd frontend && npm run test:unit`   | Vitest para componentes e lógica.                                      |
| **End-to-End (E2E)**   | `npm run test:e2e`                   | Playwright simulando fluxos reais.                                     |
| **Type check**         | `npm run typecheck`                  | Verificação de erros de tipos para frontend e E2E                      |
| **Lint (OXC)**         | `npm run lint:ox`                    | Verificação ultra-rápida com [OXC](https://github.com/oxc-project/oxc) |
| **Lint (Completo)**    | `npm run lint`                       | Executa OXC seguido de ESLint (para regras complexas)                  |

### Verificação de qualidade

Para rodar todas as verificações (SpotBugs, Testes, Lint, Typecheck) de uma só vez:

```bash
./etc/scripts/quality-check.sh all
```

Os relatórios são gerados em:

* Backend: `backend/build/reports/`
* Frontend: `frontend/coverage/`

### Dashboard de QA

O projeto possui um dashboard de desenvolvimento voltado para **saude de QA**, nao para observabilidade de producao.
Ele consolida testes, cobertura, lint, typecheck e E2E em snapshots centralizados e estaveis.

#### Como atualizar

No Windows/PowerShell:

```powershell
npm run qa:dashboard
```

Perfis disponiveis:

```powershell
powershell -ExecutionPolicy Bypass -File etc/qa-dashboard/scripts/coletar-snapshot.ps1 -Perfil rapido
powershell -ExecutionPolicy Bypass -File etc/qa-dashboard/scripts/coletar-snapshot.ps1 -Perfil frontend
powershell -ExecutionPolicy Bypass -File etc/qa-dashboard/scripts/coletar-snapshot.ps1 -Perfil backend
powershell -ExecutionPolicy Bypass -File etc/qa-dashboard/scripts/coletar-snapshot.ps1 -Perfil completo
```

#### Onde consultar

Os snapshots gerados ficam em:

* `etc/qa-dashboard/runs/<timestamp>/snapshot.json`
* `etc/qa-dashboard/runs/<timestamp>/resumo.md`

Os aliases mais recentes ficam em:

* `etc/qa-dashboard/latest/ultimo-snapshot.json`
* `etc/qa-dashboard/latest/ultimo-resumo.md`

#### Como interpretar

O dashboard deve ser lido a partir dos snapshots centralizados, e nao diretamente dos artefatos transientes de build.

Exemplos de sinais importantes:

* `statusGeral`: semaforo agregado da execucao.
* `indiceSaude`: panorama numerico da saude de QA.
* `verificacoes`: resultado individual por suite.
* `confiabilidade.suitesLentas`: suites mais caras.
* `hotspots`: arquivos ou classes com maior risco por baixa cobertura.

#### Regra importante

Nao use `backend/build/`, `frontend/coverage/`, `playwright-report/` ou `test-results/` como fonte primaria para
analise humana ou automacao do dashboard. Esses caminhos sao apenas insumos transitivos do coletor.

Consulte tambem:

* `etc/docs/dashboard-qa.md`
* `etc/qa-dashboard/config/snapshot.schema.json`

## Documentação de Negócio

Os requisitos do sistema estão documentados em casos de uso (CDUs) no diretório `etc/reqs/`.

* **Mapeamento**: Criação e definição de mapas de competências.
* **Revisão**: Fluxo de aprovação e ajuste de mapas.
* **Diagnóstico**: Avaliação de competências e identificação de gaps (apenas referências breves; requisitos serão incluídos em releases futuros). 
