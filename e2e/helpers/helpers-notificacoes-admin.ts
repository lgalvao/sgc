import {expect, type Locator, type Page} from '@playwright/test';

export interface CriteriosNotificacaoAdmin {
    assunto: string | RegExp;
    destinatario?: string | RegExp;
    tipo?: string | RegExp;
    situacao?: string | RegExp;
    trechoCorpo?: string | RegExp;
}

export async function abrirNotificacoesAdmin(page: Page): Promise<Locator> {
    const linkNotificacoes = page.getByTestId('nav-link-notificacoes');

    if (!(await linkNotificacoes.isVisible())) {
        const botaoNavbar = page.locator('.navbar-toggler:visible').first();
        await expect(botaoNavbar).toBeVisible();
        await botaoNavbar.click();
        await expect(linkNotificacoes).toBeVisible();
    }

    await linkNotificacoes.click();
    await expect(page).toHaveURL(/\/administracao\/notificacoes/);

    const tabela = page.getByTestId('tbl-notificacoes');
    await expect(tabela).toBeVisible();
    await page.getByTestId('btn-notificacoes-atualizar').click();
    await expect(tabela).toBeVisible();
    return tabela;
}

export async function verificarNotificacaoAdmin(page: Page, criterios: CriteriosNotificacaoAdmin): Promise<void> {
    const tabela = await abrirNotificacoesAdmin(page);

    let linha = tabela.locator('tbody tr');
    linha = linha.filter({hasText: criterios.assunto});
    if (criterios.destinatario) linha = linha.filter({hasText: criterios.destinatario});
    if (criterios.tipo) linha = linha.filter({hasText: criterios.tipo});
    if (criterios.situacao) linha = linha.filter({hasText: criterios.situacao});

    const linhaEncontrada = linha.first();
    await expect(linhaEncontrada).toBeVisible();

    if (!criterios.trechoCorpo) return;

    await linhaEncontrada.locator('[data-testid^="btn-preview-"]').first().click();
    const modal = page.getByTestId('modal-preview-email');
    await expect(modal).toBeVisible();
    const iframe = modal.frameLocator('[data-testid="iframe-preview-email"]');
    await expect(iframe.locator('body')).toContainText(criterios.trechoCorpo);
    await page.getByTestId('btn-fechar-preview-email').click();
    await expect(modal).toBeHidden();
}
