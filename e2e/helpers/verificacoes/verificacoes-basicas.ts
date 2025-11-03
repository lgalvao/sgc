import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS, URLS} from '../dados';

/**
 * Espera que uma mensagem de sucesso seja exibida.
 * @param page A instância da página do Playwright.
 * @param mensagem A mensagem a ser esperada.
 */
export async function esperarMensagemSucesso(page: Page, mensagem: string): Promise<void> {
    const seletorNotificacao = `[data-testid^="notificacao-"]`;
    const notificacao = page.locator(seletorNotificacao, {hasText: mensagem});
    try {
        await notificacao.first().waitFor({state: 'visible'});
    } catch {
        const parcial = mensagem.split(' ').slice(0, 3).join(' ');
        const alternativa = page.locator(seletorNotificacao, {hasText: parcial});
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
    const alerta = page.locator('[data-testid="notificacao-warning"]', { hasText: texto });
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
 * Verifica se a aplicação navegou para a página do painel.
 * @param page A instância da página do Playwright.
 */
export async function verificarNavegacaoParaPainel(page: Page): Promise<void> {
    await page.waitForURL('/painel', { timeout: 15000 });
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
    const notificacao = page.locator('[data-testid="notificacao-error"]');
    await expect(notificacao.getByText(TEXTOS.ERRO_LOGIN_INVALIDO)).toBeVisible();
}

/**
 * Verifica se a disponibilização foi concluída.
 * @param page A instância da página do Playwright.
 * @param modalSelector O seletor do modal.
 * @param notificacaoTestId O test-id da notificação.
 */
export async function verificarDisponibilizacaoConcluida(page: Page, modalTestId: string = 'disponibilizar-modal', notificacaoTestId: string = 'notificacao-disponibilizacao'): Promise<void> {
    const modal = page.getByTestId(modalTestId);
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
    await expect(page.getByTestId('disponibilizar-modal')).toBeVisible();
}

/**
 * Verifica o valor do campo de observações.
 * @param page A instância da página do Playwright.
 * @param valorEsperado O valor esperado.
 */
export async function verificarCampoObservacoesValor(page: Page, valorEsperado: string): Promise<void> {
    await expect(page.getByTestId('input-observacoes-disponibilizacao')).toHaveValue(valorEsperado);
}

/**
 * Verifica se o botão de disponibilizar está habilitado.
 * @param page A instância da página do Playwright.
 * @param habilitado `true` se o botão deve estar habilitado, `false` caso contrário.
 */
export async function verificarBotaoDisponibilizarHabilitado(page: Page, habilitado: boolean = true): Promise<void> {
    const botao = page.getByTestId('btn-disponibilizar');
    if (habilitado) {
        await expect(botao).toBeEnabled();
    } else {
        await expect(botao).toBeDisabled();
    }
}
