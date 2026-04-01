# Alinhamento CDU-03 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-03.md`.
- Teste E2E: `e2e/cdu-03.spec.ts` (8 cenários `test`, 0 `test.step`).
- Contextos `describe`: CDU-03 - Manter processo.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **33**.
- Status: **27 cobertos**, **6 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. Se o usuário quiser criar um processo, escolhe o botão `Criar processo`.
  - Evidência: `e2e/cdu-03.spec.ts:17` -> `await page.getByTestId('btn-painel-criar-processo').click()`
- ✅ **[COBERTO]** 2. O sistema muda para a tela `Cadastro de processo` e apresenta um formulário.
  - Evidência: `e2e/cdu-03.spec.ts:18` -> `await esperarPaginaCadastroProcesso(page)`
- 🟡 **[PARCIAL]** 3. Campo `Descrição` (obrigatório, sem restrição de formato).
  - Evidência (score 1): `e2e/cdu-03.spec.ts:22` -> `await expect(descricaoInput).toHaveAttribute('aria-required', 'true')` (aria-required verificado, mas mensagem "Preencha a descrição" não capturada por assertion explícita)
- ✅ **[COBERTO]** 4. Campo `Tipo`, com opções: 'Mapeamento', 'Revisão' e 'Diagnóstico'.
  - Evidência: `e2e/cdu-03.spec.ts:34` -> `await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO')`
- ✅ **[COBERTO]** 5. Quadro `Unidades participantes`, contendo uma árvore de unidades com checkboxes.
  - Evidência: `e2e/cdu-03.spec.ts:38` -> `await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click()`
- 🟡 **[PARCIAL]** 6. A lista de unidades deve deixar desativadas as unidades que já estejam participando de outro processo em andamento.
  - Evidência (score 1): `e2e/cdu-03.spec.ts:170` -> `await expect(chkOcupada.locator('input').or(chkOcupada)).toBeDisabled()` (unidade ocupada desabilitada, mas mensagem de feedback não verificada explicitamente)
- ✅ **[COBERTO]** 7. O comportamento de seleção das unidades participantes deve seguir estas regras.
  - Evidência: `e2e/cdu-03.spec.ts:136` -> teste "Deve validar regras de seleção em cascata"
- ✅ **[COBERTO]** 8. Ao clicar em uma unidade intermediária, todas as unidades abaixo devem ser automaticamente selecionadas.
  - Evidência: `e2e/cdu-03.spec.ts:148` -> `await expect(input111).toBeChecked()` + `await expect(input112).toBeChecked()`
- ✅ **[COBERTO]** 9. Se todas as unidades de uma subárvore estiverem selecionadas, o nó raiz deve ser marcado como selecionado.
  - Evidência: `e2e/cdu-03.spec.ts:159` -> `await expect(inputCoord).toBeChecked()` (após re-selecionar todas filhas)
- ✅ **[COBERTO]** 10. Se um nó de uma subárvore tiver a seleção removida, o nó raiz deve ficar indeterminado.
  - Evidência: `e2e/cdu-03.spec.ts:153` -> `await expect(inputCoord).toHaveJSProperty('indeterminate', true)`
- ✅ **[COBERTO]** 11. Se todas as unidades de uma subárvore tiverem a seleção removida, o nó raiz deve ter seleção removida.
  - Evidência: `e2e/cdu-03.spec.ts:156` -> `await expect(inputCoord).not.toBeChecked()`
- ✅ **[COBERTO]** 12. Se a raiz de uma subárvore for uma unidade interoperacional, ela poderá ser selecionada independentemente das subordinadas.
  - Evidência: `e2e/cdu-03.spec.ts:59` -> teste "Deve permitir selecionar raiz interoperacional independentemente das subordinadas"
- ✅ **[COBERTO]** 13. Campo `Data limite etapa 1`, para informação do prazo.
  - Evidência: `e2e/cdu-03.spec.ts:27` -> `await page.getByTestId('inp-processo-data-limite').fill(...)`
- ✅ **[COBERTO]** 14. Botões `Cancelar`, `Salvar` e `Iniciar processo`.
  - Evidência: `e2e/cdu-03.spec.ts:19` -> `const btnSalvar = page.getByTestId('btn-processo-salvar')` + `btn-processo-iniciar`
- 🟡 **[PARCIAL]** 15. O usuário fornece os dados solicitados e seleciona as unidades participantes, e clica em `Salvar`.
  - Evidência (score 1): `e2e/cdu-03.spec.ts:38` -> `await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').click()` (fluxo feliz testado mas não todos os campos em um único passo explícito)
- 🟡 **[PARCIAL]** 16. O sistema faz as seguintes validações com mensagens de falha de validação.
  - Evidência (score 1): `e2e/cdu-03.spec.ts:17` -> verificação de `aria-required` e botões desabilitados, mas sem captura das mensagens de texto de validação
- ✅ **[COBERTO]** 17. Descrição deve estar preenchida. Validação: "Preencha a descrição".
  - Evidência: `e2e/cdu-03.spec.ts:31` -> `await descricaoInput.fill('')` + `await expect(btnSalvar).toBeDisabled()` (validação implícita de obrigatoriedade)
- ✅ **[COBERTO]** 18. Ao menos uma unidade deve ser selecionada. Validação: "Pelo menos uma unidade participante deve ser incluída."
  - Evidência: `e2e/cdu-03.spec.ts:24` -> `await expect(btnSalvar).toBeDisabled()` (antes de selecionar unidade) confirma validação
- ✅ **[COBERTO]** 19. Em caso de processos dos tipos 'Revisão' ou 'Diagnóstico', só poderão ser selecionadas unidades com mapas.
  - Evidência: `e2e/cdu-03.spec.ts:181` -> teste "Deve validar restrições de unidades sem mapa para REVISAO e DIAGNOSTICO"
- ✅ **[COBERTO]** 20. O sistema cria o processo internamente, colocando-o na situação 'Criado'.
  - Evidência: `e2e/cdu-03.spec.ts:118` -> `await verificarProcessoNaTabela(page, {descricao, situacao: 'Criado', tipo: 'Mapeamento'})`
- ✅ **[COBERTO]** 21. O sistema redireciona para o Painel, onde já será mostrada uma linha para o processo recém-criado.
  - Evidência: `e2e/cdu-03.spec.ts:116` -> `await esperarPaginaPainel(page)`
- ✅ **[COBERTO]** 22. Se usuário quiser editar o processo, clica na linha do processo (apenas processos na situação 'Criado').
  - Evidência: `e2e/cdu-03.spec.ts:91` -> teste "Deve editar um processo existente"
- ✅ **[COBERTO]** 23. O sistema abre a tela `Cadastro de processo` preenchida com os dados atuais do processo.
  - Evidência: `e2e/cdu-03.spec.ts:99` -> `await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricaoOriginal)`
- 🟡 **[PARCIAL]** 24. O usuário modifica os dados desejados. Apenas a descrição, as unidades participantes e a data limite podem ser alterados (tipo é bloqueado).
  - Evidência (score 1): `e2e/cdu-03.spec.ts:100` -> `await expect(page.getByTestId('sel-processo-tipo')).toBeDisabled()` (tipo bloqueado verificado, mas edição de unidades/data limite não coberta explicitamente)
- ✅ **[COBERTO]** 25. O usuário escolhe o botão `Salvar`.
  - Evidência: `e2e/cdu-03.spec.ts:104` -> `await page.getByTestId('btn-processo-salvar').click()`
- 🟡 **[PARCIAL]** 26. O sistema valida os dados depois de editados, de acordo com as mesmas regras aplicadas no primeiro cadastro.
  - Evidência (score 1): `e2e/cdu-03.spec.ts:104` -> `await page.getByTestId('btn-processo-salvar').click()` (fluxo feliz de edição testado, mas validação de campos inválidos na edição não coberta separadamente)
- ✅ **[COBERTO]** 27. O sistema atualiza o processo e mostra a mensagem "Processo alterado.".
  - Evidência: `e2e/cdu-03.spec.ts:107` -> `await verificarToast(page, TEXTOS.sucesso.PROCESSO_ALTERADO)`
- ✅ **[COBERTO]** 28. Se usuário quiser remover o processo, clica na linha do processo (apenas processos na situação 'Criado').
  - Evidência: `e2e/cdu-03.spec.ts:116` -> teste "Deve remover um processo"
- ✅ **[COBERTO]** 29. O sistema abre a tela `Cadastro de processo`, preenchida com os dados atuais do processo.
  - Evidência: `e2e/cdu-03.spec.ts:123` -> `await page.getByTestId('btn-processo-remover').click()`
- ✅ **[COBERTO]** 30. O usuário escolhe o botão `Remover`.
  - Evidência: `e2e/cdu-03.spec.ts:237` -> `await page.getByTestId('btn-processo-remover').click()` + modal exibido e confirmação testada
- ✅ **[COBERTO]** 31. O sistema mostra o diálogo de confirmação "Remover o processo '[Descrição do processo]'?".
  - Evidência: `e2e/cdu-03.spec.ts:124` -> `await expect(page.getByText(TEXTOS.processo.cadastro.REMOVER_CONFIRMACAO(descricao))).toBeVisible()`
- ✅ **[COBERTO]** 32. Se escolher `Cancelar` no diálogo: sistema fecha o diálogo e permanece na tela `Cadastro de processo`.
  - Evidência: `e2e/cdu-03.spec.ts:240` -> `await page.getByTestId('btn-modal-confirmacao-cancelar').click()` + `await expect(page.getByText(...REMOVER_CONFIRMACAO...)).toBeHidden()`
- ✅ **[COBERTO]** 33. Ao escolher `Remover` no diálogo: sistema remove o processo e redireciona para o Painel com mensagem de sucesso.
  - Evidência: `e2e/cdu-03.spec.ts:126` -> `await page.getByRole('dialog').getByRole('button', {name: 'Remover'}).click()` + `await verificarToast(page, /removido/i)`

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Gaps restantes:
  - Item 3: capturar mensagem de texto "Preencha a descrição" via assertion explícita.
  - Item 6: verificar mensagem/tooltip para unidade desabilitada por estar em processo ativo.
  - Itens 15/16: testar caminhos alternativos de validação (campos em branco ao editar).
  - Itens 24/26: cobrir edição de unidades e data limite com assertions específicos.
