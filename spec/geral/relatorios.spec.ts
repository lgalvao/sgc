import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {login} from "~/utils/auth";

test.describe('Relatórios', () => {
    test.beforeEach(async ({page}) => {
        await login(page);

        // Navegar para a página de relatórios
        await page.goto(`/relatorios`);
        //await page.waitForLoadState('networkidle');
    });

    test('deve exibir o título e os cards de relatórios', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Relatórios'})).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Mapas Vigentes'})).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Diagnósticos de Gaps'})).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Andamento Geral'})).toBeVisible();
    });
});