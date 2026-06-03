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
        if ((await selectImportancia.nth(i).inputValue()) === '3') continue;
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao`) && res.request().method() === 'POST' && res.ok()
            ),
            selectImportancia.nth(i).selectOption('3')
        ]);
    }

    const selectDominio = page.locator('[data-testid^="autoavaliacao-dominio-"]');
    for (let i = 0; i < total; i++) {
        if ((await selectDominio.nth(i).inputValue()) === '4') continue;
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(`/api/diagnosticos/subprocessos/${codSubprocesso}/autoavaliacao`) && res.request().method() === 'POST' && res.ok()
            ),
            selectDominio.nth(i).selectOption('4')
        ]);
    }

    await expect.poll(async () => await page.evaluate(async (codigo) => {
        const resposta = await fetch(`/api/diagnosticos/subprocessos/${codigo}/autoavaliacao`, {credentials: 'include'});
        if (!resposta.ok) return false;
        const dados = await resposta.json();
        return dados.competencias.every((item: {importancia: number | null; dominio: number | null}) =>
            item.importancia === 3 && item.dominio === 4
        );
    }, codSubprocesso)).toBe(true);
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
            if ((await itens.nth(i).inputValue()) === '4') continue;
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

    await expect.poll(async () => await page.evaluate(async ({codigo, titulo}) => {
        const resposta = await fetch(`/api/diagnosticos/subprocessos/${codigo}/consenso/${titulo}`, {credentials: 'include'});
        if (!resposta.ok) return false;
        const dados = await resposta.json();
        const itens = dados.competenciasDetalhadas ?? [];
        return itens.length > 0 && itens.every((item: {
            chefiaImportancia: number | null;
            chefiaDominio: number | null;
            consensoImportancia: number | null;
            consensoDominio: number | null;
        }) =>
            item.chefiaImportancia === 4
            && item.chefiaDominio === 4
            && item.consensoImportancia === 4
            && item.consensoDominio === 4
        );
    }, {codigo: codSubprocesso, titulo: servidorTitulo})).toBe(true);
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

    await expect(page.getByText('Salvo automaticamente')).toBeVisible();
}
