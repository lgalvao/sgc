import {expect, type Page} from '@playwright/test';

/**
 * Helpers para navegação e verificação de páginas nos testes E2E.
 *
 * Este arquivo centraliza funções comuns de navegação que eram duplicadas
 * em múltiplos arquivos de teste.
 */

// Funções de Logout

/**
 * Limpa notificações (toasts e alertas) da tela.
 * Tenta clicar no botão de fechar (.btn-close) ou pressionar ESC.
 * Essencial para evitar que toasts interceptem cliques em botões.
 */
export async function limparNotificacoes(page: Page): Promise<void> {
    const notificacao = page.locator('.toast, [data-testid="global-alert"]');

    if (await notificacao.count() > 0) {
        // Tentar clicar no botão de fechar se estiver visível
        const btnFechar = notificacao.locator('.btn-close').first();
        if (await btnFechar.isVisible()) {
            await btnFechar.click();
        } else {
            await page.keyboard.press('Escape');
        }
    }

    await notificacao.waitFor({state: 'hidden'}).catch(() => {
        // Ignorar se não sumir no tempo previsto
    });
}

/**
 * Faz logout do sistema e aguarda redirecionamento para página de login.
 * Limpa notificações antes de clicar para evitar intercepção.
 */
export async function fazerLogout(page: Page): Promise<void> {
    await limparNotificacoes(page);

    const btnLogout = page.getByTestId('btn-logout');
    const linkLogout = btnLogout.locator('a').first();
    if (await btnLogout.isVisible().catch(() => false)) {
        await linkLogout.click({force: true});
    }

    if (!page.url().endsWith("/login")) {
        await page.goto('/login');
    }

    await expect(page).toHaveURL(/\/login/);
}

// Funções de Verificação de Página

/**
 * Verifica que está na página do painel principal.
 */
export async function verificarPaginaPainel(page: Page): Promise<void> {
    await expect(page).toHaveURL(/\/painel/);
}

// Funções de Navegação

/**
 * Navega para um subprocesso clicando na célula da unidade na tabela TreeTable.
 * Se já estiver na página do subprocesso (redirecionamento direto), apenas valida.
 */
export async function navegarParaSubprocesso(
    page: Page,
    siglaUnidade: string
): Promise<void> {
    const urlSubprocesso = new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`);

    // Se já estivermos na URL do subprocesso, não precisamos navegar
    if (urlSubprocesso.test(page.url())) {
        return;
    }

    // Aguardar o cabeçalho do processo (v-if="processo") para garantir carregamento inicial
    await expect(page.getByText('Carregando detalhes do processo...').first()).toBeHidden();

    // Se ainda não estivermos na URL, verificamos se o redirecionamento está em curso
    try {
        await page.waitForURL(urlSubprocesso, {timeout: 1000});
        return;
    } catch (e) {
        // Se não redirecionou em 1s, prossegue com clique manual
    }

    await expect(page.getByTestId('processo-info')).toBeVisible();

    const tabela = page.getByTestId('tbl-tree');
    await expect(tabela).toBeVisible();

    const celula = tabela.getByRole('cell', {name: siglaUnidade}).first();
    await expect(celula).toBeVisible();
    await celula.click();

    await expect(page).toHaveURL(urlSubprocesso);
}
