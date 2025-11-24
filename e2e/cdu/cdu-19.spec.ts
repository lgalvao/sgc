import {vueTest as test} from '../support/vue-specific-setup';
import {
    apresentarSugestoes,
    cancelarNoModal,
    clicarBotaoHistoricoAnalise,
    criarProcessoCompleto,
    esperarElementoInvisivel,
    esperarElementoVisivel,
    esperarTextoVisivel,
    esperarUrl,
    gerarNomeUnico,
    iniciarProcesso,
    irParaVisualizacaoMapa,
    loginComoChefeSedia,
    SELETORES,
    TEXTOS,
    validarMapa,
    verificarModalHistoricoAnaliseAberto,
} from '~/helpers';

test.describe('CDU-19: Validar mapa de competências', () => {
    let processo: any;
    const siglaUnidade = 'SEDIA';

    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-19');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', ['SEDIA']); // Unidade 9 = SEDIA
        await iniciarProcesso(page);
        await loginComoChefeSedia(page);
    });

    test('deve exibir botões Apresentar sugestões e Validar para CHEFE', async ({page}) => {
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
        await esperarElementoVisivel(page, SELETORES.BTN_APRESENTAR_SUGESTOES);
        await esperarElementoVisivel(page, SELETORES.BTN_VALIDAR);
    });

    test('deve exibir botão Histórico de análise e abrir modal', async ({page}) => {
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnaliseAberto(page);
        await cancelarNoModal(page);
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
        await page.locator(SELETORES.BTN_APRESENTAR_SUGESTOES).click();
        await esperarElementoVisivel(page, SELETORES.MODAL_APRESENTAR_SUGESTOES);
        await cancelarNoModal(page);
        await esperarElementoInvisivel(page, SELETORES.MODAL_APRESENTAR_SUGESTOES);
    });

    test('deve cancelar validação de mapa', async ({page}) => {
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await page.locator(SELETORES.BTN_VALIDAR).click();
        await esperarElementoVisivel(page, SELETORES.MODAL_VALIDAR);
        await cancelarNoModal(page);
        await esperarElementoInvisivel(page, SELETORES.MODAL_VALIDAR);
    });
});
