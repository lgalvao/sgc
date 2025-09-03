import {expect, test} from '@playwright/test';
import {loginAsChefe} from '../utils/auth';

test.describe('CDU-07: Detalhar subprocesso', () => {
   test('deve mostrar detalhes do subprocesso para CHEFE', async ({ page }) => {
     // Login como CHEFE
     await loginAsChefe(page);
 
     // Clicar em processo
     const processoRow = page.locator('table tbody tr').first();
     await processoRow.click();
 
     // Deve mostrar subprocesso
     await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+/);
 
     // Verificar elementos básicos do subprocesso
     await expect(page.getByTestId('subprocesso-header')).toBeVisible();
     await expect(page.getByTestId('processo-info')).toBeVisible();
   });
});