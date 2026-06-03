import {expect, type Page} from '@playwright/test';

export async function abrirCardDiagnostico(page: Page, testId: string, urlRegex: RegExp): Promise<void> {
    const card = page.getByTestId(testId);
    await expect(card).toBeVisible();
    await card.click();
    await expect(page).toHaveURL(urlRegex);
}

export async function preencherAutoavaliacaoCompleta(page: Page, codSubprocesso: number): Promise<void> {
    const selectImportancia = page.locator('[data-testid^="autoavaliacao-importancia-"]');
    const total = await selectImportancia.count();
    await expect(selectImportancia.first()).toBeVisible();

    for (let i = 0; i < total; i++) {
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao`) && res.request().method() === 'POST' && res.ok()
            ),
            selectImportancia.nth(i).selectOption('3')
        ]);
    }

    const selectDominio = page.locator('[data-testid^="autoavaliacao-dominio-"]');
    for (let i = 0; i < total; i++) {
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao`) && res.request().method() === 'POST' && res.ok()
            ),
            selectDominio.nth(i).selectOption('4')
        ]);
    }
}

export async function preencherConsensoMinimo(
    page: Page,
    codSubprocesso: number,
    servidorTitulo: string
): Promise<void> {
    const campos = [
        '[data-testid^="consenso-chefia-importancia-"]',
        '[data-testid^="consenso-chefia-dominio-"]',
        '[data-testid^="consenso-final-importancia-"]',
        '[data-testid^="consenso-final-dominio-"]'
    ];

    for (const seletor of campos) {
        const itens = page.locator(seletor);
        const total = await itens.count();
        await expect(itens.first()).toBeVisible();
        for (let i = 0; i < total; i++) {
            await Promise.all([
                page.waitForResponse(res =>
                    res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/consenso/${servidorTitulo}`)
                    && res.request().method() === 'POST'
                    && res.ok()
                ),
                itens.nth(i).selectOption('4')
            ]);
        }
    }
}

export async function preencherPrimeiraSituacaoCapacitacao(page: Page, codSubprocesso: number, valor = 'EC'): Promise<void> {
    const select = page.locator('[data-testid^="ocupacao-"]').first();
    await expect(select).toBeVisible();
    await Promise.all([
        page.waitForResponse(res =>
            res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/ocupacoes-criticas`)
            && res.request().method() === 'POST'
            && res.ok()
        ),
        select.selectOption(valor)
    ]);
}
