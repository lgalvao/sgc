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
