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

    test('Cenários CDU-35: ADMIN navega e gera relatórios de andamento', async ({page, request, autenticadoComoAdmin}) => {
        const descricaoProcesso = `Relatório CDU-35 ${Date.now()}`;
        await criarProcessoFixture(request, {
            descricao: descricaoProcesso,
            tipo: 'MAPEAMENTO',
            unidade: 'ASSESSORIA_12',
            diasLimite: 30,
            iniciar: true
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();

        // Cenario 1: Navegação para página de relatórios
        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();

        const selectProcesso = page.getByLabel('Selecione o Processo').first();
        const botaoGerar = page.getByRole('button', {name: 'Gerar Relatório'});
        await expect(selectProcesso).toBeVisible();
        await expect(botaoGerar).toBeDisabled();

        await selectProcesso.selectOption({label: descricaoProcesso});
        await expect(botaoGerar).toBeEnabled();
        await botaoGerar.click();

        const tabelaRelatorio = page.locator('table').last();
        await expect(tabelaRelatorio).toBeVisible();
        await expect(page.getByRole('button', {name: /PDF/i})).toBeVisible();
    });
});
