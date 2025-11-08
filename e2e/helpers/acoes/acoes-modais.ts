import {Page} from '@playwright/test';
import {SELETORES, TEXTOS} from '../dados';
import {clicarElemento} from '../utils';
import {extrairIdDoSeletor} from '../utils/utils';

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
    await clicarElemento([
        modal.getByTestId(extrairIdDoSeletor(SELETORES.BTN_MODAL_CANCELAR)),
        modal.getByRole('button', {name: TEXTOS.CANCELAR}),
        modal.locator('.btn-secondary'), // Fallback genérico
    ]);
}

/**
 * Clica no botão "Confirmar" em um modal.
 * @param page A instância da página do Playwright.
 */
export async function confirmarNoModal(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await clicarElemento([
        modal.getByTestId(extrairIdDoSeletor(SELETORES.BTN_MODAL_CONFIRMAR)),
        modal.getByRole('button', {name: TEXTOS.CONFIRMAR}),
        modal.locator('.btn-primary'), // Fallback genérico
        modal.locator('.btn-success'), // Fallback genérico
    ]);
}

/**
 * Clica no botão "Remover" em um modal de confirmação de remoção.
 * @param page A instância da página do Playwright.
 */
export async function confirmarRemocaoNoModal(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await clicarElemento([
        modal.getByRole('button', {name: TEXTOS.REMOVER}),
        modal.locator('.btn-danger'),
        modal.getByTestId(extrairIdDoSeletor(SELETORES.BTN_MODAL_CONFIRMAR)), // Fallback para modais de confirmação genéricos
        modal.getByRole('button', {name: TEXTOS.CONFIRMAR}),
    ]);
}

/**
 * Abre o diálogo de remoção de um processo.
 * @param page A instância da página do Playwright.
 */
export async function abrirDialogoRemocaoProcesso(page: Page): Promise<void> {
    await clicarElemento([page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_EXCLUIR)), page.getByRole('button', {name: TEXTOS.REMOVER})]);
}

/**
 * Clica no botão "Iniciar processo".
 * @param page A instância da página do Playwright.
 */
export async function clicarIniciarProcesso(page: Page): Promise<void> {
    await clicarElemento([
        page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_INICIAR_PROCESSO)),
        page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}),
    ]);
}

/**
 * Abre o modal de inicialização de processo.
 * @param page A instância da página do Playwright.
 */
export async function abrirModalInicializacaoProcesso(page: Page): Promise<void> {
    await clicarIniciarProcesso(page); // Reutiliza a função mais simples
}

/**
 * Clica no botão "Confirmar" no modal de inicialização de processo.
 * @param page A instância da página do Playwright.
 */
export async function confirmarInicializacaoNoModal(page: Page): Promise<void> {
    await confirmarNoModal(page); // Reutiliza a função de confirmação genérica
}

/**
 * Cancela um modal clicando no botão "Cancelar", "Fechar", ou em um botão de fechar genérico.
 * @param page A instância da página do Playwright.
 */
export async function cancelarModal(page: Page): Promise<void> {
    const modalVisivel = page.locator('.modal.show');
    await clicarElemento([
        modalVisivel.getByTestId(extrairIdDoSeletor(SELETORES.BTN_MODAL_CANCELAR)).last(),
        modalVisivel.getByRole('button', {name: TEXTOS.CANCELAR}).last(),
        modalVisivel.getByTestId(extrairIdDoSeletor(SELETORES.BTN_MODAL_FECHAR)).last(),
        modalVisivel.getByRole('button', {name: 'Fechar'}).last(),
        modalVisivel.locator('[data-bs-dismiss="modal"]').last(),
    ]);
}

/**
 * Abre o modal para disponibilizar o mapa.
 * @param page A instância da página do Playwright.
 */
export async function abrirModalDisponibilizarMapa(page: Page): Promise<void> {
    await page.getByRole('button', { name: /Disponibilizar mapa/i }).click();
}

/**
 * Preenche o modal de disponibilização de mapa.
 * @param page A instância da página do Playwright.
 * @param data A data de disponibilização.
 * @param observacoes As observações.
 */
export async function preencherModalDisponibilizarMapa(page: Page, data: string, observacoes: string): Promise<void> {
    const modal = page.locator('.modal.show');
    await modal.locator('input[type="date"]').fill(data);
    await modal.locator('textarea').fill(observacoes);
}
