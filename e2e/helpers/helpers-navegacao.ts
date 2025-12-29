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
 * Faz logout do sistema e aguarda redirecionamento para página de login.
 */
export async function fazerLogout(page: Page): Promise<void> {
    // Wait for the button to be stable and visible
    const btnLogout = page.getByTestId('btn-logout');
    await expect(btnLogout).toBeVisible();
    await btnLogout.click();

    // Explicitly wait for the login URL with a longer timeout
    await expect(page).toHaveURL(/\/login/, {timeout: 15000});
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
    const tabela = page.getByTestId('tbl-tree');
    await expect(tabela).toBeVisible();
    
    const celula = tabela.getByRole('cell', {name: siglaUnidade}).first();
    await expect(celula).toBeVisible();
    await celula.click();
    
    await expect(page).toHaveURL(new RegExp(`/processo/\\d+/${siglaUnidade}$`));
}
