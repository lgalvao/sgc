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
    // BOrchestrator renderiza toasts como role="alert" dentro de .orchestrator-container
    const closeButtons = page.locator('.toast .btn-close, .orchestrator-container .btn-close, [role="alert"] .btn-close');
    const count = await closeButtons.count();
    for (let i = 0; i < count; i++) {
        const btn = closeButtons.nth(i);
        if (await btn.isVisible()) {
            await btn.click().catch(() => {}); // Toast pode auto-fechar entre isVisible e click
        }
    }
}

/**
 * Verifica toast exibido pelo BOrchestrator (notificação transitória).
 * Use em fluxos com navegação após ação mutante.
 */
export async function verificarToast(page: Page, mensagem?: string | RegExp) {
    const toast = page.locator('.orchestrator-container .toast').first();
    await expect(toast).toBeVisible();
    if (mensagem) {
        await expect(toast).toContainText(mensagem);
    }
}

/**
 * Verifica alerta inline do componente AppAlert.
 * Use em fluxos sem navegação (erros/avisos persistentes na própria tela).
 */
export async function verificarAppAlert(page: Page, mensagem?: string | RegExp): Promise<void> {
    const alerta = page.getByTestId('app-alert').first();
    await expect(alerta).toBeVisible();
    if (mensagem) {
        await expect(alerta).toContainText(mensagem);
    }
}

/**
 * Verifica mensagem persistente na tabela de alertas do painel.
 */
export async function verificarAlertaPainel(page: Page, mensagem: string | RegExp): Promise<void> {
    const tabelaAlertas = page.getByTestId('tbl-alertas');
    await expect(tabelaAlertas).toBeVisible();
    await expect(tabelaAlertas).toContainText(mensagem);
}

/**
 * Faz logout do sistema clicando no link "Sair".
 */
export async function fazerLogout(page: Page): Promise<void> {
    await page.keyboard.press('Escape');
    // Disparar click via JS para evitar bloqueio por toast sobreposto
    await page.getByTestId('btn-logout').locator('a').dispatchEvent('click');
    await expect(page).toHaveURL(/\/login/);

    // Limpar possíveis toasts de "Não autorizado" ou "Sessão expirada" que aparecem no teardown
    await limparNotificacoes(page);
}

/**
 * Verifica que está na página do painel principal.
 */
export async function verificarPaginaPainel(page: Page): Promise<void> {
    await expect(page).toHaveURL(/\/painel/);
}

/**
 * Aguarda a navegação para a página de painel.
 */
export async function esperarPaginaPainel(page: Page): Promise<void> {
    await page.waitForURL(/\/painel/);
}

/**
 * Aguarda a navegação para a página de cadastro de processo (novo ou edição).
 */
export async function esperarPaginaCadastroProcesso(page: Page): Promise<void> {
    await page.waitForURL(/\/processo\/cadastro/);
}

/**
 * Aguarda a navegação para a página de detalhes de um processo.
 */
export async function esperarPaginaDetalhesProcesso(page: Page, codigo?: number): Promise<void> {
    const regex = codigo ? new RegExp(String.raw`\/processo\/${codigo}$`) : /\/processo\/\d+$/;
    await page.waitForURL(regex);
}

/**
 * Aguarda a navegação para a página de detalhes de um subprocesso.
 */
export async function esperarPaginaSubprocesso(page: Page, siglaUnidade?: string): Promise<void> {
    const regex = siglaUnidade 
        ? new RegExp(String.raw`\/processo\/\d+\/${siglaUnidade}$`) 
        : /\/processo\/\d+\/[A-Z0-9_]+$/;
    await page.waitForURL(regex);
}

/**
 * Navega para um subprocesso clicando na célula da unidade na tabela TreeTable.
 * Se já estiver na página do subprocesso (redirecionamento direto), apenas valida.
 */
export async function navegarParaSubprocesso(
    page: Page,
    siglaUnidade: string
): Promise<void> {
    // Aguardar qualquer transição de rota antes de checar a URL
    await page.waitForURL(/\/processo\/\d+/);

    const urlSubprocesso = new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`);
    if (urlSubprocesso.test(page.url())) return;

    await expect(page.getByText('Carregando detalhes do processo...').first()).toBeHidden();
    const info = page.getByTestId('processo-info');
    await expect(info).toBeVisible();

    const tabela = page.getByTestId('tbl-tree');
    await expect(tabela).toBeVisible();

    const celula = tabela.getByRole('cell', {name: new RegExp(`^${siglaUnidade}\\b`)}).first();
    await expect(celula).toBeVisible();
    await celula.click();

    await expect(page).toHaveURL(urlSubprocesso);
}
