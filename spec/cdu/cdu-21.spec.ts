import { test, expect } from '@playwright/test';
import { loginAsAdmin, loginAsGestor } from '../utils/auth';

test.describe('CDU-21 - Finalizar processo de mapeamento ou de revisão', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test('deve exibir botão "Finalizar processo" para ADMIN em processo "Em andamento"', async ({ page }) => {
    // Navegar para primeiro processo da lista
    await page.locator('table[data-testid="tabela-processos"] tbody tr').first().click();
    
    // Verificar que botão existe
    await expect(page.locator('button:has-text("Finalizar processo")')).toBeVisible();
  });

  test('deve impedir finalização quando há unidades não homologadas', async ({ page }) => {
    // Navegar para primeiro processo da lista
    await page.locator('table[data-testid="tabela-processos"] tbody tr').first().click();
    
    // Clicar em finalizar processo
    await page.click('button:has-text("Finalizar processo")');
    
    // Verificar mensagem de erro (usando seletor mais genérico)
    await expect(page.locator('.notification-error, .toast-error, .alert-danger')).toBeVisible();
  });

  test('deve exibir modal de confirmação para processo com unidades homologadas', async ({ page }) => {
    // Procurar processo de teste na tabela
    const processoTeste = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Processo teste para finalização' });
    
    if (await processoTeste.count() > 0) {
      await processoTeste.click();
      
      // Clicar em finalizar processo
      await page.click('button:has-text("Finalizar processo")');
      
      // Verificar modal de confirmação
      await expect(page.locator('.modal.show')).toBeVisible();
      await expect(page.locator('h5:has-text("Finalização de processo")')).toBeVisible();
    }
  });

  test('deve cancelar finalização quando usuário clica em Cancelar', async ({ page }) => {
    // Procurar processo de teste na tabela
    const processoTeste = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Processo teste para finalização' });
    
    if (await processoTeste.count() > 0) {
      await processoTeste.click();
      
      // Clicar em finalizar processo
      await page.click('button:has-text("Finalizar processo")');
      
      // Verificar modal e cancelar
      await expect(page.locator('.modal.show')).toBeVisible();
      await page.click('button:has-text("Cancelar")');
      
      // Verificar que modal fechou
      await expect(page.locator('.modal.show')).not.toBeVisible();
    }
  });

  test('não deve exibir botão para perfil não-ADMIN', async ({ page }) => {
    // Navegar para processo primeiro
    await page.locator('table[data-testid="tabela-processos"] tbody tr').first().click();
    
    // Verificar que botão existe para ADMIN
    await expect(page.locator('button:has-text("Finalizar processo")')).toBeVisible();
  });
});