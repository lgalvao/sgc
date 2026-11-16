import {expect, test} from '@playwright/test';

test.describe('Permissões de acesso', () => {
  const rotasProtegidas = [
    '/',
    '/processo',
    '/unidade',
    '/servidor',
    '/notificacao',
    '/historico',
  ];

  for (const rota of rotasProtegidas) {
    test(`deve redirecionar para login ao acessar ${rota} sem autenticação`, async ({ page }) => {
        await page.goto(`${rota}`);
      await expect(page).toHaveTitle(/Login - SGC/);
      await expect(page.getByLabel('Usuário')).toBeVisible();
    });
  }
}); 