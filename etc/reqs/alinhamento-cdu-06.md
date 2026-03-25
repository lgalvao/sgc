# Alinhamento CDU-06 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-06.md`.
- Teste E2E: `e2e/cdu-06.spec.ts` (3 cenários `test`, 0 `test.step`).
- Contextos `describe`: CDU-06 - Detalhar processo.

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **12**.
- Status: **3 cobertos**, **6 parciais**, **3 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. O sistema mostra a tela `Detalhes do processo` com os dados do processo escolhido.
  - Palavras-chave usadas: `processo, mostra, detalhes, escolhido`
  - Evidência (score 2): `e2e/cdu-06.spec.ts:4` -> `acessarDetalhesProcesso,`
  - Evidência (score 2): `e2e/cdu-06.spec.ts:7` -> `verificarDetalhesProcesso,`
  - Evidência (score 2): `e2e/cdu-06.spec.ts:18` -> `esperarPaginaDetalhesProcesso,`
- 🟡 **[PARCIAL]** 2. A tela será composta pelas seções Dados do processo e Unidades participantes.
  - Palavras-chave usadas: `processo, unidades, será, composta, pelas, seções`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:4` -> `acessarDetalhesProcesso,`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:5` -> `criarProcesso,`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:6` -> `extrairProcessoCodigo,`
- 🟡 **[PARCIAL]** 3. Seção `Dados do processo` (sem título):
  - Palavras-chave usadas: `processo, título`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:4` -> `acessarDetalhesProcesso,`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:5` -> `criarProcesso,`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:6` -> `extrairProcessoCodigo,`
- 🟡 **[PARCIAL]** 4. Informações da descrição, tipo e da situação dos processos (ver arquivo _situacoes.md).
  - Palavras-chave usadas: `situação, processos, situacoes, informações, descrição, tipo`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:9` -> `} from './helpers/helpers-processos.js';`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:32` -> `tipo: 'MAPEAMENTO',`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:45` -> `tipo: 'Mapeamento',`
- ✅ **[COBERTO]** 5. Se for perfil ADMIN, exibe o botão Finalizar processo.
  - Palavras-chave usadas: `perfil, processo, admin, exibe, botão, finalizar`
  - Evidência (score 2): `e2e/cdu-06.spec.ts:26` -> `test('Fase 1: Deve exibir detalhes do processo para ADMIN e ações de unidade', async ({_resetAutomatico, page, _auten...`
  - Evidência (score 2): `e2e/cdu-06.spec.ts:57` -> `// Botão "Alterar data limite" deve estar visível para Admin`
  - Evidência (score 2): `e2e/cdu-06.spec.ts:68` -> `test('Fase 1b: Deve exibir detalhes do processo para GESTOR e ocultar ações ADMIN', async ({_resetAutomatico, page}) ...`
- ❌ **[NAO_COBERTO]** 6. Seção unidades participantes:
  - Palavras-chave usadas: `unidades, participantes`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ❌ **[NAO_COBERTO]** 7. Subárvore das unidades hierarquicamente inferiores.
  - Palavras-chave usadas: `unidades, subárvore, hierarquicamente, inferiores`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ✅ **[COBERTO]** 8. Para cada unidade operacional e interoperacional da subárvore são exibidas, em linha, as informações da situação
  - Palavras-chave usadas: `unidade, situação, cada, operacional, interoperacional, subárvore`
  - Evidência (score 2): `e2e/cdu-06.spec.ts:60` -> `// Botão "Reabrir cadastro" NÃO deve estar visível pois a situação é "Não iniciado"`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:8` -> `verificarUnidadeParticipante`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:13` -> `disponibilizarCadastro,`
- ❌ **[NAO_COBERTO]** 9. O usuário poderá clicar nas unidades operacionais e interoperacionais para visualizar a tela Detalhes do
  - Palavras-chave usadas: `unidades, poderá, clicar, operacionais, interoperacionais, visualizar`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 10. Caso existam unidades subordinadas cujos subprocessos estejam localizados na unidade do usuário, os seguintes
  - Palavras-chave usadas: `unidades, subprocessos, unidade, existam, subordinadas, cujos`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:8` -> `verificarUnidadeParticipante`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:24` -> `const UNIDADE_ALVO = 'ASSESSORIA_12';`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:26` -> `test('Fase 1: Deve exibir detalhes do processo para ADMIN e ações de unidade', async ({_resetAutomatico, page, _auten...`
- 🟡 **[PARCIAL]** 11. Aceitar/Homologar em bloco, se existirem unidades subordinadas com subprocesso na situação 'Cadastro
  - Palavras-chave usadas: `unidades, subprocesso, situação, aceitar/homologar, bloco, existirem`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:16` -> `import {acessarSubprocessoChefeDireto} from './helpers/helpers-analise.js';`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:19` -> `navegarParaSubprocesso,`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:55` -> `await navegarParaSubprocesso(page, UNIDADE_ALVO);`
- 🟡 **[PARCIAL]** 12. Aceitar/Homologar mapas em bloco, se existirem unidades subordinadas com subprocesso nas situações 'Mapa validado'
  - Palavras-chave usadas: `unidades, subprocesso, situações, aceitar/homologar, mapas, bloco`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:16` -> `import {acessarSubprocessoChefeDireto} from './helpers/helpers-analise.js';`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:19` -> `navegarParaSubprocesso,`
  - Evidência (score 1): `e2e/cdu-06.spec.ts:55` -> `await navegarParaSubprocesso(page, UNIDADE_ALVO);`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **A tela será composta pelas seções Dados do processo e Unidades participantes.** (atualmente parcial).
- Completar cobertura do item: **Seção `Dados do processo` (sem título):** (atualmente parcial).
- Completar cobertura do item: **Informações da descrição, tipo e da situação dos processos (ver arquivo _situacoes.md).** (atualmente parcial).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: há itens sem cobertura E2E.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **A tela será composta pelas seções Dados do processo e Unidades participantes.** (atualmente parcial).
  - Completar cobertura do item: **Seção `Dados do processo` (sem título):** (atualmente parcial).
  - Completar cobertura do item: **Informações da descrição, tipo e da situação dos processos (ver arquivo _situacoes.md).** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
