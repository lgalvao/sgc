import { test, expect } from '@playwright/test';
import { login } from './helpers';

test.describe('Login', () => {
  const usuarios = [
    { usuario: 'sedoc@tre-pe.jus.br', senha: 'admin123', perfil: 'SEDOC' },
    { usuario: 'chefe@tre-pe.jus.br', senha: 'chefe123', perfil: 'CHEFE' },
    { usuario: 'gestor@tre-pe.jus.br', senha: 'gestor123', perfil: 'GESTOR' },
  ];

  for (const { usuario, senha, perfil } of usuarios) {
    test(`deve fazer login com sucesso para o perfil ${perfil}`, async ({ page }) => {
      await login(page, usuario, senha);
      await expect(page).toHaveTitle(/Dashboard - SGC/);
      await expect(page.getByTestId('dashboard-title')).toBeVisible();
    });
  }

  test('deve exibir erro ao tentar login com credenciais inválidas', async ({ page }) => {
    await login(page, 'usuario_invalido@tre-pe.jus.br', 'senhaerrada');
    await expect(page.locator('.alert-danger')).toContainText(/Erro de autenticação|usuário ou senha inválidos/i);
  });
}); 