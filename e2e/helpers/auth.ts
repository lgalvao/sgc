import {Page} from '@playwright/test';
import {USUARIOS} from './dados/constantes';

/**
 * Realiza o login direto para usuários com um único perfil.
 *
 * @param page A instância da página do Playwright.
 * @param usuario O objeto do usuário (ex: USUARIOS.ADMIN).
 */
export async function loginComo(
    page: Page,
    usuario: { titulo: string; senha: string }
) {
    await loginDireto(page, usuario);
}

/**
 * Realiza o login direto para usuários com um único perfil.
 *
 * @param page A instância da página do Playwright.
 * @param usuario O objeto do usuário (ex: USUARIOS.ADMIN).
 */
async function loginDireto(
    page: Page,
    usuario: { titulo: string; senha: string }
) {
    await page.goto('/login');
    await page.getByTestId('input-titulo').fill(usuario.titulo);
    await page.getByTestId('input-senha').fill(usuario.senha);

    const navigationPromise = page.waitForURL('/painel');
    await page.getByTestId('botao-entrar').click();
    await navigationPromise;
    await page.waitForLoadState('networkidle');
}

/**
 * Realiza o login com seleção de perfil para usuários com múltiplos perfis.
 *
 * @param page A instância da página do Playwright.
 * @param usuario O objeto do usuário (ex: USUARIOS.MULTI_PERFIL).
 * @param perfilUnidadeLabel O perfil a ser selecionado (ex: 'ADMIN - STIC').
 */
async function loginComSelecaoPerfil(
    page: Page,
    usuario: { titulo: string; senha: string },
    perfilUnidadeLabel: string
) {
    await page.goto('/login');
    await page.getByTestId('input-titulo').fill(usuario.titulo);
    await page.getByTestId('input-senha').fill(usuario.senha);

    await page.getByTestId('botao-entrar').click();

    const seletorPerfil = page.getByTestId('select-perfil-unidade');
    await seletorPerfil.waitFor({ state: 'visible'});
    await seletorPerfil.selectOption({ label: perfilUnidadeLabel });

    const navigationPromise = page.waitForURL('/painel');
    await page.getByTestId('botao-entrar').click();
    await navigationPromise;

    await page.waitForLoadState('networkidle');
}

/**
 * Realiza o login como administrador.
 * @param page A instância da página do Playwright.
 */
export async function loginComoAdmin(page: Page) {
    await loginDireto(page, USUARIOS.ADMIN);
}

/**
 * Realiza o login como gestor.
 * @param page A instância da página do Playwright.
 */
export async function loginComoGestor(page: Page) {
    await loginDireto(page, USUARIOS.GESTOR);
}

/**
 * Realiza o login como chefe.
 * @param page A instância da página do Playwright.
 */
export async function loginComoChefe(page: Page) {
    await loginDireto(page, USUARIOS.CHEFE_SGP);
}

/**
 * Realiza o login como chefe da STIC.
 * @param page A instância da página do Playwright.
 */
export async function loginComoChefeStic(page: Page) {
    await loginDireto(page, USUARIOS.CHEFE_STIC);
}

/**
 * Realiza o login como servidor.
 * @param page A instância da página do Playwright.
 */
export async function loginComoServidor(page: Page) {
    await loginDireto(page, USUARIOS.SERVIDOR);
}

/**
 * Realiza o login com um usuário de múltiplos perfis e seleciona o perfil de administrador.
 * @param page A instância da página do Playwright.
 */
export async function loginComMultiPerfilAdmin(page: Page) {
    await loginComSelecaoPerfil(page, USUARIOS.MULTI_PERFIL, 'ADMIN - STIC');
}

/**
 * Realiza o login como chefe da SEDIA.
 * @param page A instância da página do Playwright.
 */
export async function loginComoChefeSedia(page: Page) {
    await loginDireto(page, USUARIOS.CHEFE_SEDIA);
}
