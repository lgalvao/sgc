import {vueTest as test} from './support/vue-specific-setup';
import {
    apresentarSugestoes,
    cancelarModal,
    clicarBotaoHistoricoAnalise,
    clicarPorTestIdOuRole,
    esperarElementoInvisivel,
    esperarElementoVisivel,
    esperarTextoVisivel,
    irParaVisualizacaoMapa,
    loginComoChefeSedia,
    TEXTOS,
    validarMapa,
    verificarModalHistoricoAnaliseAberto
} from './helpers';
import {esperarUrl} from "./helpers/verificacoes/verificacoes-basicas";

test.describe('CDU-19: Validar mapa de competências', () => {
    test.beforeEach(async ({page}) => await loginComoChefeSedia(page));

    test('deve exibir botões Apresentar sugestões e Validar para CHEFE', async ({page}) => {
        await irParaVisualizacaoMapa(page, 5, 'SEDIA');
        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
        await esperarElementoVisivel(page, 'apresentar-sugestoes-btn');
        await esperarElementoVisivel(page, 'validar-btn');
    });

    test('deve exibir botão Histórico de análise e abrir modal', async ({page}) => {
        await irParaVisualizacaoMapa(page, 5, 'SEDIA');
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnaliseAberto(page);
        await cancelarModal(page);
    });

    test('deve permitir apresentar sugestões', async ({page}) => {
        await irParaVisualizacaoMapa(page, 5, 'SEDIA');
        await apresentarSugestoes(page, 'Sugestão de teste para o mapa');
        await esperarUrl(page, /\/processo\/5\/SEDIA$/);
    });

    test('deve permitir validar mapa', async ({page}) => {
        await irParaVisualizacaoMapa(page, 5, 'SEDIA');
        await validarMapa(page);
        await esperarUrl(page, /\/processo\/5\/SEDIA$/);
    });

    test('deve cancelar apresentação de sugestões', async ({page}) => {
        await irParaVisualizacaoMapa(page, 5, 'SEDIA');
        await clicarPorTestIdOuRole(page, 'apresentar-sugestoes-btn');
        await esperarElementoVisivel(page, 'modal-apresentar-sugestoes');
        await cancelarModal(page);
        await esperarElementoInvisivel(page, 'modal-apresentar-sugestoes');
    });

    test('deve cancelar validação de mapa', async ({page}) => {
        await irParaVisualizacaoMapa(page, 5, 'SEDIA');
        await clicarPorTestIdOuRole(page, 'validar-btn');
        await esperarElementoVisivel(page, 'modal-validar');
        await cancelarModal(page);
        await esperarElementoInvisivel(page, 'modal-validar');
    });
});
