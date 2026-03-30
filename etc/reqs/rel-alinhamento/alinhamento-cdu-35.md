# Alinhamento CDU-35 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-35.md`.
- Teste E2E: `e2e/cdu-35.spec.ts` (1 cenário `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **12**.
- Status: **12 cobertos**, **0 parciais**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. O usuário acessa Relatórios.
  - Evidência: `await page.getByRole('link', {name: /Relatórios/i}).click()` + `await expect(page).toHaveURL(/\/relatorios/)`.
- ✅ **[COBERTO]** 2. O usuário seleciona a opção "Andamento de processo".
  - Evidência: `await expect(page.getByRole('tab', {name: /Andamento de processo/i})).toBeVisible()`.
- ✅ **[COBERTO]** 3. O usuário seleciona o Processo desejado.
  - Evidência: `await selectProcesso.selectOption({label: descricaoProcesso})`.
- ✅ **[COBERTO]** 4. O sistema exibe o relatório em tela contendo as seguintes colunas:
  - Evidência: `const tabelaRelatorio = page.locator('table').last()` + `await expect(tabelaRelatorio).toBeVisible()`.
- ✅ **[COBERTO]** 5. Sigla da unidade
  - Evidência: `await expect(tabelaRelatorio.locator('th', {hasText: /Sigla/i}).first()).toBeVisible()` + linha contém `ASSESSORIA_12`.
- ✅ **[COBERTO]** 6. Nome da unidade
  - Evidência: `await expect(tabelaRelatorio.locator('th', {hasText: /Nome/i}).first()).toBeVisible()`.
- ✅ **[COBERTO]** 7. Situação atual do subprocesso da unidade, para o processo selecionado
  - Evidência: `await expect(tabelaRelatorio.locator('th', {hasText: /Situação|Situacao/i}).first()).toBeVisible()`.
- ✅ **[COBERTO]** 8. Data da última movimentação
  - Evidência: `await expect(tabelaRelatorio.locator('th', {hasText: /Data/i}).first()).toBeVisible()` + `await expect(primeiraLinha).toContainText(/\d{4}-\d{2}-\d{2}|\d{2}\/\d{2}\/\d{4}/)`.
- ✅ **[COBERTO]** 9. Responsável
  - Evidência: `await expect(tabelaRelatorio.locator('th', {hasText: /Responsável|Responsavel/i}).first()).toBeVisible()`.
- ✅ **[COBERTO]** 10. Titular (Se não for o responsavel)
  - Evidência: `await expect(tabelaRelatorio.locator('th', {hasText: /Titular/i}).first()).toBeVisible()`.
- ✅ **[COBERTO]** 11. O usuário pode optar por exportar os dados para PDF clicando no botao `PDF`.
  - Evidência: `await expect(botaoPdf).toBeVisible()` + `await botaoPdf.click()`.
- ✅ **[COBERTO]** 12. O sistema gera o arquivo selecionado e o disponibiliza para download.
  - Evidência: `page.waitForEvent('download')` + `expect(download.suggestedFilename()).toContain('relatorio-andamento-...')`.

## Ajustes recomendados para próximo ciclo
- Nenhum gap pendente. Cobertura completa para os itens verificáveis via E2E.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Checklist mínimo:
  - [x] confirmar massa de dados/fixtures para cenário positivo;
  - [x] definir assert de regra de negócio (colunas presentes, linha com valores corretos);
  - [x] validar perfil/unidade necessários (ADMIN);
  - [x] mapear se precisa teste de integração backend complementar.

## Observações metodológicas
- Rodada 3: alinhamento atualizado — itens 8, 9, 10 (Data, Responsável, Titular) estavam marcados como ❌ mas estão
  efetivamente cobertos no spec por asserções de `th` e de linha da tabela. Nenhuma mudança no spec foi necessária.
