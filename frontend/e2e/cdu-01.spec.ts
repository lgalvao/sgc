import { test, expect } from '@playwright/test';
import {
  loginComoAdmin,
  loginComoChefe,
  loginComMultiPerfilAdmin,
} from './helpers/auth';
import { USUARIOS } from './helpers/dados/constantes-teste';

test.describe('CDU-01: Fluxo de Login e Seleção de Perfil', () => {

  test.describe('Login Convencional (via UI)', () => {

    test('deve fazer login e ir direto para o painel com perfil único', async ({ page }) => {
      await page.goto('/login');
      await page.getByTestId('input-titulo').fill(USUARIOS.CHEFE_SGP.titulo);
      await page.getByTestId('input-senha').fill(USUARIOS.CHEFE_SGP.senha);
      await page.getByTestId('botao-entrar').click();

      await expect(page).toHaveURL('/painel');
      await expect(page.getByTestId('titulo-processos')).toBeVisible();
    });

    test('deve mostrar seleção de perfis para usuário com múltiplos perfis', async ({ page }) => {
      await page.goto('/login');
      await page.getByTestId('input-titulo').fill(USUARIOS.MULTI_PERFIL.titulo);
      await page.getByTestId('input-senha').fill(USUARIOS.MULTI_PERFIL.senha);
      await page.getByTestId('botao-entrar').click();

      await expect(page.getByText('Selecione o perfil e a unidade')).toBeVisible();
      const seletor = page.getByTestId('select-perfil-unidade');
      await expect(seletor).toBeVisible();

      await seletor.selectOption({ label: 'ADMIN - STIC' });
      await page.getByTestId('botao-entrar').click();

      await expect(page).toHaveURL('/painel');
      await expect(page.getByTitle('Configurações do sistema')).toBeVisible(); // Ícone de engrenagem do admin
    });
  });

  test.describe('Login Programático (Helpers de Teste)', () => {

    test('deve funcionar para loginComoAdmin', async ({ page }) => {
      await loginComoAdmin(page);
      await expect(page).toHaveURL('/painel');
      await expect(page.getByTitle('Configurações do sistema')).toBeVisible();
    });

    test('deve funcionar para loginComoChefe', async ({ page }) => {
      await loginComoChefe(page);
      await expect(page).toHaveURL('/painel');
      // Chefe não vê o ícone de admin
      await expect(page.getByTitle('Configurações do sistema')).not.toBeVisible();
    });

    test('deve funcionar para login com múltiplos perfis', async ({ page }) => {
        await loginComMultiPerfilAdmin(page);
        await expect(page).toHaveURL('/painel');
        await expect(page.getByTitle('Configurações do sistema')).toBeVisible();
    });
  });
});
