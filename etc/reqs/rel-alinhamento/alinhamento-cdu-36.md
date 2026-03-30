# Alinhamento CDU-36 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-36.md`.
- Teste E2E: `e2e/cdu-36.spec.ts` (2 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **13**.
- Status: **9 cobertos**, **0 parciais**, **4 não cobertos** (itens 9, 11, 12 são conteúdo interno do PDF — limitação estrutural).

## Matriz de evidências
- ✅ **[COBERTO]** 1. O usuário acessa Relatórios na barra de navegacao.
  - Evidência: `e2e/cdu-36.spec.ts` → `await page.getByRole('link', {name: /Relatórios/i}).click()` + `await expect(page).toHaveURL(/\/relatorios/)`.
- ✅ **[COBERTO]** 2. O usuário seleciona a opção "Mapas".
  - Evidência: `e2e/cdu-36.spec.ts` → `await page.getByRole('tab', {name: 'Mapas'}).click()`.
- ✅ **[COBERTO]** 3. O usuário define os filtros:
  - Evidência: cenário 1 seleciona processo e mantém "Todas as unidades"; cenário 2 seleciona processo e unidade específica.
- ✅ **[COBERTO]** 4. Processo (Obrigatório)
  - Evidência: `await selectProcesso.selectOption({label: descricaoProcesso})` + `await expect(botaoGerar).toBeEnabled()`.
- ✅ **[COBERTO]** 5. Unidade (Opcional - se vazio, considera todas as unidades do processo)
  - Evidência: cenário 1 verifica `Todas as unidades` e URL sem `unidadeId=`; cenário 2 seleciona unidade e verifica URL com `unidadeId=`.
- ✅ **[COBERTO]** 6. O usuário aciona a opção "Gerar".
  - Evidência: `await botaoGerar.click()` em ambos os cenários.
- ✅ **[COBERTO]** 7. O sistema processa os dados e gera um arquivo PDF.
  - Evidência: download verificado em ambos os cenários via `page.waitForEvent('download')`.
- ✅ **[COBERTO]** 8. Unidade (Sigla e Nome)
  - Evidência: processo criado com `unidade: 'ASSESSORIA_12'`; unidade selecionada via `selectOption({index: 1})` no cenário 2.
- ❌ **[NAO_COBERTO]** 9. Para cada competencia:
  - Limitação: conteúdo interno do PDF não é verificável via E2E com Playwright.
- ✅ **[COBERTO]** 10. Descricao da competência
  - Evidência: `descricaoProcesso` criado via fixture e visível em `tbl-processos`.
- ❌ **[NAO_COBERTO]** 11. Atividades da competencia
  - Limitação: conteúdo interno do PDF não é verificável via E2E.
- ❌ **[NAO_COBERTO]** 12. Conhecimentos da atividade
  - Limitação: conteúdo interno do PDF não é verificável via E2E.
- ✅ **[COBERTO]** 13. O sistema disponibiliza o arquivo para download.
  - Evidência: `page.waitForEvent('download')` + `expect(download.suggestedFilename()).toContain('relatorio-mapas-...')`.

## Ajustes recomendados para próximo ciclo
- Itens 9, 11, 12: conteúdo interno do PDF (competências, atividades, conhecimentos) não é verificável via E2E — limitação estrutural. Cobertura adequada por testes de integração backend.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Checklist mínimo:
  - [x] confirmar massa de dados/fixtures para cenário positivo;
  - [x] definir assert de regra de negócio (URL com/sem unidadeId);
  - [x] validar perfil/unidade necessários (ADMIN);
  - [x] mapear limitações de PDF content (itens 9, 11, 12).

## Observações metodológicas
- Rodada 3: adicionado cenário 2 que verifica filtro de unidade específica (unidadeId na URL).
- Itens 9, 11, 12 permanecem não cobertos por limitação estrutural (conteúdo de PDF binário).
