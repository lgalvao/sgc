import {Page} from '@playwright/test';

/**
 * Gera nome único para testes
 */
export function gerarNomeUnico(prefixo: string): string {
  return `${prefixo} ${Date.now()}`;
}

/**
 * Realiza o login pela UI, preenchendo título e senha.
 * Não clica em "Entrar", permitindo interações adicionais na tela de login.
 */
export async function login(page: Page, idServidor: string): Promise<void> {
  // Importando aqui para evitar dependência circular
  const { URLS, ROTULOS } = await import('./constantes-teste');
  
  await page.goto(URLS.LOGIN);
  await page.waitForLoadState('networkidle');

  // O login mockado usa o ID do servidor como "título" e uma senha padrão
  await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(idServidor);
  await page.getByLabel(ROTULOS.SENHA).fill('senha-padrao'); // A senha é ignorada pelo mock
}