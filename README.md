# SGC - Sistema de Gestão de Competências

Aplicação corporativa para mapeamento, revisão e diagnóstico de competências técnicas das unidades organizacionais do TRE-PE.

**Status:** Stable | **Versão:** 1.0.0

---

## Visão Geral

O **SGC (Sistema de Gestão de Competências)** é uma aplicação corporativa web desenvolvida para o TRE-PE. Tem como objetivo gerenciar as competências técnicas das unidades organizacionais do Tribunal, por meio de três tipos de processos:

- **Mapeamento**: Coleta inicial das atividades e conhecimentos das unidades, gerando o primeiro mapa de competências.
- **Revisão**: Atualização periódica dos mapas de competências vigentes, considerando mudanças organizacionais.
- **Diagnóstico**: Avaliação da importância e domínio das competências pelos servidores, identificando gaps e necessidades de capacitação. *(Módulo em especificação.)*

O sistema opera sobre uma **árvore hierárquica de unidades organizacionais**, cuja raiz virtual é a unidade `ADMIN` (geralmente servida pela SEDOC). Cada processo percorre essa hierarquia, desde as unidades operacionais de ponta até a homologação final pelo perfil ADMIN.

---

## Arquitetura e Stack Tecnológico

O projeto segue uma arquitetura **Modular Monolith** no backend e **Component-Based** no frontend.

| Camada | Tecnologias |
|---|---|
| **Backend** | Java 25, Spring Boot 4, Hibernate 7 / JPA, Bean Validation, Gradle (Kotlin DSL) |
| **Banco de Dados** | Oracle JDBC (`ojdbc11`) em produção; H2 em memória para desenvolvimento e testes |
| **Frontend** | Vue.js 3.5 (Composition API, `<script setup>`), TypeScript 5.9, Vite 7.3, Pinia 3, BootstrapVueNext 0.44, Axios |
| **Autenticação** | JWT (via API de Acesso AD do TRE-PE) |
| **Testes** | JUnit 6 + Mockito (backend), Vitest 4 (frontend unitário), Playwright 1.58 (E2E), Storybook 10 (componentes) |

### Estrutura do Repositório

```text
sgc/
├── backend/            # API REST (Spring Boot 4)
├── frontend/           # SPA (Vue.js 3.5)
├── e2e/                # Testes End-to-End (Playwright)
└── etc/
    ├── reqs/           # Especificações de requisitos (CDUs e Regras de Negócio)
    ├── docs/           # Documentação técnica (regras de acesso, E2E, etc.)
    └── scripts/        # Scripts utilitários
```

---

## Anatomia dos Módulos

### Módulos do Backend

O backend é organizado em pacotes por domínio em `backend/src/main/java/sgc/`:

| Pacote | Responsabilidade |
|---|---|
| `processo` | Ciclo de vida dos processos (Mapeamento, Revisão, Diagnóstico): criação, inicialização, finalização e painel. |
| `subprocesso` | Execução das tarefas por unidade: motor de estados, transições, movimentações e módulo de análise/auditoria. |
| `mapa` | Núcleo do domínio de competências: Mapas, Competências, Atividades e Conhecimentos. |
| `organizacao` | Modelagem organizacional: Usuários, Unidades (com hierarquia e cache), Perfis, Atribuições temporárias. |
| `seguranca` | Controle de acesso centralizado (`SgcPermissionEvaluator`), autenticação JWT e sanitização. |
| `alerta` | Sistema de alertas internos e notificações reativas (SSE). |
| `relatorio` | Geração de documentos e exportações (PDF/Excel). |
| `parametros` | Configurações dinâmicas do sistema (ex: dias de inativação de processo). |
| `comum` | Utilitários, exceções base, modelo base (`EntidadeBase`) e componentes compartilhados. |

Cada módulo expõe uma camada de **Controller → Facade → Service → Repo (JPA)**.

### Estrutura do Frontend

O frontend é uma SPA organizada em `frontend/src/`:

| Pasta | Responsabilidade |
|---|---|
| `views/` | Telas principais da aplicação (uma por caso de uso). |
| `components/` | Componentes reutilizáveis, agrupados por domínio (`processo/`, `mapa/`, etc). |
| `stores/` | Estado global reativo com Pinia (Setup stores). |
| `services/` | Abstração das chamadas HTTP à API REST. |
| `composables/` | Lógica de estado reutilizável (Composition API). |
| `mappers/` | Transformação de dados entre API e View. |
| `types/` | Definições de tipos e DTOs TypeScript. |
| `utils/` | Funções utilitárias (incluindo tratamento de erros e loggers). |
| `router/` | Roteamento modularizado. |

---

## Como Executar

### Pré-requisitos
- JDK 25+
- Node.js 22+ (recomendado 25+)

### Desenvolvimento

**1. Stack completa (backend + frontend) com perfil e2e:**
```bash
node e2e/lifecycle.js
```

**2. Apenas backend (perfil `e2e` ou `local`):**
```bash
cd backend
./gradlew bootRun -PENV=e2e
```
*A API estará disponível em `http://localhost:10000`.*

**3. Apenas frontend:**
```bash
cd frontend
npm install
npm run dev
```
*Acesse em `http://localhost:5173`.*

### Perfis de Backend
| Perfil | Banco | Uso |
|---|---|---|
| `local` (padrão) | H2 em memória | Desenvolvimento rápido |
| `e2e` | H2 em memória + fixtures | Testes automáticos E2E |
| `hom` | Oracle | Homologação |

---

## Especificações de Domínio

### Entidades Principais
- **Processo:** Define o ciclo (`Mapeamento`, `Revisão`, `Diagnóstico`), situação (`Criado`, `Em Andamento`, `Finalizado`) e armazena o snapshot das unidades participantes (`UnidadeProcesso`).
- **Subprocesso:** Instância do processo para uma unidade específica. Possui situação (Motor de Estados) e histórico de `Movimentacao` (origem/destino/data).
- **Mapa, Competência, Atividade e Conhecimento:** O núcleo do mapeamento técnico.
- **Unidade:** Pode ser `Operacional`, `Intermediária`, `Interoperacional` ou `Sem Equipe`.

### Padrões e Convenções de Código
- **Idioma:** Todo o código, comentários e documentação em Português brasileiro.
- **Identificadores:** `codigo` no lugar de `id` para chaves primárias e referências.
- **Camadas:** `Controller`, `Service`, `Repo`, `Dto`, `Mapper`, `Facade`. Exceções usam prefixo `Erro` (ex: `ErroNegocio`).
- **Persistência:** Tabelas em `UPPER_CASE`, colunas em `snake_case`, chaves como `codigo`. Schema do banco: `sgc`.
- **Frontend:** Componentes em `PascalCase`, stores como `use{Nome}Store`, logging com `import { logger } from '@/utils'`.

---

## Comportamentos do Sistema

### Perfis de Usuário e Controle de Acesso
O `SgcPermissionEvaluator` centraliza o acesso em dois eixos:
1. **Hierarquia (Leitura):** Quem vê o que.
   - **ADMIN:** Tudo.
   - **GESTOR:** Sua unidade e subordinadas.
   - **CHEFE/SERVIDOR:** Apenas sua unidade.
2. **Localização (Escrita):** A unidade de destino da última movimentação do subprocesso determina quem tem posse para editá-lo no momento.

### Motor de Estados do Subprocesso
Cada tipo de processo possui uma trilha isolada de estados (`SituacaoSubprocesso`), progredindo de `Cadastro em andamento` até `Homologado/Concluído`, exigindo ações da unidade de ponta, gestores intermediários e admin.

### Alertas (SSE)
Eventos em tempo real notificados por *Server-Sent Events* (`/api/alertas/stream`).
- Servidores comuns veem apenas alertas diretos.
- Chefes/Gestores/Admins veem alertas da sua unidade ou globais aplicáveis.

---

## Testes e Qualidade

| Tipo | Comando |
|---|---|
| **Todos backend** | `./gradlew :backend:test` |
| **Unitários backend** | `./gradlew :backend:unitTest` |
| **Integração backend** | `./gradlew :backend:integrationTest` |
| **Mutation backend** | `./gradlew :backend:mutationTest` |
| **Unitários frontend** | `npm run test:unit --prefix frontend` |
| **End-to-End (E2E)** | `npm run test:e2e` |
| **Lint Completo** | `npm run lint` |

**Smoke Test (Verificação rápida):**
- **Linux/macOS:** `./smoke-test.sh`
- **Windows:** `./smoke-test.ps1`

---

## Documentação Essencial

- **[AGENTS.md](AGENTS.md)**: **Leitura obrigatória** para desenvolvedores e agentes de IA. Contém convenções adicionais.
- **[Casos de Uso (CDUs)](etc/reqs/)**: Especificações de negócio do 01 ao 36, regras de negócio e glossário.
- **[Regras de Acesso](etc/docs/regras-acesso.md)**: Especificação completa de permissões.
- **[Regras E2E](etc/docs/regras-e2e.md)**: Guia de arquitetura de testes com Playwright.
