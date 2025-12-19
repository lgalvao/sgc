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
    await page.getByTestId('btn-logout').click();
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
