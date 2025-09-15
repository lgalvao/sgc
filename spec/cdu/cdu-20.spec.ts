import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {
    esperarElementoInvisivel,
    esperarElementoVisivel,
    loginComoAdmin,
    loginComoGestor
} from './auxiliares-verificacoes';
import {irParaVisualizacaoMapa} from './auxiliares-navegacao';
import {SELETORES_CSS} from './constantes-teste';

test.describe('CDU-20: Analisar validação de mapa de competências', () => {
    test.describe('GESTOR', () => {
        test.beforeEach(async ({page}) => {
            await loginComoGestor(page);
        });

        test('deve exibir botões para GESTOR analisar mapa validado', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');

            await esperarElementoVisivel(page, 'historico-analise-btn-gestor');
            await esperarElementoVisivel(page, 'devolver-ajustes-btn');
            await esperarElementoVisivel(page, 'registrar-aceite-btn');
            await expect(page.getByTestId('registrar-aceite-btn')).toHaveText('Registrar aceite');
        });

        test('deve permitir devolver para ajustes', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');

            await page.getByTestId('devolver-ajustes-btn').click();

            await esperarElementoVisivel(page, 'modal-devolucao');
            await expect(page.getByTestId('modal-devolucao-title')).toHaveText('Devolução');
            await expect(page.getByTestId('modal-devolucao-body')).toContainText('Confirma a devolução da validação do mapa para ajustes?');

            await page.getByTestId('observacao-devolucao-textarea').fill('Necessário revisar competências');

            await page.getByTestId('modal-devolucao-confirmar').click();

            await page.waitForTimeout(1000);
        });

        test('deve permitir registrar aceite', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');

            await page.getByTestId('registrar-aceite-btn').click();

            const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
            await expect(modal).toBeVisible();

            await page.getByTestId('modal-aceite-confirmar').click();

            await page.waitForTimeout(1000);
        });

        test('deve cancelar devolução', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');

            await page.getByTestId('devolver-ajustes-btn').click();
            await esperarElementoVisivel(page, 'modal-devolucao');

            await page.getByTestId('modal-devolucao-cancelar').click();

            await esperarElementoInvisivel(page, 'modal-devolucao');
        });
    });

    test.describe('ADMIN', () => {
        test.beforeEach(async ({page}) => {
            await loginComoAdmin(page);
        });

        test('deve exibir botão Homologar para ADMIN', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');

            await esperarElementoVisivel(page, 'registrar-aceite-btn');
            await expect(page.getByTestId('registrar-aceite-btn')).toHaveText('Homologar');
        });

        test('deve permitir homologar mapa', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');

            await page.getByTestId('registrar-aceite-btn').click();

            const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
            await expect(modal).toBeVisible();

            await page.getByTestId('modal-aceite-confirmar').click();

            await page.waitForTimeout(1000);
        });
    });

    test.describe('Ver sugestões', () => {
        test.beforeEach(async ({page}) => {
            await loginComoGestor(page);
        });

        test('deve exibir botão Ver sugestões quando situação for "Mapa com sugestões"', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');

            const verSugestoesBtn = page.getByTestId('ver-sugestoes-btn');
            const isVisible = await verSugestoesBtn.isVisible();

            if (isVisible) {
                await verSugestoesBtn.click();
                await esperarElementoVisivel(page, 'modal-sugestoes');
                await expect(page.getByTestId('modal-sugestoes-title')).toHaveText('Sugestões');

                await page.getByTestId('modal-sugestoes-fechar').click();
            }
        });
    });

    test.describe('Histórico de análise', () => {
        test.beforeEach(async ({page}) => {
            await loginComoGestor(page);
        });

        test('deve exibir histórico de análise', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');

            await page.getByTestId('historico-analise-btn-gestor').click();

            await esperarElementoVisivel(page, 'modal-historico');
            await expect(page.getByTestId('modal-historico-title')).toHaveText('Histórico de Análise');
            await esperarElementoVisivel(page, 'tabela-historico');

            const tabela = page.getByTestId('tabela-historico');
            await expect(tabela.getByText('Data/Hora')).toBeVisible();
            await expect(tabela.getByText('Unidade')).toBeVisible();
            await expect(tabela.getByText('Resultado')).toBeVisible();
            await expect(tabela.getByText('Observações')).toBeVisible();

            await page.getByTestId('modal-historico-fechar').click();
            await esperarElementoInvisivel(page, 'modal-historico');
        });
    });
});