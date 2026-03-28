# Plano de Reorganizacao dos Scripts do SGC

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

- contrato minimo de ajuda para todos os comandos publicos
- contrato de saida com `--json` nos comandos candidatos
- padrao unico de mensagens de erro para operacao e validacao de argumentos

Checklist sugerido:

- [ ] mapear comandos sem `--help` consistente
- [ ] mapear comandos que ja suportam JSON e documentar formato
- [ ] implementar camada compartilhada de ajuda/saida para reduzir duplicacao
- [ ] atualizar snapshots/testes do toolkit para refletir contratos

### Ciclo B - Reducao de heranca CommonJS

Objetivo:

- reduzir risco tecnico sem reescrever todo o toolkit de uma vez

Estrategia:

- priorizar scripts com baixa dependencia interna
- migrar primeiro os que ja usam utilitarios modernos de `etc/scripts/lib`
- manter compatibilidade de CLI durante a transicao

Checklist sugerido:

- [ ] inventariar scripts `.cjs` por criticidade e frequencia de uso
- [ ] migrar lote inicial de frontend (mais isolados)
- [ ] migrar lote inicial de backend sem quebrar `backend/lib/cobertura-base`
- [ ] remover adaptacoes temporarias quando todos os consumidores estiverem em ESM

### Ciclo C - Confiabilidade e governanca

Objetivo:

- aumentar confianca para evolucao continua do toolkit

Entregas:

- suite de testes com foco em contratos publicos
- regra clara de deprecacao e remocao de wrappers remanescentes
- orientacao de CI para validacao minima obrigatoria

Checklist sugerido:

- [ ] criar matriz de testes por grupo (`backend`, `frontend`, `qa`, `projeto`, `codigo`)
- [ ] cobrir cenarios de falha de argumentos e falha de execucao de subprocesso
- [ ] avaliar comando `qa validar` como agregador para pipeline
- [ ] publicar politica de deprecacao no README da CLI

## Backlog executivo priorizado

### Bloco 1 - Fazer agora

- padronizar `--help` e codigos de saida dos comandos com maior uso diario
- cobrir em teste os comandos `qa snapshot coletar` e `qa resumo`
- definir padrao unico de logging interno (sem `console.log` em producao)

### Bloco 2 - Fazer na sequencia

- migrar lote inicial de scripts frontend `.cjs` para ESM
- extrair utilitarios duplicados para `etc/scripts/lib`
- revisar naming de comandos para manter consistencia semantica

### Bloco 3 - Fazer depois da estabilizacao

- avaliar comando de comparacao de snapshots de QA
- expandir modos de saida estruturada para analise externa
- revisar custo/beneficio de migrar 100% dos scripts legados de uma vez
