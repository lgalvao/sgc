import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {login} from "~/utils/auth";

test.describe('Detalhes da Unidade no Processo', () => {
    test.beforeEach(async ({page}) => {
        await login(page);
        await page.goto('/processo/4/SESEL');
        //await page.waitForLoadState('networkidle');
    });

    test('deve exibir os detalhes da unidade e os cards de funcionalidade', async ({page}) => {
        await expect(page.getByText('Titular:')).toBeVisible();

        await expect(page.getByTestId('atividades-card')).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Mapa de Competências'})).toBeVisible();
    });

    test('deve navegar para a página de atividades ao clicar no card', async ({page}) => {
        await page.getByTestId('atividades-card').click();
        await page.waitForURL(/.*\/processo\/\d+\/SESEL\/vis-cadastro/);
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });
});
