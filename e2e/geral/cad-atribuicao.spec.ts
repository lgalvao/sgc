import {expect} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {login} from "~/utils/auth";

test.describe('Cadastro de Atribuição Temporária', () => {
    test.beforeEach(async ({page}) => {
        await login(page);

        // Navegar para a página de cadastro de atribuição (unidade STIC)
        await page.goto(`/unidade/STIC/atribuicao`);
        //await page.waitForLoadState('networkidle');
    });

    test('deve exibir o título da página e os campos do formulário', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Criar atribuição temporária'})).toBeVisible();
        await expect(page.getByLabel('Servidor')).toBeVisible();
        await expect(page.getByLabel('Data de término')).toBeVisible();
        await expect(page.getByLabel('Justificativa')).toBeVisible();
        await expect(page.getByRole('button', {name: 'Criar'})).toBeVisible();
        await expect(page.getByRole('button', {name: 'Cancelar'})).toBeVisible();
    });

    test('deve permitir criar uma nova atribuição temporária', async ({page}) => {
        // Selecionar o primeiro servidor elegível disponível de forma robusta
        const firstEligibleValue = await page.locator('[data-testid="select-servidor"] option:not([disabled])').first().getAttribute('value');
        expect(firstEligibleValue).toBeTruthy();
        await page.locator('[data-testid="select-servidor"]').selectOption(firstEligibleValue!);

        // Preencher data de término
        await page.getByTestId('input-data-termino').fill('2025-12-31');

        // Preencher justificativa
        await page.getByTestId('textarea-justificativa').fill(`Atribuição de Teste ${Date.now()}`);

        // Clicar em Criar
        await page.getByTestId('btn-criar-atribuicao').click();

        // Verificar redirecionamento
        await page.waitForURL(`/unidade/STIC`);
    });
});