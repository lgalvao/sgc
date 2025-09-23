import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {
    esperarMensagemErro,
    esperarMensagemSucesso,
    esperarUrl,
    loginComoAdmin,
    navegarParaCriacaoProcesso
} from './auxiliares-verificacoes';
import {cancelarModal, iniciarProcesso} from './auxiliares-acoes';
import {SELETORES, TEXTOS, URLS} from './constantes-teste';

test.describe('CDU-05: Iniciar processo de revisão', () => {
  test.beforeEach(async ({ page }) => {
    await loginComoAdmin(page);
  });

  test('deve navegar para processo Criado e exibir botão Iniciar', async ({ page }) => {
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

  test('deve exibir modal de confirmação para processo válido', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);
    
    await page.fill('#descricao', 'Teste CDU-05');
    await page.selectOption('#tipo', 'Revisão');
    await page.fill('#dataLimite', '2025-12-31');
    await page.check('#chk-STIC');
    
    await page.click(`[data-testid="${SELETORES.BTN_INICIAR_PROCESSO}"]`);
    
    await expect(page.locator('.modal.show')).toBeVisible();
    await expect(page.locator('h5:has-text("Iniciar processo")')).toBeVisible();
    await expect(page.locator('text=' + TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
    await expect(page.locator('text=' + TEXTOS.NOTIFICACAO_EMAIL)).toBeVisible();
  });

  test('deve cancelar iniciação e permanecer na mesma tela', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);
    
    await page.fill('#descricao', 'Teste CDU-05 Cancelar');
    await page.selectOption('#tipo', 'Revisão');
    await page.fill('#dataLimite', '2025-12-31');
    await page.check('#chk-STIC');
    
    await page.click(`[data-testid="${SELETORES.BTN_INICIAR_PROCESSO}"]`);
    await expect(page.locator('.modal.show')).toBeVisible();
    
    await cancelarModal(page);
    
    await expect(page.locator('.modal.show')).not.toBeVisible();
    await expect(page.url()).toContain('/processo/cadastro');
  });

  test('deve iniciar processo com sucesso', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);
    
    await page.fill('#descricao', 'Teste CDU-05 Sucesso');
    await page.selectOption('#tipo', 'Revisão');
    await page.fill('#dataLimite', '2025-12-31');
    await page.check('#chk-STIC');
    
    await iniciarProcesso(page);
    
    await esperarMensagemSucesso(page, TEXTOS.PROCESSO_INICIADO);
    await esperarUrl(page, URLS.PAINEL);
  });

  test('deve validar dados antes de mostrar modal', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);
    
    await page.click(`[data-testid="${SELETORES.BTN_INICIAR_PROCESSO}"]`);
    
    await esperarMensagemErro(page, TEXTOS.DADOS_INCOMPLETOS);
    await expect(page.locator('.modal.show')).not.toBeVisible();
  });
});