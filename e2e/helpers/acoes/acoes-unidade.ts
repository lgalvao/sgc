import { Page } from '@playwright/test';

export async function selecionarUnidadesPorSigla(page: Page, siglas: string[]): Promise<void> {
    for (const sigla of siglas) {
        await page.locator(`#chk-${sigla}`).check();
    }
}
