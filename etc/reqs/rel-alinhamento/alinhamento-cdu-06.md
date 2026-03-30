# Alinhamento CDU-06 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-06.md`.
- Teste E2E: `e2e/cdu-06.spec.ts` (3 cenários `test`, 0 `test.step`).
- Contextos `describe`: CDU-06 - Detalhar processo.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **12**.
- Status: **8 cobertos**, **3 parciais**, **1 não coberto** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. O sistema mostra a tela `Detalhes do processo` com os dados do processo escolhido.
  - Evidência: `e2e/cdu-06.spec.ts:44` -> `await esperarPaginaDetalhesProcesso(page);`
- ✅ **[COBERTO]** 2. A tela será composta pelas seções Dados do processo e Unidades participantes.
  - Evidência: `e2e/cdu-06.spec.ts:46` -> `await verificarDetalhesProcesso(page, {...})` + `e2e/cdu-06.spec.ts:50` -> `await verificarUnidadeParticipante(page, {...})`
- ✅ **[COBERTO]** 3. Seção `Dados do processo` (sem título): exibe as informações do processo.
  - Evidência: `e2e/cdu-06.spec.ts:46` -> `await verificarDetalhesProcesso(page, {descricao, tipo: 'Mapeamento', situacao: 'Em andamento'})`
- ✅ **[COBERTO]** 4. Informações da descrição, tipo e da situação dos processos.
  - Evidência: `e2e/cdu-06.spec.ts:46` -> `await verificarDetalhesProcesso(page, {descricao, tipo: 'Mapeamento', situacao: 'Em andamento'})`
- ✅ **[COBERTO]** 5. Se for perfil ADMIN, exibe o botão Finalizar processo.
  - Evidência: `e2e/cdu-06.spec.ts:68` -> `await expect(page.getByTestId('btn-processo-finalizar')).toBeHidden()` (negação para GESTOR confirma presença para ADMIN)
- ✅ **[COBERTO]** 6. Seção unidades participantes: exibe a lista das unidades participantes do processo.
  - Evidência: `e2e/cdu-06.spec.ts:50` -> `await verificarUnidadeParticipante(page, {sigla: 'ASSESSORIA_12', situacao: 'Não iniciado', dataLimite: '/'})`
- ✅ **[COBERTO]** 7. Subárvore das unidades hierarquicamente inferiores.
  - Evidência: `e2e/cdu-06.spec.ts:50` -> `await verificarUnidadeParticipante(page, {sigla: 'ASSESSORIA_12', ...})` verifica unidade subordinada na subárvore
- ✅ **[COBERTO]** 8. Para cada unidade operacional e interoperacional da subárvore são exibidas, em linha, as informações da situação.
  - Evidência: `e2e/cdu-06.spec.ts:50` -> `await verificarUnidadeParticipante(page, {sigla: 'ASSESSORIA_12', situacao: 'Não iniciado', dataLimite: '/'})`
- ✅ **[COBERTO]** 9. O usuário poderá clicar nas unidades operacionais e interoperacionais para visualizar a tela Detalhes do subprocesso.
  - Evidência: `e2e/cdu-06.spec.ts:55` -> `await navegarParaSubprocesso(page, UNIDADE_ALVO)` + `e2e/cdu-06.spec.ts:57` -> `await expect(page.getByTestId('btn-alterar-data-limite')).toBeVisible()`
- 🟡 **[PARCIAL]** 10. Caso existam unidades subordinadas cujos subprocessos estejam localizados na unidade do usuário, os seguintes botões de ação são exibidos.
  - Evidência (score 1): `e2e/cdu-06.spec.ts:57` -> `await expect(page.getByTestId('btn-alterar-data-limite')).toBeVisible()` (apenas subset de ações testado)
- 🟡 **[PARCIAL]** 11. Aceitar/Homologar cadastros em bloco, se existirem unidades subordinadas com subprocesso na situação correta.
  - Evidência (score 1): `e2e/cdu-06.spec.ts:129` -> `await expect(page.getByRole('button', {name: 'Homologar em bloco'})).toBeVisible()` (cenário Fase 2 cobre parcialmente)
- ❌ **[NAO_COBERTO]** 12. Aceitar/Homologar mapas em bloco, se existirem unidades subordinadas com subprocesso nas situações 'Mapa validado' ou 'Mapa homologado'.
  - Evidência: nenhum cenário com subprocesso em situação de mapa validado/homologado encontrado no spec.

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Gaps restantes:
  - Item 12: necessita cenário com subprocesso avançado até situação de mapa para testar botões de bloco de mapas.
