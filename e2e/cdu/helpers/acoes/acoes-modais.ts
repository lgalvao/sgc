import {Page} from '@playwright/test';
import {SELETORES, TEXTOS} from '../dados';
import {clicarPorTestIdOuRole} from '../utils';

/**
 * AÇÕES ESPECÍFICAS PARA MODAIS
 * Funções para interação com modais em testes
 */

/**
 * Cancelar ação no modal
 */
export async function cancelarNoModal(page: Page): Promise<void> {
    // Priorizar test-id de cancelamento no modal, fallback por texto/role
    const clicked = await clicarPorTestIdOuRole(page, SELETORES.BTN_MODAL_CANCELAR, 'button', TEXTOS.CANCELAR, '.modal.show .btn-secondary');
    if (!clicked) {
        // última tentativa: botão cancel no escopo do modal
        await page.locator('.modal.show').getByRole('button', { name: TEXTOS.CANCELAR }).first().click();
    }
}

/**
 * Confirmar ação no modal
 */
export async function confirmarNoModal(page: Page): Promise<void> {
    // Priorizar test-id do botão confirmar em modais
    const clicked = await clicarPorTestIdOuRole(page, SELETORES.BTN_MODAL_CONFIRMAR, 'button', TEXTOS.CONFIRMAR, '.modal.show .btn-primary, .modal.show .btn-success');
    if (!clicked) {
        await page.locator('.modal.show').getByRole('button', { name: TEXTOS.CONFIRMAR }).first().click();
    }
}

/**
 * Confirmar remoção no modal usando botão de danger
 */
export async function confirmarRemocaoNoModal(page: Page): Promise<void> {
    // Preferir botão com classe danger dentro do modal, mas tentar também test-id caso exista
    const clicked = await clicarPorTestIdOuRole(page, undefined, 'button', TEXTOS.REMOVER, '.modal.show .btn-danger');
    if (!clicked) {
        await page.locator('.modal.show .btn-danger').first().click();
    }
}

/**
 * Abrir diálogo de remoção
 */
export async function abrirDialogoRemocaoProcesso(page: Page): Promise<void> {
    // Tenta pelo test-id padrão de remover, senão por role/text
    const clicked = await clicarPorTestIdOuRole(page, SELETORES.BTN_EXCLUIR, 'button', TEXTOS.REMOVER);
    if (!clicked) {
        await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    }
}

/**
 * Iniciar processo através do botão
 */
export async function clicarIniciarProcesso(page: Page): Promise<void> {
    const clicked = await clicarPorTestIdOuRole(page, SELETORES.BTN_INICIAR_PROCESSO, 'button', TEXTOS.INICIAR_PROCESSO);
    if (!clicked) await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
}

/**
 * Abrir modal de inicialização de processo
 */
export async function abrirModalInicializacaoProcesso(page: Page): Promise<void> {
    const clicked = await clicarPorTestIdOuRole(page, SELETORES.BTN_INICIAR_PROCESSO, 'button', TEXTOS.INICIAR_PROCESSO);
    if (!clicked) await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
}

/**
 * Confirmar inicialização no modal
 */
export async function confirmarInicializacaoNoModal(page: Page): Promise<void> {
    const clicked = await clicarPorTestIdOuRole(page, SELETORES.BTN_MODAL_CONFIRMAR, 'button', TEXTOS.CONFIRMAR);
    if (!clicked) await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Cancela um modal clicando no botão "Cancelar", "Fechar", ou em um botão de fechar genérico.
 */
export async function cancelarModal(page: Page): Promise<void> {
    const modalVisivel = page.locator('.modal.show');

    // 1) Tentar botão cancel via test-id no modal
    if (await modalVisivel.getByTestId(SELETORES.BTN_MODAL_CANCELAR).count() > 0) {
        await modalVisivel.getByTestId(SELETORES.BTN_MODAL_CANCELAR).last().click();
        return;
    }

    // 2) Tentar botão 'Cancelar' por role/text
    const botaoCancelar = modalVisivel.getByRole('button', {name: TEXTOS.CANCELAR});
    if (await botaoCancelar.count() > 0) {
        await botaoCancelar.last().click();
        return;
    }

    // 3) Tentar botão 'Fechar' por test-id ou por role
    if (await modalVisivel.getByTestId(SELETORES.BTN_MODAL_FECHAR).count() > 0) {
        await modalVisivel.getByTestId(SELETORES.BTN_MODAL_FECHAR).last().click();
        return;
    }
    const botaoFechar = modalVisivel.getByRole('button', {name: 'Fechar'});
    if (await botaoFechar.count() > 0) {
        await botaoFechar.last().click();
        return;
    }

    // 4) Fallback para elemento com data-bs-dismiss
    const botaoDismiss = modalVisivel.locator('[data-bs-dismiss="modal"]');
    if (await botaoDismiss.count() > 0) {
        await botaoDismiss.last().click();
    }
}

/**
 * Clica no botão "Histórico de análise".
 */
export async function clicarBotaoHistoricoAnalise(page: Page): Promise<void> {
    await page.getByRole('button', {name: 'Histórico de análise'}).click();
}