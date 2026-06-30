import {expect, type Page} from '@playwright/test';
import {TEXTOS} from '../../frontend/src/constants/textos.js';

function caminhoDiagnosticoApi(codSubprocesso: number, sufixo: string): string {
    return `/api/subprocessos/${codSubprocesso}/diagnostico${sufixo}`;
}

export async function abrirCardDiagnostico(page: Page, testId: string, urlRegex: RegExp): Promise<void> {
    const card = page.getByTestId(testId).first();
    await expect(card).toBeVisible();
    await card.click();
    await expect(page).toHaveURL(urlRegex);
}

export async function buscarCodSubprocessoDiagnostico(
    page: Page,
    codProcesso: number,
    siglaUnidade: string
): Promise<number> {
    const codigo = await page.evaluate(async ({codProcessoAtual, siglaUnidadeAtual}) => {
        const url = new URL('/api/subprocessos/contexto-edicao/buscar', window.location.origin);
        url.searchParams.set('codProcesso', String(codProcessoAtual));
        url.searchParams.set('siglaUnidade', siglaUnidadeAtual);
        const resposta = await fetch(url.toString(), {credentials: 'include'});
        if (!resposta.ok) {
            return null;
        }
        const contexto = await resposta.json() as {
            detalhes?: {
                subprocesso?: {
                    codigo?: number;
                };
            };
        };
        return contexto.detalhes?.subprocesso?.codigo ?? null;
    }, {
        codProcessoAtual: codProcesso,
        siglaUnidadeAtual: siglaUnidade,
    });
    expect(codigo).toBeTruthy();
    return Number(codigo);
}

export async function preencherAutoavaliacaoCompleta(page: Page, codSubprocesso: number): Promise<void> {
    const selectImportancia = page.locator('[data-testid^="autoavaliacao-importancia-"]');
    const total = await selectImportancia.count();
    await expect(selectImportancia.first()).toBeVisible();

    for (let i = 0; i < total; i++) {
        if ((await selectImportancia.nth(i).inputValue()) === '3') continue;
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(caminhoDiagnosticoApi(codSubprocesso, '/autoavaliacao')) && res.request().method() === 'POST' && res.ok()
            ),
            selectImportancia.nth(i).selectOption('3')
        ]);
    }

    const selectDominio = page.locator('[data-testid^="autoavaliacao-dominio-"]');
    for (let i = 0; i < total; i++) {
        if ((await selectDominio.nth(i).inputValue()) === '4') continue;
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(caminhoDiagnosticoApi(codSubprocesso, '/autoavaliacao')) && res.request().method() === 'POST' && res.ok()
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
        if (total === 0) continue;
        await expect(itens.first()).toBeVisible();
        for (let i = 0; i < total; i++) {
            if ((await itens.nth(i).inputValue()) === '4') continue;
            await Promise.all([
                page.waitForResponse(res =>
                    res.url().includes(caminhoDiagnosticoApi(codSubprocesso, `/consenso/${servidorTitulo}`))
                    && res.request().method() === 'POST'
                    && res.ok()
                ),
                itens.nth(i).selectOption('4')
            ]);
        }
    }
}

export async function abrirAcaoConsensoDiagnostico(page: Page, servidorTitulo: string): Promise<void> {
    const cardConsenso = page.getByTestId('card-subprocesso-consenso');
    if (await cardConsenso.count() > 0 && await cardConsenso.first().isVisible()) {
        await cardConsenso.first().click();
        return;
    }

    const dropdownAcoes = page.getByTestId(`dropdown-acoes-${servidorTitulo}`);
    await expect(dropdownAcoes).toBeVisible();
    await dropdownAcoes.getByRole('button', {name: 'Ações'}).click();
    const botaoManterConsenso = page.getByTestId(`btn-manter-consenso-${servidorTitulo}`);
    await expect(botaoManterConsenso).toBeVisible();
    await botaoManterConsenso.click();
}

export async function navegarParaConsensoDiagnostico(page: Page, servidorTitulo: string): Promise<void> {
    if (page.url().includes('/consenso/')) {
        return;
    }

    const cardConsenso = page.getByTestId('card-subprocesso-consenso');
    if (await cardConsenso.count() > 0 && await cardConsenso.first().isVisible()) {
        await cardConsenso.first().click();
        return;
    }

    const botaoManterConsenso = page.getByTestId(`btn-manter-consenso-${servidorTitulo}`);
    if (!(await botaoManterConsenso.isVisible())) {
        await abrirAcaoConsensoDiagnostico(page, servidorTitulo);
        return;
    }
    await botaoManterConsenso.click();
}

export async function abrirAcaoCapacitacaoDiagnostico(page: Page): Promise<void> {
    const card = page.getByTestId('card-subprocesso-situacoes-capacitacao');
    await expect(card).toBeVisible();
    await card.click();
}

export async function aprovarConsensoDiagnostico(page: Page, codSubprocesso: number): Promise<void> {
    const botaoAprovar = page.getByTestId('btn-aprovar-consenso');
    await expect(botaoAprovar).toBeVisible();
    await expect(botaoAprovar).toBeEnabled();
    await botaoAprovar.click();

    const botaoConfirmar = page.getByTestId('btn-confirmar-aprovar-consenso');
    await expect(botaoConfirmar).toBeVisible();
    await Promise.all([
        page.waitForResponse(res =>
            res.url().includes(caminhoDiagnosticoApi(codSubprocesso, '/consenso/aprovar'))
            && res.request().method() === 'POST'
            && res.ok()
        ),
        botaoConfirmar.click()
    ]);
}

export async function concluirConsensoDiagnostico(page: Page, codSubprocesso: number, servidorTitulo: string): Promise<void> {
    const botaoConcluir = page.getByTestId('btn-concluir-avaliacao');
    await expect(botaoConcluir).toBeVisible();
    await expect(botaoConcluir).toBeEnabled();
    await botaoConcluir.click();

    const dialogo = page.getByRole('dialog');
    await expect(dialogo).toContainText(TEXTOS.diagnostico.MODAL_CONCLUIR_CONSENSO_MENSAGEM);

    await Promise.all([
        page.waitForResponse(res =>
            res.url().includes(caminhoDiagnosticoApi(codSubprocesso, `/consenso/${encodeURIComponent(servidorTitulo)}/concluir`))
            && res.request().method() === 'POST'
            && res.ok()
        ),
        page.getByTestId('btn-confirmar-concluir').click()
    ]);
}

export async function preencherPrimeiraSituacaoCapacitacao(page: Page, codSubprocesso: number, valor = 'EC'): Promise<void> {
    const botaoServidorAprovado = page.locator(
        '[data-testid^="btn-selecionar-servidor-situacao-capacitacao-"]',
        {hasText: 'Avaliação de consenso aprovada'}
    ).first();
    if (await botaoServidorAprovado.count() > 0) {
        await expect(botaoServidorAprovado).toBeVisible();
        await botaoServidorAprovado.click();
        await expect(page.getByTestId('detalhes-servidor-situacao-capacitacao')).toBeVisible();
    } else {
        const botaoSelecionarServidor = page.locator('[data-testid^="btn-selecionar-servidor-situacao-capacitacao-"]').first();
        if (await botaoSelecionarServidor.count() > 0) {
            await expect(botaoSelecionarServidor).toBeVisible();
            await botaoSelecionarServidor.click();
            await expect(page.getByTestId('detalhes-servidor-situacao-capacitacao')).toBeVisible();
        }
    }
    const select = page.locator('[data-testid^="situacao-"]').first();
    await expect(select).toBeVisible();
    await Promise.all([
        page.waitForResponse(res =>
            res.url().includes(caminhoDiagnosticoApi(codSubprocesso, '/situacoes-capacitacao'))
            && res.request().method() === 'POST'
            && res.ok()
        ),
        select.selectOption(valor)
    ]);
}

export async function preencherTodasSituacoesCapacitacao(page: Page, codSubprocesso: number, valor = 'EC'): Promise<void> {
    const selects = page.locator('[data-testid^="situacao-"]');
    const total = await selects.count();
    let houveAlteracao = false;
    await expect(selects.first()).toBeVisible();

    for (let i = 0; i < total; i++) {
        if ((await selects.nth(i).inputValue()) === valor) {
            continue;
        }
        houveAlteracao = true;
        await Promise.all([
            page.waitForResponse(res =>
                res.url().includes(caminhoDiagnosticoApi(codSubprocesso, '/situacoes-capacitacao'))
                && res.request().method() === 'POST'
                && res.ok()
            ),
            selects.nth(i).selectOption(valor)
        ]);
    }

    void houveAlteracao;
}

export async function preencherSituacoesCapacitacaoPendentesPelaTela(page: Page, codSubprocesso: number, valor = 'EC'): Promise<void> {
    const botoesServidores = page.locator('[data-testid^="btn-selecionar-servidor-situacao-capacitacao-"]');
    const totalServidores = await botoesServidores.count();
    await expect(botoesServidores.first()).toBeVisible();

    let encontrouServidorComCapacitacaoEditavel = false;
    for (let i = 0; i < totalServidores; i++) {
        const botaoServidor = botoesServidores.nth(i);
        await botaoServidor.click();
        await expect(page.getByTestId('detalhes-servidor-situacao-capacitacao')).toBeVisible();

        const selects = page.locator('[data-testid^="situacao-"]');
        const totalSelects = await selects.count();
        if (totalSelects === 0) {
            await expect(page.getByText('Aguardando aprovação de consenso', {exact: true})).toBeVisible();
            continue;
        }

        encontrouServidorComCapacitacaoEditavel = true;
        for (let j = 0; j < totalSelects; j++) {
            if ((await selects.nth(j).inputValue()) === valor) {
                continue;
            }
            await Promise.all([
                page.waitForResponse(res =>
                    res.url().includes(caminhoDiagnosticoApi(codSubprocesso, '/situacoes-capacitacao'))
                    && res.request().method() === 'POST'
                    && res.ok()
                ),
                selects.nth(j).selectOption(valor)
            ]);
        }
    }

    expect(encontrouServidorComCapacitacaoEditavel).toBeTruthy();
}
