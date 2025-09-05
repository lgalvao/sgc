import {expect, test} from '@playwright/test';
import {loginAsAdmin} from '~/utils/auth';

test.describe('CDU-04: Iniciar processo de mapeamento', () => {
   test('deve iniciar processo de mapeamento', async ({ page }) => {
     await loginAsAdmin(page); // ADMIN
 
     // Clicar em Criar processo
     await page.getByText('Criar processo').click();
 
     // Deve navegar para tela de cadastro
     await expect(page).toHaveURL(/\/processo\/cadastro$/);
 
     // Preencher formulário
     await page.getByLabel('Descrição').fill('Processo de Mapeamento Teste');
     await page.getByLabel('Tipo').selectOption('Mapeamento');
     await page.getByLabel('Data limite').fill('2025-12-31');
 
     // Selecionar uma unidade (primeiro checkbox disponível)
     const firstCheckbox = page.locator('input[type="checkbox"]').first();
     await firstCheckbox.check();
 
     // Clicar em Iniciar processo
     await page.getByText('Iniciar processo').click();
 
     // Deve voltar ao painel
     await expect(page).toHaveURL(/\/painel$/);
 
     // Deve mostrar mensagem de sucesso
     await expect(page.getByText('Processo iniciado')).toBeVisible();
   });
});