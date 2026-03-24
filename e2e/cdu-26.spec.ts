import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaValidadoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';

/**
 * CDU-26 - Homologar validação de mapas de competências em bloco
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões'
 *
 * Fluxo principal:
 * 1. ADMIN acessa processo em andamento
 * 2. Sistema mostra Detalhes do processo
 * 3. Sistema identifica unidades elegíveis para homologação
 * 4. ADMIN clica no botão 'Homologar mapas em bloco'
 * 5. Sistema abre modal com lista de unidades
 * 6. ADMIN confirma
 * 7. Sistema executa homologação para cada unidade
 */
test.describe.serial('CDU-26 - Homologar validação de mapas em bloco', () => {
    const UNIDADE_1 = 'SECRETARIA_2';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-26 ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoMapaValidadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('Cenario 1: ADMIN visualiza botão Homologar mapas em bloco', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnHomologar = page.getByRole('button', {name: /^Homologar mapas em bloco$/i}).first();
        await expect(btnHomologar).toBeVisible();
        await expect(btnHomologar).toBeEnabled();
    });

    test('Cenario 2: ADMIN abre modal de homologação de mapa em bloco', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        const btnHomologar = page.getByRole('button', {name: /^Homologar mapas em bloco$/i}).first();
        await expect(btnHomologar).toBeVisible();
        await btnHomologar.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Homologação de mapa em bloco/i)).toBeVisible();
        await expect(modal.getByText(/Selecione abaixo as unidades cujos mapas deverão ser homologados/i)).toBeVisible();
        await expect(modal.locator('table')).toBeVisible();
        await expect(modal.getByRole('button', {name: /Cancelar/i})).toBeVisible();
        await expect(modal.getByRole('button', {name: /^Homologar$/i})).toBeVisible();

        await modal.getByRole('button', {name: /Cancelar/i}).click();
    });

    test('Cenario 3: Cancelar homologação de mapa em bloco', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        const btnHomologar = page.getByRole('button', {name: /^Homologar mapas em bloco$/i}).first();
        await expect(btnHomologar).toBeVisible();
        await btnHomologar.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        await modal.getByRole('button', {name: /Cancelar/i}).click();

        await expect(modal).toBeHidden();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    });

    test('Cenario 4: ADMIN confirma homologação em bloco e é redirecionado ao painel', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        const btnHomologar = page.getByRole('button', {name: /^Homologar mapas em bloco$/i}).first();
        await expect(btnHomologar).toBeVisible();
        await btnHomologar.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.locator('table')).toBeVisible();

        // Unidade deve aparecer marcada (checkbox selecionado por padrão)
        const checkboxUnidade = modal.locator('input[type="checkbox"]').first();
        await expect(checkboxUnidade).toBeChecked();

        const responsePromise = page.waitForResponse(resp => resp.url().includes('/api/') && resp.request().method() === 'POST');
        await modal.getByRole('button', {name: /^Homologar$/i}).click();
        await responsePromise;

        await page.waitForURL(/\/painel/);
        await expect(page.getByTestId('tbl-processos')).toBeVisible();
    });
});
