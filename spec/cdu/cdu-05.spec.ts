import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../utils/auth';

test.describe('CDU-05 - Iniciar processo de revisão', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test('Passo 1-3: deve navegar do Painel para processo Criado e exibir botão Iniciar', async ({ page }) => {
    // Passo 1: No Painel, clicar em processo Criado
    await expect(page.locator('[data-testid="titulo-processos"]')).toContainText('Processos');
    const processoCriado = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Processo teste revisão CDU-05' });
    await processoCriado.click();
    
    // Passo 2: Verificar tela Cadastro de processo com campos preenchidos
    await expect(page.locator('h2:has-text("Cadastro de processo")')).toBeVisible();
    await expect(page.locator('#descricao')).toHaveValue('Processo teste revisão CDU-05');
    
    // Passo 3: Verificar botão Iniciar processo
    await expect(page.locator('[data-testid="btn-iniciar-processo"]')).toBeVisible();
  });

  test('Passo 4: deve exibir modal de confirmação com mensagem exata para processo válido', async ({ page }) => {
    // Criar processo de revisão válido
    await page.click('[data-testid="btn-criar-processo"]');
    await page.fill('#descricao', 'Teste CDU-05');
    await page.selectOption('#tipo', 'Revisão');
    await page.fill('#dataLimite', '2025-12-31');
    await page.check('#chk-STIC'); // STIC tem mapa vigente
    
    // Passo 4: Clicar em Iniciar processo e verificar modal
    await page.click('[data-testid="btn-iniciar-processo"]');
    await expect(page.locator('.modal.show')).toBeVisible();
    await expect(page.locator('h5:has-text("Confirmação")')).toBeVisible();
    await expect(page.locator('text=Ao iniciar o processo, não será mais possível editá-lo ou removê-lo')).toBeVisible();
    await expect(page.locator('text=todas as unidades participantes serão notificadas por e-mail')).toBeVisible();
  });

  test('Passo 5: deve cancelar iniciação e permanecer na mesma tela', async ({ page }) => {
    // Criar processo válido
    await page.click('[data-testid="btn-criar-processo"]');
    await page.fill('#descricao', 'Teste CDU-05 Cancelar');
    await page.selectOption('#tipo', 'Revisão');
    await page.fill('#dataLimite', '2025-12-31');
    await page.check('#chk-STIC');
    
    // Clicar em Iniciar processo
    await page.click('[data-testid="btn-iniciar-processo"]');
    
    // Passo 5: Cancelar e verificar que permanece na mesma tela
    await expect(page.locator('.modal.show')).toBeVisible();
    await page.click('button:has-text("Cancelar")');
    await expect(page.locator('.modal.show')).not.toBeVisible();
    await expect(page.url()).toContain('/processo/cadastro');
  });

  test('Passo 6-13: deve iniciar processo com sucesso', async ({ page }) => {
    // Criar processo válido
    await page.click('[data-testid="btn-criar-processo"]');
    await page.fill('#descricao', 'Teste CDU-05 Sucesso');
    await page.selectOption('#tipo', 'Revisão');
    await page.fill('#dataLimite', '2025-12-31');
    await page.check('#chk-STIC');
    
    // Passo 6: Confirmar iniciação
    await page.click('[data-testid="btn-iniciar-processo"]');
    await expect(page.locator('.modal.show')).toBeVisible();
    await page.click('button:has-text("Confirmar")');
    
    // Verificar notificação de sucesso
    await expect(page.locator('.notification-success')).toContainText('Processo iniciado');
    
    // Verificar redirecionamento para painel
    await expect(page).toHaveURL('/painel');
  });

  test('Pré-condição: deve validar dados antes de mostrar modal', async ({ page }) => {
    // Ir para cadastro de processo
    await page.click('[data-testid="btn-criar-processo"]');
    
    // Tentar iniciar processo sem preencher dados
    await page.click('[data-testid="btn-iniciar-processo"]');
    
    // Verificar mensagem de erro
    await expect(page.locator('.notification-error')).toContainText('Dados incompletos');
    
    // Verificar que modal não apareceu
    await expect(page.locator('.modal.show')).not.toBeVisible();
  });


});