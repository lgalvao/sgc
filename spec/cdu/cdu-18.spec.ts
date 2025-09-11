import {expect, test} from '@playwright/test';
import {expectTextVisible, expectVisible, loginAsAdmin, loginAsChefe, loginAsServidor} from './test-helpers';
import {SELECTORS, TEXTS} from './test-constants';

test.describe('CDU-18 - Visualizar mapa de competências', () => {
  test('ADMIN/GESTOR: navegar pelo processo até visualização do mapa', async ({ page }) => {
    await loginAsAdmin(page);
    
    // Clicar no processo de revisão (processo 2 tem mapa para STIC)
    const processoRevisao = page.locator('table tbody tr').filter({ hasText: 'Revisão de mapeamento STIC/COINF' }).first();
    await processoRevisao.click();
    
    // Verificar que está na tela de processo
    await expect(page).toHaveURL(/\/processo\/\d+/);
    
    // Clicar em uma unidade operacional (STIC)
    const unidadeStic = page.locator('[data-testid^="tree-table-row-"]').filter({ hasText: 'STIC' }).first();
    await unidadeStic.click();
    
    // Verificar que está na tela de subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/STIC/);
    
    // Aguardar o card aparecer e clicar
    await page.waitForSelector('[data-testid="mapa-card"]');
    await page.getByTestId(SELECTORS.MAPA_CARD).click();
    
    // Verificar que está na tela de visualização do mapa
    await expect(page).toHaveURL(/\/processo\/\d+\/STIC\/vis-mapa/);
    await expectTextVisible(page, TEXTS.MAPA_COMPETENCIAS_TECNICAS);
  });

  test('CHEFE/SERVIDOR: navegar direto para subprocesso e visualizar mapa', async ({ page }) => {
    await loginAsChefe(page);
    
    // Clicar no processo de revisão (processo 2 tem mapa para STIC)
    const processoRevisao = page.locator('table tbody tr').filter({ hasText: 'Revisão de mapeamento STIC/COINF' }).first();
    await processoRevisao.click();
    
    // Verificar que vai direto para o subprocesso da unidade do usuário
    await expect(page).toHaveURL(/\/processo\/\d+\/STIC/);
    
    // Aguardar o card aparecer e clicar
    await page.waitForSelector('[data-testid="mapa-card"]');
    await page.getByTestId(SELECTORS.MAPA_CARD).click();
    
    // Verificar que está na tela de visualização do mapa
    await expect(page).toHaveURL(/\/processo\/\d+\/STIC\/vis-mapa/);
    await expectTextVisible(page, TEXTS.MAPA_COMPETENCIAS_TECNICAS);
  });

  test('Verificar elementos obrigatórios da visualização do mapa', async ({ page }) => {
    await loginAsChefe(page);
    
    // Navegar para visualização do mapa
    const processoRevisao = page.locator('table tbody tr').filter({ hasText: 'Revisão de mapeamento STIC/COINF' }).first();
    await processoRevisao.click();
    await page.waitForSelector('[data-testid="mapa-card"]');
    await page.getByTestId(SELECTORS.MAPA_CARD).click();
    
    // Verificar título "Mapa de competências técnicas"
    await expectTextVisible(page, TEXTS.MAPA_COMPETENCIAS_TECNICAS);
    
    // Verificar identificação da unidade (sigla e nome)
    await expectVisible(page, SELECTORS.UNIDADE_INFO);
    await expect(page.getByTestId(SELECTORS.UNIDADE_INFO)).toContainText('STIC');
    
    // Verificar que existem competências
    const competencias = page.getByTestId(SELECTORS.COMPETENCIA_BLOCK);
    await expect(competencias.first()).toBeVisible();
    
    // Verificar estrutura de uma competência
    const primeiraCompetencia = competencias.first();
    
    // Verificar descrição da competência como título
    const descricaoCompetencia = primeiraCompetencia.getByTestId('competencia-descricao');
    await expect(descricaoCompetencia).toBeVisible();
    
    // Verificar atividades associadas
    const atividades = page.getByTestId(SELECTORS.ATIVIDADE_ITEM);
    if (await atividades.count() > 0) {
      await expect(atividades.first()).toBeVisible();
      
      // Verificar conhecimentos das atividades
      const conhecimentos = page.getByTestId(SELECTORS.CONHECIMENTO_ITEM);
      if (await conhecimentos.count() > 0) {
        await expect(conhecimentos.first()).toBeVisible();
      }
    }
  });

  test('SERVIDOR: verificar que não tem botões de ação', async ({ page }) => {
    await loginAsServidor(page);
    
    // Navegar para visualização do mapa
    const processoRevisao = page.locator('table tbody tr').filter({ hasText: 'Revisão de mapeamento STIC/COINF' }).first();
    await processoRevisao.click();
    await page.waitForSelector('[data-testid="mapa-card"]');
    await page.getByTestId(SELECTORS.MAPA_CARD).click();
    
    // Verificar que não existem botões de validação ou análise
    await expect(page.getByTestId('validar-btn')).not.toBeVisible();
    await expect(page.getByTestId('apresentar-sugestoes-btn')).not.toBeVisible();
    await expect(page.getByTestId('registrar-aceite-btn')).not.toBeVisible();
  });
});