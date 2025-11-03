import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS} from '../dados';
import {preencherCampo} from '../utils';

/**
 * Apresenta sugestões em um processo.
 * @param page A instância da página do Playwright.
 * @param sugestoes As sugestões a serem apresentadas.
 */
export async function apresentarSugestoes(page: Page, sugestoes: string): Promise<void> {
    await page.getByTestId('apresentar-sugestoes-btn').click();
    const modal = page.locator(SELETORES.MODAL_APRESENTAR_SUGESTOES);
    await expect(modal).toBeVisible();
    await preencherCampo([modal.getByLabel('Sugestões')], sugestoes);
    await modal.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
}

/**
 * Valida o mapa de competências.
 * @param page A instância da página do Playwright.
 */
export async function validarMapa(page: Page): Promise<void> {
    await page.getByTestId('validar-btn').click();
    const modal = page.locator(SELETORES.MODAL_VALIDAR);
    await expect(modal).toBeVisible();
    await modal.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
}
