import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginAsAdmin} from '~/utils/auth';
import {navigateToProcessDetails} from './test-helpers';

test.describe('CDU-06: Detalhar processo', () => {
  test.beforeEach(async ({page}) => {
    await loginAsAdmin(page);
  });

  test('deve mostrar detalhes do processo para ADMIN', async ({page}) => {
    await navigateToProcessDetails(page, 'STIC/COINF');
    await expect(page.getByText('Situação:')).toBeVisible();
    await expect(page.getByText('Unidades participantes')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Finalizar processo' })).toBeVisible();
  });

  test('deve permitir clicar em unidade', async ({ page }) => {
    await navigateToProcessDetails(page, 'STIC/COINF');
    const unidadeRow = page.locator('[data-testid^="tree-table-row-"]').filter({hasText: 'STIC'}).first();
    await unidadeRow.click();
    await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+/);
  });
});