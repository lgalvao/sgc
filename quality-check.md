# Relatório de Verificação de Qualidade — SGC

> **Data de execução:** 2026-03-02  
> **Branch:** `copilot/full-quality-check-update`  
> **Java:** Temurin 21.0.10 | **Node.js:** v24.13.1 | **Vitest:** v4.0.18 | **Gradle:** 9.3.1

---

## Resumo Executivo

| Área | Ferramenta | Status | Detalhes |
|------|-----------|--------|----------|
| Backend — Testes | JUnit 5 / Gradle | ⚠️ **1 falha** | 990/991 passou |
| Backend — Cobertura | JaCoCo | ❌ **Abaixo do limite** | Branch 87,64% (< 90%), Line 96,72% (< 98%), Instruction 96,27% (< 98%) |
| Backend — Análise estática | SpotBugs | ⚠️ **3 avisos** | Build não bloqueado (`ignoreFailures=true`) |
| Frontend — Testes | Vitest | ⚠️ **4 falhas** | 1230/1234 passou |
| Frontend — Lint | ESLint | ⚠️ **1 aviso** | `no-unused-vars` em `ProcessoDetalheView.vue` (limite: 0 avisos) |
| Frontend — Typecheck | vue-tsc | ✅ **Passou** | Sem erros de tipo |
| Frontend — Cobertura | Vitest/v8 | ℹ️ **N/D** | Relatório não gerado (ver nota) |
| Frontend — Segurança | npm audit | ⚠️ **4 vulnerabilidades** | 1 moderada, 3 altas (dev deps) |

---

## 1. Backend

### 1.1 Testes Unitários e de Integração

```
Total:    991 testes executados
Passou:   990
Falhou:   1
Ignorado: 0
Tempo:    ~71 s
```

**Teste com falha:**

| Classe | Método | Motivo |
|--------|--------|--------|
| `sgc.organizacao.model.UsuarioJsonViewTest` | `deveSerializarApenasCamposPublicosDoUsuario()` | O campo `ramal` está sendo serializado na view pública, mas o teste espera que ele seja omitido. |

**Detalhes do erro:**
```
Expecting actual:
  "{"email":"...","matricula":"...","nome":"...","ramal":"1234",...}"
not to contain:
  ""ramal""
```

> **Diagnóstico:** O campo `ramal` provavelmente perdeu a anotação `@JsonView` correta ou foi adicionado sem a restrição de visibilidade esperada.

---

### 1.2 Cobertura de Código (JaCoCo)

Configuração de limites (`jacocoTestCoverageVerification`):

| Contador | Cobertura Obtida | Limite Configurado | Status |
|----------|------------------|--------------------|--------|
| Instrução | **96,27%** (18699/19424) | 98% | ❌ Abaixo |
| Branch | **87,64%** (1213/1384) | 90% | ❌ Abaixo |
| Linha | **96,72%** (3949/4083) | 98% | ❌ Abaixo |
| Complexidade | 89,43% (1455/1627) | — | — |
| Método | 97,19% (898/924) | — | — |
| Classe | **100%** (150/150) | — | ✅ |

> **Nota:** As exclusões configuradas em `jacocoTestReport` já descartam classes de configuração, Erros, DTOs, Mappers gerados e Enums — os números acima refletem apenas o código de negócio efetivo.

---

### 1.3 Análise Estática (SpotBugs 4.9.8)

O build não é bloqueado por SpotBugs (`ignoreFailures = true`), mas foram reportados **3 avisos**:

| Arquivo | Linha | Categoria | Descrição |
|---------|-------|-----------|-----------|
| `MapaManutencaoService.java` | 34 | `EI2` (Medium) | Construtor pode expor representação interna ao armazenar `competenciaRepo` (objeto mutável externo) |
| `MapaManutencaoService.java` | 38 | `EI2` (Medium) | Construtor pode expor representação interna ao armazenar `subprocessoService` (objeto mutável externo) |
| `E2eController.java` | 150 | `REC` (Medium) | `Exception` é capturada, mas `Exception` não é lançada no bloco correspondente |

> **Nota:** Os avisos `EI2` são falsos positivos típicos de injeção de dependência via construtor com Spring (campos são interfaces injetadas, não coleções mutáveis). O aviso `REC` no `E2eController` é código exclusivo de ambiente de testes/e2e.

---

## 2. Frontend

### 2.1 Testes Unitários (Vitest v4.0.18)

```
Test Files:  3 failed | 122 passed (125)
Tests:       4 failed | 1230 passed (1234)
Duração:     ~139 s
```

**Testes com falha:**

| Arquivo | Teste | Erro |
|---------|-------|------|
| `TabelaProcessos.spec.ts` | `deve emitir o evento selecionarProcesso ao pressionar Enter em uma linha` | O evento `selecionarProcesso` não é emitido ao pressionar Enter; esperado `truthy`, recebido `undefined` |
| `TabelaProcessosCoverage.spec.ts` | `handles Space key on row to select process` | `TypeError: tbodyTrAttr is not a function` — incompatibilidade entre o teste e a implementação atual do componente |
| `TabelaProcessosCoverage.spec.ts` | `returns empty class/attr for invalid inputs in row functions` | `expected 1 to be 0` — classe inesperada presente no elemento `[data-testid="wrong-type-class"]` |
| `HistoricoViewCoverage.spec.ts` | `renders correctly and rows are accessible` | Atributo `tabindex="0"` ausente nas linhas da tabela de histórico |

> **Diagnóstico:** As falhas convergem para a mesma causa raiz — acessibilidade de teclado (`tabindex`, handler de `keydown`/`keyup`) nos componentes `TabelaProcessos` e `HistoricoView`. Os testes de cobertura foram escritos para verificar comportamentos ainda não implementados ou que sofreram regressão.

---

### 2.2 Lint (ESLint)

```
1 problem (0 errors, 1 warning)
ESLint found too many warnings (maximum: 0).
```

**Aviso encontrado:**

| Arquivo | Linha | Regra | Descrição |
|---------|-------|-------|-----------|
| `src/views/ProcessoDetalheView.vue` | 132 | `@typescript-eslint/no-unused-vars` | `'formatSituacaoProcesso'` está definida mas nunca usada |

---

### 2.3 Typecheck (vue-tsc)

```
Status: ✅ PASSOU — nenhum erro de tipo encontrado
```

---

### 2.4 Cobertura de Código (Vitest/v8)

> **ℹ️ Nota:** O relatório de cobertura não foi gerado nesta execução. O provider `@vitest/coverage-v8` coleta dados brutos de cobertura V8 mas não chega a gravar o relatório em disco (diretório `coverage/` não é criado). O comportamento foi reproduzido consistentemente em múltiplas execuções. Isso pode ser um problema de compatibilidade entre o Vitest v4 e o ambiente de execução (Node.js v24 + pool de workers com jsdom). O código de saída do processo (`0`) indica que o problema é silencioso.  
>  
> **Limites configurados** (`vitest.config.ts`): statements ≥ 90%, branches ≥ 80%, functions ≥ 89%, lines ≥ 90%.

---

### 2.5 Segurança de Dependências (npm audit)

```
4 vulnerabilities (1 moderate, 3 high)
```

| Pacote | Severidade | CVE / Advisory | Caminho | Correção |
|--------|-----------|----------------|---------|---------|
| `minimatch` (5.0.0–5.1.7, 9.0.0–9.0.6) | **Alta** | GHSA-3ppc-4f35-3m26 | `@redocly/openapi-core` → `minimatch` | `npm audit fix` |
| `minimatch` (5.0.0–5.1.7, 9.0.0–9.0.6) | **Alta** | GHSA-7r86-cg39-jmmj | `@redocly/openapi-core` → `minimatch` | `npm audit fix` |
| `minimatch` (5.0.0–5.1.7, 9.0.0–9.0.6) | **Alta** | GHSA-23c5-xmqv-rm74 | `editorconfig` → `minimatch` | `npm audit fix` |
| `editorconfig` (1.0.3–1.0.4, 2.0.0) | **Moderada** | depende de minimatch vulnerável | `editorconfig` → `minimatch` | `npm audit fix` |

> Todas as vulnerabilidades estão em dependências de desenvolvimento (ferramentas de lint/documentação). Nenhuma afeta o bundle de produção.

---

## 3. Itens de Ação

### Bloqueantes (impedem o pipeline de qualidade)

| # | Área | Item |
|---|------|------|
| 1 | Backend — Teste | Corrigir `UsuarioJsonViewTest`: adicionar/restaurar `@JsonView` no campo `ramal` do modelo `Usuario` |
| 2 | Backend — Cobertura | Aumentar cobertura de branches (87,64% → 90%+) e de instruções/linhas (96,x% → 98%+) |
| 3 | Frontend — Testes | Implementar acessibilidade de teclado (`tabindex="0"` + handlers `keydown`/`keyup`) em `TabelaProcessos` e `HistoricoView`, corrigindo os 4 testes |
| 4 | Frontend — Lint | Remover ou usar a função `formatSituacaoProcesso` em `ProcessoDetalheView.vue` |

### Não-bloqueantes (melhorias recomendadas)

| # | Área | Item |
|---|------|------|
| 5 | Backend — SpotBugs | Adicionar `@SuppressFBWarnings("EI_EXPOSE_REP2")` nos construtores do `MapaManutencaoService` para suprimir falsos positivos de DI |
| 6 | Backend — SpotBugs | Refatorar o bloco `catch (Exception e)` no `E2eController.limparProcessoCompleto` para capturar apenas exceções verificadas realmente lançadas |
| 7 | Frontend — Segurança | Executar `npm audit fix` no frontend para atualizar `minimatch` (dependências de dev) |
| 8 | Frontend — Cobertura | Investigar e corrigir a geração do relatório de cobertura com Vitest v4 + Node.js v24 |
