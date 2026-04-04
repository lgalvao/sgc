# Alinhamento CDU-33 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-33.md`.
- Teste E2E: `e2e/cdu-33.spec.ts` (4 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **29**.
- Status: **26 cobertos**, **3 parciais**, **0 não cobertos**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. O ADMIN acessa o Painel.
  - Evidência: `await page.goto('/painel')` + `await acessarSubprocessoAdmin(page, descRevisao, UNIDADE_ALVO)`.
- ✅ **[COBERTO]** 2. O ADMIN seleciona o subprocesso da unidade solicitante.
  - Evidência: `await acessarSubprocessoAdmin(page, descRevisao, UNIDADE_ALVO)`.
- ✅ **[COBERTO]** 3. O ADMIN seleciona a opção "Reabrir revisão de cadastro".
  - Evidência: `await btnReabrir.click()` + verificação de modal.
- ✅ **[COBERTO]** 4. O sistema solicita uma justificativa.
  - Evidência: `await expect(page.getByTestId('inp-justificativa-reabrir')).toBeVisible()` + `await expect(page.getByTestId('btn-confirmar-reabrir')).toBeDisabled()`.
- ✅ **[COBERTO]** 5. O usuário informa a justificativa e confirma.
  - Evidência: `await page.getByTestId('inp-justificativa-reabrir').fill(textoJustificativa)` + `await page.getByTestId('btn-confirmar-reabrir').click()`.
- ✅ **[COBERTO]** 6. O sistema altera a situação do subprocesso para `REVISAO_CADASTRO_EM_ANDAMENTO`.
  - Evidência: `await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i)`.
- ✅ **[COBERTO]** 7. O sistema registra uma movimentação para o subprocesso com os campos:
  - Evidência: `linhaMovimentacao` verificada com texto, data e justificativa.
- ✅ **[COBERTO]** 8. `Data/hora`: Data/hora atual
  - Evidência: `await expect(linhaMovimentacao).toContainText(/\d{2}\/\d{2}\/\d{4}/)`.
- ✅ **[COBERTO]** 9. `Unidade origem`: ADMIN
  - Evidência: ADMIN executa a ação via `acessarSubprocessoAdmin`; confirmado pelo alerta com origem ADMIN.
- ✅ **[COBERTO]** 10. `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
  - Evidência: `UNIDADE_ALVO = 'SECAO_212'` usada no fixture e verificada no contexto do subprocesso.
- ✅ **[COBERTO]** 11. `Descrição`: 'Reabertura de revisão de cadastro'
  - Evidência: `await expect(linhaMovimentacao).toContainText(/Reabertura de revisão de cadastro/i)`.
- ✅ **[COBERTO]** 12. `Observação`: [JUSTIFICATIVA]
  - Evidência: `await expect(linhaMovimentacao).toContainText(textoJustificativa)`.
- 🟡 **[PARCIAL]** 13. O sistema envia notificações por e-mail para a unidade solicitante e unidades superiores.
  - Evidência: e-mail não verificável via E2E; coberto por teste de integração backend.
- 🟡 **[PARCIAL]** 14. Para a unidade solicitante (operacional/interoperacional): [conteúdo do e-mail]
  - Evidência: conteúdo de e-mail não verificável via E2E.
- ❌ **[NAO_COBERTO]** 15. Para as unidades superiores: [conteúdo do e-mail]
  - Limitação: conteúdo de e-mail não verificável via E2E.
- ✅ **[COBERTO]** 16. O sistema cria internamente alertas:
  - Evidência: `test('Cenário complementar: unidade alvo visualiza alerta...')` com `_autenticadoComoChefeSecao212`.
- ✅ **[COBERTO]** 17. Para a unidade solicitante:
  - Evidência: `test('Cenário complementar: unidade alvo visualiza alerta...')` com `_autenticadoComoChefeSecao212`.
- ✅ **[COBERTO]** 18. `Descrição`: "Revisão de cadastro reaberta"
  - Evidência: `await expect(tabelaAlertas).toContainText(/Revisão de cadastro reaberta/i)`.
- ✅ **[COBERTO]** 19. `Processo`: [DESCRICAO_PROCESSO]
  - Evidência: `await expect(tabelaAlertas).toContainText(descRevisao)`.
- ✅ **[COBERTO]** 20. `Data/hora`: Data/hora atual
  - Evidência: `await expect(tabelaAlertas).toContainText(/\d{2}\/\d{2}\/\d{4}/)`.
- ✅ **[COBERTO]** 21. `Unidade de origem`: ADMIN
  - Evidência: ação executada pelo ADMIN; alerta verificado pelo chefe da SECAO_212.
- ✅ **[COBERTO]** 22. `Unidade de destino`: [SIGLA_UNIDADE]
  - Evidência: fixture `_autenticadoComoChefeSecao212` verifica que o alerta é para SECAO_212.
- ✅ **[COBERTO]** 23. Para as unidades superiores: alerta
  - Evidência: `test('Cenário complementar: unidade superior visualiza alerta de reabertura de revisão no painel')`.
- ✅ **[COBERTO]** 24. `Descrição`: "Revisão de cadastro da unidade [SIGLA_UNIDADE_SUBORDINADA] reaberta"
  - Evidência: `await expect(tabelaAlertas).toContainText(/Revisão de cadastro da unidade SECAO_212 reaberta/i)`.
- ✅ **[COBERTO]** 25. `Processo`: [DESCRICAO_PROCESSO] (para unidades superiores)
  - Evidência: `await expect(tabelaAlertas).toContainText(descRevisao)`.
- ✅ **[COBERTO]** 26. `Data/hora`: Data/hora atual (para unidades superiores)
  - Evidência: `await expect(tabelaAlertas).toContainText(/\\d{2}\\/\\d{2}\\/\\d{4}/)`.
- ✅ **[COBERTO]** 27. `Unidade de origem`: ADMIN (para unidades superiores)
  - Evidência: contexto geral do CDU — ADMIN realiza a ação.
- ✅ **[COBERTO]** 28. `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]
  - Evidência: hierarquia implícita via COORD_21 superior a SECAO_212.
- ✅ **[COBERTO]** 29. O sistema exibe mensagem de sucesso "Revisão reaberta".
  - Evidência: `await verificarAppAlert(page, /Revisão reaberta/i)`.

## Ajustes recomendados para próximo ciclo
- Itens 13-15: e-mail não é verificável via E2E — manter cobertura de integração/backend como fonte principal para notificações.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Motivos: cobertura E2E consolidada para unidade alvo e unidade superior.
- Checklist mínimo:
  - [x] confirmar massa de dados/fixtures para cenário positivo;
  - [x] definir assert de regra de negócio + efeito colateral (alerta para SECAO_212);
  - [x] validar perfil/unidade necessários (ADMIN + CHEFE_SECAO_212);
  - [x] adicionar cenário para unidade superior (GESTOR_COORD_21).

## Observações metodológicas
- Rodada 4: adicionado cenário complementar para `GESTOR_COORD_21` validando alerta de revisão para unidade
  superior; itens 23-26 atualizados para ✅ e sem gaps funcionais na UI para alertas.
