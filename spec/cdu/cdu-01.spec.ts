import {expect, test} from '@playwright/test';
import {
    esperarTextoVisivel,
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    loginComoServidor,
    verificarUrl
} from './auxiliares-teste';
import {ROTULOS, TEXTOS, URLS} from './constantes-teste';

test.describe('CDU-01: Realizar login e exibir estrutura das telas', () => {
  test('deve permitir login SERVIDOR e exibir estrutura da aplicação', async ({ page }) => {
    await loginComoServidor(page);
  });

  test('deve permitir login GESTOR e exibir estrutura da aplicação', async ({ page }) => {
    await loginComoGestor(page);
  });

  test('deve permitir login CHEFE e exibir estrutura da aplicação', async ({ page }) => {
    await loginComoChefe(page);
  });

  test('deve permitir login ADMIN e exibir estrutura da aplicação', async ({ page }) => {
    await loginComoAdmin(page);
  });

  test('deve carregar a página de login corretamente', async ({ page }) => {
    await page.goto('/');
    await verificarUrl(page, `**${URLS.LOGIN}`);
    await esperarTextoVisivel(page, TEXTOS.SISTEMA_GESTAO_COMPETENCIAS);
    await expect(page.getByLabel(ROTULOS.TITULO_ELEITORAL)).toBeVisible();
    await expect(page.getByLabel(ROTULOS.SENHA)).toBeVisible();
    await expect(page.getByRole('button', { name: TEXTOS.ENTRAR })).toBeVisible();
  });
});