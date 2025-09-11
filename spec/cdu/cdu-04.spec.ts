import {vueTest as test} from '../../tests/vue-specific-setup';
import {esperarMensagemSucesso, esperarUrl, loginComoAdmin} from './auxiliares-teste';
import {navegarParaCriacaoProcesso} from './auxiliares-teste';
import {iniciarProcesso} from './auxiliares-acoes';
import {TEXTOS, URLS, ROTULOS} from './constantes-teste';

test.describe('CDU-04: Iniciar processo de mapeamento', () => {
  test.beforeEach(async ({ page }) => {
    await loginComoAdmin(page);
  });

  test('deve iniciar processo de mapeamento', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);
    
    await page.getByLabel(ROTULOS.DESCRICAO).fill('Processo de Mapeamento Teste');
    await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');
    await page.getByLabel(ROTULOS.DATA_LIMITE).fill('2025-12-31');
    
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.check();
    
    await iniciarProcesso(page);
    
    await esperarUrl(page, URLS.PAINEL);
    await esperarMensagemSucesso(page, TEXTOS.PROCESSO_INICIADO);
  });
});