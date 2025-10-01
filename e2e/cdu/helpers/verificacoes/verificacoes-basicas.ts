import {expect, Page} from '@playwright/test';
import {SELETORES_CSS, TEXTOS, URLS} from '../dados';

/**
 * VERIFICAÇÕES BÁSICAS E PRIMITIVAS
 * Funções de baixo nível para verificações simples e reutilizáveis
 */

/**
 * Espera uma mensagem de sucesso aparecer
 */
export async function esperarMensagemSucesso(page: Page, mensagem: string): Promise<void> {
    // Procuramos qualquer notificação que contenha o texto, independente da classe específica.
    const notificacao = page.locator('.notification', {hasText: mensagem});
    // Espera explícita e tolerante para notificações que podem demorar a aparecer
    try {
        await notificacao.first().waitFor({state: 'visible'});
    } catch {
        // fallback: tentar buscar por qualquer notificação que contenha parte da mensagem (nome do processo, etc.)
        const parcial = mensagem.split(' ').slice(0, 3).join(' ');
        const alternativa = page.locator('.notification', {hasText: parcial});
        await alternativa.first().waitFor({state: 'visible'});
    }
}

/**
 * Espera uma mensagem de erro aparecer
 */
export async function esperarMensagemErro(page: Page, mensagem: string): Promise<void> {
    const notificacao = page.locator(SELETORES_CSS.NOTIFICACAO_ERRO);
    await expect(notificacao).toBeVisible();
    await expect(notificacao).toContainText(mensagem);
}

/**
 * Espera um texto ficar visível na página
 */
export async function esperarTextoVisivel(page: Page, texto: string): Promise<void> {
    await expect(page.getByText(texto)).toBeVisible();
}

/**
 * Espera um elemento com test-id ficar visível
 */
export async function esperarElementoVisivel(page: Page, testId: string): Promise<void> {
    await expect(page.getByTestId(testId).first()).toBeVisible();
}

/**
 * Espera por um elemento ser invisível
 */
export async function esperarElementoInvisivel(page: Page, seletor: string): Promise<void> {
    await expect(page.getByTestId(seletor).first()).not.toBeVisible();
}

/**
 * Verifica URL com regex
 */
export async function verificarUrl(page: Page, url: string): Promise<void> {
    const regexUrl = new RegExp(url.replace(/\*\*/g, '.*'));
    await expect(page).toHaveURL(regexUrl);
}

/**
 * Espera por uma URL específica
 */
export async function esperarUrl(page: Page, url: string | RegExp): Promise<void> {
    if (typeof url === 'string') {
        await expect(page).toHaveURL(new RegExp(url));
    } else {
        await expect(page).toHaveURL(url);
    }
}

/**
 * Verifica se a URL atual é a do Painel.
 */
export async function verificarUrlDoPainel(page: Page): Promise<void> {
    await expect(page).toHaveURL(URLS.PAINEL);
}

/**
 * Verifica se modal está visível
 */
export async function verificarModalVisivel(page: Page): Promise<void> {
    await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
}

/**
 * Verifica se modal foi fechado
 */
export async function verificarModalFechado(page: Page): Promise<void> {
    await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).not.toBeVisible();
}

/**
 * Espera um botão ficar visível na página
 */
export async function esperarBotaoVisivel(page: Page, nomeBotao: string): Promise<void> {
    await expect(page.getByRole('button', {name: nomeBotao})).toBeVisible();
}

/**
 * Espera uma notificação de erro de login inválido.
 */
export async function esperarNotificacaoLoginInvalido(page: Page): Promise<void> {
    const notificacao = page.locator('.notification-container');
    await expect(notificacao.getByText(TEXTOS.ERRO_LOGIN_INVALIDO)).toBeVisible();
}