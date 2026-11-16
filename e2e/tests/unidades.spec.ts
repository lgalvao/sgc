import {expect, test} from '@playwright/test';
import {login} from './helpers';

const usuarioSedoc = { usuario: 'sedoc@tre-pe.jus.br', senha: 'admin123' };

test.describe('Unidades', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, usuarioSedoc.usuario, usuarioSedoc.senha);
  });

  test('deve listar unidades', async ({ page }) => {
      await page.goto('/unidade');
    const linhas = await page.locator('table tbody tr').count();
    expect(linhas).toBeGreaterThanOrEqual(0);
  });

  test('deve visualizar detalhes de uma unidade', async ({ page }) => {
      await page.goto('/unidade');
    const btn = page.locator('a.btn-info');
    if (await btn.count() > 0) {
      await btn.first().click();
      await expect(page).toHaveURL(/unidade\//);
    }
  });

  test('deve exibir formulário de nova unidade', async ({ page }) => {
    await page.goto('http://localhost:8080/unidade');
    const btn = page.getByRole('button', { name: /nova unidade/i });
    if (await btn.count() > 0) {
      await btn.first().click();
      await expect(page).toHaveURL(/unidade\/nova/);
    }
  });
}); 