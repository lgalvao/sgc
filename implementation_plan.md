# E2E Helper Refactoring: Remove Defensive Code, Split Branching Functions

Remove silent error swallowing (`.catch(() => false)`, `try/catch`) and replace runtime-branching helpers with explicit, role/context-specific methods.

---

## Proposed Changes

### helpers-analise.ts — Navigation functions

#### [MODIFY] [helpers-analise.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts)

**[acessarSubprocessoGestor](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#13-55)** (L16-53) — Remove 3-fallback cascade. Single strategy: click row → extract process ID → `page.goto(/processo/{id}/{sigla})`. Remove `.catch(() => false)` and all fallback blocks.

**[acessarSubprocessoChefeDireto](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#56-93)** (L59-91) — Make `siglaUnidade` **required** (no default `''`). Remove `else if` branch that blindly clicks first `<tbody tr>`. Same direct URL navigation as above.

**[acessarSubprocessoAdmin](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#94-126)** (L97-125) — Remove `.isVisible().catch(() => false)` auto-recovery to `/painel`. Remove "improvável" fallback. Assert `/painel`, then direct URL navigation.

---

### helpers-atividades.ts — Context guards + branching

#### [MODIFY] [helpers-atividades.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts)

**Delete [garantirContextoSubprocesso](file:///Users/leonardo/sgc/e2e/helpers/helpers-mapas.ts#5-20)** (L4-18) — `try/catch` that `console.warn`s and continues.

**[navegarParaAtividades](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#20-30)** (L20-29) — Remove call to [garantirContextoSubprocesso](file:///Users/leonardo/sgc/e2e/helpers/helpers-mapas.ts#5-20). The `expect(card).toBeVisible()` already validates context.

**[navegarParaAtividadesVisualizacao](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#31-45)** (L31-44) — Make **strict**: only look for `card-subprocesso-atividades-vis`. No fallback to edit card or generic button. Tests must explicitly choose the correct route.

**Split [abrirModalImpacto](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#167-177)** (L167-176) → two methods:
- `abrirModalImpactoEdicao(page)` — opens dropdown → clicks edit button
- `abrirModalImpactoVisualizacao(page)` — clicks direct button
- Delete the branching [abrirModalImpacto](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#167-177)

**Split [verificarBotaoImpactoAusente](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#151-166)** (L151-165) → two methods:
- `verificarBotaoImpactoAusenteEdicao(page)` — opens dropdown, asserts hidden
- `verificarBotaoImpactoAusenteDireto(page)` — asserts button hidden
- Delete the branching [verificarBotaoImpactoAusente](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#151-166)

---

### helpers-processos.ts — Checkbox handling

#### [MODIFY] [helpers-processos.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-processos.ts)

**[criarProcesso](file:///Users/leonardo/sgc/e2e/helpers/helpers-processos.ts#12-63)** (L41-44) — Replace `.catch(() => {})` with strict `await expect(checkbox).toBeEnabled()`.

---

### helpers-mapas.ts — Context guard

#### [MODIFY] [helpers-mapas.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-mapas.ts)

**Delete [garantirContextoSubprocesso](file:///Users/leonardo/sgc/e2e/helpers/helpers-mapas.ts#5-20)** (L5-19) — blind first-row click with `.catch(() => false)`.

**[navegarParaMapa](file:///Users/leonardo/sgc/e2e/helpers/helpers-mapas.ts#21-39)** (L21-38) — Remove [garantirContextoSubprocesso](file:///Users/leonardo/sgc/e2e/helpers/helpers-mapas.ts#5-20) call and `/vis-mapa` early-return. Use Playwright `.or()` for edit/vis card (this is acceptable — it's Playwright's built-in wait, not manual branching).

---

### helpers-navegacao.ts — Toast cleanup

#### [MODIFY] [helpers-navegacao.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-navegacao.ts)

**[limparNotificacoes](file:///Users/leonardo/sgc/e2e/helpers/helpers-navegacao.ts#10-35)** (L14-33) — Combine locators into single CSS selector. Remove outer `try/catch`. Keep `.catch(() => {})` only on `click()` (toast can auto-dismiss).

---

### Spec file updates

#### [MODIFY] Multiple spec files — `siglaUnidade` now required

| Spec | Line | Current call | Fix |
|------|------|-------------|-----|
| [cdu-15.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-15.spec.ts#L60) | 60 | [acessarSubprocessoChefeDireto(page, descProcesso)](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#56-93) | Add `'SECAO_211'` |
| [cdu-16.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-16.spec.ts#L70) | 70 | [acessarSubprocessoChefeDireto(page, descProcessoMapeamento)](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#56-93) | Add `'SECAO_211'` |
| [cdu-16.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-16.spec.ts#L124) | 124 | [acessarSubprocessoChefeDireto(page, descProcessoMapeamento)](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#56-93) | Add `'SECAO_211'` |
| [cdu-16.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-16.spec.ts#L188) | 188 | [acessarSubprocessoChefeDireto(page, descProcessoRevisao)](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#56-93) | Add `'SECAO_211'` |
| [cdu-33.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-33.spec.ts#L57) | 57 | [acessarSubprocessoChefeDireto(page, descMapeamento)](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#56-93) | Add `'SECAO_212'` |
| [cdu-33.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-33.spec.ts#L87) | 87 | [acessarSubprocessoChefeDireto(page, descMapeamento)](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#56-93) | Add `'SECAO_212'` |
| [cdu-33.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-33.spec.ts#L144) | 144 | [acessarSubprocessoChefeDireto(page, descRevisao)](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts#56-93) | Add `'SECAO_212'` |

#### [MODIFY] Spec files — [abrirModalImpacto](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#167-177) / [verificarBotaoImpactoAusente](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#151-166) callers

| Spec | Function | Context | New function |
|------|----------|---------|-------------|
| [cdu-08.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-08.spec.ts#L123) | [abrirModalImpacto](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#167-177) | Edit (Chefe) | → `abrirModalImpactoEdicao` |
| [cdu-08.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-08.spec.ts#L79) | [verificarBotaoImpactoAusente](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#151-166) | Edit (Chefe) | → `verificarBotaoImpactoAusenteEdicao` |
| [cdu-12.spec.ts](file:///Users/leonardo/sgc/e2e/cdu-12.spec.ts) | [abrirModalImpacto](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts#167-177) (5 calls) | Needs checking per call | → `abrirModalImpactoEdicao` or `...Visualizacao` |

---

## Verification Plan

### Automated Tests

Incremental — refactor one helper at a time, verify with affected specs:

1. [helpers-processos.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-processos.ts) → `npx playwright test e2e/cdu-01.spec.ts e2e/cdu-02.spec.ts`
2. [helpers-navegacao.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-navegacao.ts) → `npx playwright test e2e/smoke.spec.ts`
3. [helpers-mapas.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-mapas.ts) → `npx playwright test e2e/cdu-11.spec.ts e2e/cdu-17.spec.ts`
4. [helpers-atividades.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-atividades.ts) → `npx playwright test e2e/cdu-08.spec.ts e2e/cdu-12.spec.ts`
5. [helpers-analise.ts](file:///Users/leonardo/sgc/e2e/helpers/helpers-analise.ts) + spec updates → `npx playwright test e2e/cdu-15.spec.ts e2e/cdu-16.spec.ts e2e/cdu-33.spec.ts`

### TypeScript check
```bash
npm run typecheck:e2e
```
