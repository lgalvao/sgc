import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/index.js';

/**
 * CDU-35 - Gerar relatório de andamento
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Usuário logado como ADMIN
 */
test.describe.serial('CDU-35 - Gerar relatório de andamento', () => {

    test('Cenários CDU-35: ADMIN gera relatório com colunas esperadas e exporta PDF', async ({_resetAutomatico, page, request, _autenticadoComoAdmin}) => {
        test.slow();
        const descricaoProcesso = `Relatório CDU-35 ${Date.now()}`;
        const processo = await criarProcessoFixture(request, {
            descricao: descricaoProcesso,
            tipo: 'MAPEAMENTO',
            unidade: 'ASSESSORIA_12',
            diasLimite: 30,
            iniciar: true
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();
        await page.getByTestId('card-relatorio-andamento').click();
        await expect(page).toHaveURL(/\/relatorios\/andamento/);

        const selectProcesso = page.getByTestId('select-processo-andamento');
        const botaoGerar = page.getByTestId('btn-gerar-andamento');
        await expect(selectProcesso).toBeVisible();
        await expect(botaoGerar).toBeDisabled();

        await selectProcesso.selectOption({label: descricaoProcesso});
        await expect(botaoGerar).toBeEnabled();
        await botaoGerar.click();

        const cardsRelatorio = page.getByTestId('card-resultado-andamento');
        await expect(cardsRelatorio.first()).toBeVisible();

        const primeiroCard = cardsRelatorio.first();
        await expect(primeiroCard).toContainText(/ASSESSORIA_12|ASSESSORIA/);
        await expect(primeiroCard).toContainText(/Situação|Localização|Última movimentação/i);
        await expect(primeiroCard).toContainText(/\d{4}-\d{2}-\d{2}|\d{2}\/\d{2}\/\d{4}/);

        const botaoPdf = page.getByTestId('btn-exportar-andamento');
        await expect(botaoPdf).toBeVisible();

        const downloadPromise = page.waitForEvent('download');
        await botaoPdf.click();
        const download = await downloadPromise;
        expect(download.suggestedFilename()).toContain(`relatorio-andamento-${processo.codigo}.pdf`);
    });
});
