import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaHomologadoFixture} from './fixtures/fixtures-processos.js';

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

        await page.getByRole('tab', {name: 'Mapas'}).click();

        const painelMapas = page.getByRole('tabpanel', {name: /^Mapas$/i});
        const selectProcesso = painelMapas.getByLabel('Selecione o Processo');
        const selectUnidade = painelMapas.getByLabel('Selecione a unidade');
        const botaoGerar = painelMapas.getByRole('button', {name: 'Gerar PDF'});

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

        expect(download.suggestedFilename()).toContain(`relatorio-mapas-${processo.codigo}.pdf`);
    });
});
