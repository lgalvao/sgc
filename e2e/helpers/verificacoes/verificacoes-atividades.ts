import {expect, Page} from '@playwright/test';
import {SELETORES} from '../dados';

/**
 * Verifica se uma atividade está visível na página.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade.
 */
export async function verificarAtividadeVisivel(page: Page, nomeAtividade: string): Promise<void> {
    await expect(page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade})).toBeVisible();
}

/**
 * Verifica se uma atividade não está visível na página.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade.
 */
export async function verificarAtividadeNaoVisivel(page: Page, nomeAtividade: string): Promise<void> {
    await expect(page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade})).not.toBeVisible();
}

/**
 * Verifica se um conhecimento está visível em uma atividade.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade.
 * @param nomeConhecimento O nome do conhecimento.
 */
export async function verificarConhecimentoNaAtividade(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    await expect(cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento})).toBeVisible();
}

/**
 * Verifica se um conhecimento não está visível em uma atividade.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade.
 * @param nomeConhecimento O nome do conhecimento.
 */
export async function verificarConhecimentoNaoVisivelNaAtividade(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    await expect(cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento})).not.toBeVisible();
}
