import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginAsAdmin} from '~/utils/auth';
import {navigateToProcessCreation} from './test-helpers';

test.describe('CDU-04: Iniciar processo de mapeamento', () => {
  test.beforeEach(async ({page}) => {
    await loginAsAdmin(page);
  });

  test('deve iniciar processo de mapeamento', async ({ page }) => {
    await navigateToProcessCreation(page);
    
    await page.getByLabel('Descrição').fill('Processo de Mapeamento Teste');
    await page.getByLabel('Tipo').selectOption('Mapeamento');
    await page.getByLabel('Data limite').fill('2025-12-31');
    
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.check();
    
    await page.getByText('Iniciar processo').click();
    await page.waitForSelector('.modal.show');
    await page.getByText('Confirmar').click();
    await page.waitForLoadState('networkidle');
    
    await expect(page).toHaveURL(/\/painel$/);
    await expect(page.getByText('Processo iniciado')).toBeVisible();
  });
});