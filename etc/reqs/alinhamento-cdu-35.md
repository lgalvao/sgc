# Alinhamento CDU-35 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-35.md`.
- Teste E2E: `e2e/cdu-35.spec.ts` (1 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **12**.
- Status: **0 cobertos**, **9 parciais**, **3 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- 🟡 **[PARCIAL]** 1. O usuário acessa Relatórios.
  - Palavras-chave usadas: `relatórios, acessa`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:14` -> `test('Cenários CDU-35: ADMIN navega e gera relatórios de andamento', async ({_resetAutomatico, page, request, _autent...`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:27` -> `// Cenario 1: Navegação para página de relatórios`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:28` -> `await page.getByRole('link', {name: /Relatórios/i}).click();`
- 🟡 **[PARCIAL]** 2. O usuário seleciona a opção "Andamento de processo".
  - Palavras-chave usadas: `processo, seleciona, opção, andamento`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:2` -> `import {criarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:5` -> `* CDU-35 - Gerar relatório de andamento`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:12` -> `test.describe.serial('CDU-35 - Gerar relatório de andamento', () => {`
- 🟡 **[PARCIAL]** 3. O usuário seleciona o Processo desejado (ex: "Mapeamento 2027").
  - Palavras-chave usadas: `processo, seleciona, desejado, mapeamento`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:2` -> `import {criarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:15` -> `const descricaoProcesso = `Relatório CDU-35 ${Date.now()}`;`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:16` -> `await criarProcessoFixture(request, {`
- 🟡 **[PARCIAL]** 4. O sistema exibe o relatório em tela contendo as seguintes colunas:
  - Palavras-chave usadas: `relatório, exibe, contendo, seguintes, colunas`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:5` -> `* CDU-35 - Gerar relatório de andamento`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:12` -> `test.describe.serial('CDU-35 - Gerar relatório de andamento', () => {`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:14` -> `test('Cenários CDU-35: ADMIN navega e gera relatórios de andamento', async ({_resetAutomatico, page, request, _autent...`
- 🟡 **[PARCIAL]** 5. Sigla da unidade
  - Palavras-chave usadas: `unidade, sigla`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:19` -> `unidade: 'ASSESSORIA_12',`
- 🟡 **[PARCIAL]** 6. Nome da unidade
  - Palavras-chave usadas: `unidade, nome`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:19` -> `unidade: 'ASSESSORIA_12',`
- 🟡 **[PARCIAL]** 7. Situação atual do subprocesso da unidade, para o processo selecionado
  - Palavras-chave usadas: `situação, subprocesso, unidade, processo, atual, selecionado`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:2` -> `import {criarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:15` -> `const descricaoProcesso = `Relatório CDU-35 ${Date.now()}`;`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:16` -> `await criarProcessoFixture(request, {`
- ❌ **[NAO_COBERTO]** 8. Data da última movimentação
  - Palavras-chave usadas: `data, última, movimentação`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ❌ **[NAO_COBERTO]** 9. Responsável
  - Palavras-chave usadas: `responsável`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ❌ **[NAO_COBERTO]** 10. Titular (Se não for o responsavel)
  - Palavras-chave usadas: `titular, responsavel`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 11. O usuário pode optar por exportar os dados para PDF clicando no botao `PDF`.
  - Palavras-chave usadas: `pode, optar, exportar, clicando, botao`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:33` -> `const botaoGerar = page.getByRole('button', {name: 'Gerar relatório'});`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:35` -> `await expect(botaoGerar).toBeDisabled();`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:38` -> `await expect(botaoGerar).toBeEnabled();`
- 🟡 **[PARCIAL]** 12. O sistema gera o arquivo selecionado e o disponibiliza para download.
  - Palavras-chave usadas: `gera, arquivo, selecionado, disponibiliza, download`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:5` -> `* CDU-35 - Gerar relatório de andamento`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:12` -> `test.describe.serial('CDU-35 - Gerar relatório de andamento', () => {`
  - Evidência (score 1): `e2e/cdu-35.spec.ts:14` -> `test('Cenários CDU-35: ADMIN navega e gera relatórios de andamento', async ({_resetAutomatico, page, request, _autent...`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **O usuário acessa Relatórios.** (atualmente parcial).
- Completar cobertura do item: **O usuário seleciona a opção "Andamento de processo".** (atualmente parcial).
- Completar cobertura do item: **O usuário seleciona o Processo desejado (ex: "Mapeamento 2027").** (atualmente parcial).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: há itens sem cobertura E2E.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **O usuário acessa Relatórios.** (atualmente parcial).
  - Completar cobertura do item: **O usuário seleciona a opção "Andamento de processo".** (atualmente parcial).
  - Completar cobertura do item: **O usuário seleciona o Processo desejado (ex: "Mapeamento 2027").** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
