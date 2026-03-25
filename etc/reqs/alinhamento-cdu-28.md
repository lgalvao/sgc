# Alinhamento CDU-28 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-28.md`.
- Teste E2E: `e2e/cdu-28.spec.ts` (3 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **21**.
- Status: **9 cobertos**, **7 parciais**, **5 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. ADMIN clica em `Unidade` no menu (este é o comando equivalente a `Minha unidade`, visto por outros perfis).
  - Palavras-chave usadas: `unidade, admin, clica, menu, este, comando`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:16` -> `test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticad...`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:4` -> `const SIGLA_UNIDADE = 'SECRETARIA_2';`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:6` -> `async function acessarUnidadeAlvo(page: import('@playwright/test').Page) {`
- 🟡 **[PARCIAL]** 2. Sistema mostra a árvore completa de unidades.
  - Palavras-chave usadas: `unidades, mostra, árvore, completa`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:12` -> `await page.getByRole('link', {name: /Unidades/i}).click();`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:13` -> `await expect(page).toHaveURL(/\/unidades/);`
- 🟡 **[PARCIAL]** 3. ADMIN clica em umas das unidades.
  - Palavras-chave usadas: `unidades, admin, clica`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:11` -> `test.beforeEach(async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:12` -> `await page.getByRole('link', {name: /Unidades/i}).click();`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:13` -> `await expect(page).toHaveURL(/\/unidades/);`
- ✅ **[COBERTO]** 4. Sistema mostra a pagina `Detalhes da unidade`
  - Palavras-chave usadas: `unidade, mostra, pagina, detalhes`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:16` -> `test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticad...`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:4` -> `const SIGLA_UNIDADE = 'SECRETARIA_2';`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:6` -> `async function acessarUnidadeAlvo(page: import('@playwright/test').Page) {`
- ✅ **[COBERTO]** 5. ADMIN clica no botão `Criar atribuição`.
  - Palavras-chave usadas: `admin, clica, botão, criar, atribuição`
  - Evidência (score 3): `e2e/cdu-28.spec.ts:16` -> `test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticad...`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:42` -> `test('Cenario 3: ADMIN cria atribuição temporária com sucesso', async ({_resetAutomatico, _autenticadoComoAdmin, page...`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:3` -> `test.describe.serial('CDU-28 - Manter atribuição temporária', () => {`
- ❌ **[NAO_COBERTO]** 6. Sistema apresenta um modal com estes campos:
  - Palavras-chave usadas: `apresenta, modal, estes`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 7. Dropdown pesquisável `Servidores` com os nomes dos servidores da unidade
  - Palavras-chave usadas: `unidade, dropdown, pesquisável, servidores, nomes`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:4` -> `const SIGLA_UNIDADE = 'SECRETARIA_2';`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:6` -> `async function acessarUnidadeAlvo(page: import('@playwright/test').Page) {`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:7` -> `await expect(page.getByTestId(`link-arvore-unidade-${SIGLA_UNIDADE}`)).toBeVisible();`
- ✅ **[COBERTO]** 8. `Data de início`
  - Palavras-chave usadas: `data, início`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:39` -> `await expect(page.getByText('Informe a data de início.')).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:36` -> `await page.getByTestId('input-data-termino').fill('2030-12-31');`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:53` -> `await page.getByTestId('input-data-inicio').fill('2030-01-01');`
- 🟡 **[PARCIAL]** 9. `Data de término`
  - Palavras-chave usadas: `data, término`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:36` -> `await page.getByTestId('input-data-termino').fill('2030-12-31');`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:39` -> `await expect(page.getByText('Informe a data de início.')).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:53` -> `await page.getByTestId('input-data-inicio').fill('2030-01-01');`
- 🟡 **[PARCIAL]** 10. `Justificativa`
  - Palavras-chave usadas: `justificativa`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:35` -> `await page.getByTestId('textarea-justificativa').fill('Cobertura de férias');`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:55` -> `await page.getByTestId('textarea-justificativa').fill('Cobertura de férias');`
- ❌ **[NAO_COBERTO]** 11. Botões `Confirmar` e `Cancelar`
  - Palavras-chave usadas: `botões, confirmar, cancelar`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- 🟡 **[PARCIAL]** 12. ADMIN seleciona o servidor, define as datas e inclui uma justificativa. Todos os campos *são obrigatórios*.
  - Palavras-chave usadas: `admin, seleciona, servidor, define, datas, inclui`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:11` -> `test.beforeEach(async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:16` -> `test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticad...`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:23` -> `test('Cenario 2: Campos obrigatórios devem ser validados', async ({_resetAutomatico, _autenticadoComoAdmin, page}) => {`
- ✅ **[COBERTO]** 13. Sistema registra internamente a atribuição temporária e mostra uma confirmação "Atribuição criada".
  - Palavras-chave usadas: `registra, internamente, atribuição, temporária, mostra, confirmação`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:3` -> `test.describe.serial('CDU-28 - Manter atribuição temporária', () => {`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:42` -> `test('Cenario 3: ADMIN cria atribuição temporária com sucesso', async ({_resetAutomatico, _autenticadoComoAdmin, page...`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:16` -> `test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticad...`
- ✅ **[COBERTO]** 14. O sistema envia notificação por e-mail para o usuário que recebeu a atribuição temporária:
  - Palavras-chave usadas: `envia, notificação, e-mail, recebeu, atribuição, temporária`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:3` -> `test.describe.serial('CDU-28 - Manter atribuição temporária', () => {`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:42` -> `test('Cenario 3: ADMIN cria atribuição temporária com sucesso', async ({_resetAutomatico, _autenticadoComoAdmin, page...`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:16` -> `test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticad...`
- 🟡 **[PARCIAL]** 15. O sistema cria internamente um alerta para o usuário:
  - Palavras-chave usadas: `alerta, cria, internamente`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:16` -> `test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticad...`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:20` -> `await expect(page.getByTestId('unidade-view__btn-criar-atribuicao')).toBeVisible();`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:25` -> `await page.getByTestId('unidade-view__btn-criar-atribuicao').click();`
- ✅ **[COBERTO]** 16. `Descrição`: "Atribuição temporária de perfil de CHEFE na unidade [SIGLA_UNIDADE]"
  - Palavras-chave usadas: `perfil, unidade, sigla_unidade, descrição, atribuição, temporária`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:3` -> `test.describe.serial('CDU-28 - Manter atribuição temporária', () => {`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:4` -> `const SIGLA_UNIDADE = 'SECRETARIA_2';`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:7` -> `await expect(page.getByTestId(`link-arvore-unidade-${SIGLA_UNIDADE}`)).toBeVisible();`
- ❌ **[NAO_COBERTO]** 17. `Processo`: (Vazio)
  - Palavras-chave usadas: `processo, vazio`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ❌ **[NAO_COBERTO]** 18. `Data/hora`: Data/hora atual
  - Palavras-chave usadas: `data/hora, atual`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ✅ **[COBERTO]** 19. `Unidade de origem`: ADMIN
  - Palavras-chave usadas: `unidade, origem, admin`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:16` -> `test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticad...`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:4` -> `const SIGLA_UNIDADE = 'SECRETARIA_2';`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:6` -> `async function acessarUnidadeAlvo(page: import('@playwright/test').Page) {`
- ❌ **[NAO_COBERTO]** 20. `Usuário destino`: [USUARIO_SERVIDOR]
  - Palavras-chave usadas: `destino, usuario_servidor`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ✅ **[COBERTO]** 21. O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE. A atribuição temporária
  - Palavras-chave usadas: `perfil, recebe, atribuição, temporária, passa, mesmos`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:3` -> `test.describe.serial('CDU-28 - Manter atribuição temporária', () => {`
  - Evidência (score 2): `e2e/cdu-28.spec.ts:42` -> `test('Cenario 3: ADMIN cria atribuição temporária com sucesso', async ({_resetAutomatico, _autenticadoComoAdmin, page...`
  - Evidência (score 1): `e2e/cdu-28.spec.ts:16` -> `test('Cenario 1: ADMIN acessa detalhes da unidade e opção de criar atribuição', async ({_resetAutomatico, _autenticad...`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **Sistema mostra a árvore completa de unidades.** (atualmente parcial).
- Completar cobertura do item: **ADMIN clica em umas das unidades.** (atualmente parcial).
- Implementar cenário específico para: **Sistema apresenta um modal com estes campos:** (sem evidência no E2E atual).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: há itens sem cobertura E2E.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **Sistema mostra a árvore completa de unidades.** (atualmente parcial).
  - Completar cobertura do item: **ADMIN clica em umas das unidades.** (atualmente parcial).
  - Implementar cenário específico para: **Sistema apresenta um modal com estes campos:** (sem evidência no E2E atual).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
