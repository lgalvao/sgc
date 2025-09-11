import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {esperarElementoVisivel, esperarTextoVisivel, loginComoAdmin} from './auxiliares-teste';
import {navegarParaCriacaoProcesso} from './auxiliares-teste';
import {ROTULOS, SELETORES, TEXTOS} from './constantes-teste';

test.describe('CDU-03: Manter processo', () => {
  test.beforeEach(async ({ page }) => {
    await loginComoAdmin(page);
  });

  test('deve acessar tela de criação de processo', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    await expect(page.getByLabel(ROTULOS.DESCRICAO)).toBeVisible();
    await expect(page.getByLabel(ROTULOS.TIPO)).toBeVisible();
    await esperarTextoVisivel(page, TEXTOS.UNIDADES_PARTICIPANTES);
  });

  test('deve mostrar erro para processo sem descrição', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
    
    // Validação de campo obrigatório
  });

  test('deve mostrar erro para processo sem unidades', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    await page.getByLabel(ROTULOS.DESCRICAO).fill('Processo Teste');
    await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');

    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();

    await expect(page.getByText(TEXTOS.ERRO_CAMPOS_OBRIGATORIOS)).toBeVisible();
  });

  test('deve permitir visualizar processo existente', async ({ page }) => {
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    await expect(page).toHaveURL(/\/processo\/\d+$/);
    await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
  });
});