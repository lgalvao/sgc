# Alinhamento CDU-04 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-04.md`.
- Teste E2E: `e2e/cdu-04.spec.ts` (2 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **13**.
- Status: **9 cobertos**, **0 parciais**, **4 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências

- ✅ **[COBERTO]** 1. No `Painel`, o usuário clica em um processo de mapeamento que esteja na situação 'Criado'.
  - Evidência: `e2e/cdu-04.spec.ts:44` -> `await acessarDetalhesProcesso(page, descricao);`
  - Evidência: `e2e/cdu-04.spec.ts:45` -> `await esperarPaginaCadastroProcesso(page);`

- ✅ **[COBERTO]** 2. O sistema muda para a tela `Cadastro de processo`, com os campos preenchidos com as informações do processo selecionado.
  - Evidência: `e2e/cdu-04.spec.ts:46` -> `const codProcesso = await extrairProcessoCodigo(page);`
  - Evidência: `e2e/cdu-04.spec.ts:47` -> `const dataLimiteStr = await page.getByTestId('inp-processo-data-limite').inputValue();`

- ✅ **[COBERTO]** 3. O usuário clica no botão `Iniciar processo`.
  - Evidência: `e2e/cdu-04.spec.ts:49` -> `await page.getByTestId('btn-processo-iniciar').click();`

- ✅ **[COBERTO]** 4. O sistema mostra modal de confirmação com mensagem sobre bloqueio de edição, notificação de unidades e botões `Confirmar` e `Cancelar`.
  - Evidência: `e2e/cdu-04.spec.ts:51` -> `await expect(modal.getByText(TEXTOS.processo.cadastro.INICIAR_CONFIRMACAO)).toBeVisible();`

- ❌ **[NAO_COBERTO]** 5. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de iniciação do processo, permanecendo na mesma tela.
  - O spec confirma o modal e procede diretamente para confirmação. O cenário de cancelamento não é testado neste CDU (verificado no CDU-05).

- ✅ **[COBERTO]** 6. O usuário confirma.
  - Evidência: `e2e/cdu-04.spec.ts:52` -> `await page.getByTestId('btn-iniciar-processo-confirmar').click();`

- ❌ **[NAO_COBERTO]** 7. O sistema armazena internamente uma cópia da árvore de unidades participantes ('snapshot') e a vincula com o processo.
  - Comportamento interno do servidor; não verificável diretamente via E2E de interface.

- ✅ **[COBERTO]** 8. O sistema muda a situação do processo para 'Em andamento'.
  - Evidência: `e2e/cdu-04.spec.ts:56` -> `await verificarProcessoNaTabela(page, {descricao, situacao: 'Em andamento', tipo: 'Mapeamento'});`

- ✅ **[COBERTO]** 9. O sistema cria internamente um subprocesso para cada unidade operacional ou interoperacional participante, com Situação 'Não iniciado', Data limite e campos Observações/Sugestões.
  - Evidência: `e2e/cdu-04.spec.ts:67` -> `await expect(linhaAss11).toContainText('Não iniciado');`
  - Evidência: `e2e/cdu-04.spec.ts:68` -> `await expect(linhaAss11).toContainText(dataLimiteStr.split('-').reverse().join('/'));`
  - Evidência: `e2e/cdu-04.spec.ts:71` -> `await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText('Não iniciado');`

- ❌ **[NAO_COBERTO]** 10. O sistema cria internamente um mapa de competências vazio e o vincula ao subprocesso da unidade.
  - Comportamento interno não verificado diretamente no spec. O mapa vazio somente seria verificado ao acessar a tela de mapa do subprocesso.

- ✅ **[COBERTO]** 11. O sistema registra uma movimentação para cada subprocesso criado com os campos Data/hora, Unidade origem ADMIN, Unidade destino e Descrição 'Processo iniciado'.
  - Evidência: `e2e/cdu-04.spec.ts:74` -> `await expect(timeline.getByText(TEXTOS.movimentacao.PROCESSO_INICIADO)).toBeVisible();`

- ❌ **[NAO_COBERTO]** 12. O sistema envia notificações por e-mail para todas as unidades participantes (operacionais, interoperacionais e intermediárias).
  - Não testável via E2E (envio de e-mail é comportamento de infraestrutura).

- ✅ **[COBERTO]** 13. O sistema cria internamente alertas para todas as unidades participantes.
  - 13.1. Evidência (unidade operacional - ASSESSORIA_11): `e2e/cdu-04.spec.ts:79-84` -> alerta 'Início do processo' (não subordinada) para CHEFE_ASSESSORIA_11.
  - 13.1. Evidência (unidade interoperacional como operacional - SECRETARIA_1 como CHEFE): `e2e/cdu-04.spec.ts:87-95` -> alerta 'Início do processo' (não subordinada).
  - 13.2. Evidência (unidade intermediária - SECRETARIA_1 como GESTOR): `e2e/cdu-04.spec.ts:98-106` -> alerta 'Início do processo em unidade(s) subordinada(s)'.

## Cenários adicionais cobertos (além do fluxo principal)
- **Teste 1**: Unidade sem responsável efetivo marcada como inelegível na árvore (`chk-arvore-unidade-SECAO_SEM_RESP` disabled), garantindo a pré-condição de elegibilidade antes da iniciação.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Gaps remanescentes:
  - Item 5 (cancelar modal): cenário de cancelamento poderia ser adicionado para completude; atualmente coberto no CDU-05.
  - Item 7 (snapshot da árvore): não testável via E2E.
  - Item 10 (mapa vazio): poderia ser verificado navegando até a tela de mapa do subprocesso após iniciar.
  - Item 12 (e-mail): não testável via E2E.
