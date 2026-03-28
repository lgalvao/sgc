# Plano de Reorganizacao dos Scripts do SGC

_Ultima atualizacao do plano: 2026-03-28 (reenvio de PR para aplicacao manual)._

## Estado atual (marco consolidado)

A consolidacao estrutural foi concluida.

Superficie oficial:

- ponto de entrada unico: `node etc/scripts/sgc.js`
- subdominios ativos: `backend`, `frontend`, `qa`, `projeto`, `codigo`, `e2e`
- scripts de snapshot de QA consolidados em `etc/scripts/qa`
- ausencia de legados em `backend/etc/scripts` e `frontend/etc/scripts`
- testes automatizados do toolkit ativos em `etc/scripts/test/sgc.test.js`

## O que ja nao esta pendente

- criacao da CLI raiz
- centralizacao dos comandos principais em `etc/scripts`
- integracao do dashboard de QA ao namespace `qa`
- consolidacao dos comandos transversais de projeto
- eliminacao dos antigos polos `backend/etc/scripts` e `frontend/etc/scripts`

## Pendencias reais (backlog ativo)

### 1) Padronizacao de contrato CLI

Objetivo:

- todos os comandos publicos com comportamento previsivel

Falta fechar:

- padrao unico de `--help`
- padrao unico de codigos de saida
- criterio claro para quando expor `--json`
- mensagens de erro consistentes entre dominios

### 2) Reducao de heranca CommonJS

Objetivo:

- reduzir risco tecnico sem quebrar compatibilidade

Falta fechar:

- inventario final dos `.cjs` remanescentes
- definicao por comando: manter em `cjs` ou migrar para ESM
- migracao gradual dos comandos de maior retorno

### 3) Biblioteca compartilhada

Objetivo:

- reduzir duplicacao entre backend/frontend/qa

Falta fechar:

- extrair utilitarios repetidos para `etc/scripts/lib`
- manter utilitarios de dominio apenas quando houver justificativa clara

### 4) Cobertura de testes do toolkit

Objetivo:

- proteger contratos publicos da CLI

Falta fechar:

- ampliar cenarios de erro
- ampliar verificacao de saida estruturada (`--json`)
- validar contratos minimos dos comandos mais usados

## Plano de execucao (proximos ciclos)

### Ciclo A - Contrato de UX

Entregas:

- matriz de comandos com suporte a `--help`, `--json`, codigos de saida
- normalizacao de mensagens de erro
- testes de contrato para comandos prioritarios

Prioridade:

- `backend cobertura verificar`
- `frontend cobertura verificar`
- `qa snapshot coletar`
- `qa resumo`

### Ciclo B - CommonJS -> ESM

Entregas:

- lista oficial de scripts `.cjs` por criticidade
- lote inicial de migracao (baixo acoplamento)
- decisao registrada para itens que permanecerem em `cjs`

### Ciclo C - Fechamento de governanca

Entregas:

- atualizacao final do `etc/scripts/README.md`
- politica de deprecacao para caminhos antigos
- checklist de encerramento do plano concluido

## Checklist de encerramento

O plano sera considerado encerrado quando todos os itens abaixo estiverem concluidos:

- [ ] contratos de ajuda e saida padronizados nos comandos prioritarios
- [ ] estrategia `CommonJS x ESM` registrada e executada para os casos criticos
- [ ] duplicacoes principais movidas para `etc/scripts/lib`
- [ ] suite do toolkit cobrindo fluxos principais e erros comuns
- [ ] documentacao central validada e sem referencia a caminhos obsoletos

## Fonte de verdade operacional

- CLI oficial: `etc/scripts/sgc.js`
- testes do toolkit: `etc/scripts/test/sgc.test.js`
- dashboard QA: `etc/qa-dashboard/latest/ultimo-snapshot.json` e `etc/qa-dashboard/latest/ultimo-resumo.md`
