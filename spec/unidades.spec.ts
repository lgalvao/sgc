import {expect, test} from "@playwright/test";
import {login} from "./utils/auth";

test.describe('Detalhes da Unidade', () => {
    test.beforeEach(async ({page}) => {
        await login(page);

        // Navegar para a página da unidade STIC
        await page.goto(`/unidade/STIC`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir os detalhes da unidade e a tabela de subunidades', async ({page}) => {
        await expect(page.getByRole('heading', { name: 'STIC - Secretaria de Informática e Comunicações' })).toBeVisible();

        await expect(page.getByRole('button', {name: 'Visualizar Mapa'})).not.toBeVisible(); // Verifica que o botão não está visível quando não há mapa
        await expect(page.getByRole('heading', {name: 'Unidades Subordinadas'})).toBeVisible();
        await expect(page.getByRole('table')).toBeVisible();
    });

    test('deve exibir subunidades na tabela', async ({page}) => {
        // Verifica se há pelo menos uma linha de dados na tabela (excluindo o cabeçalho)
        const rows = page.getByRole('row').filter({hasNot: page.getByRole('rowgroup', {name: 'Unidade'})});
        await expect(rows).toHaveCount(6); // Assumindo que há 6 subunidades no snapshot para STIC
    });
});