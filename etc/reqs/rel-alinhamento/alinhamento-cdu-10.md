# Alinhamento CDU-10 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-10.md`.
- Teste E2E: `e2e/cdu-10.spec.ts` (7 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **17**.
- Status: **15 cobertos**, **0 parciais**, **2 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências

- ✅ **[COBERTO]** 1. No `Painel`, o usuário clica no processo de revisão na situação 'Em andamento'.
  - Evidência: `e2e/cdu-10.spec.ts:35` -> `await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);`

- ✅ **[COBERTO]** 2. O sistema mostra tela `Detalhes do subprocesso` para a unidade.
  - Evidência: `e2e/cdu-10.spec.ts:57` -> `await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO, level: 2})).toBeVisible();`

- ✅ **[COBERTO]** 3. O usuário clica em `Atividades e conhecimentos`.
  - Evidência: `e2e/cdu-10.spec.ts:36` -> `await navegarParaAtividades(page);`

- ✅ **[COBERTO]** 4. O sistema mostra a tela `Cadastro de atividades e conhecimentos` preenchida com os dados cadastrados/revisados até o momento.
  - Evidência: `e2e/cdu-10.spec.ts:57` -> `await expect(page.getByTestId('inp-nova-atividade')).toBeVisible();`
  - Evidência (4.1): `e2e/cdu-10.spec.ts:64` -> `await expect(page.getByTestId('chk-disponibilizacao-sem-mudancas')).toBeVisible();` (teste 1.2)

- ✅ **[COBERTO]** 5. Se o subprocesso tiver retornado de análise, o botão `Histórico de análise` é habilitado.
  - Evidência: `e2e/cdu-10.spec.ts:144` -> `const modal = await abrirHistoricoAnalise(page);` (Cenário 3 - após devolução)

- ✅ **[COBERTO]** 5.1. Modal histórico com tabela data/hora, sigla da unidade, resultado e observações.
  - Evidência: `e2e/cdu-10.spec.ts:145` -> `await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);`
  - Evidência: `e2e/cdu-10.spec.ts:146` -> `await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivoDevolucao);`
  - Evidência: `e2e/cdu-10.spec.ts:188` -> `await expect(modal.getByTestId('cell-resultado-1')).toHaveText(/Devolu[cç][aã]o/i);` (Cenário 4 - múltiplas)

- ✅ **[COBERTO]** 6. O usuário faz mudanças ou marca o checkbox `Disponibilização sem mudanças`.
  - Evidência: `e2e/cdu-10.spec.ts:74` -> `await checkboxSemMudancas.check();` (teste 1.2)
  - Evidência: `e2e/cdu-10.spec.ts:38` -> `await adicionarAtividade(page, ...)` (setup - mudanças diretas)

- ✅ **[COBERTO]** 7. O sistema verifica se todas as atividades têm ao menos um conhecimento associado e bloqueia disponibilização caso negativo.
  - Evidência: `e2e/cdu-10.spec.ts:101` -> `await adicionarAtividade(page, atividadeIncompleta);`
  - Evidência: `e2e/cdu-10.spec.ts:102` -> `await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();`

- ✅ **[COBERTO]** 8. O sistema habilita o botão `Disponibilizar` após atender às condições.
  - Evidência: `e2e/cdu-10.spec.ts:104` -> `await adicionarConhecimento(page, atividadeIncompleta, 'Conhecimento corretivo');`
  - Evidência: `e2e/cdu-10.spec.ts:77` -> `await expect(botaoDisponibilizar).toBeEnabled();` (Cenário 2)

- ✅ **[COBERTO]** 9. O usuário clica no botão `Disponibilizar`.
  - Evidência: `e2e/cdu-10.spec.ts:78` -> `await botaoDisponibilizar.click();`

- ✅ **[COBERTO]** 10. O sistema mostra diálogo de confirmação com título e mensagem.
  - Evidência: `e2e/cdu-10.spec.ts:80` -> `await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TITULO)).toBeVisible();`
  - Evidência: `e2e/cdu-10.spec.ts:81` -> `await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TEXTO)).toBeVisible();`

- ✅ **[COBERTO]** 8.1. Caso o usuário escolha `Cancelar`, o sistema interrompe a disponibilização.
  - Evidência: `e2e/cdu-10.spec.ts:107` -> `await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();` (Cenário 1)
  - Evidência: `e2e/cdu-10.spec.ts:218` -> `await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();` (Cenário 5)
  - Evidência: `e2e/cdu-10.spec.ts:219` -> `await expect(page.getByRole('dialog')).toBeHidden();`

- ✅ **[COBERTO]** 11. O usuário escolhe `Confirmar`.
  - Evidência: `e2e/cdu-10.spec.ts:82` -> `await page.getByTestId('btn-confirmar-disponibilizacao').click();`

- ✅ **[COBERTO]** 12. O sistema altera a situação do subprocesso para 'Revisão do cadastro disponibilizada'.
  - Evidência: `e2e/cdu-10.spec.ts:91` -> `await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);`

- ✅ **[COBERTO]** 13. O sistema registra uma movimentação para o subprocesso.
  - Evidência: `e2e/cdu-10.spec.ts:92` -> `await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.REVISAO_CADASTRO_DISPONIBILIZADA);`

- ❌ **[NAO_COBERTO]** 14. O sistema notifica a unidade superior hierárquica por e-mail.
  - Não testável via E2E (envio de e-mail é comportamento de infraestrutura).

- ✅ **[COBERTO]** 15. O sistema cria internamente um alerta para a unidade superior.
  - Evidência: `e2e/cdu-10.spec.ts:88` -> `await expect(page.getByTestId('tbl-alertas')).toContainText(TEXTOS.alerta.SUCESSO_REVISAO_DISPONIBILIZADA(UNIDADE_ALVO));` (login gestor)

- ❌ **[NAO_COBERTO]** 16. O sistema define a data/hora de conclusão da Etapa 1 como sendo a data/hora atual.
  - Não há assertion explícita verificando este campo de data/hora de conclusão no spec.

- ✅ **[COBERTO]** 17. O sistema redireciona para o `Painel` e mostra a mensagem "Revisão do cadastro disponibilizada".
  - Evidência: `e2e/cdu-10.spec.ts:84` -> `await expect(page.getByText(/disponibilizada?|Disponibilizado/i).first()).toBeVisible();`
  - Evidência: `e2e/cdu-10.spec.ts:85` -> `await verificarPaginaPainel(page);`

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Gaps remanescentes:
  - Item 14 (e-mail de notificação): não testável via E2E por natureza da funcionalidade.
  - Item 16 (data/hora de conclusão de etapa): poderia ser verificado indiretamente via API ou campo de prazo na tela de detalhes; baixa prioridade.
