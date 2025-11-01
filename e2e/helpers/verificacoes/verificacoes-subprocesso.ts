import { Page, expect } from '@playwright/test';
import { SELETORES } from '../dados';

/**
 * Verifica se os detalhes de um subprocesso estão visíveis.
 * @param page A instância da página do Playwright.
 */
export async function verificarDetalhesSubprocesso(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.SUBPROCESSO_HEADER)).toBeVisible();
    await expect(page.locator(SELETORES.PROCESSO_INFO)).toBeVisible();
}
