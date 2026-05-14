import {expect, type Locator, type Page} from '@playwright/test';

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
    try {
        // BOrchestrator renderiza toasts como role="alert" ou .toast
        // Procuramos botões de fechar (X) em toasts, alertas ou no orchestrator
        const closeButtons = page.locator('.toast .btn-close, .orchestrator-container .btn-close, [role="alert"] .btn-close, .alert .btn-close, button[aria-label="Close"]');

        // Os toasts podem desaparecer ou reordenar durante a iteração.
        // Reconsultamos sempre o primeiro botão para evitar índices obsoletos.
        while (await closeButtons.count() > 0) {
            const btn = closeButtons.first();
            if (!(await btn.isVisible())) {
                break;
            }

            try {
                await btn.click({timeout: 500});
            } catch (e: any) {
                const mensagem = (e.message ?? '').toLowerCase();
                if (
                    mensagem.includes('not visible')
                    || mensagem.includes('not attached')
                    || mensagem.includes('detached from the dom')
                    || mensagem.includes('timeout')
                ) {
                    continue;
                }
                throw e;
            }
        }
    } catch (e: any) {
        // Ignorar erros se a página ou contexto foram fechados durante a limpeza (comum em timeouts)
        if (e.message?.includes('closed') || e.message?.includes('Target page, context or browser has been closed')) {
            return;
        }
        throw e;
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
    try {
        if (/\/login(?:\?.*)?$/.test(page.url())) {
            return;
        }

        // Limpar notificações que possam estar sobrepondo o menu ou botões
        await limparNotificacoes(page);

        const candidatosLogout = [
            page.getByTestId('btn-logout'),
            page.getByTitle('Sair'),
            page.getByRole('link', {name: /^sair$/i}),
            page.getByRole('button', {name: /^sair$/i})
        ];

        let botaoLogout: Locator | null = null;
        for (const candidato of candidatosLogout) {
            if (await candidato.count() > 0 && await candidato.first().isVisible()) {
                botaoLogout = candidato.first();
                break;
            }
        }

        if (!botaoLogout) {
            await page.waitForURL(/\/login(?:\?.*)?$/, {timeout: 2_000}).catch(() => null);
            if (/\/login(?:\?.*)?$/.test(page.url())) {
                return;
            }
            throw new Error('Botão de logout não encontrado na navegação atual.');
        }

        await botaoLogout.scrollIntoViewIfNeeded();

        try {
            await botaoLogout.click({timeout: 2_000});
        } catch (e: any) {
            const mensagem = (e.message ?? '').toLowerCase();
            if (
                mensagem.includes('intercepts pointer events')
                || mensagem.includes('another element would receive the click')
                || mensagem.includes('timeout')
            ) {
                await limparNotificacoes(page);
                await botaoLogout.click({force: true, timeout: 2_000});
            } else {
                throw e;
            }
        }
        await page.waitForURL(/\/login/);

        // Limpar possíveis toasts de "Não autorizado" ou "Sessão expirada" que aparecem no teardown após o logout
        await limparNotificacoes(page);
    } catch (e: any) {
        if (e.message?.includes('closed') || e.message?.includes('Target page, context or browser has been closed')) {
            return;
        }
        throw e;
    }
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
    const pattern = codigo
        ? String.raw`\/processo\/(?:cadastro\?codProcesso=)?${codigo}(?:\?.*)?$`
        : String.raw`\/processo\/(?:cadastro\?codProcesso=)?\d+(?:\?.*)?$`;
    await page.waitForURL(new RegExp(pattern));
}


/**
 * Aguarda a navegação para a página de detalhes de um subprocesso.
 */
export async function esperarPaginaSubprocesso(page: Page, siglaUnidade?: string): Promise<void> {
    const regex = siglaUnidade
        ? new RegExp(String.raw`\/processo\/\d+\/${siglaUnidade}(?:\?.*)?$`)
        : /\/processo\/\d+\/[A-Z0-9_]+(?:\?.*)?$/;
    await page.waitForURL(regex);
}

/**
 * Navega para um subprocesso a partir da tela de detalhes do processo.
 * Suporta tanto a árvore de subprocessos quanto a tabela simples exibida em alguns perfis/fluxos.
 * Se já estiver na página do subprocesso (redirecionamento direto), apenas valida.
 */
export async function navegarParaSubprocesso(
    page: Page,
    siglaUnidade: string
): Promise<void> {
    // Aguardar qualquer transição de rota antes de checar a URL
    await page.waitForURL(/\/processo\/\d+/);

    const urlSubprocesso = new RegExp(String.raw`/processo/\d+/${siglaUnidade}(?:\?.*)?$`);
    if (urlSubprocesso.test(page.url())) return;

    const info = page.getByTestId('processo-info');
    await expect(info).toBeVisible();

    const padraoUnidade = new RegExp(String.raw`^${siglaUnidade}\b`, 'i');
    const tabelaArvore = page.getByTestId('tbl-tree');
    if (await tabelaArvore.count() > 0 && await tabelaArvore.isVisible()) {
        const celula = tabelaArvore.getByRole('cell', {name: padraoUnidade}).first();
        await expect(celula).toBeVisible();
        await celula.click();
        await expect(page).toHaveURL(urlSubprocesso);
        return;
    }

    const tabelaProcessos = page.getByTestId('tbl-processos');
    if (await tabelaProcessos.count() > 0 && await tabelaProcessos.isVisible()) {
        const linhaProcesso = tabelaProcessos.locator('tr').filter({hasText: padraoUnidade}).first();
        await expect(linhaProcesso).toBeVisible();
        await linhaProcesso.click();
        await expect(page).toHaveURL(urlSubprocesso);
        return;
    }

    const linhaGenerica = page.locator('main table tr').filter({hasText: padraoUnidade}).first();
    await expect(linhaGenerica).toBeVisible();
    await linhaGenerica.click();

    await expect(page).toHaveURL(urlSubprocesso);
}

export async function obterAcaoCabecalhoSubprocesso(page: Page, testIdAcao: string) {
    const dropdown = page.getByTestId('btn-subprocesso-acoes');
    await expect(dropdown).toBeVisible();
    await dropdown.click();

    const acao = page.locator(`[data-testid="${testIdAcao}"]:visible`).first();
    await expect(acao).toBeVisible();
    return acao;
}
