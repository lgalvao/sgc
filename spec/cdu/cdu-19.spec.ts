import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {
    esperarElementoInvisivel,
    esperarElementoVisivel,
    esperarTextoVisivel,
    loginComoChefeSedia
} from './auxiliares-teste';
import {validarMapa} from './auxiliares-acoes';
import {irParaVisualizacaoMapa} from './auxiliares-navegacao';

test.describe('CDU-19: Validar mapa de competências', () => {
  test.beforeEach(async ({ page }) => {
    await loginComoChefeSedia(page);
  });

  test('deve exibir botões Apresentar sugestões e Validar para CHEFE', async ({ page }) => {
    await irParaVisualizacaoMapa(page, 5, 'SEDIA');
    await page.waitForLoadState('networkidle');

    await esperarTextoVisivel(page, 'Mapa de competências técnicas');
    
    await esperarElementoVisivel(page, 'apresentar-sugestoes-btn');
    await esperarElementoVisivel(page, 'validar-btn');
  });

  test('deve exibir botão Histórico de análise quando houver análises', async ({ page }) => {
    await irParaVisualizacaoMapa(page, 5, 'SEDIA');
    await page.waitForLoadState('networkidle');

    const historicoBtn = page.getByTestId('historico-analise-btn');
    const isVisible = await historicoBtn.isVisible();
    
    if (isVisible) {
      await historicoBtn.click();
      await esperarElementoVisivel(page, 'modal-historico');
      await esperarElementoVisivel(page, 'tabela-historico');
      
      await page.getByTestId('modal-historico-fechar').click();
    }
  });

  test('deve permitir apresentar sugestões', async ({ page }) => {
    await irParaVisualizacaoMapa(page, 5, 'SEDIA');
    await page.waitForLoadState('networkidle');

    await page.getByTestId('apresentar-sugestoes-btn').click();
    
    await esperarElementoVisivel(page, 'modal-apresentar-sugestoes');
    await expect(page.getByTestId('modal-apresentar-sugestoes-title')).toHaveText('Apresentar Sugestões');
    
    await page.getByTestId('sugestoes-textarea').fill('Sugestão de teste para o mapa');
    
    await page.getByTestId('modal-apresentar-sugestoes-confirmar').click();
    
    await expect(page).toHaveURL(/\/processo\/5\/SEDIA$/);
  });

  test('deve permitir validar mapa', async ({ page }) => {
    await irParaVisualizacaoMapa(page, 5, 'SEDIA');
    await page.waitForLoadState('networkidle');

    await validarMapa(page);
    
    await expect(page).toHaveURL(/\/processo\/5\/SEDIA$/);
  });

  test('deve cancelar apresentação de sugestões', async ({ page }) => {
    await irParaVisualizacaoMapa(page, 5, 'SEDIA');
    await page.waitForLoadState('networkidle');

    await page.getByTestId('apresentar-sugestoes-btn').click();
    await esperarElementoVisivel(page, 'modal-apresentar-sugestoes');
    
    await page.getByTestId('modal-apresentar-sugestoes-cancelar').click();
    
    await esperarElementoInvisivel(page, 'modal-apresentar-sugestoes');
  });

  test('deve cancelar validação de mapa', async ({ page }) => {
    await irParaVisualizacaoMapa(page, 5, 'SEDIA');
    await page.waitForLoadState('networkidle');

    await page.getByTestId('validar-btn').click();
    await esperarElementoVisivel(page, 'modal-validar');
    
    await page.getByTestId('modal-validar-cancelar').click();
    
    await esperarElementoInvisivel(page, 'modal-validar');
  });
});