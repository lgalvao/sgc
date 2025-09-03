import {expect, test} from '@playwright/test';
import {loginAsAdmin} from '../utils/auth';

test.describe('CDU-06: Detalhar processo', () => {
  test('deve mostrar detalhes do processo para ADMIN', async ({ page }) => {
    await loginAsAdmin(page);

    // Clicar em processo 'Revisão de mapeamento STIC/COINF'
    const processoRow = page.locator('table tbody tr').filter({ hasText: 'STIC/COINF' }).first();
    await processoRow.click();

    // Deve mostrar tela de detalhes
    await expect(page).toHaveURL(/\/processo\/\d+/);

    // Verificar seções
    await expect(page.getByText('Situação:')).toBeVisible();
    await expect(page.getByText('Unidades participantes')).toBeVisible();

    // Botão Finalizar processo para ADMIN
    await expect(page.getByRole('button', { name: 'Finalizar processo' })).toBeVisible();
  });

  test('deve permitir clicar em unidade', async ({ page }) => {
    await loginAsAdmin(page);

    const processoRow = page.locator('table tbody tr').filter({ hasText: 'STIC/COINF' }).first();
    await processoRow.click();

    // Clicar em uma unidade participante
    const unidadeRow = page.locator('[data-testid="tree-table-row"]').filter({ hasText: 'STIC' }).first();
    await unidadeRow.click();

    // Deve navegar para subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+/);
  });
});