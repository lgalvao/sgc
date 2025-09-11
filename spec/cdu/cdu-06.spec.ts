import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {loginComoAdmin, verificarUrl} from './auxiliares-teste';
import {navegarParaDetalhesProcesso} from './auxiliares-teste';
import {TEXTOS} from './constantes-teste';

test.describe('CDU-06: Detalhar processo', () => {
  test.beforeEach(async ({ page }) => {
    await loginComoAdmin(page);
  });

  test('deve mostrar detalhes do processo para ADMIN', async ({ page }) => {
    await navegarParaDetalhesProcesso(page, 'STIC/COINF');
    
    await expect(page.getByText(TEXTOS.SITUACAO_LABEL)).toBeVisible();
    await expect(page.getByText(TEXTOS.UNIDADES_PARTICIPANTES)).toBeVisible();
    await expect(page.getByRole('button', { name: TEXTOS.FINALIZAR_PROCESSO })).toBeVisible();
  });

  test('deve permitir clicar em unidade', async ({ page }) => {
    await navegarParaDetalhesProcesso(page, 'STIC/COINF');
    
    const unidadeRow = page.locator('[data-testid^="tree-table-row-"]')
      .filter({ hasText: 'STIC' }).first();
    await unidadeRow.click();
    
    await verificarUrl(page, '/processo/\\d+/[^/]+');
  });
});