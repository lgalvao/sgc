import {expect, Page} from '@playwright/test';

/**
 * Valida um mapa de competências
 */
export async function validarMapa(page: Page, sugestoes?: string): Promise<void> {
    await page.getByTestId('validar-btn').click();
    await expect(page.getByTestId('modal-validar')).toBeVisible();

    if (sugestoes) {
        await page.fill('textarea[placeholder*="sugestões"]', sugestoes);
    }

    await page.getByTestId('modal-validar-confirmar').click();
}

/**
 * Clica no botão "Impactos no mapa" para abrir o modal de impactos.
 */
export async function clicarBotaoImpactosMapa(page: Page): Promise<void> {
    await page.getByTestId('impactos-mapa-button').click();
}

/**
 * Fecha o modal de impactos no mapa.
 */
export async function fecharModalImpactos(page: Page): Promise<void> {
    await page.getByTestId('fechar-impactos-mapa-button').click();
}