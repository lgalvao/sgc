import {expect, test} from '@playwright/test';

test('deve exibir o dashboard após login', async ({page}) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'sedoc@tre-pe.jus.br');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');

    // Aguarda o dashboard ser carregado
    await expect(page).toHaveTitle(/Dashboard - SGC/);
    await expect(page.getByTestId('dashboard-title')).toBeVisible();
    await expect(page.getByTestId('novo-processo-btn')).toBeVisible();
    await expect(page.getByTestId('total-processos')).toBeVisible();
    await expect(page.getByTestId('unidades-ativas')).toBeVisible();
    await expect(page.getByTestId('processos-finalizados')).toBeVisible();
}); 