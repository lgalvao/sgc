import {expect, type Page} from '@playwright/test';

/**
 * Helpers para navegação e verificação de páginas nos testes E2E.
 * 
 * Este arquivo centraliza funções comuns de navegação que eram duplicadas
 * em múltiplos arquivos de teste.
 */

// ============================================================================
// Funções de Logout
// ============================================================================

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

    // Wait for the button to be stable and visible
    const btnLogout = page.getByTestId('btn-logout');
    await expect(btnLogout).toBeVisible();
    await btnLogout.click();

    await expect(page).toHaveURL(/\/login/);
}

// ============================================================================
// Funções de Verificação de Página
// ============================================================================

/**
 * Verifica que está na página do painel principal.
 */
export async function verificarPaginaPainel(page: Page): Promise<void> {
    await expect(page).toHaveURL(/\/painel/);
}

/**
 * Verifica que está na página de um subprocesso.
 * @param page - Instância da página do Playwright
 * @param unidade - Se fornecido, valida que a URL contém a sigla da unidade
 */
export async function verificarPaginaSubprocesso(
    page: Page,
    unidade?: string
): Promise<void> {
    const regex = unidade
        ? new RegExp(String.raw`/processo/\d+/${unidade}$`)
        : /\/processo\/\d+\/\w+$/;
    await expect(page).toHaveURL(regex);
}

// ============================================================================
// Funções de Navegação
// ============================================================================

/**
 * Navega para um subprocesso clicando na célula da unidade na tabela TreeTable.
 * Este é o padrão correto para navegar: clicar na célula, não na linha.
 * 
 * @param page - Instância da página do Playwright
 * @param siglaUnidade - Sigla da unidade a clicar (ex: 'SECAO_221')
 */
export async function navegarParaSubprocesso(
    page: Page,
    siglaUnidade: string
): Promise<void> {
    // Aguardar o cabeçalho do processo (v-if="processo") para garantir carregamento inicial
    await expect(page.getByText('Carregando detalhes do processo...').first()).toBeHidden();
    await expect(page.getByTestId('processo-info')).toBeVisible();

    const tabela = page.getByTestId('tbl-tree');
    await expect(tabela).toBeVisible();
    
    const celula = tabela.getByRole('cell', {name: siglaUnidade}).first();
    await expect(celula).toBeVisible();
    await celula.click();
    
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`));
}
