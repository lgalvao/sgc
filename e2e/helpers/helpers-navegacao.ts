import {expect, type Page} from '@playwright/test';

/**
 * Helpers para navegação e verificação de páginas nos testes E2E.
 *
 * Este arquivo centraliza funções comuns de navegação que eram duplicadas
 * em múltiplos arquivos de teste.
 */

/**
 * Limpa toasts (notificações temporárias) da tela.
 * Fecha cada toast visível clicando no seu botão "X".
 */
export async function limparNotificacoes(page: Page): Promise<void> {
    // Localizadores para os botões de fechar dos toasts (incluindo BOrchestrator do bootstrap-vue-next)
    const locButtons = [
        page.locator('.toast .btn-close'),
        page.locator('.orchestrator-container .btn-close'),
        page.locator('button[aria-label="Close"]')
    ];

    for (const locator of locButtons) {
        try {
            const buttons = await locator.all();
            for (const btn of buttons) {
                if (await btn.isVisible()) {
                    await btn.click().catch(() => {});
                }
            }
        } catch (e) {
            // Ignora erros de seleção
        }
    }
}

/**
 * Faz logout do sistema clicando no link "Sair".
 */
export async function fazerLogout(page: Page): Promise<void> {
    await page.getByTestId('btn-logout').locator('a').click();
    await expect(page).toHaveURL(/\/login/);
}

/**
 * Verifica que está na página do painel principal.
 */
export async function verificarPaginaPainel(page: Page): Promise<void> {
    await expect(page).toHaveURL(/\/painel/);
}

/**
 * Navega para um subprocesso clicando na célula da unidade na tabela TreeTable.
 * Se já estiver na página do subprocesso (redirecionamento direto), apenas valida.
 */
export async function navegarParaSubprocesso(
    page: Page,
    siglaUnidade: string
): Promise<void> {
    const urlSubprocesso = new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`);
    if (urlSubprocesso.test(page.url())) return;

    await expect(page.getByText('Carregando detalhes do processo...').first()).toBeHidden();
    await expect(page.getByTestId('processo-info')).toBeVisible();

    const tabela = page.getByTestId('tbl-tree');
    await expect(tabela).toBeVisible();

    const celula = tabela.getByRole('cell', {name: siglaUnidade}).first();
    await expect(celula).toBeVisible();
    await celula.click();

    await expect(page).toHaveURL(urlSubprocesso);
}
