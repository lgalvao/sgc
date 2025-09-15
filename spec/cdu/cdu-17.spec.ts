import {expect, Page} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginComoAdmin} from './auxiliares-teste';
import {criarCompetencia} from './auxiliares-acoes';
import {irParaMapaCompetencias} from './auxiliares-navegacao';
import {TEXTOS} from './constantes-teste';

async function navegarParaMapa(page: Page) {
  await loginComoAdmin(page);
  await irParaMapaCompetencias(page, 4, 'SESEL');
  await page.waitForLoadState('networkidle');
  await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
}

async function criarCompetenciaSimples(page: Page, nome: string) {
  await criarCompetencia(page, nome);
}

test.describe('CDU-17: Disponibilizar mapa de competências', () => {
  test('deve exibir modal com título e campos corretos', async ({ page }) => {
    await navegarParaMapa(page);
    
    await criarCompetenciaSimples(page, 'Competência Teste');
    
    await page.getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
    
    await expect(page.getByRole('heading', { name: 'Disponibilização do mapa de competências' })).toBeVisible();
    await expect(page.getByLabel('Data limite para validação')).toBeVisible();
        await expect(page.getByLabel('Observações')).toBeVisible();
  });

  test('deve preencher observações no modal', async ({ page }) => {
    await navegarParaMapa(page);
    
    await criarCompetenciaSimples(page, 'Competência Teste');
    
    await page.getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
    
    await page.locator('#observacoes').fill('Observações de teste para CDU-17');
    await expect(page.locator('#observacoes')).toHaveValue('Observações de teste para CDU-17');
  });

  test('deve validar data obrigatória', async ({ page }) => {
    await navegarParaMapa(page);
    
    await criarCompetenciaSimples(page, 'Competência Teste');
    
    await page.getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
    
    const btnDisponibilizar = page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', { name: TEXTOS.DISPONIBILIZAR });
    await expect(btnDisponibilizar).toBeDisabled();
    
    await page.locator('#dataLimite').fill('2025-12-31');
    await expect(btnDisponibilizar).toBeEnabled();
  });

  test('deve validar campos obrigatórios do modal', async ({ page }) => {
    await navegarParaMapa(page);
    await criarCompetenciaSimples(page, 'Competência para Validação');
    
    await page.getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
    
    await expect(page.getByRole('heading', { name: 'Disponibilização do mapa de competências' })).toBeVisible();
    
    const btnDisponibilizar = page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', { name: TEXTOS.DISPONIBILIZAR });
    await expect(btnDisponibilizar).toBeDisabled();
    
    await page.locator('#dataLimite').fill('2025-12-31');
    await expect(btnDisponibilizar).toBeEnabled();
    
        await expect(page.getByLabel('Observações')).toBeVisible();
    await page.locator('#observacoes').fill('Teste de observações');
    await expect(page.locator('#observacoes')).toHaveValue('Teste de observações');
  });

  test('deve processar disponibilização', async ({ page }) => {
    await navegarParaMapa(page);
    await criarCompetenciaSimples(page, 'Competência para Disponibilizar');
    
    await page.getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
    await page.locator('#dataLimite').fill('2025-12-31');
    await page.locator('#observacoes').fill('Observações de teste CDU-17');
    
    const btnDisponibilizar = page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', { name: TEXTOS.DISPONIBILIZAR });
    await expect(btnDisponibilizar).toBeEnabled();
    
    await btnDisponibilizar.click();
    
    await page.waitForTimeout(2000);
  });

  test('deve cancelar disponibilização', async ({ page }) => {
    await navegarParaMapa(page);
    
    await criarCompetenciaSimples(page, 'Competência Teste');
    
    await page.getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
    await page.getByRole('button', { name: TEXTOS.CANCELAR }).click();
    
    await expect(page.getByRole('heading', { name: 'Disponibilização do mapa de competências' })).not.toBeVisible();
    await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
  });
});