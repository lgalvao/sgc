import {expect, Page} from "@playwright/test";

/**
 * Realiza fluxo de login reutilizável.
 * - preenche credenciais (id, senha = 123)
 * - lida com seletor de perfil se aparecer
 * - aguarda navegação para /painel
 */
export async function performLogin(page: Page, userId: string, profileOptionLabel: string) {
  const baseUrl = "http://localhost:5173/";
  await page.goto(baseUrl);
  await page.waitForLoadState('domcontentloaded');

  const tituloInput = page.getByTestId('input-titulo');
  await tituloInput.fill(userId);

  const senhaInput = page.getByTestId('input-senha');
  await senhaInput.fill('123');

  await page.getByTestId('botao-entrar').click();

  const seletorDePerfil = page.getByTestId('select-perfil-unidade');
  try {
    await seletorDePerfil.waitFor({ state: 'visible', timeout: 2000 });
  } catch {
    // ignore - selector may not appear for single-profile users
  }

  if (await seletorDePerfil.isVisible()) {
    await seletorDePerfil.selectOption({ label: profileOptionLabel });
    await page.getByTestId('botao-entrar').click();
  }

  await page.waitForURL(`${baseUrl}painel`);
  await expect(page.getByTestId('titulo-processos')).toBeVisible();
}