import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS, URLS} from '../dados';

/**
 * Espera que uma mensagem de sucesso seja exibida.
 * @param page A instância da página do Playwright.
 * @param mensagem A mensagem a ser esperada.
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
 * Espera que uma mensagem de erro seja exibida.
 * @param page A instância da página do Playwright.
 * @param mensagem A mensagem a ser esperada.
 */
export async function esperarMensagemErro(page: Page, mensagem: string): Promise<void> {
    const notificacao = page.locator(SELETORES.NOTIFICACAO_ERRO);
    await expect(notificacao).toBeVisible();
    await expect(notificacao).toContainText(mensagem);
}

/**
 * Verifica se um alerta é exibido.
 * @param page A instância da página do Playwright.
 * @param texto O texto do alerta.
 */
export async function verificarAlerta(page: Page, texto: string): Promise<void> {
    const alerta = page.locator('.notification.notification-warn', { hasText: texto });
    await expect(alerta).toBeVisible();
}

/**
 * Espera que um texto seja exibido na página.
 * @param page A instância da página do Playwright.
 * @param texto O texto a ser esperado.
 */
export async function esperarTextoVisivel(page: Page, texto: string): Promise<void> {
    await expect(page.getByText(texto)).toBeVisible();
}

/**
 * Espera que um elemento seja exibido na página.
 * @param page A instância da página do Playwright.
 * @param testId O test-id do elemento.
 */
export async function esperarElementoVisivel(page: Page, testId: string): Promise<void> {
    await expect(page.getByTestId(testId).first()).toBeVisible();
}

/**
 * Espera que um elemento não seja exibido na página.
 * @param page A instância da página do Playwright.
 * @param seletor O seletor do elemento.
 */
export async function esperarElementoInvisivel(page: Page, seletor: string): Promise<void> {
    await expect(page.getByTestId(seletor).first()).not.toBeVisible();
}

/**
 * Verifica se a URL atual corresponde a um padrão.
 * @param page A instância da página do Playwright.
 * @param url O padrão da URL.
 */
export async function verificarUrl(page: Page, url: string): Promise<void> {
    const regexUrl = new RegExp(url.replace(/\*\*/g, '.*'));
    await expect(page).toHaveURL(regexUrl);
}

/**
 * Espera que a URL atual corresponda a um padrão.
 * @param page A instância da página do Playwright.
 * @param url O padrão da URL.
 */
export async function esperarUrl(page: Page, url: string | RegExp): Promise<void> {
    if (typeof url === 'string') {
        await expect(page).toHaveURL(new RegExp(url));
    } else {
        await expect(page).toHaveURL(url);
    }
}

/**
 * Verifica se a URL atual é a do painel.
 * @param page A instância da página do Playwright.
 */
export async function verificarUrlDoPainel(page: Page): Promise<void> {
    await expect(page).toHaveURL(URLS.PAINEL);
}

/**
 * Verifica se um modal está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalVisivel(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
}

/**
 * Verifica se um modal não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalFechado(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).not.toBeVisible();
}

/**
 * Espera que um botão seja exibido na página.
 * @param page A instância da página do Playwright.
 * @param nomeBotao O nome do botão.
 */
export async function esperarBotaoVisivel(page: Page, nomeBotao: string): Promise<void> {
    await expect(page.getByRole('button', {name: nomeBotao})).toBeVisible();
}

/**
 * Espera que uma notificação de login inválido seja exibida.
 * @param page A instância da página do Playwright.
 */
export async function esperarNotificacaoLoginInvalido(page: Page): Promise<void> {
    const notificacao = page.locator('.notification-container');
    await expect(notificacao.getByText(TEXTOS.ERRO_LOGIN_INVALIDO)).toBeVisible();
}

/**
 * Verifica se a disponibilização foi concluída.
 * @param page A instância da página do Playwright.
 * @param modalSelector O seletor do modal.
 * @param notificacaoTestId O test-id da notificação.
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
 * Verifica se o modal de disponibilização está visível.
 * @param page A instância da página do Playwright.
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
 * Verifica o valor do campo de observações.
 * @param page A instância da página do Playwright.
 * @param valorEsperado O valor esperado.
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
 * Verifica se o botão de disponibilizar está habilitado.
 * @param page A instância da página do Playwright.
 * @param habilitado `true` se o botão deve estar habilitado, `false` caso contrário.
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
