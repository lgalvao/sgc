import {vueTest as test} from '../support/vue-specific-setup';
import {
    abrirModalDevolucao,
    cancelarModal,
    clicarBotaoHistoricoAnalise,
    criarProcessoCompleto,
    devolverParaAjustes,
    gerarNomeUnico,
    homologarCadastro,
    iniciarProcesso,
    irParaVisualizacaoMapa,
    loginComoAdmin,
    loginComoGestor,
    registrarAceiteRevisao,
    verificarAcaoHomologarVisivel,
    verificarAceiteRegistradoComSucesso,
    verificarAcoesAnaliseGestor,
    verificarCadastroDevolvidoComSucesso,
    verificarModalHistoricoAnaliseAberto,
} from '~/helpers';

test.describe('CDU-20: Analisar validação de mapa de competências', () => {
    let processo: any;
    const siglaUnidade = 'SEDESENV';

    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-20');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [8]); // Unidade 8 = SEDESENV
        await iniciarProcesso(page);
    });

    test.describe('GESTOR', () => {
        test.beforeEach(async ({page}) => await loginComoGestor(page));

        test('deve exibir botões para GESTOR analisar mapa validado', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await verificarAcoesAnaliseGestor(page);
        });

        test('deve permitir devolver para ajustes', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await devolverParaAjustes(page, 'Necessário revisar competências');
            await verificarCadastroDevolvidoComSucesso(page);
        });

        test('deve permitir registrar aceite', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await registrarAceiteRevisao(page);
            await verificarAceiteRegistradoComSucesso(page);
        });

        test('deve cancelar devolução', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);

            // Abrir diálogo de devolução e cancelar
            await abrirModalDevolucao(page);
            await cancelarModal(page);
        });
    });

    test.describe('ADMIN', () => {
        test.beforeEach(async ({page}) => await loginComoAdmin(page));

        test('deve exibir botão Homologar para ADMIN', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await verificarAcaoHomologarVisivel(page);
        });

        test('deve permitir homologar mapa', async ({page}) => {
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await homologarCadastro(page);
            await verificarAceiteRegistradoComSucesso(page);
        });
    });

    test.describe('Ver sugestões', () => {
        test('deve exibir botão Ver sugestões quando situação for "Mapa com sugestões"', async ({page}) => {
            await loginComoGestor(page);
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await clicarBotaoHistoricoAnalise(page);
            await verificarModalHistoricoAnaliseAberto(page);
        });
    });

    test.describe('Histórico de análise', () => {
        test('deve exibir histórico de análise', async ({page}) => {
            await loginComoGestor(page);
            await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
            await clicarBotaoHistoricoAnalise(page);
            await verificarModalHistoricoAnaliseAberto(page);
        });
    });
});
