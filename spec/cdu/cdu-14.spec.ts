import {expect, Page, test} from '@playwright/test';
import {loginAsAdmin, loginAsGestor, MODAL_SELECTOR} from './test-helpers';

test.describe('CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos', () => {
  const ANALYSIS_URL = '/processo/2/SESEL/vis-cadastro';
  
  async function navigateToAnalysis(page: Page, loginFn: (page: Page) => Promise<void>) {
    await loginFn(page);
    await page.goto(ANALYSIS_URL);
    await page.waitForLoadState('networkidle');
    await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
  }
  
  async function expectSuccessAndRedirect(page: Page, message: string) {
    const notification = page.locator('.notification.notification-success');
    await expect(notification).toBeVisible();
    await expect(notification).toContainText(message);
    await expect(page).toHaveURL(/\/painel/);
  }

  test('deve exibir botões corretos por perfil', async ({page}) => {
    await navigateToAnalysis(page, loginAsGestor);
    await expect(page.getByText('Histórico de análise')).toBeVisible();
    await expect(page.getByText('Devolver para ajustes')).toBeVisible();
    await expect(page.getByText('Registrar aceite')).toBeVisible();
    
    await navigateToAnalysis(page, loginAsAdmin);
    await expect(page.getByText('Homologar')).toBeVisible();
  });

  test('deve permitir devolução e aceite', async ({page}) => {
    await navigateToAnalysis(page, loginAsGestor);
    
    // Testar devolução
    await page.getByText('Devolver para ajustes').click();
    await expect(page.getByRole('heading', {name: 'Devolução da revisão do cadastro'})).toBeVisible();
    await page.getByLabel('Observação (opcional)').fill('Ajustes necessários.');
    await page.getByRole('button', {name: 'Confirmar'}).click();
    await expectSuccessAndRedirect(page, 'devolvido para ajustes');
    
    // Testar aceite
    await navigateToAnalysis(page, loginAsGestor);
    await page.getByText('Registrar aceite').click();
    await expect(page.getByRole('heading', {name: 'Aceite da revisão do cadastro'})).toBeVisible();
    await page.getByLabel('Observação (opcional)').fill('Aceite após análise.');
    await page.getByRole('button', {name: 'Confirmar'}).click();
    await expectSuccessAndRedirect(page, 'Aceite registrado');
  });

  test('deve exibir histórico de análise', async ({page}) => {
    await navigateToAnalysis(page, loginAsGestor);
    await page.getByText('Histórico de análise').click();
    
    const modal = page.locator(MODAL_SELECTOR);
    await expect(modal).toBeVisible();
    await expect(page.getByRole('heading', {name: 'Histórico de Análise'})).toBeVisible();
    
    await page.getByRole('button', {name: 'Fechar'}).click();
    await expect(modal).not.toBeVisible();
  });
});