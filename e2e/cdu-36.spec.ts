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

    test('Cenários CDU-36: ADMIN navega e gera relatórios de mapas', async ({page, request}) => {
        const descricaoProcesso = `Relatório CDU-36 ${Date.now()}`;
        const processo = await criarProcessoMapaHomologadoFixture(request, {
            descricao: descricaoProcesso,
            unidade: 'ASSESSORIA_12',
            diasLimite: 30
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();

        // Cenario 1: Navegação para página de relatórios
        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();

        await page.getByRole('tab', {name: 'Mapas'}).click();

        const selectProcesso = page.getByLabel('Selecione o Processo').last();
        const selectUnidade = page.getByLabel('Selecione a unidade');
        const botaoGerar = page.getByRole('button', {name: 'Gerar PDF'});
        await expect(selectProcesso).toBeVisible();
        await expect(selectUnidade).toBeVisible();
        await expect(botaoGerar).toBeDisabled();

        await selectProcesso.selectOption({label: descricaoProcesso});
        await expect(botaoGerar).toBeEnabled();

        const downloadPromise = page.waitForEvent('download');
        await botaoGerar.click();
        const download = await downloadPromise;
        expect(download.suggestedFilename()).toContain(`relatorio-mapas-${processo.codigo}.pdf`);
    });
});
