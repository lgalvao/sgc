import { expect, Page } from '@playwright/test';
import { SELETORES, TEXTOS } from '../dados';
import { preencherCampo } from '../utils';

export async function apresentarSugestoes(page: Page, sugestoes: string): Promise<void> {
    await page.getByTestId('apresentar-sugestoes-btn').click();
    const modal = page.locator(SELETORES.MODAL_APRESENTAR_SUGESTOES);
    await expect(modal).toBeVisible();
    await preencherCampo([modal.getByLabel('Sugestões')], sugestoes);
    await modal.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
}

export async function validarMapa(page: Page): Promise<void> {
    await page.getByTestId('validar-btn').click();
    const modal = page.locator(SELETORES.MODAL_VALIDAR);
    await expect(modal).toBeVisible();
    await modal.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
}
