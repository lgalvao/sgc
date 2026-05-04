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

    test('Cenários CDU-36: ADMIN define filtros e gera PDF de mapas', async ({
                                                                                 _resetAutomatico,
                                                                                 page,
                                                                                 request,
                                                                                 _autenticadoComoAdmin
                                                                             }) => {
        test.slow();
        const descricaoProcesso = `Relatório CDU-36 ${Date.now()}`;
        await criarProcessoMapaHomologadoFixture(request, {
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

        const arvoreContainer = page.getByTestId('container-arvore-unidades-mapas');
        const botaoGerar = page.getByTestId('btn-gerar-mapas');

        await expect(arvoreContainer).toBeVisible();
        await expect(botaoGerar).toBeDisabled();

        // Expande para encontrar a unidade do fixture
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
        const chkUnidade = page.getByTestId('chk-arvore-unidade-ASSESSORIA_12');
        await expect(chkUnidade).toBeVisible();
        await chkUnidade.click();

        await expect(botaoGerar).toBeEnabled();

        const downloadPromise = page.waitForEvent('download');
        await botaoGerar.click();
        const download = await downloadPromise;

        const filename = download.suggestedFilename();
        expect(filename).toMatch(new RegExp(`relatorio-mapas-(vigentes-)?`));
    });

    test('Cenário CDU-36: Seleciona unidade e gera PDF', async ({
                                                                    _resetAutomatico,
                                                                    page,
                                                                    request,
                                                                    _autenticadoComoAdmin
                                                                }) => {
        test.slow();
        const descricaoProcesso = `Relatório CDU-36 filtro ${Date.now()}`;
        await criarProcessoMapaHomologadoFixture(request, {
            descricao: descricaoProcesso,
            unidade: 'SECRETARIA_1',
            diasLimite: 30
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await page.getByTestId('card-relatorio-mapas').click();
        await expect(page).toHaveURL(/\/relatorios\/mapas-vigentes/);

        const chkUnidade = page.getByTestId('chk-arvore-unidade-SECRETARIA_1');
        await expect(chkUnidade).toBeVisible();
        await chkUnidade.click();

        const downloadPromise = page.waitForEvent('download');
        await page.getByTestId('btn-gerar-mapas').click();
        await downloadPromise;
    });
});
