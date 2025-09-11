import {expect, Page} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {esperarElementoVisivel, loginComoAdmin} from './auxiliares-teste';
import {criarCompetencia} from './auxiliares-acoes';
import {irParaMapaCompetencias} from './auxiliares-navegacao';
import {TEXTOS} from './constantes-teste';

async function navegarParaMapa(page: Page) {
  await loginComoAdmin(page);
  await irParaMapaCompetencias(page, 4, 'SESEL');
  await page.waitForLoadState('networkidle');
  await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
}



test.describe('CDU-15: Manter mapa de competências', () => {
  test('deve exibir tela de edição de mapa com elementos corretos', async ({ page }) => {
    await navegarParaMapa(page);
    
    await esperarElementoVisivel(page, 'btn-abrir-criar-competencia');
    await expect(page.getByRole('button', { name: TEXTOS.DISPONIBILIZAR })).toBeVisible();
  });

  test('deve criar competência e alterar situação do subprocesso', async ({ page }) => {
    await navegarParaMapa(page);
    
    const competenciaDescricao = `Competência Teste ${Date.now()}`;
    await criarCompetencia(page, competenciaDescricao);
    
    const competenciaCard = page.locator('.competencia-card').filter({ hasText: competenciaDescricao });
    await expect(competenciaCard).toBeVisible();
    
    await competenciaCard.hover();
    await esperarElementoVisivel(page, 'btn-editar-competencia');
    await esperarElementoVisivel(page, 'btn-excluir-competencia');
  });

  test('deve editar competência existente', async ({ page }) => {
    await navegarParaMapa(page);
    
    const competenciaOriginal = `Competência Original ${Date.now()}`;
    await criarCompetencia(page, competenciaOriginal);
    
    const competenciaCard = page.locator('.competencia-card').filter({ hasText: competenciaOriginal });
    await competenciaCard.hover();
    await competenciaCard.getByTestId('btn-editar-competencia').click();
    
    const competenciaEditada = `Competência Editada ${Date.now()}`;
    await page.getByTestId('input-nova-competencia').fill(competenciaEditada);
    await page.getByTestId('btn-criar-competencia').click();
    
    await expect(page.getByText(competenciaEditada)).toBeVisible();
    await expect(page.getByText(competenciaOriginal)).not.toBeVisible();
  });

  test('deve excluir competência com confirmação', async ({ page }) => {
    await navegarParaMapa(page);
    
    const competenciaParaExcluir = `Competência para Excluir ${Date.now()}`;
    await criarCompetencia(page, competenciaParaExcluir);
    
    const competenciaCard = page.locator('.competencia-card').filter({ hasText: competenciaParaExcluir });
    await competenciaCard.hover();
    await competenciaCard.getByTestId('btn-excluir-competencia').click();
    
    await expect(page.getByRole('heading', { name: 'Exclusão de competência' })).toBeVisible();
    await expect(page.getByText(`Confirma a exclusão da competência "${competenciaParaExcluir}"?`)).toBeVisible();
    
    await page.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
    
    await expect(competenciaCard).not.toBeVisible();
  });
});