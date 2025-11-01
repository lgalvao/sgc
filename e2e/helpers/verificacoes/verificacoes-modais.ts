import { Page, expect } from '@playwright/test';

export async function verificarBotaoModalDisponibilizarDesabilitado(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await expect(modal.getByRole('button', { name: /Confirmar/i })).toBeDisabled();
}

export async function verificarBotaoModalDisponibilizarHabilitado(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await expect(modal.getByRole('button', { name: /Confirmar/i })).toBeEnabled();
}
