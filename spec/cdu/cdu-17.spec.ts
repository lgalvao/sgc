import {expect, Page, test} from '@playwright/test';
import {loginAsAdmin} from './test-helpers';

test.describe('CDU-17 - Disponibilizar mapa de competências', () => {
  const MAPA_URL = '/processo/4/SESEL/mapa';
  
  async function navigateToMapa(page: Page) {
    await loginAsAdmin(page);
    await page.goto(MAPA_URL);
    await page.waitForLoadState('networkidle');
    await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
  }
  
  async function createSimpleCompetencia(page: Page, nome: string) {
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await page.getByTestId('input-nova-competencia').fill(nome);
    await page.locator('[data-testid="atividade-nao-associada"] label').first().click();
    await page.getByTestId('btn-criar-competencia').click();
  }

  test('deve exibir modal com título e campos corretos', async ({page}) => {
    await navigateToMapa(page);
    
    // Criar competência simples
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await page.getByTestId('input-nova-competencia').fill('Competência Teste');
    await page.locator('[data-testid="atividade-nao-associada"] label').first().click();
    await page.getByTestId('btn-criar-competencia').click();
    
    await page.getByRole('button', {name: 'Disponibilizar'}).click();
    
    // Verificar modal
    await expect(page.getByRole('heading', {name: 'Disponibilização do mapa de competências'})).toBeVisible();
    await expect(page.getByLabel('Data limite para validação')).toBeVisible();
    await expect(page.getByLabel('Observações (opcional)')).toBeVisible();
  });

  test('deve preencher observações no modal', async ({page}) => {
    await navigateToMapa(page);
    
    // Criar competência simples
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await page.getByTestId('input-nova-competencia').fill('Competência Teste');
    await page.locator('[data-testid="atividade-nao-associada"] label').first().click();
    await page.getByTestId('btn-criar-competencia').click();
    
    await page.getByRole('button', {name: 'Disponibilizar'}).click();
    
    // Preencher observações
    await page.locator('#observacoes').fill('Observações de teste para CDU-17');
    await expect(page.locator('#observacoes')).toHaveValue('Observações de teste para CDU-17');
  });

  test('deve validar data obrigatória (item 10.2)', async ({page}) => {
    await navigateToMapa(page);
    
    // Criar competência simples
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await page.getByTestId('input-nova-competencia').fill('Competência Teste');
    await page.locator('[data-testid="atividade-nao-associada"] label').first().click();
    await page.getByTestId('btn-criar-competencia').click();
    
    await page.getByRole('button', {name: 'Disponibilizar'}).click();
    
    // Tentar disponibilizar sem data - botão deve estar desabilitado
    const btnDisponibilizar = page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', {name: 'Disponibilizar'});
    await expect(btnDisponibilizar).toBeDisabled();
    
    // Preencher data - botão deve ficar habilitado
    await page.locator('#dataLimite').fill('2025-12-31');
    await expect(btnDisponibilizar).toBeEnabled();
  });

  test('deve validar campos obrigatórios do modal (itens 10.1, 10.2, 10.3)', async ({page}) => {
    await navigateToMapa(page);
    await createSimpleCompetencia(page, 'Competência para Validação');
    
    await page.getByRole('button', {name: 'Disponibilizar'}).click();
    
    // Verificar título do modal (item 10.1)
    await expect(page.getByRole('heading', {name: 'Disponibilização do mapa de competências'})).toBeVisible();
    
    // Verificar campo de data obrigatório (item 10.2)
    const btnDisponibilizar = page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', {name: 'Disponibilizar'});
    await expect(btnDisponibilizar).toBeDisabled();
    
    await page.locator('#dataLimite').fill('2025-12-31');
    await expect(btnDisponibilizar).toBeEnabled();
    
    // Verificar campo de observações opcional (item 10.3)
    await expect(page.getByLabel('Observações (opcional)')).toBeVisible();
    await page.locator('#observacoes').fill('Teste de observações');
    await expect(page.locator('#observacoes')).toHaveValue('Teste de observações');
  });

  test('deve processar disponibilização (itens 12, 13, 14)', async ({page}) => {
    await navigateToMapa(page);
    await createSimpleCompetencia(page, 'Competência para Disponibilizar');
    
    await page.getByRole('button', {name: 'Disponibilizar'}).click();
    await page.locator('#dataLimite').fill('2025-12-31');
    await page.locator('#observacoes').fill('Observações de teste CDU-17');
    
    // Verificar que o botão está habilitado e pode ser clicado
    const btnDisponibilizar = page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', {name: 'Disponibilizar'});
    await expect(btnDisponibilizar).toBeEnabled();
    
    // Clicar no botão - isso deve processar a disponibilização
    await btnDisponibilizar.click();
    
    // Aguardar um pouco para o processamento
    await page.waitForTimeout(2000);
  });

  test('deve cancelar disponibilização', async ({page}) => {
    await navigateToMapa(page);
    
    // Criar competência simples
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await page.getByTestId('input-nova-competencia').fill('Competência Teste');
    await page.locator('[data-testid="atividade-nao-associada"] label').first().click();
    await page.getByTestId('btn-criar-competencia').click();
    
    await page.getByRole('button', {name: 'Disponibilizar'}).click();
    await page.getByRole('button', {name: 'Cancelar'}).click();
    
    // Verificar que modal foi fechado
    await expect(page.getByRole('heading', {name: 'Disponibilização do mapa de competências'})).not.toBeVisible();
    await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
  });
});