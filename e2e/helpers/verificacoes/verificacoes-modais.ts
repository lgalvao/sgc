import { Page, expect } from '@playwright/test';

/**
 * Verifica se o botão de disponibilizar no modal está desabilitado.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoModalDisponibilizarDesabilitado(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await expect(modal.getByRole('button', { name: /Confirmar/i })).toBeDisabled();
}

/**
 * Verifica se o botão de disponibilizar no modal está habilitado.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoModalDisponibilizarHabilitado(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await expect(modal.getByRole('button', { name: /Confirmar/i })).toBeEnabled();
}
