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
        // Cenario 1: Navegação para parâmetros
        await page.getByTestId('btn-parametros').click();
        await expect(page).toHaveURL(/\/parametros/);
        await expect(page.getByRole('heading', {name: 'Parâmetros', exact: true})).toBeVisible();

        // Cenario 2: Visualizar configurações editáveis
        await expect(page.getByLabel(/Dias para inativação de processos/i)).toBeVisible();
        await expect(page.getByLabel(/Dias para indicação de alerta como novo/i)).toBeVisible();
        await expect(page.getByRole('button', {name: /Salvar Configurações/i})).toBeVisible();

        // Cenario 3: Salvar configurações
        await page.getByLabel(/Dias para inativação de processos/i).fill('30');
        await page.getByLabel(/Dias para indicação de alerta como novo/i).fill('3');
        await page.getByRole('button', {name: /Salvar Configurações/i}).click();
        await expect(page.getByText('Configurações salvas.')).toBeVisible();
    });
});
