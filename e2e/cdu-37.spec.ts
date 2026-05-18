import {expect, test} from './fixtures/complete-fixtures.js';

/**
 * CDU-37 - Gerar relatório de unidades sem mapas vigentes
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Usuário logado como ADMIN
 */
test.describe.serial('CDU-37 - Gerar relatório de unidades sem mapas vigentes', () => {

    test('Cenários CDU-37: ADMIN visualiza árvore de unidades sem mapa vigente e exporta PDF', async ({
                                                                                                         _resetAutomatico,
                                                                                                         page,
                                                                                                         _autenticadoComoAdmin
                                                                                                     }) => {
        test.slow();

        // 1. O usuário acessa Relatórios na barra de navegação.
        await page.goto('/painel');
        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();

        // 3. O usuário aciona o card Unidades sem mapas vigentes.
        const cardUnidadesSemMapa = page.getByTestId('card-relatorio-unidades-sem-mapas-vigentes');
        await expect(cardUnidadesSemMapa).toBeVisible();
        await cardUnidadesSemMapa.click();

        // 4. O sistema mostra a tela Unidades sem mapas vigentes, com os botões Visualizar e PDF.
        await expect(page).toHaveURL(/\/relatorios\/unidades-sem-mapas-vigentes/);
        const botaoVisualizar = page.getByTestId('btn-visualizar-unidades-sem-mapa');
        const botaoPdf = page.getByTestId('btn-pdf-unidades-sem-mapa');

        await expect(botaoVisualizar).toBeVisible();
        await expect(botaoPdf).toBeVisible();

        // 5. O usuário clica em Visualizar.
        await botaoVisualizar.click();

        // 7. O sistema apresenta uma prévia organizada em árvore hierárquica contendo apenas os ramos que possuem ao menos uma unidade sem mapa vigente.
        const arvoreUnidades = page.locator('.arvore-unidades-sem-mapa');
        await expect(arvoreUnidades.first()).toBeVisible();
        await expect(page.getByText('SECRETARIA_1')).toBeVisible();

        // 10. O usuário clica em PDF.
        const downloadPromise = page.waitForEvent('download');
        await botaoPdf.click();
        const download = await downloadPromise;

        // 12. O nome do arquivo gerado segue o padrão sgc-rel-unidades-sem-mapas-vigentes-YYYY-MM-DD.pdf.
        // en-CA retorna YYYY-MM-DD respeitando o fuso local
        const hoje = new Date().toLocaleDateString('en-CA');
        expect(download.suggestedFilename()).toBe(`sgc-rel-unidades-sem-mapas-vigentes-${hoje}.pdf`);
    });
});
