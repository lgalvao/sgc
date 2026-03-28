# Dashboard de QA do SGC

## Objetivo

Consolidar a saude de QA do sistema em um painel de desenvolvimento orientado a risco, e nao a observabilidade de
producao.

O dashboard deve responder com clareza:

- Quais suites estao verdes, amarelas ou vermelhas.
- Quais modulos estao mais arriscados.
- Onde a cobertura esta baixa ou regrediu.
- Quais testes estao lentos, instaveis ou falhando com frequencia.
- Quais verificacoes de qualidade estrutural estao falhando.

## Principio central

O dashboard nao deve ler diretamente os artefatos transientes das ferramentas como fonte primaria.

Exemplos de fontes transientes:

- `backend/build/reports/jacoco/test/jacocoTestReport.xml`
- `backend/build/test-results/**`
- `frontend/coverage/coverage-final.json`
- `playwright-report/`
- `test-results/`
- saidas textuais de `eslint`, `vue-tsc`, `spotbugs`, `semgrep` e `pitest`

Esses arquivos continuam existindo, mas passam a ser apenas insumos temporarios. A fonte de verdade do dashboard deve
ser um conjunto de snapshots normalizados, com schema estavel, escritos em um local centralizado.

## Estrutura proposta

### Diretorio versionado

`etc/qa-dashboard/`

Conteudo versionado:

- `README.md`
- `config/snapshot.schema.json`
- configuracoes futuras do dashboard
- definicoes de pesos, regras e baselines aprovados

### Diretorios gerados localmente

`etc/qa-dashboard/runs/`
`etc/qa-dashboard/latest/`
`etc/qa-dashboard/tmp/`

Esses diretorios devem ser ignorados no Git.

## Modelo de execucao

### Camada 1: Orquestracao

Um comando principal executa as verificacoes desejadas.

Sugestao de comandos:

- `node etc/qa-dashboard/scripts/coletar-snapshot.mjs --perfil rapido`
- `node etc/qa-dashboard/scripts/coletar-snapshot.mjs --perfil completo`
- `node etc/qa-dashboard/scripts/coletar-snapshot.mjs --perfil backend`
- `node etc/qa-dashboard/scripts/coletar-snapshot.mjs --perfil frontend`

### Camada 2: Adaptadores

Cada adaptador conhece:

- qual comando executar
- quais artefatos transientes sao esperados
- como extrair dados relevantes
- como normalizar a saida para o formato do dashboard

Adaptadores iniciais:

- `backend-testes`
- `backend-cobertura`
- `backend-spotbugs`
- `backend-mutation`
- `backend-arquitetura`
- `frontend-testes`
- `frontend-cobertura`
- `frontend-lint`
- `frontend-typecheck`
- `e2e-playwright`
- `seguranca-semgrep`

### Camada 3: Consolidacao

Ao fim da execucao, todos os adaptadores alimentam um unico `snapshot.json`.

Exemplo de saida:

`etc/qa-dashboard/runs/2026-03-26T14-30-00-0300/snapshot.json`

Alias uteis:

- `etc/qa-dashboard/latest/ultimo-snapshot.json`
- `etc/qa-dashboard/latest/ultimo-resumo.json`

## Schema de snapshot

O snapshot deve ser a unica fonte lida pelo dashboard.

Blocos recomendados:

### 1. Metadados

- `versaoSchema`
- `geradoEm`
- `duracaoTotalMs`
- `perfilExecucao`
- `maquina`
- `sistemaOperacional`
- `git.branch`
- `git.commit`
- `git.commitCurto`
- `git.worktreeSujo`

### 2. Resumo executivo

- `statusGeral`
- `indiceSaude`
- `totais.verificacoes`
- `totais.sucesso`
- `totais.falha`
- `totais.alerta`
- `totais.naoExecutado`

### 3. Verificacoes

Lista com uma entrada por suite ou verificacao, por exemplo:

- backend unitario
- backend integracao
- backend cobertura
- backend spotbugs
- backend mutation
- frontend unitario
- frontend cobertura
- frontend lint
- frontend typecheck
- e2e
- semgrep

Cada verificacao deve conter:

- `codigo`
- `nome`
- `categoria`
- `status`
- `duracaoMs`
- `comando`
- `diretorio`
- `sumario`
- `metricas`
- `erros`
- `artefatos`

### 4. Cobertura consolidada

Separar backend e frontend, com totais e detalhes por arquivo ou classe.

Campos recomendados:

- `linhas.percentual`
- `branches.percentual`
- `funcoes.percentual`
- `statements.percentual`
- `itensCriticos`
- `lacunasPrioritarias`

### 5. Qualidade estrutural

- `spotbugs`
- `archunit`
- `lint`
- `typecheck`
- `semgrep`

### 6. Confiabilidade da suite

- testes lentos
- testes flaky
- falhas mais recorrentes
- ignorados e skipped
- suites mais caras

### 7. Hotspots

Lista de modulos, pacotes ou areas priorizadas por risco composto.

Formula sugerida:

`risco = regressao + baixa_cobertura + instabilidade + lentidao + mudanca_recente`

## Estrutura minima do snapshot

Mesmo no MVP, o snapshot deve sair consistente, ainda que alguns blocos venham vazios:

- `metadados`
- `resumo`
- `verificacoes`
- `cobertura`
- `qualidade`
- `confiabilidade`
- `hotspots`

## Localizacao dos artefatos transientes atuais

### Backend

- JUnit XML: `backend/build/test-results/**`
- JaCoCo XML e CSV: `backend/build/reports/jacoco/test/`
- SpotBugs: `backend/build/reports/spotbugs/`
- PIT: `backend/build/reports/pitest/`

### Frontend

- Vitest coverage: `frontend/coverage/`
- Vitest stdout: comando `npm run coverage:unit`
- ESLint: stdout
- Typecheck: stdout

### E2E

- Playwright report: `playwright-report/`
- Resultados temporarios: `test-results/`

## Comandos recomendados para o MVP

### Backend

- `./gradlew :backend:unitTest`
- `./gradlew :backend:integrationTest`
- `./gradlew :backend:jacocoTestReport`
- `./gradlew :backend:spotbugsMain :backend:spotbugsTest`

### Frontend

- `npm run coverage:unit --prefix frontend`
- `npm run lint --prefix frontend`
- `npm run typecheck --prefix frontend`

### E2E

- `npx playwright test --reporter=json`

### Seguranca

- `npm run sast`

## Estrategia de persistencia

### Nao versionar

- snapshots de execucao
- logs detalhados
- relatorios transientes copiados

### Versionar

- schema
- configuracao dos adaptadores
- pesos do indice de saude
- thresholds
- baselines manuais aprovados

## Regras de implementacao

- O dashboard nunca le `build/`, `coverage/` ou `test-results/` diretamente.
- Apenas os adaptadores leem artefatos das ferramentas.
- Cada adaptador devolve JSON normalizado.
- O consolidado final grava um `snapshot.json` por execucao.
- O schema deve evoluir com versao explicita.
- O dashboard deve tolerar execucoes parciais.

## MVP recomendado

### Fase 1

Gerar snapshots centralizados sem interface grafica nova.

Entregas:

- orquestrador unico
- adaptadores para backend, frontend e e2e
- snapshot consolidado
- resumo em Markdown ou HTML simples

### Fase 2

Criar uma rota de desenvolvimento no frontend para visualizar os snapshots.

Entregas:

- pagina com saude geral
- filtros por tipo de suite
- hotspots
- historico local

### Fase 3

Adicionar comparacao temporal e entre commits.

Entregas:

- comparacao com ultimo snapshot verde
- tendencias por modulo
- regressao de cobertura
- top falhas recorrentes

## Decisoes sugeridas para o SGC

- Usar `etc/qa-dashboard/` como raiz funcional do dashboard.
- Usar JSON como contrato principal entre coleta e visualizacao.
- Reaproveitar scripts existentes de cobertura e qualidade onde ja fizer sentido.
- Preferir um orquestrador Node.js, pois o repositorio ja usa Node para frontend, Playwright e scripts utilitarios.
- Tratar `snapshot.json` como API interna do dashboard.

## Proximos passos

1. Criar o schema final do snapshot.
2. Implementar o orquestrador base.
3. Implementar adaptadores para backend cobertura, frontend cobertura, testes e e2e.
4. Gerar primeiro snapshot consolidado local.
5. Subir a primeira visualizacao com base apenas nesse snapshot.
