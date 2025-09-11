import { test, expect } from '@playwright/test';
import { loginAsAdmin, loginAsGestor } from '../utils/auth';

test.describe('CDU-21 - Finalizar processo de mapeamento ou de revisão', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test('Passo 1-3: deve navegar do Painel para processo Em andamento e exibir botão Finalizar', async ({ page }) => {
    // Passo 1: No Painel, clicar em processo Em andamento
    await expect(page.locator('[data-testid="titulo-processos"]')).toContainText('Processos');
    const processoEmAndamento = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Em andamento' }).first();
    await processoEmAndamento.click();
    
    // Passo 2: Verificar tela Detalhes do processo
    await expect(page.locator('h2.display-6')).toBeVisible();
    await expect(page.locator('text=Unidades participantes')).toBeVisible();
    
    // Passo 3: Verificar botão Finalizar processo
    await expect(page.locator('button:has-text("Finalizar processo")')).toBeVisible();
  });

  test('Passo 4-5: deve impedir finalização quando há unidades não homologadas', async ({ page }) => {
    // Navegar para processo com unidades não homologadas
    const processoEmAndamento = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Em andamento' }).first();
    await processoEmAndamento.click();
    
    // Passo 3: Clicar em Finalizar processo
    await page.click('button:has-text("Finalizar processo")');
    
    // Passo 5: Verificar mensagem exata de erro
    await expect(page.locator('.notification')).toContainText('Não é possível encerrar o processo enquanto houver unidades com mapa de competência ainda não homologado');
  });

  test('Passo 6: deve exibir modal de confirmação com título e mensagem corretos', async ({ page }) => {
    // Navegar para processo de teste com unidades homologadas
    const processoTeste = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Processo teste para finalização' });
    
    if (await processoTeste.count() > 0) {
      await processoTeste.click();
      await page.click('button:has-text("Finalizar processo")');
      
      // Passo 6: Verificar modal com título e mensagem exatos
      await expect(page.locator('.modal.show')).toBeVisible();
      await expect(page.locator('h5:has-text("Finalização de processo")')).toBeVisible();
      await expect(page.locator('text=Confirma a finalização do processo')).toBeVisible();
      await expect(page.locator('text=Essa ação tornará vigentes os mapas de competências homologados')).toBeVisible();
      await expect(page.locator('button:has-text("Confirmar")')).toBeVisible();
      await expect(page.locator('button:has-text("Cancelar")')).toBeVisible();
    }
  });

  test('Passo 6.1: deve cancelar finalização e permanecer na mesma tela', async ({ page }) => {
    const processoTeste = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Processo teste para finalização' });
    
    if (await processoTeste.count() > 0) {
      await processoTeste.click();
      
      await page.click('button:has-text("Finalizar processo")');
      await expect(page.locator('.modal.show')).toBeVisible();
      
      // Passo 6.1: Cancelar e verificar que permanece na mesma tela
      await page.click('button:has-text("Cancelar")');
      await expect(page.locator('.modal.show')).not.toBeVisible();
      await expect(page.url()).toContain('/processo/99');
    }
  });

  test('Passo 7-10: deve finalizar processo com sucesso', async ({ page }) => {
    const processoTeste = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Processo teste para finalização' });
    
    if (await processoTeste.count() > 0) {
      await processoTeste.click();
      
      // Passo 7: Confirmar finalização
      await page.click('button:has-text("Finalizar processo")');
      await expect(page.locator('.modal.show')).toBeVisible();
      await page.click('button:has-text("Confirmar")');
      
      // Passo 10: Verificar mensagem "Processo finalizado" e redirecionamento
      await expect(page.locator('.notification-success')).toContainText('Processo finalizado');
      await expect(page).toHaveURL('/painel');
      
      // Passo 9: Verificar que processo mudou para "Finalizado"
      await expect(page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Processo teste para finalização' })).toContainText('Finalizado');
    }
  });

  test('Passo 9.1-9.2: deve enviar notificações por email', async ({ page }) => {
    const processoTeste = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Processo teste para finalização' });
    
    if (await processoTeste.count() > 0) {
      await processoTeste.click();
      
      await page.click('button:has-text("Finalizar processo")');
      await page.click('button:has-text("Confirmar")');
      
      // Verificar notificações de email foram enviadas
      await expect(page.locator('.notification-email').first()).toBeVisible();
    }
  });

  test('Pré-condição: não deve exibir botão para perfil não-ADMIN', async ({ page }) => {
    // Logout e login como GESTOR
    await page.goto('/login');
    await loginAsGestor(page);
    
    // Navegar para processo
    const processoEmAndamento = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Em andamento' }).first();
    await processoEmAndamento.click();
    
    // Verificar que botão não existe
    await expect(page.locator('button:has-text("Finalizar processo")')).not.toBeVisible();
  });

  // Teste adicional para Passo 8: deve definir mapas como vigentes
  test('Passo 8: deve definir mapas como vigentes após finalização', async ({ page }) => {
    // Finalizar processo de mapeamento
    await page.goto('/processo/101');
    await page.waitForSelector('button:has-text("Finalizar processo")');
    
    await page.click('button:has-text("Finalizar processo")');
    await page.waitForSelector('.modal.show');
    await page.click('button:has-text("Confirmar")');
    await page.waitForSelector('.notification-success', { timeout: 10000 });
    
    // Verificar que o processo foi finalizado (redirecionado para painel)
    await expect(page).toHaveURL('/painel');
    
    // Verificar que o processo aparece como finalizado na tabela
    const processoFinalizado = page.locator('table[data-testid="tabela-processos"] tbody tr').filter({ hasText: 'Processo teste mapeamento para finalização' });
    await expect(processoFinalizado).toContainText('Finalizado');
    
    // Verificar que a notificação de sucesso menciona mapas vigentes
    const notificacaoSucesso = await page.locator('.notification-success').textContent();
    expect(notificacaoSucesso).toContain('mapas de competências estão agora vigentes');
  });

  // Teste mais específico para emails
  test('Passo 9.1-9.2: deve enviar emails com conteúdo correto', async ({ page }) => {
    await page.goto('/processo/101');
    await page.waitForSelector('button:has-text("Finalizar processo")');
    
    await page.click('button:has-text("Finalizar processo")');
    await page.waitForSelector('.modal.show');
    await page.click('button:has-text("Confirmar")');
    await page.waitForSelector('.notification-success', { timeout: 10000 });
    
    // Verificar notificações de email
    const emailNotifications = await page.locator('.notification-email').count();
    expect(emailNotifications).toBeGreaterThan(0);
    
    // Verificar que pelo menos um email foi enviado
    const firstEmailNotification = await page.locator('.notification-email').first().textContent();
    expect(firstEmailNotification).toContain('E-mail enviado');
  });

  // Teste para diferentes tipos de processo
  test('deve funcionar para processos de mapeamento e revisão', async ({ page }) => {
    // Testar com processo de mapeamento
    await page.goto('/processo/101');
    await page.waitForSelector('button:has-text("Finalizar processo")');
    
    await page.click('button:has-text("Finalizar processo")');
    await page.waitForSelector('.modal.show');
    
    const modalTitle = await page.textContent('.modal-title');
    expect(modalTitle).toContain('Finalização de processo');
    
    await page.click('button:has-text("Cancelar")');
    await page.waitForSelector('.modal.show', { state: 'hidden' });
    
    // Testar com processo de revisão
    await page.goto('/processo/102');
    await page.waitForSelector('button:has-text("Finalizar processo")');
    
    await page.click('button:has-text("Finalizar processo")');
    await page.waitForSelector('.modal.show');
    
    const modalTitleRevisao = await page.textContent('.modal-title');
    expect(modalTitleRevisao).toContain('Finalização de processo');
    
    await page.click('button:has-text("Confirmar")');
    await page.waitForSelector('.notification-success', { timeout: 10000 });
    
    const alertText = await page.textContent('.notification-success');
    expect(alertText).toContain('Processo finalizado');
  });
});