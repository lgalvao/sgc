import {Page} from '@playwright/test';
import {USUARIOS} from '../../dados';

export async function preencherFormularioLogin(page: Page, titulo: string, senha_plana: string): Promise<void> {
    await page.getByTestId('input-titulo').fill(titulo);
    await page.getByTestId('input-senha').fill(senha_plana);
}

export async function selecionarPerfil(page: Page, perfil: string): Promise<void> {
    const seletor = page.getByTestId('select-perfil-unidade');
    await seletor.selectOption({label: perfil});
}

export async function fecharAlerta(page: Page): Promise<void> {
    await page.locator('.btn-close').click();
}

export async function cancelarNoModal(page: Page): Promise<void> {
    await page.locator('.modal.show .btn-secondary').click();
}

export async function confirmarNoModal(page: Page): Promise<void> {
    await page.locator('.modal.show .btn-primary').click();
}
