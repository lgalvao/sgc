# Frontend do SGC

## Visão geral

Este módulo contém a SPA do SGC.

- **Vue 3.5** com `<script setup lang="ts">`
- **TypeScript 5.9**
- **Vite 7**
- **Pinia (setup stores)**
- **BootstrapVueNext**

## Estrutura principal (`src/`)

- `views/`: telas de caso de uso.
- `components/`: componentes reutilizáveis.
- `stores/`: estado global com Pinia.
- `composables/`: lógica reutilizável.
- `services/`: integração HTTP com backend.
- `router/`: rotas modulares.
- `types/`: contratos TypeScript.
- `utils/`: utilitários transversais.

## Execução local

```bash
pnpm --dir frontend install
pnpm --dir frontend run dev
```

Aplicação em `http://localhost:5173`.

## Build

```bash
pnpm --dir frontend run build
pnpm --dir frontend run build:hom
pnpm --dir frontend run build:prod
```

## Testes e qualidade

```bash
pnpm --dir frontend run test:unit
pnpm --dir frontend run typecheck
pnpm --dir frontend run lint
pnpm --dir frontend run quality:all
```

## Convenções do módulo

- Componentes em `PascalCase`.
- Stores no padrão `use{Nome}Store`.
- Tratamento de erro centralizado (evitar recuperação local de erro irrecuperável de backend).
- Permissões de UI consumidas a partir da estrutura enviada pelo backend.

## Referências

- [README raiz](../README.md)
- [Regras de acesso](../etc/reqs/regras-acesso.md)
- [Regras E2E](../etc/docs/regras-e2e.md)
