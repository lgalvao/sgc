import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaHomologadoFixture} from './fixtures/index.js';

/**
 * CDU-36 - Gerar relatório de mapas
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Usuário logado como ADMIN
 */
test.describe.serial('CDU-36 - Gerar relatório de mapas', () => {

    test('Cenários CDU-36: ADMIN define filtros e gera PDF de mapas', async ({_resetAutomatico, page, request, _autenticadoComoAdmin}) => {
        test.slow();
        const descricaoProcesso = `Relatório CDU-36 ${Date.now()}`;
        const processo = await criarProcessoMapaHomologadoFixture(request, {
            descricao: descricaoProcesso,
            unidade: 'ASSESSORIA_12',
            diasLimite: 30
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();

        await page.getByTestId('card-relatorio-mapas').click();
        await expect(page).toHaveURL(/\/relatorios\/mapas-vigentes/);

        const selectProcesso = page.getByTestId('select-processo-mapas');
        const selectUnidade = page.getByTestId('select-unidade-mapas');
        const botaoGerar = page.getByTestId('btn-gerar-mapas');

        await expect(selectProcesso).toBeVisible();
        await expect(selectUnidade).toBeVisible();
        await expect(selectUnidade).toContainText(/Todas as unidades/i);
        await expect(botaoGerar).toBeDisabled();

        await selectProcesso.selectOption({label: descricaoProcesso});
        await expect(botaoGerar).toBeEnabled();

        const requisicaoSemFiltroUnidade = page.waitForRequest((req) => {
            return req.url().includes(`/relatorios/mapas/${processo.codigo}/exportar`) && !req.url().includes('unidadeId=');
        });

        const downloadPromise = page.waitForEvent('download');
        await botaoGerar.click();
        await requisicaoSemFiltroUnidade;
        const download = await downloadPromise;

        const filename = download.suggestedFilename();
        expect(filename).toMatch(new RegExp(`relatorio-mapas-(vigentes-)?${processo.codigo}.pdf`));
    });

    test('Cenário CDU-36: Mantém opção única de unidade e gera PDF sem unidadeId', async ({_resetAutomatico, page, request, _autenticadoComoAdmin}) => {
        test.slow();
        const descricaoProcesso = `Relatório CDU-36 filtro ${Date.now()}`;
        const processo = await criarProcessoMapaHomologadoFixture(request, {
            descricao: descricaoProcesso,
            unidade: 'SECRETARIA_1',
            diasLimite: 30
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await page.getByTestId('card-relatorio-mapas').click();
        await expect(page).toHaveURL(/\/relatorios\/mapas-vigentes/);

        const selectProcesso = page.getByTestId('select-processo-mapas');
        const selectUnidade = page.getByTestId('select-unidade-mapas');
        const botaoGerar = page.getByTestId('btn-gerar-mapas');

        await selectProcesso.selectOption({label: descricaoProcesso});
        await expect(botaoGerar).toBeEnabled();

        await expect(selectUnidade).toContainText(/Todas as unidades/i);

        const requisicaoSemFiltroUnidade = page.waitForRequest((req) => {
            return req.url().includes(`/relatorios/mapas/${processo.codigo}/exportar`) && !req.url().includes('unidadeId=');
        });

        const downloadPromise = page.waitForEvent('download');
        await botaoGerar.click();
        await requisicaoSemFiltroUnidade;
        await downloadPromise;
    });
});
