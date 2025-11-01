import {Page} from '@playwright/test';

/**
 * Preenche o formulário de login com o título de eleitor e a senha.
 * @param page A instância da página do Playwright.
 * @param titulo O título de eleitor do usuário.
 * @param senha_plana A senha do usuário.
 */
export async function preencherFormularioLogin(page: Page, titulo: string, senha_plana: string): Promise<void> {
    await page.getByTestId('input-titulo').fill(titulo);
    await page.getByTestId('input-senha').fill(senha_plana);
}

/**
 * Seleciona um perfil de usuário na tela de seleção de perfis.
 * @param page A instância da página do Playwright.
 * @param perfil O nome do perfil a ser selecionado.
 */
export async function selecionarPerfil(page: Page, perfil: string): Promise<void> {
    const seletor = page.getByTestId('select-perfil-unidade');
    await seletor.selectOption({label: perfil});
}

/**
 * Fecha um alerta clicando no botão de fechar.
 * @param page A instância da página do Playwright.
 */
export async function fecharAlerta(page: Page): Promise<void> {
    await page.locator('.btn-close').click();
}

/**
 * Clica no botão "Cancelar" em um modal.
 * @param page A instância da página do Playwright.
 */
export async function cancelarNoModal(page: Page): Promise<void> {
    await page.locator('.modal.show .btn-secondary').click();
}

/**
 * Clica no botão "Confirmar" em um modal.
 * @param page A instância da página do Playwright.
 */
export async function confirmarNoModal(page: Page): Promise<void> {
    await page.locator('.modal.show .btn-primary').click();
}
