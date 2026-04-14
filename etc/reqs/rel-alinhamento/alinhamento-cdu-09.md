# Alinhamento CDU-09 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-09.md`.
- Teste E2E: `e2e/cdu-09.spec.ts` (3 cenários `test` + 1 setup, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **29**.
- Status: **18 cobertos**, **7 parciais**, **4 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências

- ✅ **[COBERTO]** 1. No painel, o usuário escolhe um processo de mapeamento na situação 'Em andamento'.
  - Evidência: `e2e/cdu-09.spec.ts:42` -> `await acessarSubprocessoChefeDireto(page, descricaoProcesso, UNIDADE_ALVO);`

- ✅ **[COBERTO]** 2. O sistema mostra tela `Detalhes do subprocesso` da unidade.
  - Evidência: `e2e/cdu-09.spec.ts:43` -> `await acessarSubprocessoChefeDireto(page, descricaoProcesso, UNIDADE_ALVO);`

- ✅ **[COBERTO]** 3. O usuário clica no card `Atividades e conhecimentos`.
  - Evidência: `e2e/cdu-09.spec.ts:44` -> `await navegarParaAtividades(page);`

- ✅ **[COBERTO]** 4. O sistema mostra a tela `Cadastro de atividades e conhecimentos`, preenchida com os dados cadastrados até o momento.
  - Evidência: `e2e/cdu-09.spec.ts:46` -> `await adicionarAtividade(page, atividadeDesc);`

- ❌ **[NAO_COBERTO]** 5. Se o subprocesso já tiver sido disponibilizado anteriormente e estiver localizado em unidade diferente da unidade ativa do usuário, o sistema mostra um alerta fixo no topo da tela com o texto: "Cadastro disponibilizado para análise pelas unidades superiores.", não permitindo edição ou disponibilização.
  - Não há assertion verificando esse alerta fixo na tela de cadastro de atividades quando subprocesso está em unidade diferente.

- ✅ **[COBERTO]** 6. Se o subprocesso tiver retornado de análise, o botão `Histórico de análise` é habilitado.
  - Evidência: `e2e/cdu-09.spec.ts:112` -> `const modal = await abrirHistoricoAnalise(page);` (Cenário 3 - após devolução do gestor)

- ✅ **[COBERTO]** 7. Se o usuário clicar no botão `Histórico de análise`, o sistema mostra, em tela modal, os dados das análises do cadastro.
  - Evidência: `e2e/cdu-09.spec.ts:112` -> `const modal = await abrirHistoricoAnalise(page);`
  - Evidência: `e2e/cdu-09.spec.ts:116` -> `await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);`

- ✅ **[COBERTO]** 8. As análises deverão ser apresentadas em uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações.
  - Evidência: `e2e/cdu-09.spec.ts:113` -> `await expect(modal.getByTestId('cell-dataHora-0')).not.toHaveText('');`
  - Evidência: `e2e/cdu-09.spec.ts:114` -> `await expect(modal.getByTestId('cell-unidade-0')).toHaveText('COORD_22');`
  - Evidência: `e2e/cdu-09.spec.ts:115` -> `await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);`
  - Evidência: `e2e/cdu-09.spec.ts:116` -> `await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivo);`

- ✅ **[COBERTO]** 9. O usuário escolhe `Disponibilizar`.
  - Evidência: `e2e/cdu-09.spec.ts:49` -> `await page.getByTestId('btn-cad-atividades-disponibilizar').click();`

- ✅ **[COBERTO]** 10. O sistema verifica se todas as atividades têm ao menos um conhecimento associado.
  - Evidência: `e2e/cdu-09.spec.ts:48` -> `await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();`

- 🟡 **[PARCIAL]** 11. Se houver esses problemas de validação, o sistema indica quais atividades estão precisando de adição de conhecimentos e interrompe a operação de disponibilização.
  - O btn-cad-atividades-disponibilizar fica disabled, confirmando o bloqueio, mas não há assertion explícita verificando qual atividade específica é indicada com o problema.
  - Evidência: `e2e/cdu-09.spec.ts:48` -> `await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeDisabled();`

- ✅ **[COBERTO]** 12. O sistema mostra um diálogo de confirmação com título "Disponibilização do cadastro" e mensagem correspondente.
  - Evidência: `e2e/cdu-09.spec.ts:51` -> `await expect(modalConfirmacao.getByRole('heading', {name: TEXTOS.atividades.MODAL_DISPONIBILIZAR_TITULO})).toBeVisible();`
  - Evidência: `e2e/cdu-09.spec.ts:52` -> `await expect(modalConfirmacao.getByText(TEXTOS.atividades.MODAL_DISPONIBILIZAR_TEXTO)).toBeVisible();`

- ✅ **[COBERTO]** 13. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de disponibilização, permanecendo na mesma tela.
  - Evidência: `e2e/cdu-09.spec.ts:54` -> `await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();`
  - Evidência: `e2e/cdu-09.spec.ts:55` -> `await expect(page.getByRole('heading', {name: TEXTOS.atividades.TITULO, level: 2})).toBeVisible();`

- ✅ **[COBERTO]** 14. O usuário escolhe `Confirmar`.
  - Evidência: `e2e/cdu-09.spec.ts:75` -> `await page.getByTestId('btn-confirmar-disponibilizacao').click();`

- ✅ **[COBERTO]** 15. O sistema altera a situação do subprocesso da unidade para 'Cadastro disponibilizado'.
  - Evidência: `e2e/cdu-09.spec.ts:84` -> `await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);`

- ✅ **[COBERTO]** 16. O sistema registra uma movimentação para o subprocesso.
  - Evidência: `e2e/cdu-09.spec.ts:85` -> `await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.CADASTRO_DISPONIBILIZADO);`

- ❌ **[NAO_COBERTO]** 17. `Data/hora` da movimentação: Data/hora atual.
  - Não há assertion explícita verificando o campo data/hora da movimentação com validação de timestamp.

- 🟡 **[PARCIAL]** 18. `Unidade origem` da movimentação: [SIGLA_UNIDADE_SUBPROCESSO].
  - A movimentação é verificada via containsText mas sem assertion isolada do campo Unidade origem.
  - Evidência: `e2e/cdu-09.spec.ts:85` -> `await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.CADASTRO_DISPONIBILIZADO);`

- 🟡 **[PARCIAL]** 19. `Unidade destino` da movimentação: [SIGLA_UNIDADE_SUPERIOR].
  - A movimentação é verificada mas sem assertion isolada do campo Unidade destino.

- ✅ **[COBERTO]** 20. `Descrição` da movimentação: 'Disponibilização do cadastro'.
  - Evidência: `e2e/cdu-09.spec.ts:85` -> `await expect(page.getByTestId('tbl-movimentacoes')).toContainText(TEXTOS.movimentacao.CADASTRO_DISPONIBILIZADO);`

- ❌ **[NAO_COBERTO]** 21. O sistema notifica a unidade superior hierárquica por e-mail.
  - Não testável via E2E (envio de e-mail é comportamento de infraestrutura).

- ✅ **[COBERTO]** 22. O sistema cria internamente um alerta para a unidade superior.
  - Evidência: `e2e/cdu-09.spec.ts:81` -> `await verificarAlertaPainel(page, /Cadastro da unidade SECAO_221 disponibilizado para análise/i);`

- ✅ **[COBERTO]** 23. `Descrição` do alerta: "Cadastro da unidade [SIGLA_UNIDADE_SUBPROCESSO] disponibilizado para análise".
  - Evidência: `e2e/cdu-09.spec.ts:81` -> `await verificarAlertaPainel(page, /Cadastro da unidade SECAO_221 disponibilizado para análise/i);`

- 🟡 **[PARCIAL]** 24. `Processo` do alerta: [DESCRICAO_PROCESSO].
  - O alerta é verificado via texto mas o campo Processo não é isolado no assert.

- ❌ **[NAO_COBERTO]** 25. `Data/hora` do alerta: Data/hora atual.
  - Não há assertion explícita verificando o campo data/hora do alerta.

- 🟡 **[PARCIAL]** 26. `Unidade de origem` do alerta: [SIGLA_UNIDADE_SUBPROCESSO].
  - Não há assertion isolada do campo Unidade de origem do alerta.

- 🟡 **[PARCIAL]** 27. `Unidade de destino` do alerta: [SIGLA_UNIDADE_SUPERIOR].
  - Não há assertion isolada do campo Unidade de destino do alerta.

- 🟡 **[PARCIAL]** 28. O sistema define a data/hora de conclusão da etapa 1 do subprocesso da unidade como sendo a data/hora atual.
  - Não há assertion explícita verificando este campo; a mudança de situação implica que ocorreu, mas sem verificação direta.

- ✅ **[COBERTO]** 29. O sistema redireciona para o Painel, mostrando a mensagem "Cadastro de atividades disponibilizado".
  - Evidência: `e2e/cdu-09.spec.ts:77` -> `await verificarToast(page, TEXTOS.sucesso.CADASTRO_ATIVIDADES_DISPONIBILIZADO);`
  - Evidência: `e2e/cdu-09.spec.ts:78` -> `await verificarPaginaPainel(page);`

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Gaps remanescentes:
  - Item 5 (alerta fixo "Cadastro disponibilizado para análise pelas unidades superiores." na tela de cadastro): cenário a adicionar verificando que chefe não pode editar quando subprocesso está em análise.
  - Item 17 (data/hora da movimentação): adicionar assertion com regex de timestamp.
  - Item 21 (e-mail): não testável via E2E.
  - Item 25 (data/hora do alerta): adicionar assertion verificando campo data/hora no alerta.
  - Itens 18, 19, 24, 26, 27, 28 (campos individuais de movimentação/alerta): considerar assertions por célula específica na tabela.
