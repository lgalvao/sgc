import { expect, test } from "@playwright/test";
import { login } from "../utils/auth";

test.describe('Detalhes da Unidade no Processo', () => {
    test.setTimeout(5000);

    test.beforeEach(async ({ page }) => {
        await login(page);

        // Navegar para a página de detalhes da unidade no processo (ID 1) no novo padrão
        await page.goto(`/processo/1/SESEL`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir os detalhes da unidade e os cards de funcionalidade', async ({ page }) => {
        await expect(page.getByText('Responsável:')).toBeVisible();
        await expect(page.getByText('Situação:')).toBeVisible();
        await expect(page.getByText('Unidade Atual: SESEL')).toBeVisible();

        await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();
        await expect(page.getByRole('heading', { name: 'Mapa de Competências' })).toBeVisible();
    });

    test('deve navegar para a página de atividades ao clicar no card', async ({ page }) => {
        await page.getByRole('heading', { name: 'Atividades e conhecimentos' }).click();
        await page.waitForURL(/.*\/processo\/\d+\/SESEL\/cadastro/);
        await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();
    });

    
});