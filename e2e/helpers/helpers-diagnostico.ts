import {expect, type Page} from '@playwright/test';

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

    await expect.poll(async () => await page.evaluate(async (codigo) => {
        const resposta = await fetch(`/api/subprocessos/${codigo}/diagnostico/autoavaliacao`, {credentials: 'include'});
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

    let alterouAlgumCampo = false;
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
            alterouAlgumCampo = true;
            break;
        }
        if (alterouAlgumCampo) break;
    }

    await expect.poll(async () => await page.evaluate(async ({codigo, titulo}) => {
        const resposta = await fetch(`/api/subprocessos/${codigo}/diagnostico/consenso/${titulo}`, {credentials: 'include'});
        if (!resposta.ok) return false;
        const dados = await resposta.json();
        return dados.situacaoServidor === 'CONSENSO_CRIADO';
    }, {codigo: codSubprocesso, titulo: servidorTitulo})).toBe(true);
}

export async function abrirAcaoConsensoDiagnostico(page: Page, servidorTitulo: string): Promise<void> {
    const dropdownAcoes = page.getByTestId(`dropdown-acoes-${servidorTitulo}`);
    await expect(dropdownAcoes).toBeVisible();
    await dropdownAcoes.getByRole('button', {name: 'Ações'}).click();
    await page.getByTestId(`btn-manter-consenso-${servidorTitulo}`).click();
}

export async function abrirAcaoCapacitacaoDiagnostico(page: Page): Promise<void> {
    const card = page.getByTestId('card-subprocesso-situacoes-capacitacao');
    await expect(card).toBeVisible();
    await card.click();
}

export async function aprovarConsensoDiagnostico(page: Page, codSubprocesso: number): Promise<void> {
    const botaoAprovar = page.getByTestId('btn-aprovar-consenso');
    await expect(botaoAprovar).toBeVisible();
    await Promise.all([
        page.waitForResponse(res =>
            res.url().includes(caminhoDiagnosticoApi(codSubprocesso, '/consenso/aprovar'))
            && res.request().method() === 'POST'
            && res.ok()
        ),
        botaoAprovar.click()
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

    if (houveAlteracao) {
        await page.waitForLoadState('networkidle');
    }
}

export async function preencherSituacoesCapacitacaoPendentesPorApi(page: Page, codSubprocesso: number, valor = 'EC'): Promise<void> {
    await page.evaluate(async ({codigo, valorAtual}) => {
        const respostaAtual = await fetch(`/api/subprocessos/${codigo}/diagnostico/unidade`, {credentials: 'include'});
        if (!respostaAtual.ok) {
            throw new Error(`Falha ao carregar situações de capacitação do subprocesso ${codigo}.`);
        }

        const dados = await respostaAtual.json();
        const situacoes = dados.situacoesCapacitacao.map((item: {
            servidorTitulo: string;
            competenciaCodigo: number;
            situacaoCapacitacao: string | null;
        }) => ({
            servidorTitulo: item.servidorTitulo,
            competenciaCodigo: item.competenciaCodigo,
            situacaoCapacitacao: item.situacaoCapacitacao ?? valorAtual,
        }));

        const respostaSalvar = await fetch(`/api/subprocessos/${codigo}/diagnostico/situacoes-capacitacao`, {
            method: 'POST',
            credentials: 'include',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({situacoes}),
        });

        if (!respostaSalvar.ok) {
            throw new Error(`Falha ao preencher situações de capacitação do subprocesso ${codigo}.`);
        }
    }, {codigo: codSubprocesso, valorAtual: valor});
}
