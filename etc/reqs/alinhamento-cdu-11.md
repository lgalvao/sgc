# Alinhamento CDU-11 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-11.md`.
- Teste E2E: `e2e/cdu-11.spec.ts` (5 cenários `test`, 0 `test.step`).
- Contextos `describe`: Em processo em Andamento, Em processo finalizado.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **10**.
- Status: **9 cobertos**, **1 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. No painel, o usuário clica no processo de mapeamento ou revisão na situação `Em andamento` ou `Finalizado`.
  - Palavras-chave usadas: `processo, situação, painel, clica, mapeamento, revisão`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:62` -> `const descProcesso = `Processo mapeamento CDU-11 ${timestamp}`;`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:3` -> `criarProcessoCadastroDisponibilizadoFixture,`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:4` -> `criarProcessoFinalizadoFixture,`
- ✅ **[COBERTO]** 2. Se usuário estiver logado com perfil ADMIN ou GESTOR:
  - Palavras-chave usadas: `perfil, estiver, logado, admin, gestor`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:26` -> `test('Fluxo ADMIN/GESTOR: Navega via Detalhes do Processo (Passo 2)', async ({_resetAutomatico, page, _autenticadoCom...`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:72` -> `test('Fluxo ADMIN: Visualizar em processo finalizado', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {`
- ✅ **[COBERTO]** 3. O sistema mostra a tela `Detalhes do processo`
  - Palavras-chave usadas: `processo, mostra, detalhes`
  - Evidência (score 3): `e2e/cdu-11.spec.ts:29` -> `// 2.1. O sistema mostra a tela Detalhes do processo`
  - Evidência (score 3): `e2e/cdu-11.spec.ts:35` -> `// 2.3. O sistema mostra a tela Detalhes do subprocesso`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:9` -> `import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';`
- ✅ **[COBERTO]** 4. Usuário clica em uma unidade subordinada, que seja operacional ou interoperacional
  - Palavras-chave usadas: `unidade, clica, subordinada, seja, operacional, interoperacional`
  - Evidência (score 3): `e2e/cdu-11.spec.ts:32` -> `// 2.2. Usuário clica em uma unidade subordinada`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:14` -> `const UNIDADE_ALVO = 'SECAO_111';`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:20` -> `unidade: UNIDADE_ALVO,`
- ✅ **[COBERTO]** 5. O sistema mostra a tela `Detalhes do subprocesso`, com os dados do subprocesso da unidade selecionada
  - Palavras-chave usadas: `subprocesso, unidade, mostra, detalhes, selecionada`
  - Evidência (score 3): `e2e/cdu-11.spec.ts:35` -> `// 2.3. O sistema mostra a tela Detalhes do subprocesso`
  - Evidência (score 3): `e2e/cdu-11.spec.ts:48` -> `// 3.1. O sistema exibe a tela Detalhes do subprocesso com os dados da unidade do usuário`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:29` -> `// 2.1. O sistema mostra a tela Detalhes do processo`
- ✅ **[COBERTO]** 6. Se perfil logado for CHEFE ou SERVIDOR:
  - Palavras-chave usadas: `perfil, logado, chefe, servidor`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:45` -> `test('Fluxo CHEFE/SERVIDOR: Navega direto para Detalhes do Subprocesso (Passo 3)', async ({_resetAutomatico, page, _a...`
- ✅ **[COBERTO]** 7. O sistema exibe a tela `Detalhes do subprocesso` com os dados do subprocesso da unidade do usuário
  - Palavras-chave usadas: `subprocesso, unidade, exibe, detalhes`
  - Evidência (score 4): `e2e/cdu-11.spec.ts:48` -> `// 3.1. O sistema exibe a tela Detalhes do subprocesso com os dados da unidade do usuário`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:33` -> `await navegarParaSubprocesso(page, UNIDADE_ALVO);`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:35` -> `// 2.3. O sistema mostra a tela Detalhes do subprocesso`
- ✅ **[COBERTO]** 8. Na tela de Detalhes do subprocesso, usuário clica no card `Atividades e conhecimentos`.
  - Palavras-chave usadas: `subprocesso, atividades, detalhes, clica, card, conhecimentos`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:11` -> `test.describe.serial('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:35` -> `// 2.3. O sistema mostra a tela Detalhes do subprocesso`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:45` -> `test('Fluxo CHEFE/SERVIDOR: Navega direto para Detalhes do Subprocesso (Passo 3)', async ({_resetAutomatico, page, _a...`
- ✅ **[COBERTO]** 9. O sistema apresenta a tela `Atividades e conhecimentos`, preenchida com os dados da unidade.
  - Palavras-chave usadas: `atividades, unidade, apresenta, conhecimentos, preenchida`
  - Evidência (score 2): `e2e/cdu-11.spec.ts:11` -> `test.describe.serial('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:7` -> `import {navegarParaAtividadesVisualizacao} from './helpers/helpers-atividades.js';`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:14` -> `const UNIDADE_ALVO = 'SECAO_111';`
- 🟡 **[PARCIAL]** 10. Nesta tela, são apresentados a sigla e o nome da unidade, e cada atividade é apresentada como uma tabela, com
  - Palavras-chave usadas: `unidade, atividade, nesta, apresentados, sigla, nome`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:7` -> `import {navegarParaAtividadesVisualizacao} from './helpers/helpers-atividades.js';`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:11` -> `test.describe.serial('CDU-11 - Visualizar cadastro de atividades e conhecimentos', () => {`
  - Evidência (score 1): `e2e/cdu-11.spec.ts:14` -> `const UNIDADE_ALVO = 'SECAO_111';`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **Nesta tela, são apresentados a sigla e o nome da unidade, e cada atividade é apresentada como uma tabela, com** (atualmente parcial).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Motivos: base de análise e pendências objetivas definidas.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **Nesta tela, são apresentados a sigla e o nome da unidade, e cada atividade é apresentada como uma tabela, com** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
