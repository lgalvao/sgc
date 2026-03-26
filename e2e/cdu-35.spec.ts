import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture} from './fixtures/fixtures-processos.js';

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
        await expect(page.getByRole('tab', {name: /Andamento de processo/i})).toBeVisible();

        const selectProcesso = page.getByLabel('Selecione o Processo').first();
        const botaoGerar = page.getByRole('button', {name: 'Gerar relatório'});
        await expect(selectProcesso).toBeVisible();
        await expect(botaoGerar).toBeDisabled();

        await selectProcesso.selectOption({label: descricaoProcesso});
        await expect(botaoGerar).toBeEnabled();
        await botaoGerar.click();

        const tabelaRelatorio = page.locator('table').last();
        await expect(tabelaRelatorio).toBeVisible();

        await expect(tabelaRelatorio.locator('th', {hasText: /Sigla/i}).first()).toBeVisible();
        await expect(tabelaRelatorio.locator('th', {hasText: /Nome/i}).first()).toBeVisible();
        await expect(tabelaRelatorio.locator('th', {hasText: /Situação|Situacao/i}).first()).toBeVisible();
        await expect(tabelaRelatorio.locator('th', {hasText: /Data/i}).first()).toBeVisible();
        await expect(tabelaRelatorio.locator('th', {hasText: /Responsável|Responsavel/i}).first()).toBeVisible();
        await expect(tabelaRelatorio.locator('th', {hasText: /Titular/i}).first()).toBeVisible();

        const primeiraLinha = tabelaRelatorio.locator('tbody tr').first();
        await expect(primeiraLinha).toContainText(/ASSESSORIA_12|ASSESSORIA/);
        await expect(primeiraLinha).toContainText(/NAO_INICIADO|EM_ANDAMENTO|MAPEAMENTO/i);
        await expect(primeiraLinha).toContainText(/\d{4}-\d{2}-\d{2}|\d{2}\/\d{2}\/\d{4}/);

        const botaoPdf = page.getByRole('button', {name: /PDF/i});
        await expect(botaoPdf).toBeVisible();

        const downloadPromise = page.waitForEvent('download');
        await botaoPdf.click();
        const download = await downloadPromise;
        expect(download.suggestedFilename()).toContain(`relatorio-andamento-${processo.codigo}.pdf`);
    });
});
