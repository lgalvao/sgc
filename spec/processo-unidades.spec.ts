import { expect, test } from "@playwright/test";
import { login } from "../utils/auth";

test.describe('Detalhes do Processo - Unidades', () => {
    test.setTimeout(5000);

    test.beforeEach(async ({ page }) => {
        await login(page);

        // Navegar para a página de detalhes do processo (ID 1)
        await page.goto(`/processos/1/unidades`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir os detalhes do processo e a tabela de unidades participantes', async ({ page }) => {
        await expect(page.getByRole('heading', { name: 'Mapeamento de competências - 2025' })).toBeVisible();
        await expect(page.getByText('Tipo: Mapeamento')).toBeVisible();
        await expect(page.getByText('Situação: Em andamento')).toBeVisible();
        await expect(page.getByRole('heading', { name: 'Unidades participantes' })).toBeVisible();
        await expect(page.getByRole('table')).toBeVisible();
        await expect(page.getByRole('cell', { name: 'Unidade', exact: true })).toBeVisible();
        await expect(page.getByRole('cell', { name: 'Situação' })).toBeVisible();
        await expect(page.getByRole('cell', { name: 'Data limite' })).toBeVisible();
        await expect(page.getByRole('cell', { name: 'Unidade Atual' })).toBeVisible();
    });

    test('deve exibir unidades na tabela', async ({ page }) => {
        // Verifica se há pelo menos uma linha de dados na tabela (excluindo o cabeçalho)
        const rows = page.getByRole('row').filter({ hasNot: page.getByRole('rowgroup', { name: 'Unidade Situação Data limite Unidade Atual' }) });
        await expect(rows).toHaveCount(5); // Assumindo que há 5 unidades no snapshot
    });
});