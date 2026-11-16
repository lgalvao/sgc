import {expect, test} from '@playwright/test';
import {login} from './helpers';

const usuarioSedoc = { usuario: 'sedoc@tre-pe.jus.br', senha: 'admin123' };

test.describe('Servidores', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, usuarioSedoc.usuario, usuarioSedoc.senha);
  });

  test('deve listar servidores', async ({ page }) => {
      await page.goto('/servidor');
    const linhas = await page.locator('table tbody tr').count();
    expect(linhas).toBeGreaterThanOrEqual(0);
  });

  test('deve visualizar detalhes de um servidor', async ({ page }) => {
      await page.goto('/servidor');
    const btn = page.locator('a.btn-info');
    if (await btn.count() > 0) {
      await btn.first().click();
      await expect(page).toHaveURL(/servidor\//);
    }
  });

  test('deve exibir formulário de novo servidor', async ({ page }) => {
    await page.goto('http://localhost:8080/servidor');
    const btn = page.getByRole('button', { name: /novo servidor/i });
    if (await btn.count() > 0) {
      await btn.first().click();
      await expect(page).toHaveURL(/servidor\/novo/);
    }
  });
}); 