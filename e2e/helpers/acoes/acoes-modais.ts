import {Page} from '@playwright/test';
import {SELETORES, TEXTOS} from '../dados';
import {clicarElemento} from '../utils';

/**
 * AÇÕES ESPECÍFICAS PARA MODAIS
 * Funções para interação com modais em testes
 */

/**
 * Cancelar ação no modal
 */
export async function cancelarNoModal(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await clicarElemento([
        modal.getByTestId(SELETORES.BTN_MODAL_CANCELAR),
        modal.getByRole('button', {name: TEXTOS.CANCELAR}),
        modal.locator('.btn-secondary'), // Fallback genérico
    ]);
}

/**
 * Confirmar ação no modal
 */
export async function confirmarNoModal(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await clicarElemento([
        modal.getByTestId(SELETORES.BTN_MODAL_CONFIRMAR),
        modal.getByRole('button', {name: TEXTOS.CONFIRMAR}),
        modal.locator('.btn-primary'), // Fallback genérico
        modal.locator('.btn-success'), // Fallback genérico
    ]);
}

/**
 * Confirmar remoção no modal usando botão de danger
 */
export async function confirmarRemocaoNoModal(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await clicarElemento([
        modal.getByRole('button', {name: TEXTOS.REMOVER}),
        modal.locator('.btn-danger'),
        modal.getByTestId(SELETORES.BTN_MODAL_CONFIRMAR), // Fallback para modais de confirmação genéricos
        modal.getByRole('button', {name: TEXTOS.CONFIRMAR}),
    ]);
}

/**
 * Abrir diálogo de remoção de processo
 */
export async function abrirDialogoRemocaoProcesso(page: Page): Promise<void> {
    await clicarElemento([page.getByTestId(SELETORES.BTN_EXCLUIR), page.getByRole('button', {name: TEXTOS.REMOVER})]);
}

/**
 * Iniciar processo através do botão
 */
export async function clicarIniciarProcesso(page: Page): Promise<void> {
    await clicarElemento([
        page.getByTestId(SELETORES.BTN_INICIAR_PROCESSO),
        page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}),
    ]);
}

/**
 * Abrir modal de inicialização de processo
 */
export async function abrirModalInicializacaoProcesso(page: Page): Promise<void> {
    await clicarIniciarProcesso(page); // Reutiliza a função mais simples
}

/**
 * Confirmar inicialização no modal
 */
export async function confirmarInicializacaoNoModal(page: Page): Promise<void> {
    await confirmarNoModal(page); // Reutiliza a função de confirmação genérica
}

/**
 * Cancela um modal clicando no botão "Cancelar", "Fechar", ou em um botão de fechar genérico.
 */
export async function cancelarModal(page: Page): Promise<void> {
    const modalVisivel = page.locator('.modal.show');
    await clicarElemento([
        modalVisivel.getByTestId(SELETORES.BTN_MODAL_CANCELAR).last(),
        modalVisivel.getByRole('button', {name: TEXTOS.CANCELAR}).last(),
        modalVisivel.getByTestId(SELETORES.BTN_MODAL_FECHAR).last(),
        modalVisivel.getByRole('button', {name: 'Fechar'}).last(),
        modalVisivel.locator('[data-bs-dismiss="modal"]').last(),
    ]);
}

/**
 * Clica no botão "Histórico de análise" de forma robusta.
 * - Prioriza test-id (SELETORES.BTN_HISTORICO_ANALISE)
 * - Fallback por role/text (TEXTOS.HISTORICO_ANALISE)
 * - Lança erro claro se não encontrado para diagnóstico imediato
 */
export async function clicarBotaoHistoricoAnalise(page: Page): Promise<void> {
    await clicarElemento(
        [
            page.getByTestId(SELETORES.BTN_HISTORICO_ANALISE),
            page.getByTestId('historico-analise-btn'),
            page.getByTestId('historico-analise-btn-gestor'),
            page.getByRole('button', {name: TEXTOS.HISTORICO_ANALISE}),
        ],
        {force: true} // Mantém o clique forçado como fallback, encapsulado no utilitário
    );
}
