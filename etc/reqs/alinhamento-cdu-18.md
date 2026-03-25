# Alinhamento CDU-18 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-18.md`.
- Teste E2E: `e2e/cdu-18.spec.ts` (2 cenários `test`, 9 `test.step`).
- Contextos `describe`: CDU-18: Visualizar mapa de competências.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **15**.
- Status: **14 cobertos**, **1 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. No `Painel`, o usuário clica no processo de mapeamento ou revisão na situação 'Em andamento' ou 'Finalizado'.
  - Palavras-chave usadas: `processo, situação, painel, clica, mapeamento, revisão`
  - Evidência (score 3): `e2e/cdu-18.spec.ts:11` -> `* - Processo de mapeamento ou de revisão iniciado ou finalizado`
  - Evidência (score 3): `e2e/cdu-18.spec.ts:74` -> `// CHEFE vê processo no painel e clica`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:26` -> `// Clicar no processo 99 que tem mapa homologado`
- ✅ **[COBERTO]** 2. Se perfil logado for ADMIN ou GESTOR:
  - Palavras-chave usadas: `perfil, logado, admin, gestor`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:10` -> `* - Usuário logado com qualquer perfil`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:15` -> `* - Processo 99 (FINALIZADO) com mapa homologado para unidade de assessoria`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:20` -> `test('Cenário 1: ADMIN visualiza mapa via detalhes do processo', async ({_resetAutomatico, page, _autenticadoComoAdmi...`
- ✅ **[COBERTO]** 3. O sistema exibe a tela `Detalhes do processo`.
  - Palavras-chave usadas: `processo, exibe, detalhes`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:2` -> `import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:20` -> `test('Cenário 1: ADMIN visualiza mapa via detalhes do processo', async ({_resetAutomatico, page, _autenticadoComoAdmi...`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:27` -> `await acessarDetalhesProcesso(page, 'Processo 99');`
- 🟡 **[PARCIAL]** 4. Usuário clica em uma unidade subordinada que seja operacional ou interoperacional.
  - Palavras-chave usadas: `unidade, clica, subordinada, seja, operacional, interoperacional`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:12` -> `* - Subprocesso da unidade com mapa de competência já disponibilizado`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:15` -> `* - Processo 99 (FINALIZADO) com mapa homologado para unidade de assessoria`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:26` -> `// Clicar no processo 99 que tem mapa homologado`
- ✅ **[COBERTO]** 5. O sistema exibe a tela `Detalhes do subprocesso` com os dados do subprocesso da unidade selecionada.
  - Palavras-chave usadas: `subprocesso, unidade, exibe, detalhes, selecionada`
  - Evidência (score 3): `e2e/cdu-18.spec.ts:77` -> `// CHEFE vai direto para detalhes do subprocesso da sua unidade`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:12` -> `* - Subprocesso da unidade com mapa de competência já disponibilizado`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:36` -> `// Verificar navegação para detalhes do subprocesso`
- ✅ **[COBERTO]** 6. Se perfil logado for CHEFE ou SERVIDOR:
  - Palavras-chave usadas: `perfil, logado, chefe, servidor`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:10` -> `* - Usuário logado com qualquer perfil`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:15` -> `* - Processo 99 (FINALIZADO) com mapa homologado para unidade de assessoria`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:26` -> `// Clicar no processo 99 que tem mapa homologado`
- ✅ **[COBERTO]** 7. O sistema exibe a tela `Detalhes do subprocesso` com os dados do subprocesso da unidade do usuário.
  - Palavras-chave usadas: `subprocesso, unidade, exibe, detalhes`
  - Evidência (score 3): `e2e/cdu-18.spec.ts:77` -> `// CHEFE vai direto para detalhes do subprocesso da sua unidade`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:12` -> `* - Subprocesso da unidade com mapa de competência já disponibilizado`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:36` -> `// Verificar navegação para detalhes do subprocesso`
- ✅ **[COBERTO]** 8. Na tela de `Detalhes do subprocesso`, usuário clica no card `Mapa de Competências`.
  - Palavras-chave usadas: `subprocesso, competências, detalhes, clica, card, mapa`
  - Evidência (score 3): `e2e/cdu-18.spec.ts:41` -> `await test.step('4. Acessar mapa de competências via card', async () => {`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:7` -> `* CDU-18: Visualizar mapa de competências`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:12` -> `* - Subprocesso da unidade com mapa de competência já disponibilizado`
- ✅ **[COBERTO]** 9. O sistema mostra a tela `Visualização de mapa`, com as seguintes informações:
  - Palavras-chave usadas: `mostra, visualização, mapa, seguintes, informações`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:46` -> `await test.step('5. Verificar visualização do mapa (CDU-18)', async () => {`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:85` -> `await test.step('4. Verificar visualização do mapa', async () => {`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:3` -> `import {navegarParaMapa} from './helpers/helpers-mapas.js';`
- ✅ **[COBERTO]** 10. Título "Mapa de competências técnicas"
  - Palavras-chave usadas: `competências, título, mapa, técnicas`
  - Evidência (score 4): `e2e/cdu-18.spec.ts:47` -> `// 5.1 Título "Mapa de competências técnicas"`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:7` -> `* CDU-18: Visualizar mapa de competências`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:18` -> `test.describe('CDU-18: Visualizar mapa de competências', () => {`
- ✅ **[COBERTO]** 11. Identificação da unidade (sigla e nome).
  - Palavras-chave usadas: `unidade, identificação, sigla, nome`
  - Evidência (score 3): `e2e/cdu-18.spec.ts:50` -> `// 5.2 Identificação da unidade (sigla)`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:12` -> `* - Subprocesso da unidade com mapa de competência já disponibilizado`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:15` -> `* - Processo 99 (FINALIZADO) com mapa homologado para unidade de assessoria`
- ✅ **[COBERTO]** 12. Conjunto de competências, com cada competência mostrada em um bloco individual, contendo:
  - Palavras-chave usadas: `competências, competência, conjunto, cada, mostrada, bloco`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:7` -> `* CDU-18: Visualizar mapa de competências`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:18` -> `test.describe('CDU-18: Visualizar mapa de competências', () => {`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:41` -> `await test.step('4. Acessar mapa de competências via card', async () => {`
- ✅ **[COBERTO]** 13. Descrição da competência como título.
  - Palavras-chave usadas: `competência, descrição, título`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:47` -> `// 5.1 Título "Mapa de competências técnicas"`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:7` -> `* CDU-18: Visualizar mapa de competências`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:12` -> `* - Subprocesso da unidade com mapa de competência já disponibilizado`
- ✅ **[COBERTO]** 14. Conjunto das atividades associadas àquela competência.
  - Palavras-chave usadas: `atividades, competência, conjunto, associadas, àquela`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:16` -> `* - Mapa 99 com competência "Competência técnica seed 99" vinculada às atividades`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:58` -> `// 5.4 Atividades da competência`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:7` -> `* CDU-18: Visualizar mapa de competências`
- ✅ **[COBERTO]** 15. Para cada atividade, conjunto de conhecimentos da atividade.
  - Palavras-chave usadas: `atividade, cada, conjunto, conhecimentos`
  - Evidência (score 2): `e2e/cdu-18.spec.ts:62` -> `// 5.5 Conhecimentos das atividades`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:16` -> `* - Mapa 99 com competência "Competência técnica seed 99" vinculada às atividades`
  - Evidência (score 1): `e2e/cdu-18.spec.ts:58` -> `// 5.4 Atividades da competência`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **Usuário clica em uma unidade subordinada que seja operacional ou interoperacional.** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
