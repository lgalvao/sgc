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
    verificarModalHistoricoAnaliseAberto,
    criarProcessoCompleto,
    iniciarProcesso,
    gerarNomeUnico
} from './helpers';
import {esperarUrl} from "./helpers/verificacoes/verificacoes-basicas";

test.describe('CDU-19: Validar mapa de competências', () => {
    let processo: any;
    const siglaUnidade = 'SEDIA';

    test.beforeEach(async ({ page }) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-19');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [9]); // Unidade 9 = SEDIA
        await iniciarProcesso(page);
        await loginComoChefeSedia(page)
    });

    test('deve exibir botões Apresentar sugestões e Validar para CHEFE', async ({page}) => {
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
        await esperarElementoVisivel(page, 'apresentar-sugestoes-btn');
        await esperarElementoVisivel(page, 'validar-btn');
    });

    test('deve exibir botão Histórico de análise e abrir modal', async ({page}) => {
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnaliseAberto(page);
        await cancelarModal(page);
    });

    test('deve permitir apresentar sugestões', async ({page}) => {
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await apresentarSugestoes(page, 'Sugestão de teste para o mapa');
        await esperarUrl(page, new RegExp(`/processo/${processo.processo.codigo}/${siglaUnidade}$`));
    });

    test('deve permitir validar mapa', async ({page}) => {
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await validarMapa(page);
        await esperarUrl(page, new RegExp(`/processo/${processo.processo.codigo}/${siglaUnidade}$`));
    });

    test('deve cancelar apresentação de sugestões', async ({page}) => {
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await clicarPorTestIdOuRole(page, 'apresentar-sugestoes-btn');
        await esperarElementoVisivel(page, 'modal-apresentar-sugestoes');
        await cancelarModal(page);
        await esperarElementoInvisivel(page, 'modal-apresentar-sugestoes');
    });

    test('deve cancelar validação de mapa', async ({page}) => {
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await clicarPorTestIdOuRole(page, 'validar-btn');
        await esperarElementoVisivel(page, 'modal-validar');
        await cancelarModal(page);
        await esperarElementoInvisivel(page, 'modal-validar');
    });
});
