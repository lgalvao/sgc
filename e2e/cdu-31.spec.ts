import {expect, test} from './fixtures/complete-fixtures.js';

/**
 * CDU-31 - Configurar sistema
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Usuário logado como ADMIN
 */
test.describe.serial('CDU-31 - Configurar sistema', () => {

    test('Cenários CDU-31: ADMIN navega e altera configurações do sistema', async ({page, autenticadoComoAdmin}) => {
        // Cenario 1: Navegação para configurações
        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);
        await expect(page.getByRole('heading', {name: 'Configurações', exact: true})).toBeVisible();

        // Cenario 2: Visualizar configurações editáveis
        const formInputs = page.locator('input[type="number"], input[type="text"]');
        await expect(formInputs.first()).toBeVisible();
        await expect(page.getByRole('button', {name: /Salvar/i})).toBeVisible();

        // Cenario 3: Salvar configurações
        const primeiroInput = page.locator('input[type="number"]').first();
        if (await primeiroInput.count() > 0) {
            await primeiroInput.clear();
            await primeiroInput.fill('30');
        }

        await page.getByRole('button', {name: /Salvar/i}).click();
        await expect(page.getByText('Configurações salvas com sucesso!')).toBeVisible();
    });
});
