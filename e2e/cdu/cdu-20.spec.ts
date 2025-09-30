import {vueTest as test} from '../support/vue-specific-setup';
import {
    cancelarModal,
    clicarBotaoHistoricoAnalise,
    devolverParaAjustes,
    homologarCadastro,
    irParaVisualizacaoMapa,
    loginComoAdmin,
    loginComoGestor,
    registrarAceiteRevisao,
    verificarAcaoHomologarVisivel,
    verificarAceiteRegistradoComSucesso,
    verificarAcoesAnaliseGestor,
    verificarCadastroDevolvidoComSucesso,
    verificarModalHistoricoAnaliseAberto
} from './helpers';

test.describe('CDU-20: Analisar validação de mapa de competências', () => {
    test.describe('GESTOR', () => {
        test.beforeEach(async ({page}) => await loginComoGestor(page));

        test('deve exibir botões para GESTOR analisar mapa validado', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
            await verificarAcoesAnaliseGestor(page);
        });

        test('deve permitir devolver para ajustes', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
            await devolverParaAjustes(page, 'Necessário revisar competências');
            await verificarCadastroDevolvidoComSucesso(page);
        });

        test('deve permitir registrar aceite', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
            await registrarAceiteRevisao(page);
            await verificarAceiteRegistradoComSucesso(page);
        });

        test('deve cancelar devolução', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');

            // Abrir diálogo de devolução e cancelar
            await page.getByRole('button', {name: 'Devolver para ajustes'}).click();
            await cancelarModal(page);
        });
    });

    test.describe('ADMIN', () => {
        test.beforeEach(async ({page}) => await loginComoAdmin(page));

        test('deve exibir botão Homologar para ADMIN', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
            await verificarAcaoHomologarVisivel(page);
        });

        test('deve permitir homologar mapa', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
            await homologarCadastro(page);
            await verificarAceiteRegistradoComSucesso(page);
        });
    });

    test.describe('Ver sugestões', () => {
        test.beforeEach(async ({page}) => await loginComoGestor(page));

        test('deve exibir botão Ver sugestões quando situação for "Mapa com sugestões"', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
            await clicarBotaoHistoricoAnalise(page);
            await verificarModalHistoricoAnaliseAberto(page);
        });
    });

    test.describe('Histórico de análise', () => {
        test.beforeEach(async ({page}) => await loginComoGestor(page));

        test('deve exibir histórico de análise', async ({page}) => {
            await irParaVisualizacaoMapa(page, 1, 'SEDESENV');
            await clicarBotaoHistoricoAnalise(page);
            await verificarModalHistoricoAnaliseAberto(page);
        });
    });
});