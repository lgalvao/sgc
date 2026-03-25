# Alinhamento CDU-21 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-21.md`.
- Teste E2E: `e2e/cdu-21.spec.ts` (5 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **13**.
- Status: **5 cobertos**, **8 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. No `Painel`, o usuário clica em um processo de mapeamento ou de revisão com situação 'Em andamento'.
  - Palavras-chave usadas: `processo, situação, painel, clica, mapeamento, revisão`
  - Evidência (score 3): `e2e/cdu-21.spec.ts:7` -> `test.describe.serial('CDU-21 - Finalizar processo de mapeamento ou de revisão', () => {`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:4` -> `import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:11` -> `const descProcesso = `Mapeamento CDU-21 ${timestamp}`;`
- ✅ **[COBERTO]** 2. O sistema exibe a tela `Detalhes do processo`.
  - Palavras-chave usadas: `processo, exibe, detalhes`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:3` -> `import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:25` -> `test('Cenario 1: ADMIN navega para detalhes do processo', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:28` -> `await acessarDetalhesProcesso(page, descProcesso);`
- ✅ **[COBERTO]** 3. O usuário clica no botão `Finalizar`.
  - Palavras-chave usadas: `clica, botão, finalizar`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:32` -> `// Botão finalizar visível`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:7` -> `test.describe.serial('CDU-21 - Finalizar processo de mapeamento ou de revisão', () => {`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:33` -> `await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();`
- 🟡 **[PARCIAL]** 4. O sistema verifica se todos os subprocessos das unidades operacionais e interoperacionais participantes estão na situação 'Mapa homologado'.
  - Palavras-chave usadas: `subprocessos, unidades, situação, verifica, todos, operacionais`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:4` -> `import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:30` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:50` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
- 🟡 **[PARCIAL]** 5. Caso negativo, o sistema exibe a mensagem "Não é possível finalizar o processo enquanto houver unidades com mapa ainda não homologado".
  - Palavras-chave usadas: `processo, unidades, negativo, exibe, mensagem, possível`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:2` -> `import {criarProcessoMapaHomologadoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:3` -> `import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:4` -> `import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';`
- 🟡 **[PARCIAL]** 6. Caso positivo, sistema mostra diálogo de confirmação: título "Finalização de processo", mensagem "Confirma a finalização do processo [DESCRICAO_PROCESSO]? Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades participantes do processo." e botões `Confirmar` e `Finalizar`.
  - Palavras-chave usadas: `processo, descricao_processo, competências, unidades, positivo, mostra`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:2` -> `import {criarProcessoMapaHomologadoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:3` -> `import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:4` -> `import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';`
- 🟡 **[PARCIAL]** 7. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de finalização, permanecendo na mesma tela.
  - Palavras-chave usadas: `escolha, cancelar, interrompe, operação, finalização, permanecendo`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:36` -> `test('Cenario 2: ADMIN cancela finalização - permanece na tela', async ({_resetAutomatico, page, _autenticadoComoAdmi...`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:45` -> `await expect(modal.getByText(/Confirma a finalização/i)).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:47` -> `await page.getByTestId('btn-finalizar-processo-cancelar').click();`
- 🟡 **[PARCIAL]** 8. O usuário escolhe `Finalizar`.
  - Palavras-chave usadas: `escolhe, finalizar`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:7` -> `test.describe.serial('CDU-21 - Finalizar processo de mapeamento ou de revisão', () => {`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:32` -> `// Botão finalizar visível`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:33` -> `await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();`
- 🟡 **[PARCIAL]** 9. O sistema define os mapas de competências dos subprocessos como os mapas de competências vigentes das respectivas unidades.
  - Palavras-chave usadas: `competências, subprocessos, unidades, define, mapas, vigentes`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:30` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:50` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:78` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
- ✅ **[COBERTO]** 10. O sistema muda a situação do processo para 'Finalizado' e envia notificações por e-mail para todas as unidades participantes, como a seguir:
  - Palavras-chave usadas: `situação, processo, unidades, muda, finalizado, envia`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:68` -> `await expect(page.getByText(TEXTOS.sucesso.PROCESSO_FINALIZADO)).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:70` -> `// Verificar que processo não aparece mais no painel ativo (foi finalizado)`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:71` -> `// (Processo finalizado não aparece na lista de processos ativos)`
- 🟡 **[PARCIAL]** 11. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo:
  - Palavras-chave usadas: `unidades, operacionais, interoperacionais, receber, e-mail, segundo`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:30` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:50` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:78` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
- 🟡 **[PARCIAL]** 12. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das unidades operacionais e interoperacionais subordinadas a elas, segundo o modelo:
  - Palavras-chave usadas: `unidades, intermediárias, interoperacionais, receber, e-mail, informações`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:30` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:50` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-21.spec.ts:78` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
- ✅ **[COBERTO]** 13. O sistema redireciona para o `Painel`, mostrando a mensagem "Processo finalizado".
  - Palavras-chave usadas: `processo, redireciona, painel, mostrando, mensagem, finalizado`
  - Evidência (score 3): `e2e/cdu-21.spec.ts:70` -> `// Verificar que processo não aparece mais no painel ativo (foi finalizado)`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:4` -> `import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';`
  - Evidência (score 2): `e2e/cdu-21.spec.ts:68` -> `await expect(page.getByText(TEXTOS.sucesso.PROCESSO_FINALIZADO)).toBeVisible();`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **O sistema verifica se todos os subprocessos das unidades operacionais e interoperacionais participantes estão na situação 'Mapa homologado'.** (atualmente parcial).
- Completar cobertura do item: **Caso negativo, o sistema exibe a mensagem "Não é possível finalizar o processo enquanto houver unidades com mapa ainda não homologado".** (atualmente parcial).
- Completar cobertura do item: **Caso positivo, sistema mostra diálogo de confirmação: título "Finalização de processo", mensagem "Confirma a finalização do processo [DESCRICAO_PROCESSO]? Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades participantes do processo." e botões `Confirmar` e `Finalizar`.** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
