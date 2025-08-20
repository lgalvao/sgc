import {expect, test} from "@playwright/test";
import {login} from "../utils/auth";

test.describe('Configurações', () => {
    test.setTimeout(5000);

    test.beforeEach(async ({page}) => {
        await login(page);

        // Navegar para a página de configurações
        await page.goto(`/configuracoes`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir o título e o texto de configurações', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Configurações do Sistema'})).toBeVisible();
        await expect(page.getByText('Ainda não há configurações disponíveis.')).toBeVisible();
    });
});