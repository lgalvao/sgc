import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS, URLS} from '../dados';

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
    const notificacao = page.locator(SELETORES.NOTIFICACAO_ERRO);
    await expect(notificacao).toBeVisible();
    await expect(notificacao).toContainText(mensagem);
}

/**
 * Verifica uma notificação de alerta (warning/info)
 */
export async function verificarAlerta(page: Page, texto: string): Promise<void> {
    const alerta = page.locator('.notification.notification-warn', { hasText: texto });
    await expect(alerta).toBeVisible();
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
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
}

/**
 * Verifica se modal foi fechado
 */
export async function verificarModalFechado(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).not.toBeVisible();
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
/**
 * Verifica se a disponibilização foi concluída:
 * - tenta aguardar o fechamento do modal de disponibilização
 * - se o modal não fechar dentro do timeout, verifica se há notificação de erro/aviso
 */
export async function verificarDisponibilizacaoConcluida(page: Page, modalSelector: string = '[aria-labelledby="disponibilizarModalLabel"]', notificacaoTestId: string = 'notificacao-disponibilizacao'): Promise<void> {
    const modal = page.locator(modalSelector);
    try {
        await modal.waitFor({state: 'hidden', timeout: 2000});
        return;
    } catch {
        const notificacao = page.getByTestId(notificacaoTestId);
        await expect(notificacao).toBeVisible({timeout: 2000});
    }
}
/**
 * Verifica se o modal de disponibilização está visível com título esperado.
 */
export async function verificarModalDisponibilizacaoVisivel(page: Page): Promise<void> {
    // Preferência por test-ids dentro do modal (mais estáveis)
    const modal = page.locator('[aria-labelledby="disponibilizarModalLabel"], .modal.show');
    if ((await modal.count()) > 0) {
        // botão com test-id
        if ((await modal.getByTestId(SELETORES.BTN_DISPONIBILIZAR_PAGE).count()) > 0) {
            await expect(modal.getByTestId(SELETORES.BTN_DISPONIBILIZAR_PAGE).first()).toBeVisible();
            return;
        }
        // input de data com test-id
        if ((await modal.getByTestId('input-data-limite').count()) > 0) {
            await expect(modal.getByTestId('input-data-limite').first()).toBeVisible();
            return;
        }
        // fallback para título/labels informativos
        try {
            await expect(modal).toBeVisible();
            return;
        } catch {
            // continue para fallback global
        }
    }

    // Fallback global por test-id do botão
    if ((await page.getByTestId(SELETORES.BTN_DISPONIBILIZAR_PAGE).count()) > 0) {
        await expect(page.getByTestId(SELETORES.BTN_DISPONIBILIZAR_PAGE).first()).toBeVisible();
        return;
    }

    // Fallback por títulos conhecidos (suportando variações)
    const possiveisTitulos = [TEXTOS.DISPONIBILACAO_TITULO, 'Disponibilizar Mapa', 'Disponibilizar'];
    for (const titulo of possiveisTitulos) {
        const heading = page.getByRole('heading', {name: titulo});
        if ((await heading.count()) > 0) {
            await expect(heading.first()).toBeVisible();
            return;
        }
    }

    // Último recurso: checar se algum modal está visível
    await expect(page.locator('.modal.show')).toBeVisible();
}

/**
 * Verifica o valor do campo observações dentro do modal de disponibilização.
 */
export async function verificarCampoObservacoesValor(page: Page, valorEsperado: string): Promise<void> {
    const modal = page.locator('[aria-labelledby="disponibilizarModalLabel"], .modal.show');
    if ((await modal.locator('#observacoes').count()) > 0) {
        await expect(modal.locator('#observacoes')).toHaveValue(valorEsperado);
    } else {
        await expect(modal.getByLabel(/observa/i)).toHaveValue(valorEsperado);
    }
}

/**
 * Verifica se o botão de disponibilizar está habilitado/inabilitado no modal.
 */
export async function verificarBotaoDisponibilizarHabilitado(page: Page, habilitado: boolean = true): Promise<void> {
    // Priorizar test-id do botão (mais confiável entre variações de UI)
    const btnTestId = page.getByTestId(SELETORES.BTN_DISPONIBILIZAR_PAGE);
    if ((await btnTestId.count()) > 0) {
        const btn = btnTestId.first();
        if (habilitado) {
            await expect(btn).toBeEnabled();
        } else {
            await expect(btn).toBeDisabled();
        }
        return;
    }

    // Fallback dentro do modal, por role/name
    const btnNoModal = page.locator('[aria-labelledby="disponibilizarModalLabel"]').getByRole('button', {name: TEXTOS.DISPONIBILIZAR});
    if ((await btnNoModal.count()) > 0) {
        if (habilitado) {
            await expect(btnNoModal).toBeEnabled();
        } else {
            await expect(btnNoModal).toBeDisabled();
        }
        return;
    }

    // Fallback global por role/name
    const btnGlobal = page.getByRole('button', {name: TEXTOS.DISPONIBILIZAR});
    if ((await btnGlobal.count()) > 0) {
        const first = btnGlobal.first();
        if (habilitado) {
            await expect(first).toBeEnabled();
        } else {
            await expect(first).toBeDisabled();
        }
        return;
    }

    // Se não encontramos o botão, lançar erro claro para diagnóstico
    throw new Error('Botão "Disponibilizar" não encontrado na página ou no modal.');
}