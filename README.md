# SGC - Sistema de Gestão de Competências

Aplicação corporativa para gestão do ciclo de mapeamento e revisão de competências técnicas por unidade organizacional.

## Visão geral

O SGC é dividido em três frentes:

- **backend/**: API REST em Spring Boot.
- **frontend/**: SPA em Vue 3 + TypeScript.
- **e2e/**: suíte Playwright que orquestra backend e frontend para testes de ponta a ponta.

O sistema usa a hierarquia de unidades organizacionais para controle de leitura e a localização atual do subprocesso para controle de escrita.

## Stack principal

- **Backend:** Java 25, Spring Boot 4, Hibernate/JPA, Gradle.
- **Frontend:** Vue 3.5, TypeScript 5.9, Vite 7, Pinia, BootstrapVueNext.
- **Testes:** JUnit/Mockito (backend), Vitest (frontend), Playwright (E2E).
- **Banco:** H2 para desenvolvimento/testes e Oracle para homologação/produção.

## Estrutura do repositório

```text
sgc/
├── backend/
├── frontend/
├── e2e/
├── etc/
│   ├── docs/
│   ├── reqs/
│   └── scripts/
├── monitoring/
├── compose.hom.yaml
└── compose.monitoring.yaml
```

## Pré-requisitos

- JDK 25
- Node.js (compatível com os scripts Gradle e PNPM do projeto)
- pnpm (via Corepack)

## Setup rápido

A partir da raiz do repositório:

```bash
corepack enable
corepack prepare pnpm@10.33.4 --activate
pnpm install
pnpm --dir frontend install
pnpm --dir etc/scripts install
```

Opcionalmente, use o setup automatizado:

```bash
node etc/scripts/sgc.js projeto setup --instalar-dependencias
```

## Execução local

### Backend

```bash
./gradlew :backend:bootRun -PENV=e2e
```

API em `http://localhost:10000`.

### Frontend

```bash
pnpm --dir frontend run dev
```

Frontend em `http://localhost:5173`.

### Stack E2E (backend + frontend)

```bash
node e2e/lifecycle.js
```

## Comandos de qualidade mais usados

Na raiz do repositório:

```bash
npm run typecheck
npm run lint
npm run test:unit
npm run test:e2e
./gradlew --no-daemon --no-configuration-cache :backend:test
```

Toolkit auxiliar:

```bash
node etc/scripts/sgc.js qa snapshot coletar --perfil rapido
```

## Perfis de execução

- `e2e`: perfil padrão para automação (H2 + endpoints de suporte em `/e2e/*`).
- `local`: desenvolvimento backend sem suporte E2E.
- `hom`: homologação.
- `prod`: produção.

## Convenções importantes

- Código, mensagens e documentação em **Português brasileiro**.
- Use **`codigo`** (não `id`) em entidades, DTOs e contratos.
- Backend com nomenclatura `Controller`, `Service`, `Repo`, `Dto`, `Mapper`.
- Exceções com prefixo `Erro`.

## Documentação por módulo

- [backend/README.md](backend/README.md)
- [frontend/README.md](frontend/README.md)
- [e2e/README.md](e2e/README.md)
- [etc/scripts/README.md](etc/scripts/README.md)

Além disso:

- [etc/reqs/regras-acesso.md](etc/reqs/regras-acesso.md)
- [etc/docs/regras-e2e.md](etc/docs/regras-e2e.md)
- [AGENTS.md](AGENTS.md)
