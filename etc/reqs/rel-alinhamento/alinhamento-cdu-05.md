# Alinhamento CDU-05 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-05.md`.
- Teste E2E: `e2e/cdu-05.spec.ts` (12 cenários `test` em describe.serial, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **13**.
- Status: **11 cobertos**, **0 parciais**, **2 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências

- ✅ **[COBERTO]** 1. No painel, o usuário clica em um processo de revisão que esteja na situação 'Criado'.
  - Evidência: `e2e/cdu-05.spec.ts:196` -> `await acessarDetalhesProcesso(page, descProcRevisao);`
  - Evidência: `e2e/cdu-05.spec.ts:197` -> `await expect(page).toHaveURL(/\/processo\/cadastro/);`

- ✅ **[COBERTO]** 2. O sistema muda para a tela `Cadastro de processo`, com os campos preenchidos com as informações do processo selecionado.
  - Evidência: `e2e/cdu-05.spec.ts:200` -> `await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcRevisao);`
  - Evidência: `e2e/cdu-05.spec.ts:199` -> `const dataLimiteStr = (await page.getByTestId('inp-processo-data-limite').inputValue()).split('-').reverse().join('/');`

- ✅ **[COBERTO]** 3. O usuário clica no botão `Iniciar processo`.
  - Evidência: `e2e/cdu-05.spec.ts:201` -> `await page.getByTestId('btn-processo-iniciar').click();`

- ✅ **[COBERTO]** 4. O sistema mostra diálogo de confirmação com mensagem sobre bloqueio de edição e notificação de unidades participantes, além dos botões Confirmar e Cancelar.
  - Evidência: `e2e/cdu-05.spec.ts:203` -> `await expect(modal.getByText(TEXTOS.processo.cadastro.INICIAR_CONFIRMACAO)).toBeVisible();`

- ✅ **[COBERTO]** 5. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação, permanecendo na mesma tela.
  - Evidência: `e2e/cdu-05.spec.ts:204` -> `await modal.getByRole('button', {name: /Cancelar/i}).click();`
  - Evidência: `e2e/cdu-05.spec.ts:205` -> `await expect(modal).toBeHidden();`
  - Evidência: `e2e/cdu-05.spec.ts:206` -> `await expect(page).toHaveURL(/\/processo\/cadastro/);`

- ✅ **[COBERTO]** 6. O usuário confirma.
  - Evidência: `e2e/cdu-05.spec.ts:208` -> `await page.getByTestId('btn-processo-iniciar').click();`
  - Evidência: `e2e/cdu-05.spec.ts:210` -> `await page.getByTestId('btn-iniciar-processo-confirmar').click();`

- ❌ **[NAO_COBERTO]** 7. O sistema armazena internamente uma cópia da árvore de unidades participantes ('snapshot') e a vincula com o processo, a fim de preservar a hierarquia vigente no momento da iniciação.
  - Comportamento interno do servidor; não verificável diretamente via E2E de interface.

- ✅ **[COBERTO]** 8. O sistema muda a situação do processo de revisão para 'Em andamento'.
  - Evidência: `e2e/cdu-05.spec.ts:213` -> `await verificarProcessoNaTabela(page, {descricao: descProcRevisao, situacao: 'Em andamento', tipo: 'Revisão'});`

- ✅ **[COBERTO]** 9. O sistema cria internamente um subprocesso para cada unidade participante, com Situação 'Não iniciado', Data limite da etapa e campos Sugestões/Observações.
  - Evidência: `e2e/cdu-05.spec.ts:218` -> `await expect(linhaSubprocesso).toContainText('Não iniciado');`
  - Evidência: `e2e/cdu-05.spec.ts:219` -> `await expect(linhaSubprocesso).toContainText(dataLimiteStr);`
  - Evidência: `e2e/cdu-05.spec.ts:222` -> `await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText('Não iniciado');`

- ✅ **[COBERTO]** 10. O sistema cria internamente uma cópia do mapa de competências vigente para cada unidade, juntamente com as atividades e conhecimentos, vinculando ao subprocesso.
  - Evidência: `e2e/cdu-05.spec.ts` (Fase 3) -> `await expect(page.getByText(descAtividade).first()).toBeVisible();` (atividade do mapeamento aparece na revisão)
  - Evidência: `e2e/cdu-05.spec.ts` (Fase 3) -> `await expect(page.getByText('Conhecimento teste').first()).toBeVisible();`

- ✅ **[COBERTO]** 11. O sistema registra uma movimentação para cada subprocesso criado com Data/hora, Unidade origem ADMIN, Unidade destino e Descrição 'Processo iniciado'.
  - Evidência: `e2e/cdu-05.spec.ts:225` -> `await expect(timeline.getByText(/Processo iniciado/i).first()).toBeVisible();`

- ❌ **[NAO_COBERTO]** 12. O sistema envia notificações por e-mail para todas as unidades participantes (operacionais, interoperacionais e intermediárias).
  - Não testável via E2E (envio de e-mail é comportamento de infraestrutura).

- ✅ **[COBERTO]** 13. O sistema cria internamente alertas para todas as unidades participantes.
  - 13.1. Evidência (operacional): `e2e/cdu-05.spec.ts` (Fase 2.1) -> alerta 'Início do processo' (não subordinada) para chefe da unidade operacional.
  - 13.2. Evidência (intermediária): `e2e/cdu-05.spec.ts` (Fase 2.1) -> alerta 'Início do processo em unidade(s) subordinada(s)' para gestor da unidade intermediária.
  - Evidência: `e2e/cdu-05.spec.ts:250` -> `await expect(tabelaAlertasChefe.locator('tr', {hasText: descProcRevisao}).filter({hasText: 'Início do processo'}).filter({hasNotText: 'subordinada'})).toBeVisible();`
  - Evidência: `e2e/cdu-05.spec.ts:259` -> `await expect(tabelaAlertasGestor.locator('tr', {hasText: descProcRevisao}).filter({hasText: 'Início do processo em unidade(s) subordinada(s)'})).toBeVisible();`

## Cenários adicionais cobertos (além do fluxo principal do CDU-05)
- **Fases 1.1–1.7**: Setup completo de processo de Mapeamento finalizado, incluindo disponibilização, aceite, homologação e finalização.
- **Fase 2.2 e 2.2b**: Verificação que o card de atividades da Revisão é editável (`card-subprocesso-atividades`) logo após iniciar, mesmo após visita prévia a subprocesso de Mapeamento finalizado.
- **Fase 3**: Verificação de que as atividades e conhecimentos do Mapeamento foram copiados para o subprocesso de Revisão.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Gaps remanescentes:
  - Item 7 (snapshot da árvore de unidades): não testável via E2E por ser comportamento interno do servidor.
  - Item 12 (e-mail): não testável via E2E.
