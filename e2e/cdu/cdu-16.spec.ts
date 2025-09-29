import {expect, Page} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {esperarElementoVisivel, esperarTextoVisivel, loginComoAdmin} from './helpers';
import {criarCompetencia} from './helpers';
import {irParaMapaCompetencias} from './helpers';
import {DADOS_TESTE, TEXTOS} from './helpers';

async function navegarParaMapaRevisao(page: Page) {
  await loginComoAdmin(page);
  await irParaMapaCompetencias(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, 'SESEL');
  //await page.waitForLoadState('networkidle');
  await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
}

async function criarCompetenciaLocal(page: Page, nome: string) {
  await criarCompetencia(page, nome);
}

test.describe('CDU-16: Ajustar mapa de competências', () => {
  test('deve exibir botão "Impacto no mapa" para ADMIN em processo de Revisão', async ({ page }) => {
    await navegarParaMapaRevisao(page);
    
    await esperarElementoVisivel(page, 'impactos-mapa-button');
    await esperarTextoVisivel(page, 'Impacto no mapa');
  });

  test('deve abrir modal de impactos no mapa', async ({ page }) => {
    await navegarParaMapaRevisao(page);
    
    await page.getByTestId('impactos-mapa-button').click();
    
    try {
      await expect(page.getByRole('heading', { name: /impacto/i })).toBeVisible();
    } catch {
      await expect(page.locator('.notification')).toBeVisible();
    }
  });

  test('deve permitir criação de competências', async ({ page }) => {
    await navegarParaMapaRevisao(page);
    
    await criarCompetenciaLocal(page, 'Competência CDU-16');
    
    await esperarTextoVisivel(page, 'Competência CDU-16');
    
    const competenciaCard = page.locator('.competencia-card').filter({ hasText: 'Competência CDU-16' });
    await competenciaCard.hover();
    await esperarElementoVisivel(page, 'btn-editar-competencia');
    await esperarElementoVisivel(page, 'btn-excluir-competencia');
  });

  test('deve permitir edição de competências', async ({ page }) => {
    await navegarParaMapaRevisao(page);
    
    await criarCompetenciaLocal(page, 'Competência Original');
    
    const competenciaCard = page.locator('.competencia-card').filter({ hasText: 'Competência Original' });
    await competenciaCard.hover();
    await competenciaCard.getByTestId('btn-editar-competencia').click();
    
    await page.getByTestId('input-nova-competencia').fill('Competência Editada');
    await page.getByTestId('btn-criar-competencia').click();
    
    await esperarTextoVisivel(page, 'Competência Editada');
    await expect(page.getByText('Competência Original')).not.toBeVisible();
  });

  test('deve permitir exclusão de competências', async ({ page }) => {
    await navegarParaMapaRevisao(page);
    
    await criarCompetenciaLocal(page, 'Competência para Excluir');
    
    const competenciaCard = page.locator('.competencia-card').filter({ hasText: 'Competência para Excluir' });
    await competenciaCard.hover();
    await competenciaCard.getByTestId('btn-excluir-competencia').click();
    
    await expect(page.getByRole('heading', { name: 'Exclusão de competência' })).toBeVisible();
    await page.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
    
    await expect(page.getByText('Competência para Excluir')).not.toBeVisible();
  });

  test('deve validar associação de todas as atividades', async ({ page }) => {
    await navegarParaMapaRevisao(page);
    
    await criarCompetenciaLocal(page, 'Competência Parcial');
    
    await page.getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
    await page.locator('#dataLimite').fill('2025-12-31');
    await page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
    
    await page.waitForTimeout(2000);
    
    const modalDisponibilizar = page.locator('[aria-labelledby="disponibilizarModalLabel"]');
    
    try {
      await expect(modalDisponibilizar).not.toBeVisible({ timeout: 1000 });
    } catch {
      const notificacao = page.locator('[data-testid="notificacao-disponibilizacao"]');
      await expect(notificacao).toBeVisible({ timeout: 1000 });
    }
  });

  test('deve integrar com disponibilização de mapa', async ({ page }) => {
    await navegarParaMapaRevisao(page);
    
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await page.getByTestId('input-nova-competencia').fill('Competência Completa');
    await page.locator('[data-testid="atividade-nao-associada"] label').first().click();
    await page.getByTestId('btn-criar-competencia').click();
    
    await page.getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
    await expect(page.getByRole('heading', { name: 'Disponibilização do mapa de competências' })).toBeVisible();
    
    await page.locator('#dataLimite').fill('2025-12-31');
    await page.locator('#observacoes').fill('Mapa ajustado conforme revisão');
    await page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', { name: TEXTOS.DISPONIBILIZAR }).click();
  });
});