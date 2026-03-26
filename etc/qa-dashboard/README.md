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

## Contrato

O dashboard deve ler apenas os snapshots normalizados gerados em `runs/` ou `latest/`.

Os adaptadores podem consumir:

- `backend/build/**`
- `frontend/coverage/**`
- `test-results/**`
- `playwright-report/**`
- saidas dos comandos de QA

Mas esses artefatos nao sao a fonte de verdade do dashboard.

## Documento principal

Ver [dashboard-qa.md](/C:/sgc/etc/docs/dashboard-qa.md).
