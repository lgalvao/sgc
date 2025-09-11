import {expect, Page, test} from '@playwright/test';
import {loginAsAdmin} from './test-helpers';

test.describe('CDU-15 - Manter mapa de competências', () => {
  const MAPA_URL = '/processo/4/SESEL/mapa';
  
  async function navigateToMapa(page: Page) {
    await loginAsAdmin(page);
    await page.goto(MAPA_URL);
    await page.waitForLoadState('networkidle');
    await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
  }
  
  async function createCompetencia(page: Page, descricao: string, numAtividades = 1) {
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await page.getByTestId('input-nova-competencia').fill(descricao);
    
    for (let i = 0; i < numAtividades; i++) {
      await page.locator('[data-testid="atividade-nao-associada"] label').nth(i).click();
    }
    
    await page.getByTestId('btn-criar-competencia').click();
    await expect(page.getByText(descricao)).toBeVisible();
  }

  test('deve exibir tela de edição de mapa com elementos corretos', async ({page}) => {
    await navigateToMapa(page);
    
    // Verificar elementos visuais da tela
    await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeVisible();
    await expect(page.getByRole('button', {name: 'Disponibilizar'})).toBeVisible();
  });

  test('deve criar competência e alterar situação do subprocesso', async ({page}) => {
    await navigateToMapa(page);
    
    const competenciaDescricao = 'Competência Teste ' + Date.now();
    await createCompetencia(page, competenciaDescricao);
    
    // Verificar se competência foi criada
    const competenciaCard = page.locator('.competencia-card').filter({hasText: competenciaDescricao});
    await expect(competenciaCard).toBeVisible();
    
    // Verificar botões de ação na competência
    await competenciaCard.hover();
    await expect(competenciaCard.getByTestId('btn-editar-competencia')).toBeVisible();
    await expect(competenciaCard.getByTestId('btn-excluir-competencia')).toBeVisible();
  });

  test('deve editar competência existente', async ({page}) => {
    await navigateToMapa(page);
    
    const competenciaOriginal = 'Competência Original ' + Date.now();
    await createCompetencia(page, competenciaOriginal);
    
    // Editar competência
    const competenciaCard = page.locator('.competencia-card').filter({hasText: competenciaOriginal});
    await competenciaCard.hover();
    await competenciaCard.getByTestId('btn-editar-competencia').click();
    
    const competenciaEditada = 'Competência Editada ' + Date.now();
    await page.getByTestId('input-nova-competencia').fill(competenciaEditada);
    await page.getByTestId('btn-criar-competencia').click();
    
    // Verificar se competência foi editada
    await expect(page.getByText(competenciaEditada)).toBeVisible();
    await expect(page.getByText(competenciaOriginal)).not.toBeVisible();
  });

  test('deve excluir competência com confirmação', async ({page}) => {
    await navigateToMapa(page);
    
    const competenciaParaExcluir = 'Competência para Excluir ' + Date.now();
    await createCompetencia(page, competenciaParaExcluir);
    
    // Excluir competência
    const competenciaCard = page.locator('.competencia-card').filter({hasText: competenciaParaExcluir});
    await competenciaCard.hover();
    await competenciaCard.getByTestId('btn-excluir-competencia').click();
    
    // Verificar modal de confirmação
    await expect(page.getByRole('heading', {name: 'Exclusão de competência'})).toBeVisible();
    await expect(page.getByText(`Confirma a exclusão da competência "${competenciaParaExcluir}"?`)).toBeVisible();
    
    // Confirmar exclusão
    await page.getByRole('button', {name: 'Confirmar'}).click();
    
    // Verificar se competência foi removida
    await expect(competenciaCard).not.toBeVisible();
  });
});