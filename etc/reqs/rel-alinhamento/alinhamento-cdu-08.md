# Alinhamento CDU-08 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-08.md`.
- Teste E2E: `e2e/cdu-08.spec.ts` (3 cenários `test`, múltiplos `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **35**.
- Status: **27 cobertos**, **6 parciais**, **2 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências

- ✅ **[COBERTO]** 1. No painel, o usuário clica em um processo de mapeamento ou revisão da lista de processos.
  - Evidência: `e2e/cdu-08.spec.ts:52` -> `await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();`
  - Evidência: `e2e/cdu-08.spec.ts:53` -> `await acessarDetalhesProcesso(page, descricaoProcesso);`

- 🟡 **[PARCIAL]** 2. O sistema mostra a tela `Detalhes de subprocesso` com os dados do subprocesso da unidade.
  - A tela é acessada e verificada por URL, mas os campos individuais do header não são assertados neste cenário.
  - Evidência: `e2e/cdu-08.spec.ts:54` -> `await expect(page).toHaveURL(new RegExp(.../${UNIDADE_ALVO}$));`

- ✅ **[COBERTO]** 3. O usuário clica no card `Atividades e conhecimentos`.
  - Evidência: `e2e/cdu-08.spec.ts:55` -> `await AtividadeHelpers.navegarParaAtividades(page);`

- ✅ **[COBERTO]** 4. O sistema apresenta a tela `Cadastro de atividades e conhecimentos` da unidade. Se o mapa de competências da unidade do subprocesso já tiver atividades cadastradas, a tela virá preenchida com as informações atuais do mapa.
  - Evidência: `e2e/cdu-08.spec.ts:58` -> `await expect(page.getByTestId('cad-atividades-empty-state')).toBeVisible();` (estado inicial vazio)
  - Evidência: `e2e/cdu-08.spec.ts:109` -> `await expect(page.getByText(atividadeManual, { exact: true }).first()).toBeVisible();` (após reload - dados persistidos)

- ✅ **[COBERTO]** 5. Se o processo for de **Revisão**, deverá ser exibido o botão `Impacto no mapa`.
  - Evidência: `e2e/cdu-08.spec.ts:167` -> `await AtividadeHelpers.verificarBotaoImpactoDropdown(page);` (Cenário 2 - Revisão)
  - Evidência: `e2e/cdu-08.spec.ts:168` -> `await AtividadeHelpers.abrirModalImpactoEdicao(page);`

- ✅ **[COBERTO]** 6. Para incluir uma atividade, o usuário fornece a descrição da atividade, e clica no botão de adição.
  - Evidência: `e2e/cdu-08.spec.ts:91` -> `await AtividadeHelpers.adicionarAtividade(page, atividadeManual);`

- ✅ **[COBERTO]** 7. O sistema adiciona a atividade e mostra o campo para adição de conhecimento abaixo da atividade.
  - Evidência: `e2e/cdu-08.spec.ts:91` -> `await AtividadeHelpers.adicionarAtividade(page, atividadeManual);` (após este passo, o campo de conhecimento aparece)

- 🟡 **[PARCIAL]** 8. O usuário fornece a descrição do conhecimento e clica no botão de adição correspondente.
  - O helper adicionarConhecimento encapsula a ação, mas o campo específico e botão de adição não são assertados individualmente.
  - Evidência: `e2e/cdu-08.spec.ts:97` -> `await AtividadeHelpers.adicionarConhecimento(page, atividadeManual, conhecimento1);`

- ✅ **[COBERTO]** 9. O sistema adiciona o conhecimento, associando-o à atividade.
  - Evidência: `e2e/cdu-08.spec.ts:111` -> `await expect(page.locator('.group-conhecimento', { hasText: conhecimento1 }).first()).toBeVisible();` (verificado após reload)

- ✅ **[COBERTO]** 10. Deve ser indicada claramente a associação entre o conhecimento e a atividade, indentando os conhecimentos da atividade abaixo da descrição desta.
  - Evidência: `e2e/cdu-08.spec.ts:111` -> `await expect(page.locator('.group-conhecimento', { hasText: conhecimento1 }).first()).toBeVisible();`

- ❌ **[NAO_COBERTO]** 11. O usuário repete o fluxo de adição de atividades/conhecimentos.
  - Comportamento estrutural implícito pela adição de múltiplas atividades (atividadeManual, atividadeManual2) e múltiplos conhecimentos, mas não há assertion específica de repetição de ciclo.

- ✅ **[COBERTO]** 12. O usuário pode incluir primeiro várias atividades e depois os conhecimentos correspondentes; ou trabalhar em uma atividade por vez.
  - Evidência: `e2e/cdu-08.spec.ts:91-99` -> duas atividades adicionadas, depois conhecimentos; flexibilidade de fluxo demonstrada.

- ✅ **[COBERTO]** 13. Para cada atividade já cadastrada, ao passar o mouse, o sistema exibe botões de edição e remoção.
  - Evidência: `e2e/cdu-08.spec.ts:120` -> `await AtividadeHelpers.cancelarEdicaoAtividade(page, atividadeManual, atividadeCancelada);` (hover + botão editar visível no helper)
  - Evidência: `e2e/cdu-08.spec.ts:121` -> `await AtividadeHelpers.editarAtividade(page, atividadeManual, atividadeEditada);`

- ✅ **[COBERTO]** 14. Se o usuário clicar em `Editar` (atividade), o sistema habilita a edição do nome e exibe botões `Salvar` e `Cancelar`.
  - Evidência: `e2e/cdu-08.spec.ts:121` -> `await AtividadeHelpers.editarAtividade(...)` (helper verifica btn-salvar-edicao-atividade)

- 🟡 **[PARCIAL]** 15. Se o usuário clicar em `Salvar` (atividade), o sistema salva a alteração e volta a exibir os botões `Editar` e `Remover` ao lado do nome da atividade.
  - O helper editarAtividade clica em btn-salvar-edicao-atividade e verifica o texto novo, mas não verifica explicitamente o retorno dos botões Editar e Remover para a atividade.
  - Evidência: `e2e/cdu-08.spec.ts:121` -> `await AtividadeHelpers.editarAtividade(page, atividadeManual, atividadeEditada);`

- ✅ **[COBERTO]** 16. Se o usuário clicar em `Cancelar` (atividade), o sistema não salva a alteração e volta a exibir o nome original com os botões `Editar` e `Remover`.
  - Evidência: `e2e/cdu-08.spec.ts:120` -> `await AtividadeHelpers.cancelarEdicaoAtividade(page, atividadeManual, atividadeCancelada);`

- ✅ **[COBERTO]** 17. Se o usuário clicar em `Remover` (atividade), o sistema solicita confirmação e remove a atividade e todos os conhecimentos associados.
  - Evidência: `e2e/cdu-08.spec.ts:133` -> `await AtividadeHelpers.removerAtividade(page, atividadeEditada);`

- ✅ **[COBERTO]** 18. Para cada conhecimento já cadastrado, o sistema exibe, ao passar o mouse, opções Editar e Remover.
  - Evidência: `e2e/cdu-08.spec.ts:127` -> `await AtividadeHelpers.cancelarEdicaoConhecimento(...)` (hover + btn-editar-conhecimento visível no helper)

- ✅ **[COBERTO]** 19. Se o usuário clicar em `Editar` (conhecimento), o sistema habilita a edição do nome do conhecimento e exibe ao lado um botão `Salvar` e outro `Cancelar`.
  - Evidência: `e2e/cdu-08.spec.ts:129` -> `await AtividadeHelpers.editarConhecimento(...)` (helper verifica btn-salvar-edicao-conhecimento)

- ✅ **[COBERTO]** 20. Se o usuário clicar em `Salvar` (conhecimento), o sistema salva a alteração e volta a exibir os botões `Editar` e `Remover` ao lado do nome do conhecimento.
  - Evidência: `e2e/cdu-08.spec.ts:129` -> `await AtividadeHelpers.editarConhecimento(page, atividadeEditada, conhecimento1, conhecimento1Editado);`
  - Evidência: `e2e/cdu-08.spec.ts:131-136` -> assertion explícita adicionada nesta rodada: hover + `btn-editar-conhecimento` + `btn-remover-conhecimento` toBeVisible após salvar.

- 🟡 **[PARCIAL]** 21. Se o usuário clicar em `Cancelar` (conhecimento), o sistema não salva a alteração e volta a exibir o nome original com os botões `Editar` e `Remover`.
  - O helper cancelarEdicaoConhecimento verifica que o texto cancelado não foi salvo, mas não verifica explicitamente o retorno dos botões.
  - Evidência: `e2e/cdu-08.spec.ts:127` -> `await AtividadeHelpers.cancelarEdicaoConhecimento(page, atividadeEditada, conhecimento1, conhecimentoCancelado);`

- 🟡 **[PARCIAL]** 22. Se o usuário clicar em `Remover` (conhecimento), o sistema solicita confirmação e remove o conhecimento.
  - O helper removerConhecimento confirma a remoção e verifica que o texto sumiu, mas não verifica a mensagem específica do modal de confirmação isoladamente no spec.
  - Evidência: `e2e/cdu-08.spec.ts:131` -> `await AtividadeHelpers.removerConhecimento(page, atividadeEditada, conhecimento1Editado);`

- ✅ **[COBERTO]** 23. Opcionalmente, o usuário clica no botão `Importar atividades`.
  - Evidência: `e2e/cdu-08.spec.ts:74` -> `await AtividadeHelpers.importarAtividadesVazia(page, processoOrigemDescricao, UNIDADE_ORIGEM, [atividadeA, atividadeB]);`

- 🟡 **[PARCIAL]** 24. O sistema exibe um modal com uma lista dos processos com tipo Mapeamento ou Revisão que estejam com situação 'Finalizado'.
  - O modal é usado mas não há assertion verificando explicitamente que apenas processos finalizados aparecem.
  - Evidência: `e2e/cdu-08.spec.ts:74` -> `await AtividadeHelpers.importarAtividadesVazia(page, ...)`

- 🟡 **[PARCIAL]** 25. O usuário escolhe um processo da lista.
  - O helper encapsula a seleção via select-processo mas não há assertion do estado da lista.
  - Evidência: `e2e/cdu-08.spec.ts:74` -> `await AtividadeHelpers.importarAtividadesVazia(page, ...)`

- ✅ **[COBERTO]** 26. O sistema recupera as unidades operacionais e interoperacionais participantes do processo e expande o modal para mostrar as unidades.
  - Evidência: `e2e/cdu-08.spec.ts:67` -> `await AtividadeHelpers.verificarOpcoesImportacaoVazia(page, [{ processo: processoOrigemDescricao, unidades: [UNIDADE_ORIGEM] }, ...])`

- 🟡 **[PARCIAL]** 27. O usuário escolhe uma unidade da lista.
  - A unidade é selecionada no helper mas sem assertion isolada da seleção.
  - Evidência: `e2e/cdu-08.spec.ts:74` -> `await AtividadeHelpers.importarAtividadesVazia(page, processoOrigemDescricao, UNIDADE_ORIGEM, ...)`

- ✅ **[COBERTO]** 28. O sistema recupera as atividades/conhecimentos da unidade selecionada e expande o modal para mostrar a lista de atividades, permitindo seleção múltipla.
  - Evidência: `e2e/cdu-08.spec.ts:74` -> `await AtividadeHelpers.importarAtividadesVazia(page, processoOrigemDescricao, UNIDADE_ORIGEM, [atividadeA, atividadeB]);`

- ✅ **[COBERTO]** 29. O usuário marca uma ou mais atividades e clica em `Importar`.
  - Evidência: `e2e/cdu-08.spec.ts:74` -> `await AtividadeHelpers.importarAtividadesVazia(...)` (importa [atividadeA, atividadeB])

- ✅ **[COBERTO]** 30. O sistema faz uma cópia das atividades selecionadas e seus respectivos conhecimentos para o cadastro da unidade atual.
  - Evidência: `e2e/cdu-08.spec.ts:109` -> `await expect(page.getByText(atividadeA, { exact: true }).first()).toBeVisible();` (verificado após reload)

- ✅ **[COBERTO]** 31. Deverão ser importadas apenas as atividades cujas descrições não corresponderem a nenhuma atividade atualmente cadastrada na unidade.
  - Evidência: `e2e/cdu-08.spec.ts:78` -> `await AtividadeHelpers.importarAtividadesComAvisoDuplicidade(page, processoOrigemDescricao, UNIDADE_ORIGEM, [atividadeA]);`

- ✅ **[COBERTO]** 32. Caso haja coincidência, o sistema informa que uma ou mais atividades não puderam ser importadas por já existirem no cadastro, mas procede sem erros.
  - Evidência: `e2e/cdu-08.spec.ts:80` -> `await expect(page.getByText(/não foram importadas/i).first()).toBeVisible();`

- ✅ **[COBERTO]** 33. Se a situação do subprocesso ainda estiver 'Não iniciado', o sistema altera para 'Cadastro em andamento' (mapeamento) ou 'Revisão do cadastro em andamento' (revisão).
  - Evidência: `e2e/cdu-08.spec.ts:62` -> `await AtividadeHelpers.verificarSituacaoSubprocesso(page, 'Não iniciado');` (antes)
  - Evidência: `e2e/cdu-08.spec.ts:75` -> `await AtividadeHelpers.verificarSituacaoSubprocesso(page, 'Cadastro em andamento');` (após importação)

- ✅ **[COBERTO]** 34. Após finalizar, o usuário pode clicar em `Disponibilizar`; pode também navegar sem necessidade de gravar manualmente (auto-save).
  - Evidência: `e2e/cdu-08.spec.ts:140` -> `await AtividadeHelpers.disponibilizarCadastro(page);`
  - Evidência: `e2e/cdu-08.spec.ts:106` -> `await page.reload();` seguido de verificação de dados persistidos (auto-save confirmado)

- ✅ **[COBERTO]** 35. Após cada ação de criação, edição ou exclusão, as informações deverão ser salvas automaticamente.
  - Evidência: `e2e/cdu-08.spec.ts:107-113` -> reload + verificação de atividadeManual, atividadeManual2, conhecimento1, conhecimento2 e atividadeA ainda visíveis.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Gaps remanescentes:
  - Item 11 (repetição do fluxo): comportamento implícito pela adição de múltiplas atividades; difícil de agregar valor com um teste adicional.
  - Itens 15 e 21 (botões Editar/Remover retornam após Cancelar atividade/conhecimento): considerar adicionar assertion explícita no helper ou spec, similar ao que foi feito para item 20.
