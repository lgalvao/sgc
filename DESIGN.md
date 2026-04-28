---
id: sgc
title: SGC — Sistema de Gestão de Competências
description: >
  Aplicação corporativa para mapeamento, revisão e diagnóstico de
  competências técnicas das unidades organizacionais do TRE-PE.
status: stable
version: 1.0.0
tags: [sistema, competencias, mapeamento, revisao, diagnostico, trepe]
---

# Visão geral

O **SGC (Sistema de Gestão de Competências)** é uma aplicação corporativa web desenvolvida para o TRE-PE. Tem como objetivo gerenciar as competências técnicas das unidades organizacionais do Tribunal, por meio de três tipos de processos:

- **Mapeamento**: Coleta inicial das atividades e conhecimentos das unidades, gerando o primeiro mapa de competências.
- **Revisão**: Atualização periódica dos mapas de competências vigentes, considerando mudanças organizacionais.
- **Diagnóstico**: Avaliação da importância e domínio das competências pelos servidores, identificando gaps e necessidades de capacitação. *(Módulo em especificação.)*

O sistema opera sobre uma **árvore hierárquica de unidades organizacionais**, cuja raiz virtual é a unidade `ADMIN` (geralmente servida pela SEDOC). Cada processo percorre essa hierarquia, desde as unidades operacionais de ponta até a homologação final pelo perfil ADMIN.

---

# Anatomia

## Módulos do Backend

O backend é um **monólito modular** (Spring Boot 4), organizado em pacotes por domínio em `backend/src/main/java/sgc/`:

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

## Estrutura do Frontend

O frontend é uma **SPA Vue 3** com arquitetura baseada em componentes, organizada em `frontend/src/`:

| Pasta | Responsabilidade |
|---|---|
| `views/` | Telas principais da aplicação (uma por caso de uso). |
| `components/` | Componentes reutilizáveis, agrupados por domínio (`processo/`, `mapa/`, `atividades/`, `unidade/`, `relatorios/`, `comum/`, `layout/`). |
| `stores/` | Estado global reativo com Pinia (Setup stores). |
| `services/` | Abstração das chamadas HTTP à API REST. |
| `composables/` | Lógica de estado reutilizável (Composition API). |
| `mappers/` | Transformação de dados entre API e View. |
| `types/` | Definições de tipos e DTOs TypeScript. |
| `utils/` | Funções utilitárias (incluindo `normalizeError` e logger estruturado). |
| `router/` | Roteamento modularizado (cada módulo possui seu `.routes.ts`). |

## Estrutura do Repositório

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

# Especificações

## Stack Tecnológico

| Camada | Tecnologias |
|---|---|
| **Backend** | Java 25, Spring Boot 4, Hibernate 7 / JPA, Bean Validation, Gradle (Kotlin DSL) |
| **Banco de Dados** | Oracle JDBC (`ojdbc11`) em produção; H2 em memória para desenvolvimento e testes |
| **Frontend** | Vue.js 3.5 (Composition API, `<script setup>`), TypeScript 5.9, Vite 7.3, Pinia 3, BootstrapVueNext, Axios |
| **Autenticação** | JWT (via API de Acesso AD do TRE-PE) |
| **Testes** | JUnit 6 + Mockito (backend), Vitest (frontend unitário), Playwright (E2E), Storybook (componentes) |

## Modelo de Domínio

### Entidades principais

```
Processo
  ├── tipo: MAPEAMENTO | REVISAO | DIAGNOSTICO
  ├── situacao: CRIADO | EM_ANDAMENTO | FINALIZADO
  ├── dataLimite
  └── participantes: List<UnidadeProcesso>  ← snapshot da hierarquia no momento do início

Subprocesso
  ├── processo: Processo
  ├── unidade: Unidade
  ├── situacao: SituacaoSubprocesso (enum — ver Motor de Estados)
  ├── localizacaoAtual: Unidade (cache da última movimentação)
  └── movimentacoes: List<Movimentacao>

Mapa
  ├── subprocesso: Subprocesso
  ├── atividades: Set<Atividade>
  └── competencias: Set<Competencia>

Atividade
  ├── descricao
  └── conhecimentos: Set<Conhecimento>

Competencia
  └── atividades: Set<Atividade>

Unidade
  ├── codigo, sigla, nome
  ├── tipo: OPERACIONAL | INTERMEDIARIA | INTEROPERACIONAL | SEM_EQUIPE
  └── superior: Unidade

Movimentacao
  ├── subprocesso: Subprocesso
  ├── unidadeOrigem, unidadeDestino
  └── dataHora
```

### Convenções de persistência

- Tabelas em `UPPER_CASE` (ex: `PROCESSO`, `SUBPROCESSO`, `MAPA`).
- Colunas em `snake_case` (ex: `data_criacao`, `situacao_subprocesso`).
- Chave primária sempre chamada `codigo` (nunca `id`).
- Enums persistidos como `STRING`.
- Schema do banco: `sgc`.

## API REST

O backend expõe uma API em `http://localhost:10000`. Convenção de endpoints:

| Padrão | Uso |
|---|---|
| `GET /api/{recurso}/{codigo}` | Consulta |
| `POST /api/{recurso}` | Criação |
| `POST /api/{recurso}/{codigo}/atualizar` | Alteração |
| `POST /api/{recurso}/{codigo}/excluir` | Remoção |
| `POST /api/{recurso}/{codigo}/{acao}` | Ações de workflow (ex: `/iniciar`, `/finalizar`, `/disponibilizar`) |

Principais grupos de endpoints:

- `/api/processos` — CRUD e ações de ciclo de vida dos processos.
- `/api/subprocessos` — Transições de estado e ações em bloco.
- `/api/subprocessos/{codigo}/atividades` — CRUD de atividades e conhecimentos.
- `/api/mapas` — CRUD de mapas de competências.
- `/api/unidades` — Consulta de hierarquia e atribuições temporárias.
- `/api/usuarios` — Administradores do sistema.
- `/api/alertas` — Alertas e notificações (SSE).
- `/api/relatorios` — Geração e exportação de relatórios.
- `/api/configuracoes` — Parâmetros dinâmicos do sistema.

---

# Comportamentos

## Perfis de Usuário

| Perfil | Origem | Escopo de visualização | Responsabilidades |
|---|---|---|---|
| **ADMIN** | Cadastro manual na tabela `ADMINISTRADOR` | Todo o sistema | Criar/editar processos, homologar cadastros e mapas, gerenciar admins e configurações, gerar relatórios |
| **GESTOR** | Responsável por unidade INTERMEDIÁRIA ou INTEROPERACIONAL (via SGRH) | Sua unidade + subordinadas (recursivo) | Aceitar/devolver cadastros e mapas das unidades subordinadas |
| **CHEFE** | Responsável por unidade OPERACIONAL ou INTEROPERACIONAL (via SGRH) | Apenas sua unidade | Cadastrar atividades/conhecimentos, disponibilizar cadastro, validar mapa e apresentar sugestões |
| **SERVIDOR** | Servidor lotado em unidade OPERACIONAL ou INTEROPERACIONAL que não é responsável | Apenas sua unidade | Participar de diagnósticos (autoavaliação) |

Um usuário pode acumular múltiplos perfis (ex: CHEFE de uma unidade e ADMIN). Nesse caso, escolhe o perfil/unidade ativa no login. O responsável de unidade INTEROPERACIONAL acumula GESTOR e CHEFE simultaneamente.

## Motor de Estados do Subprocesso

As transições de estado são implementadas em `SituacaoSubprocesso.podeTransicionarPara()`. Os estados seguem três trilhas independentes:

### Mapeamento
```
NAO_INICIADO
  → MAPEAMENTO_CADASTRO_EM_ANDAMENTO       (CHEFE inicia cadastro)
  → MAPEAMENTO_CADASTRO_DISPONIBILIZADO    (CHEFE disponibiliza)
  → MAPEAMENTO_CADASTRO_HOMOLOGADO         (ADMIN homologa; GESTOR valida encaminhando para cima)
  → MAPEAMENTO_MAPA_CRIADO                 (ADMIN cria mapa)
  → MAPEAMENTO_MAPA_DISPONIBILIZADO        (ADMIN disponibiliza mapa)
  → MAPEAMENTO_MAPA_COM_SUGESTOES          (CHEFE apresenta sugestões)  ← opcional
  → MAPEAMENTO_MAPA_VALIDADO               (toda a hierarquia aprova)
  → MAPEAMENTO_MAPA_HOMOLOGADO             (ADMIN homologa mapa final)
```

### Revisão
```
NAO_INICIADO
  → REVISAO_CADASTRO_EM_ANDAMENTO
  → REVISAO_CADASTRO_DISPONIBILIZADA
  → REVISAO_CADASTRO_HOMOLOGADA
  → REVISAO_MAPA_AJUSTADO
  → REVISAO_MAPA_DISPONIBILIZADO
  → REVISAO_MAPA_COM_SUGESTOES             ← opcional
  → REVISAO_MAPA_VALIDADO
  → REVISAO_MAPA_HOMOLOGADO
```

### Diagnóstico
```
NAO_INICIADO
  → DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO
  → DIAGNOSTICO_MONITORAMENTO
  → DIAGNOSTICO_CONCLUIDO
```

### Situações do Processo
```
CRIADO → EM_ANDAMENTO → FINALIZADO
```

## Localização do Subprocesso

A **localização atual** é a unidade em que o subprocesso se encontra no momento (destino da última movimentação registrada). É o critério central para permissões de escrita:

1. Se `subprocesso.localizacaoAtual` não é nulo, usa-se esse valor (cache).
2. Caso contrário, busca a última movimentação em `movimentacaoRepo`.
3. Se não houver movimentação, assume-se a unidade do próprio subprocesso.

## Fluxo de Dados no Frontend

```
View → (dispara ação) → Store (Pinia) → Service (Axios) → API REST → Backend
                ↑                                                        |
                └────────── estado reativo atualizado ←─────────────────┘
```

## Alertas e Notificações

O sistema envia alertas internos via **SSE (Server-Sent Events)** (`/api/alertas/stream`). As regras de visibilidade dos alertas:

- **SERVIDOR**: Vê apenas alertas pessoais (direcionados ao seu título de eleitor).
- **ADMIN / GESTOR / CHEFE**: Veem alertas pessoais + alertas coletivos da sua unidade ativa.

Alertas são exibidos em ordem decrescente por data/hora.

---

# Uso

## Pré-requisitos

- JDK 25+
- Node.js 22+ (recomendado 25+)

## Execução em Desenvolvimento

```bash
# Stack completa (backend + frontend)
node e2e/lifecycle.js

# Apenas backend (perfil e2e)
./gradlew :backend:bootRun -PENV=e2e
# API em: http://localhost:10000

# Apenas frontend
cd frontend && npm install && npm run dev
# App em: http://localhost:5173
```

## Perfis de Backend

| Perfil | Banco | Uso |
|---|---|---|
| `local` (padrão) | H2 em memória | Desenvolvimento rápido |
| `e2e` | H2 em memória + fixtures | Testes automáticos E2E |
| `hom` | Oracle | Homologação |

## Testes e Qualidade

| Tipo | Comando |
|---|---|
| Unitários backend | `./gradlew :backend:unitTest` |
| Integração backend | `./gradlew :backend:integrationTest` |
| Mutation testing | `./gradlew :backend:mutationTest` |
| Unitários frontend | `npm run test:unit --prefix frontend` |
| Type check | `npm run typecheck` |
| Lint (OXC + ESLint) | `npm run lint` |
| E2E (Playwright) | `npm run test:e2e` |
| Dashboard QA | `npm run qa:dashboard` |

---

# Código

## Padrões e Convenções

- **Idioma**: Todo o código, comentários e documentação em **Português brasileiro**.
- **Identificadores**: `codigo` no lugar de `id` para chaves primárias e referências.
- **Parâmetros**: Máximo de 3 parâmetros por método. Se ultrapassar, usar um DTO de *command*.
- **Backend**: `PascalCase` para classes; `camelCase` para métodos. Sufixos: `Controller`, `Service`, `Repo`, `Dto`, `Mapper`, `Facade`. Exceções: prefixo `Erro` (ex: `ErroNegocio`).
- **Frontend**: Componentes `PascalCase` (ex: `ProcessoCard.vue`); arquivos TS `camelCase`. Stores: `use{Nome}Store`.
- **Logging no frontend**: Usar `import { logger } from '@/utils'` — nunca `console.log`, `console.warn` ou `console.debug`.

## Autenticação JWT

1. O usuário autentica via API de Acesso AD do TRE-PE (título de eleitor + senha).
2. O backend emite um JWT.
3. O frontend armazena o token no `localStorage` e o injeta automaticamente via `axios-setup.ts`.

## Snapshot de Unidades

Ao iniciar um processo, o sistema armazena um **snapshot** (`UnidadeProcesso`) da árvore de unidades participantes vigente. Isso preserva a hierarquia mesmo que ocorram mudanças organizacionais no SGRH posteriormente.

## Cache Organizacional

Para reduzir consultas ao banco das views do SGRH (que são lentas), o módulo `organizacao` mantém um **cache em memória** (`CacheOrganizacaoService`) com atualização periódica via `AgendadorRefreshCache`.

---

# Controle de Acesso

O `SgcPermissionEvaluator` (implementação de `PermissionEvaluator` do Spring Security) centraliza toda a lógica de controle de acesso, baseada em dois eixos:

| Eixo | Controla | Critério |
|---|---|---|
| **Hierarquia** | Visualização (Leitura) | Unidade responsável do subprocesso |
| **Localização** | Execução (Escrita) | Localização atual do subprocesso |

**Regra de ouro**: Um usuário só pode executar ações de escrita em um subprocesso se este estiver **localizado na sua unidade ativa** — incluindo o perfil ADMIN.

### Regras de Visualização por Perfil

| Perfil | Regra |
|---|---|
| **ADMIN** | Acesso global (`return true`) |
| **GESTOR** | Sua unidade e todas as subordinadas (verificado via `HierarquiaService`) |
| **CHEFE / SERVIDOR** | Apenas sua unidade ativa |

### Anotações nos Controllers

```java
// Ações de fluxo (validação de perfil + localização):
@PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'ACEITAR_CADASTRO')")

// Ações administrativas (apenas perfil):
@PreAuthorize("hasRole('ADMIN')")

// Ações em bloco:
@PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_MAPA')")
```

### Regras no Frontend

- **Esconder**: Se o perfil ativo **nunca** tem permissão para a ação.
- **Desabilitar** (com tooltip): Se o perfil permite, mas a situação ou localização atual impede.

---

# Variantes

## Tipos de Processo

| Tipo | Objetivo | Unidades participantes |
|---|---|---|
| **Mapeamento** | Criação do primeiro mapa de competências | OPERACIONAL + INTEROPERACIONAL |
| **Revisão** | Atualização periódica do mapa vigente | OPERACIONAL + INTEROPERACIONAL |
| **Diagnóstico** | Avaliação de domínio de competências pelos servidores | OPERACIONAL + INTEROPERACIONAL |

## Tipos de Unidade

| Tipo | Características |
|---|---|
| **OPERACIONAL** | Unidade-folha com 2+ servidores lotados; cadastra atividades/conhecimentos |
| **INTERMEDIÁRIA** | Possui unidades subordinadas; apenas 1 servidor (titular); só valida/devolve |
| **INTEROPERACIONAL** | Possui subordinadas e 2+ servidores; acumula papéis de CHEFE e GESTOR |
| **SEM_EQUIPE** | Unidade-folha com menos de 2 servidores; não participa de processos |

## Ações em Bloco

O sistema permite que ADMIN e GESTOR executem ações sobre múltiplos subprocessos simultaneamente:

- **Aceitar cadastro em bloco** (GESTOR) — CDU-22
- **Homologar cadastro em bloco** (ADMIN) — CDU-23
- **Disponibilizar mapa em bloco** (ADMIN) — CDU-24
- **Aceitar validação em bloco** (GESTOR) — CDU-25
- **Homologar validação em bloco** (ADMIN) — CDU-26

---

# Referências

- **[etc/reqs/cdu-01.md](etc/reqs/cdu-01.md) a [cdu-36.md](etc/reqs/cdu-36.md)** — Especificação detalhada de cada caso de uso.
- **[README.md](README.md)** — Visão geral, stack, como executar e sumário de testes.
- **[AGENTS.md](AGENTS.md)** — Convenções de código e regras para desenvolvedores e agentes de IA.
- **[backend/README.md](backend/README.md)** — Arquitetura detalhada do backend.
- **[frontend/README.md](frontend/README.md)** — Arquitetura do frontend.
- **[etc/docs/regras-acesso.md](etc/docs/regras-acesso.md)** — Especificação completa do controle de acesso.
- **[etc/docs/regras-e2e.md](etc/docs/regras-e2e.md)** — Guia para testes E2E com Playwright.
- **[etc/reqs/_intro.md](etc/reqs/_intro.md)** — Informações gerais, atores, perfis e situações dos processos.
- **[etc/reqs/_intro-glossario.md](etc/reqs/_intro-glossario.md)** — Glossário do domínio.
- **[etc/reqs/regras-negocio.md](etc/reqs/regras-negocio.md)** — Consolidação de todas as regras de negócio.