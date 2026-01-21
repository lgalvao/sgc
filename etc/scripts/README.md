# Scripts de Automação e Qualidade - SGC

Este diretório contém scripts utilitários para análise de código, testes, cobertura e manutenção do projeto SGC.

## Estrutura

- `backend/`: Scripts focados na análise do código Java (Spring Boot).
- `frontend/`: Scripts focados na análise do código Vue.js e testes E2E.
- `gerar_arvore_linhas.js`: Utilitário para contar linhas de código do projeto.
- `quality-check.ps1` / `quality-check.sh`: Wrappers para executar verificações de qualidade via Gradle.

## Scripts Backend

### `analisar-complexidade.js`
Gera um ranking de complexidade das classes Java, combinando métricas do Jacoco (complexidade ciclomática, branches) com contagem de linhas e métodos.
**Pré-requisito:** Executar testes do backend (`gradle test jacocoTestReport`).
**Uso:**
```bash
node scripts/backend/analisar-complexidade.js
```
**Saída:** Gera `complexity-ranking.md` na raiz do projeto.

### `verificar-cobertura.js`
Analisa o relatório XML do Jacoco e exibe um resumo da cobertura no terminal. Pode filtrar por pacote/classe e listar linhas não cobertas.
**Uso:**
```bash
node scripts/backend/verificar-cobertura.js [filtro] [--min=95] [--missed]
```
- `filtro`: Nome parcial do pacote ou classe.
- `--min`: Percentual mínimo de cobertura para exibir na lista de alerta (default: 95).
- `--missed`: Exibe lista detalhada de arquivos com linhas/branches perdidos.

### `auditar-verificacoes-null.js`
Audita o código Java em busca de verificações de nulidade (`!= null`) que podem ser redundantes ou excessivas.
**Uso:**
```bash
node scripts/backend/auditar-verificacoes-null.js
```
**Saída:** Gera `null-checks-audit.txt` e `null-checks-analysis.md`.

### `analisar-cobertura.js`
Wrapper simples que executa os testes Gradle e processa os dados.
**Uso:**
```bash
node scripts/backend/analisar-cobertura.js
```

## Scripts Frontend

### `capturar-telas.js`
Executa testes E2E (Playwright) para capturar screenshots do sistema.
**Uso:**
```bash
node scripts/frontend/capturar-telas.js [categoria] [--headed] [--ui]
```
**Categorias:** `seguranca`, `painel`, `processo`, `subprocesso`, `mapa`, `navegacao`, `estados`, `responsividade`, `all`.

### `verificar-cobertura.js`
Analisa o relatório de cobertura do Frontend (Vitest).
**Pré-requisito:** Executar `npm run coverage:unit` no diretório `frontend`.
**Uso:**
```bash
node scripts/frontend/verificar-cobertura.js
```

### `audit-frontend-validations.js`
Compara validações no Frontend (Vue) com validações no Backend (DTOs) para identificar inconsistências.
**Uso:**
```bash
node scripts/frontend/audit-frontend-validations.js
```
**Saída:** Gera `frontend-backend-validation-comparison.md`.

### `audit-view-validations.js`
Verifica validações redundantes no código Java para campos que já são garantidos por Views de Banco de Dados.
**Uso:**
```bash
node scripts/frontend/audit-view-validations.js
```
**Saída:** Gera `view-validations-audit.md`.

### `verificar-acessibilidade.js`
Inicia a aplicação e executa testes de acessibilidade usando Lighthouse.
**Uso:**
```bash
node scripts/frontend/verificar-acessibilidade.js
```

### `listar-test-ids.js` e `listar-test-ids-duplicados.js`
Lista todos os `data-testid` usados nos componentes Vue e verifica duplicatas.
**Uso:**
```bash
node scripts/frontend/listar-test-ids.js
node scripts/frontend/listar-test-ids-duplicados.js
```

## Utilitários Gerais

### `gerar_arvore_linhas.js`
Gera uma árvore visual dos arquivos do projeto com contagem de linhas.
**Uso:**
```bash
node scripts/gerar_arvore_linhas.js [--depth n] [--exclude-tests]
```

### `quality-check`
Executa tarefas de verificação de qualidade do Gradle.
**Uso (Windows):**
```powershell
.\scripts\quality-check.ps1 [all|backend|frontend|fast]
```
**Uso (Linux/Mac):**
```bash
./scripts/quality-check.sh [all|backend|frontend|fast]
```
