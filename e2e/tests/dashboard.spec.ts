import {expect, test} from '@playwright/test';
import {login} from './helpers';

test.describe('Dashboard', () => {
    test('deve exibir o dashboard corretamente para usuário autenticado', async ({page}) => {
        await login(page, 'sedoc@tre-pe.jus.br', 'admin123');
        await expect(page).toHaveTitle(/Dashboard - SGC/);
        await expect(page.getByTestId('dashboard-title')).toBeVisible();
        await expect(page.getByTestId('total-processos')).toBeVisible();
        await expect(page.getByTestId('unidades-ativas')).toBeVisible();
        await expect(page.getByTestId('processos-finalizados')).toBeVisible();
    });

    test('deve redirecionar para login se não autenticado', async ({page}) => {
        await page.goto('/');
        await expect(page).toHaveTitle(/Login - SGC/);
        await expect(page.getByLabel('Usuário')).toBeVisible();
        await expect(page.getByLabel('Senha')).toBeVisible();
    });
}); 