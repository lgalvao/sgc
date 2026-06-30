import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoHomologadoFixture} from './fixtures/index.js';
import {TEXTOS_RELATORIOS} from '../frontend/src/constants/textos-relatorios.js';

test.describe('CDU-55 - Gerar relatório de situação de capacitação', () => {
    test('ADMIN visualiza o relatório e exporta PDF', async ({
                                                                 _resetAutomatico,
                                                                 page,
                                                                 request,
                                                                 _autenticadoComoAdmin
                                                             }) => {
        test.slow();
        const descricaoProcesso = `Relatório CDU-54 ${Date.now()}`;
        await criarProcessoDiagnosticoHomologadoFixture(request, {
            descricao: descricaoProcesso,
            unidade: 'ASSESSORIA_12'
        });

        await page.goto('/relatorios');
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();

        await page.getByTestId('card-relatorio-situacao-capacitacao-diagnostico').click();
        await expect(page).toHaveURL(/\/relatorios\/diagnostico\/situacao-capacitacao/);
        await expect(page.getByRole('heading', {name: TEXTOS_RELATORIOS.SITUACAO_CAPACITACAO_DIAGNOSTICO})).toBeVisible();

        const selectProcesso = page.getByTestId('select-processo-relatorio-diagnostico');
        const botaoVisualizar = page.getByTestId('btn-visualizar-relatorio-diagnostico');
        const botaoExportar = page.getByTestId('btn-exportar-relatorio-diagnostico');

        await expect(botaoVisualizar).toBeDisabled();
        await selectProcesso.selectOption({label: descricaoProcesso});
        await expect(page.getByTestId('container-arvore-unidades-diagnostico')).toBeVisible();

        const busca = page.getByRole('searchbox', {name: 'Buscar unidade por sigla'});
        await busca.fill('ASSESSORIA_12');
        await page.getByTestId('chk-arvore-unidade-ASSESSORIA_12').check();

        await expect(botaoVisualizar).toBeEnabled();
        await expect(botaoExportar).toBeEnabled();

        await botaoVisualizar.click();

        const card = page.getByTestId('card-relatorio-situacao-capacitacao-diagnostico').first();
        await expect(card).toBeVisible();
        await expect(card).toContainText('ASSESSORIA_12');
        await expect(card.locator('th', {hasText: 'Competência'})).toBeVisible();
        await expect(card.locator('th', {hasText: 'NA'})).toBeVisible();
        await expect(card.locator('th', {hasText: 'AC'})).toBeVisible();
        await expect(card.locator('th', {hasText: 'EC'})).toBeVisible();
        await expect(card.getByRole('columnheader', {name: 'C', exact: true})).toBeVisible();
        await expect(card.getByRole('columnheader', {name: 'I', exact: true})).toBeVisible();
        await expect(card.locator('tbody tr').first()).toBeVisible();

        const downloadPromise = page.waitForEvent('download');
        await botaoExportar.click();
        const download = await downloadPromise;
        const hoje = new Date().toLocaleDateString('en-CA');
        expect(download.suggestedFilename()).toBe(`sgc-rel-situacao-capacitacao-${hoje}.pdf`);
    });
});
