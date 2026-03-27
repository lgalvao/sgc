# Alinhamento CDU-27 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-27.md`.
- Teste E2E: `e2e/cdu-27.spec.ts` (4 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **14**.
- Status: **10 cobertos**, **4 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. No painel, o usuário acessa um processo ativo e na tela `Detalhes do processo`, clica em uma unidade que tenha subprocesso em andamento.
  - Palavras-chave usadas: `processo, unidade, subprocesso, painel, acessa, ativo`
  - Evidência (score 4): `e2e/cdu-27.spec.ts:16` -> `* 1. ADMIN acessa processo ativo e clica em uma unidade`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:13` -> `* - Unidade participante com subprocesso iniciado e ainda não finalizado`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:52` -> `await navegarParaSubprocesso(page, UNIDADE_1);`
- ✅ **[COBERTO]** 2. O sistema mostra a tela `Detalhes do subprocesso`.
  - Palavras-chave usadas: `subprocesso, mostra, detalhes`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:17` -> `* 2. Sistema mostra tela Detalhes do subprocesso`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:47` -> `test('Cenario 1: ADMIN navega para detalhes do subprocesso', async ({_resetAutomatico, page, _autenticadoComoAdmin}) ...`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:3` -> `import {navegarParaSubprocesso, verificarAppAlert} from './helpers/helpers-navegacao.js';`
- ✅ **[COBERTO]** 3. O usuário clica no botão `Alterar data limite`.
  - Palavras-chave usadas: `clica, botão, alterar, data, limite`
  - Evidência (score 5): `e2e/cdu-27.spec.ts:18` -> `* 3. ADMIN clica no botão 'Alterar data limite'`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:8` -> `* CDU-27 - Alterar data limite de subprocesso`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:23` -> `test.describe.serial('CDU-27 - Alterar data limite de subprocesso', () => {`
- ✅ **[COBERTO]** 4. O sistema abre um modal com título "Alterar data limite", campo de data preenchido com a data limite atual da etapa em andamento, e apresenta botões `Cancelar` e `Alterar`.
  - Palavras-chave usadas: `abre, modal, título, alterar, data, limite`
  - Evidência (score 4): `e2e/cdu-27.spec.ts:73` -> `await expect(modal.getByRole('heading', {name: TEXTOS.subprocesso.BOTAO_ALTERAR_DATA_LIMITE})).toBeVisible();`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:8` -> `* CDU-27 - Alterar data limite de subprocesso`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:18` -> `* 3. ADMIN clica no botão 'Alterar data limite'`
- ✅ **[COBERTO]** 5. O usuário fornece a nova data limite e clica em `Alterar`.
  - Palavras-chave usadas: `fornece, nova, data, limite, clica, alterar`
  - Evidência (score 4): `e2e/cdu-27.spec.ts:18` -> `* 3. ADMIN clica no botão 'Alterar data limite'`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:8` -> `* CDU-27 - Alterar data limite de subprocesso`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:23` -> `test.describe.serial('CDU-27 - Alterar data limite de subprocesso', () => {`
- ✅ **[COBERTO]** 6. A data limite deve ser estritamente no futuro (amanhã em diante)
  - Palavras-chave usadas: `data, limite, estritamente, futuro, amanhã, diante`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:8` -> `* CDU-27 - Alterar data limite de subprocesso`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:18` -> `* 3. ADMIN clica no botão 'Alterar data limite'`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:23` -> `test.describe.serial('CDU-27 - Alterar data limite de subprocesso', () => {`
- ✅ **[COBERTO]** 7. O sistema atualiza a data limite do subprocesso e envia notificação por e-mail para a unidade do subprocesso, neste modelo:
  - Palavras-chave usadas: `subprocesso, unidade, atualiza, data, limite, envia`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:8` -> `* CDU-27 - Alterar data limite de subprocesso`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:23` -> `test.describe.serial('CDU-27 - Alterar data limite de subprocesso', () => {`
  - Evidência (score 3): `e2e/cdu-27.spec.ts:73` -> `await expect(modal.getByRole('heading', {name: TEXTOS.subprocesso.BOTAO_ALTERAR_DATA_LIMITE})).toBeVisible();`
- 🟡 **[PARCIAL]** 8. O sistema cria internamente um alerta com as seguintes informações:
  - Palavras-chave usadas: `alerta, cria, internamente, seguintes, informações`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:2` -> `import {criarProcessoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:38` -> `const processo = await criarProcessoFixture(request, {`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:78` -> `// BUG FIX VERIFICATION: Verificar se o modal inicia com a data do prazo (não criação)`
- ✅ **[COBERTO]** 9. `Descrição`: "Data limite da etapa [NÚMERO_ETAPA] alterada para [NOVA_DATA_LIMITE]"
  - Palavras-chave usadas: `descrição, data, limite, número_etapa, alterada, nova_data_limite`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:8` -> `* CDU-27 - Alterar data limite de subprocesso`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:18` -> `* 3. ADMIN clica no botão 'Alterar data limite'`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:23` -> `test.describe.serial('CDU-27 - Alterar data limite de subprocesso', () => {`
- 🟡 **[PARCIAL]** 10. `Processo`: [DESCRICAO_PROCESSO]
  - Palavras-chave usadas: `processo, descricao_processo`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:2` -> `import {criarProcessoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:3` -> `import {navegarParaSubprocesso, verificarAppAlert} from './helpers/helpers-navegacao.js';`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:4` -> `import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';`
- 🟡 **[PARCIAL]** 11. `Data/hora`: Data/hora atual
  - Palavras-chave usadas: `data/hora, atual`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:21` -> `* 6. Sistema atualiza e envia notificação`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:62` -> `// Obter a data atual do prazo na página`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:111` -> `// Verificar se a página atualizou o prazo`
- ✅ **[COBERTO]** 12. `Unidade de origem`: 'ADMIN'
  - Palavras-chave usadas: `unidade, origem, admin`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:16` -> `* 1. ADMIN acessa processo ativo e clica em uma unidade`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:10` -> `* Ator: ADMIN`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:13` -> `* - Unidade participante com subprocesso iniciado e ainda não finalizado`
- 🟡 **[PARCIAL]** 13. `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]
  - Palavras-chave usadas: `unidade, sigla_unidade_subprocesso, destino`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:13` -> `* - Unidade participante com subprocesso iniciado e ainda não finalizado`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:16` -> `* 1. ADMIN acessa processo ativo e clica em uma unidade`
  - Evidência (score 1): `e2e/cdu-27.spec.ts:24` -> `const UNIDADE_1 = 'SECAO_221';`
- ✅ **[COBERTO]** 14. O sistema fecha o modal e mostra uma mensagem de confirmação "Data limite alterada".
  - Palavras-chave usadas: `fecha, modal, mostra, mensagem, confirmação, data`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:19` -> `* 4. Sistema abre modal com campo de data preenchido`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:73` -> `await expect(modal.getByRole('heading', {name: TEXTOS.subprocesso.BOTAO_ALTERAR_DATA_LIMITE})).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-27.spec.ts:78` -> `// BUG FIX VERIFICATION: Verificar se o modal inicia com a data do prazo (não criação)`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **O sistema cria internamente um alerta com as seguintes informações:** (atualmente parcial).
- Completar cobertura do item: **`Processo`: [DESCRICAO_PROCESSO]** (atualmente parcial).
- Completar cobertura do item: **`Data/hora`: Data/hora atual** (atualmente parcial).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Motivos: base de análise e pendências objetivas definidas.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **O sistema cria internamente um alerta com as seguintes informações:** (atualmente parcial).
  - Completar cobertura do item: **`Processo`: [DESCRICAO_PROCESSO]** (atualmente parcial).
  - Completar cobertura do item: **`Data/hora`: Data/hora atual** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
