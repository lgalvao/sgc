import {Page} from '@playwright/test';
import {USUARIOS} from './dados/constantes-teste';

/**
 * Realiza o login completo através da interface de usuário.
 * Lida com a seleção de perfil para usuários com múltiplos perfis.
 *
 * @param page A instância da página do Playwright.
 * @param usuario O objeto do usuário (ex: USUARIOS.ADMIN).
 * @param perfilUnidadeLabel O perfil a ser selecionado, se houver múltiplos (ex: 'ADMIN - STIC').
 */
async function loginPelaUI(
    page: Page,
    usuario: { titulo: string; senha: string },
    perfilUnidadeLabel?: string
) {
    await page.goto('/login');
    await page.getByTestId('input-titulo').fill(usuario.titulo);
    await page.getByTestId('input-senha').fill(usuario.senha);
    await page.getByTestId('botao-entrar').click();

    // Se um seletor de perfil aparecer, selecione a opção desejada
    if (perfilUnidadeLabel) {
        const seletorPerfil = page.getByTestId('select-perfil-unidade');
        await seletorPerfil.waitFor({state: 'visible', timeout: 2000});
        await seletorPerfil.selectOption({label: perfilUnidadeLabel});
        await page.getByTestId('botao-entrar').click();
    }

    // Aguarda o redirecionamento para o painel
    await page.waitForURL('/painel', {timeout: 1500});
}

/**
 * Realiza o login como administrador.
 * @param page A instância da página do Playwright.
 */
export async function loginComoAdmin(page: Page) {
    await loginPelaUI(page, USUARIOS.ADMIN);
}

/**
 * Realiza o login como gestor.
 * @param page A instância da página do Playwright.
 */
export async function loginComoGestor(page: Page) {
    await loginPelaUI(page, USUARIOS.GESTOR);
}

/**
 * Realiza o login como chefe.
 * @param page A instância da página do Playwright.
 */
export async function loginComoChefe(page: Page) {
    // Usar um chefe específico como padrão
    await loginPelaUI(page, USUARIOS.CHEFE_SGP);
}

/**
 * Realiza o login como chefe da STIC.
 * @param page A instância da página do Playwright.
 */
export async function loginComoChefeStic(page: Page) {
    await loginPelaUI(page, USUARIOS.CHEFE_STIC);
}

/**
 * Realiza o login como servidor.
 * @param page A instância da página do Playwright.
 */
export async function loginComoServidor(page: Page) {
    await loginPelaUI(page, USUARIOS.SERVIDOR);
}

/**
 * Realiza o login com um usuário de múltiplos perfis e seleciona o perfil de administrador.
 * @param page A instância da página do Playwright.
 */
export async function loginComMultiPerfilAdmin(page: Page) {
    await loginPelaUI(page, USUARIOS.MULTI_PERFIL, 'ADMIN - STIC');
}

/**
 * Realiza o login como chefe da SEDIA.
 * @param page A instância da página do Playwright.
 */
export async function loginComoChefeSedia(page: Page) {
    await loginPelaUI(page, USUARIOS.CHEFE_SEDIA);
}
