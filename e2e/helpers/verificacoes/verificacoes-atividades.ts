import {expect, Page} from '@playwright/test';
import {SELETORES} from '../dados';

export async function verificarAtividadeVisivel(page: Page, nomeAtividade: string): Promise<void> {
    await expect(page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade})).toBeVisible();
}

export async function verificarAtividadeNaoVisivel(page: Page, nomeAtividade: string): Promise<void> {
    await expect(page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade})).not.toBeVisible();
}

export async function verificarConhecimentoNaAtividade(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    await expect(cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento})).toBeVisible();
}

export async function verificarConhecimentoNaoVisivelNaAtividade(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    await expect(cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento})).not.toBeVisible();
}
