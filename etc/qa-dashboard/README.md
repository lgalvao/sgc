# QA Dashboard

Este diretorio concentra a estrutura estavel do dashboard de QA do SGC.

## Objetivo

Separar artefatos permanentes do dashboard dos relatorios transientes gerados por Gradle, Vitest, Playwright e demais
ferramentas.

## Estrutura

- `config/`: schemas, pesos, thresholds e configuracoes versionadas.
- `runs/`: snapshots gerados por execucao. Ignorado no Git.
- `latest/`: atalhos para os artefatos consolidados mais recentes. Ignorado no Git.
- `tmp/`: arquivos auxiliares temporarios. Ignorado no Git.
- `dashboard.html`, `dashboard.js`, `dashboard.css`: interface visual do dashboard a partir de `latest/ultimo-snapshot.json`.

## Contrato

O dashboard deve ler apenas os snapshots normalizados gerados em `runs/` ou `latest/`.

Os adaptadores podem consumir:

- `backend/build/**`
- `frontend/coverage/**`
- `test-results/**`
- `playwright-report/**`
- saidas dos comandos de QA

Mas esses artefatos nao sao a fonte de verdade do dashboard.

## Como coletar e visualizar

1. Gere um snapshot de QA (exemplo perfil rapido):

   ```bash
   node etc/qa-dashboard/scripts/coletar-snapshot.mjs --perfil rapido
   ```

2. Sirva a raiz do repositorio com qualquer servidor HTTP simples e abra `etc/qa-dashboard/dashboard.html`.

   Exemplo:

   ```bash
   npx http-server .
   ```

   Depois acesse `http://localhost:8080/etc/qa-dashboard/dashboard.html`.

## Documento principal

Ver [dashboard-qa.md](/C:/sgc/etc/docs/dashboard-qa.md).
