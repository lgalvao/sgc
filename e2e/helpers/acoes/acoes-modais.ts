import { Page } from '@playwright/test';
import { SELETORES, TEXTOS } from '../dados';

/**
 * AÇÕES ESPECÍFICAS PARA MODAIS
 * Funções para interação com modais em testes
 */

/**
 * Clica no botão "Cancelar" em um modal.
 * @param page A instância da página do Playwright.
 */
export async function cancelarNoModal(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await modal.locator(SELETORES.BTN_MODAL_CANCELAR).click();
}

/**
 * Clica no botão "Confirmar" em um modal.
 * @param page A instância da página do Playwright.
 */
export async function confirmarNoModal(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await modal.locator(SELETORES.BTN_MODAL_CONFIRMAR).click({ force: true });

    // Wait for modal animation to complete
    await modal.waitFor({ state: 'hidden' });
}

/**
 * Clica no botão "Remover" em um modal de confirmação de remoção.
 * @param page A instância da página do Playwright.
 */
export async function confirmarRemocaoNoModal(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    // Tenta clicar no botão de confirmação padrão ou no botão "Remover" se for específico
    await modal.locator(`${SELETORES.BTN_MODAL_CONFIRMAR}, button:has-text("${TEXTOS.REMOVER}")`).first().click();
}

/**
 * Abre o diálogo de remoção de um processo.
 * @param page A instância da página do Playwright.
 */
export async function abrirDialogoRemocaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.REMOVER }).click();
}

/**
 * Clica no botão "Iniciar processo".
 * @param page A instância da página do Playwright.
 */
export async function clicarIniciarProcesso(page: Page): Promise<void> {
    await page.locator(SELETORES.BTN_INICIAR_PROCESSO).click();
}

/**
 * Abre o modal de inicialização de processo.
 * @param page A instância da página do Playwright.
 */
export async function abrirModalInicializacaoProcesso(page: Page): Promise<void> {
    await clicarIniciarProcesso(page);
}

/**
 * Clica no botão "Confirmar" no modal de inicialização de processo.
 * @param page A instância da página do Playwright.
 */
export async function confirmarInicializacaoNoModal(page: Page): Promise<void> {
    await confirmarNoModal(page);
}

/**
 * Cancela um modal clicando no botão "Cancelar", "Fechar", ou em um botão de fechar genérico.
 * @param page A instância da página do Playwright.
 */
export async function cancelarModal(page: Page): Promise<void> {
    const modalVisivel = page.locator('.modal.show');
    await modalVisivel.locator(SELETORES.BTN_MODAL_CANCELAR).click();
}

/**
 * Fecha um alerta clicando no botão de fechar.
 * @param page A instância da página do Playwright.
 */
export async function fecharAlerta(page: Page): Promise<void> {
    await page.locator('.btn-close').click();
}

