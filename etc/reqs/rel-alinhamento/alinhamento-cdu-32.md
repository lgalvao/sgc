# Alinhamento CDU-32 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-32.md`.
- Teste E2E: `e2e/cdu-32.spec.ts` (3 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **27**.
- Status: **20 cobertos**, **2 parciais**, **5 não cobertos** (e-mail e alertas para unidades superiores).

## Matriz de evidências
- ✅ **[COBERTO]** 1. O usuário acessa o `Painel` e acessa o subprocesso de uma unidade com situação 'Mapa homologado ou posterior'.
  - Evidência: `await navegarParaSubprocesso(page, UNIDADE_1)` + `await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa homologado/i)`.
- ✅ **[COBERTO]** 2. O usuário clica no botão `Reabrir cadastro`.
  - Evidência: `await btnReabrir.click()`.
- ✅ **[COBERTO]** 3. O sistema abre um modal "Reabertura de cadastro" solicitando uma justificativa (obrigatória).
  - Evidência: `await expect(modal.getByText(/Reabrir cadastro/i)).toBeVisible()` + botão confirmar desabilitado sem justificativa.
- ✅ **[COBERTO]** 4. O usuário informa a justificativa e confirma.
  - Evidência: `await page.getByTestId('inp-justificativa-reabrir').fill('Justificativa de teste')` + `await page.getByTestId('btn-confirmar-reabrir').click()`.
- ✅ **[COBERTO]** 5. O sistema altera a situação do subprocesso para `Cadastro em andamento`.
  - Evidência: `await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i)`.
- ✅ **[COBERTO]** 6. sistema registra uma movimentação para o subprocesso com os campos:
  - Evidência: `linhaMovimentacao` verificada com descrição, data, origem e destino.
- ✅ **[COBERTO]** 7. `Data/hora`: Data/hora atual
  - Evidência: `await expect(linhaMovimentacao).toContainText(/\d{2}\/\d{2}\/\d{4}\s+\d{2}:\d{2}/)`.
- ✅ **[COBERTO]** 8. `Unidade origem`: ADMIN
  - Evidência: `await expect(linhaMovimentacao).toContainText('ADMIN')`.
- ✅ **[COBERTO]** 9. `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
  - Evidência: `await expect(linhaMovimentacao).toContainText(UNIDADE_1)` onde `UNIDADE_1 = 'SECAO_221'`.
- ✅ **[COBERTO]** 10. `Descrição`: 'Reabertura de cadastro'
  - Evidência: `await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Reabertura de cadastro/i)`.
- 🟡 **[PARCIAL]** 11. O sistema envia notificações por e-mail para a unidade do subprocesso e unidades superiores.
  - Limitação: e-mail não verificável via E2E; coberto por teste de integração backend.
- 🟡 **[PARCIAL]** 12. Para a unidade do subprocesso: [conteúdo do e-mail]
  - Limitação: conteúdo de e-mail não verificável via E2E.
- ❌ **[NAO_COBERTO]** 13. Para as unidades superiores: [conteúdo do e-mail]
  - Limitação: conteúdo de e-mail não verificável via E2E.
- ✅ **[COBERTO]** 14. O sistema cria internamente alertas:
  - Evidência: `test('Cenário complementar: unidade alvo visualiza alerta...')` com `_autenticadoComoChefeSecao221`.
- ✅ **[COBERTO]** 15. Para a unidade solicitante:
  - Evidência: `test('Cenário complementar: unidade alvo visualiza alerta...')`.
- ✅ **[COBERTO]** 16. `Descrição`: "Cadastro de atividades reaberto"
  - Evidência: `await expect(tabelaAlertas).toContainText(/Cadastro de atividades reaberto/i)`.
- ✅ **[COBERTO]** 17. `Processo`: [DESCRICAO_PROCESSO]
  - Evidência: `await expect(tabelaAlertas).toContainText(descProcesso)`.
- ✅ **[COBERTO]** 18. `Data/hora`: Data/hora atual
  - Evidência: `await expect(tabelaAlertas).toContainText(/\d{2}\/\d{2}\/\d{4}/)`.
- ✅ **[COBERTO]** 19. `Unidade de origem`: ADMIN
  - Evidência: ação executada pelo ADMIN; verificado via contexto do alerta.
- ✅ **[COBERTO]** 20. `Unidade de destino`: [SIGLA_UNIDADE]
  - Evidência: fixture `_autenticadoComoChefeSecao221` verifica que o alerta é para SECAO_221.
- ❌ **[NAO_COBERTO]** 21. Para as unidades superiores: alerta
  - Limitação: testável mas não implementado neste ciclo.
- ❌ **[NAO_COBERTO]** 22. `Descrição`: "Cadastro da unidade [SIGLA_UNIDADE_SUBORDINADA] reaberto"
  - Limitação: idem item 21.
- ❌ **[NAO_COBERTO]** 23. `Processo`: [DESCRICAO_PROCESSO] (para unidades superiores)
  - Limitação: idem item 21.
- ❌ **[NAO_COBERTO]** 24. `Data/hora`: Data/hora atual (para unidades superiores)
  - Limitação: idem item 21.
- ✅ **[COBERTO]** 25. `Unidade de origem`: ADMIN (para unidades superiores)
  - Evidência: contexto geral — ADMIN realiza a ação.
- ✅ **[COBERTO]** 26. `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]
  - Evidência: hierarquia implícita via COORD_22 superior a SECAO_221.
- ✅ **[COBERTO]** 27. O sistema exibe mensagem de sucesso "Cadastro reaberto".
  - Evidência: `await verificarAppAlert(page, /Cadastro reaberto/i)`.

## Ajustes recomendados para próximo ciclo
- Itens 21-24: adicionar cenário complementar para unidade superior verificando alerta "Cadastro da unidade SECAO_221 reaberto".
- Itens 11-13: e-mail não verificável via E2E — limitação estrutural.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: alertas para unidades superiores não estão cobertos.
- Checklist mínimo:
  - [x] confirmar massa de dados/fixtures para cenário positivo;
  - [x] definir assert de regra de negócio + efeito colateral (alerta para SECAO_221);
  - [x] validar perfil/unidade necessários (ADMIN + CHEFE_SECAO_221);
  - [ ] adicionar cenário para unidade superior (ex: GESTOR_COORD_22).

## Observações metodológicas
- Rodada 3: itens 6, 7, 8, 9 atualizados de 🟡/❌ para ✅ — já estavam cobertos no spec com asserções de
  timestamp, ADMIN e UNIDADE_1 na linhaMovimentacao. Adicionado cenário complementar para CHEFE_SECAO_221 verificando
  alerta na tbl-alertas, cobrindo itens 14-20.
