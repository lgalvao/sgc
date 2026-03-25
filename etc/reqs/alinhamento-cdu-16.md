# Alinhamento CDU-16 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-16.md`.
- Teste E2E: `e2e/cdu-16.spec.ts` (2 cenários `test`, 4 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **11**.
- Status: **7 cobertos**, **4 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. No `Painel`, o usuário escolhe o processo de revisão desejado.
  - Palavras-chave usadas: `processo, painel, escolhe, revisão, desejado`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:11` -> `const descProcessoRevisao = `Revisão CDU-16 ${timestamp}`;`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:2` -> `import {criarProcessoRevisaoCadastroHomologadoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:4` -> `import {acessarSubprocessoAdmin} from './helpers/helpers-analise.js';`
- 🟡 **[PARCIAL]** 2. O sistema mostra tela `Detalhes do processo`.
  - Palavras-chave usadas: `processo, mostra, detalhes`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:2` -> `import {criarProcessoRevisaoCadastroHomologadoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:4` -> `import {acessarSubprocessoAdmin} from './helpers/helpers-analise.js';`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:11` -> `const descProcessoRevisao = `Revisão CDU-16 ${timestamp}`;`
- ✅ **[COBERTO]** 3. O usuário clica em uma unidade operacional ou interoperacional com subprocesso nas situações 'Revisão do cadastro homologada' ou 'Mapa ajustado'.
  - Palavras-chave usadas: `unidade, subprocesso, situações, clica, operacional, interoperacional`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:28` -> `await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:4` -> `import {acessarSubprocessoAdmin} from './helpers/helpers-analise.js';`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:8` -> `const UNIDADE_ALVO = 'SECAO_211';`
- ✅ **[COBERTO]** 4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada.
  - Palavras-chave usadas: `subprocesso, unidade, mostra, detalhes, selecionada`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:28` -> `await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:4` -> `import {acessarSubprocessoAdmin} from './helpers/helpers-analise.js';`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:8` -> `const UNIDADE_ALVO = 'SECAO_211';`
- ✅ **[COBERTO]** 5. O usuário clica no card `Mapa de Competências`.
  - Palavras-chave usadas: `competências, clica, card, mapa`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:7` -> `test.describe.serial('CDU-16 - Ajustar mapa de competências', () => {`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:3` -> `import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:26` -> `test('Cenários CDU-16: ADMIN ajusta mapa e visualiza impactos', async ({_resetAutomatico, page, _autenticadoComoAdmin...`
- ✅ **[COBERTO]** 6. O sistema mostra a tela `Edição de mapa` preenchida com o mapa do subprocesso da unidade (ver detalhes sobre a tela no caso de uso `Manter mapa de competências`), com os botões Impactos no mapa e `Disponibilizar`.
  - Palavras-chave usadas: `subprocesso, unidade, competências, mostra, edição, mapa`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:7` -> `test.describe.serial('CDU-16 - Ajustar mapa de competências', () => {`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:28` -> `await acessarSubprocessoAdmin(page, descProcessoRevisao, UNIDADE_ALVO);`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:3` -> `import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';`
- ✅ **[COBERTO]** 7. O usuário clica em `Impactos no mapa`.
  - Palavras-chave usadas: `clica, impactos, mapa`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:26` -> `test('Cenários CDU-16: ADMIN ajusta mapa e visualiza impactos', async ({_resetAutomatico, page, _autenticadoComoAdmin...`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:32` -> `await expect(page.getByTestId('cad-mapa__btn-impactos-mapa')).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:39` -> `await page.getByTestId('cad-mapa__btn-impactos-mapa').click();`
- ✅ **[COBERTO]** 8. O sistema mostra o modal `Impactos no mapa`. Ver caso de uso `Verificar impactos no mapa de competências`.
  - Palavras-chave usadas: `competências, mostra, modal, impactos, mapa, verificar`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:7` -> `test.describe.serial('CDU-16 - Ajustar mapa de competências', () => {`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:26` -> `test('Cenários CDU-16: ADMIN ajusta mapa e visualiza impactos', async ({_resetAutomatico, page, _autenticadoComoAdmin...`
  - Evidência (score 2): `e2e/cdu-16.spec.ts:32` -> `await expect(page.getByTestId('cad-mapa__btn-impactos-mapa')).toBeVisible();`
- 🟡 **[PARCIAL]** 9. O usuário usa como base as informações de impactos mostradas nesta tela para alterar o mapa, podendo alterar descrições de competências, de atividades e de conhecimentos; remover ou criar novas competências; e ajustar a associação das atividades às competências do mapa, conforme descrito no caso de uso `Manter mapa de competências`.
  - Palavras-chave usadas: `competências, atividades, base, informações, impactos, mostradas`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:7` -> `test.describe.serial('CDU-16 - Ajustar mapa de competências', () => {`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:26` -> `test('Cenários CDU-16: ADMIN ajusta mapa e visualiza impactos', async ({_resetAutomatico, page, _autenticadoComoAdmin...`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:32` -> `await expect(page.getByTestId('cad-mapa__btn-impactos-mapa')).toBeVisible();`
- 🟡 **[PARCIAL]** 10. O usuário deve associar a uma competência todas as atividades ainda não associadas.
  - Palavras-chave usadas: `competência, atividades, associar, todas, ainda, associadas`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:7` -> `test.describe.serial('CDU-16 - Ajustar mapa de competências', () => {`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:13` -> `const competencia1 = 'Competência fixture 1';`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:14` -> `const competencia2 = 'Competência fixture 2';`
- 🟡 **[PARCIAL]** 11. Quando concluir os ajustes, o usuário clica em `Disponibilizar` e o sistema segue o fluxo descrito no caso de uso `Disponibilizar mapa de competências`.
  - Palavras-chave usadas: `competências, concluir, ajustes, clica, disponibilizar, segue`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:7` -> `test.describe.serial('CDU-16 - Ajustar mapa de competências', () => {`
  - Evidência (score 1): `e2e/cdu-16.spec.ts:33` -> `await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **O sistema mostra tela `Detalhes do processo`.** (atualmente parcial).
- Completar cobertura do item: **O usuário usa como base as informações de impactos mostradas nesta tela para alterar o mapa, podendo alterar descrições de competências, de atividades e de conhecimentos; remover ou criar novas competências; e ajustar a associação das atividades às competências do mapa, conforme descrito no caso de uso `Manter mapa de competências`.** (atualmente parcial).
- Completar cobertura do item: **O usuário deve associar a uma competência todas as atividades ainda não associadas.** (atualmente parcial).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Motivos: base de análise e pendências objetivas definidas.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **O sistema mostra tela `Detalhes do processo`.** (atualmente parcial).
  - Completar cobertura do item: **O usuário usa como base as informações de impactos mostradas nesta tela para alterar o mapa, podendo alterar descrições de competências, de atividades e de conhecimentos; remover ou criar novas competências; e ajustar a associação das atividades às competências do mapa, conforme descrito no caso de uso `Manter mapa de competências`.** (atualmente parcial).
  - Completar cobertura do item: **O usuário deve associar a uma competência todas as atividades ainda não associadas.** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
