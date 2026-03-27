# Alinhamento CDU-36 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-36.md`.
- Teste E2E: `e2e/cdu-36.spec.ts` (1 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **13**.
- Status: **1 cobertos**, **8 parciais**, **4 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- 🟡 **[PARCIAL]** 1. O usuário acessa Relatórios na barra de navegacao.
  - Palavras-chave usadas: `relatórios, acessa, barra, navegacao`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:14` -> `test('Cenários CDU-36: ADMIN navega e gera relatórios de mapas', async ({_resetAutomatico, page, request, _autenticad...`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:25` -> `// Cenario 1: Navegação para página de relatórios`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:26` -> `await page.getByRole('link', {name: /Relatórios/i}).click();`
- 🟡 **[PARCIAL]** 2. O usuário seleciona a opção "Mapas".
  - Palavras-chave usadas: `seleciona, opção, mapas`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:5` -> `* CDU-36 - Gerar relatório de mapas`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:12` -> `test.describe.serial('CDU-36 - Gerar relatório de mapas', () => {`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:14` -> `test('Cenários CDU-36: ADMIN navega e gera relatórios de mapas', async ({_resetAutomatico, page, request, _autenticad...`
- ❌ **[NAO_COBERTO]** 3. O usuário define os filtros:
  - Palavras-chave usadas: `define, filtros`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 4. Processo (Obrigatório)
  - Palavras-chave usadas: `processo, obrigatório`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:2` -> `import {criarProcessoMapaHomologadoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:15` -> `const descricaoProcesso = `Relatório CDU-36 ${Date.now()}`;`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:16` -> `const processo = await criarProcessoMapaHomologadoFixture(request, {`
- 🟡 **[PARCIAL]** 5. Unidade (Opcional - se vazio, considera todas as unidades do processo)
  - Palavras-chave usadas: `unidade, unidades, processo, opcional, vazio, considera`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:2` -> `import {criarProcessoMapaHomologadoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:15` -> `const descricaoProcesso = `Relatório CDU-36 ${Date.now()}`;`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:16` -> `const processo = await criarProcessoMapaHomologadoFixture(request, {`
- 🟡 **[PARCIAL]** 6. O usuário aciona a opção "Gerar".
  - Palavras-chave usadas: `aciona, opção, gerar`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:5` -> `* CDU-36 - Gerar relatório de mapas`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:12` -> `test.describe.serial('CDU-36 - Gerar relatório de mapas', () => {`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:34` -> `const botaoGerar = page.getByRole('button', {name: 'Gerar PDF'});`
- ✅ **[COBERTO]** 7. O sistema processa os dados e gera um arquivo PDF, contendo, para cada mapa, as seguintes informações:
  - Palavras-chave usadas: `processa, gera, arquivo, contendo, cada, mapa`
  - Evidência (score 2): `e2e/cdu-36.spec.ts:5` -> `* CDU-36 - Gerar relatório de mapas`
  - Evidência (score 2): `e2e/cdu-36.spec.ts:12` -> `test.describe.serial('CDU-36 - Gerar relatório de mapas', () => {`
  - Evidência (score 2): `e2e/cdu-36.spec.ts:14` -> `test('Cenários CDU-36: ADMIN navega e gera relatórios de mapas', async ({_resetAutomatico, page, request, _autenticad...`
- 🟡 **[PARCIAL]** 8. Unidade (Sigla e Nome)
  - Palavras-chave usadas: `unidade, sigla, nome`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:18` -> `unidade: 'ASSESSORIA_12',`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:33` -> `const selectUnidade = page.getByLabel('Selecione a unidade');`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:36` -> `await expect(selectUnidade).toBeVisible();`
- ❌ **[NAO_COBERTO]** 9. Para cada competencia:
  - Palavras-chave usadas: `competencia, cada`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 10. Descricao da competência
  - Palavras-chave usadas: `competência, descricao`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:15` -> `const descricaoProcesso = `Relatório CDU-36 ${Date.now()}`;`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:17` -> `descricao: descricaoProcesso,`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:23` -> `await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();`
- ❌ **[NAO_COBERTO]** 11. Atividades da competencia
  - Palavras-chave usadas: `atividades, competencia`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ❌ **[NAO_COBERTO]** 12. Conhecimentos da atividade
  - Palavras-chave usadas: `atividade, conhecimentos`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 13. O sistema disponibiliza o arquivo para download.
  - Palavras-chave usadas: `disponibiliza, arquivo, download`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:42` -> `const downloadPromise = page.waitForEvent('download');`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:44` -> `const download = await downloadPromise;`
  - Evidência (score 1): `e2e/cdu-36.spec.ts:45` -> `expect(download.suggestedFilename()).toContain(`relatorio-mapas-${processo.codigo}.pdf`);`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **O usuário acessa Relatórios na barra de navegacao.** (atualmente parcial).
- Completar cobertura do item: **O usuário seleciona a opção "Mapas".** (atualmente parcial).
- Implementar cenário específico para: **O usuário define os filtros:** (sem evidência no E2E atual).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: há itens sem cobertura E2E.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **O usuário acessa Relatórios na barra de navegacao.** (atualmente parcial).
  - Completar cobertura do item: **O usuário seleciona a opção "Mapas".** (atualmente parcial).
  - Implementar cenário específico para: **O usuário define os filtros:** (sem evidência no E2E atual).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
