import {vueTest as test} from '../support/vue-specific-setup';
import {
    criarProcessoCompleto,
    esperarElementoInvisivel,
    esperarTextoVisivel,
    gerarNomeUnico,
    iniciarProcesso,
    irParaVisualizacaoMapa,
    loginComoAdmin,
    loginComoChefe,
    loginComoServidor,
    SELETORES,
    TEXTOS,
    verificarCabecalhoUnidade,
    verificarListagemAtividadesEConhecimentos,
    verificarModoSomenteLeitura,
} from '~/helpers';

test.describe('CDU-18: Visualizar mapa de competências', () => {
    let processo: any;
    const siglaUnidade = 'STIC';

    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-18');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [2]); // Unidade 2 = STIC
        await iniciarProcesso(page);
    });

    test('ADMIN: navegar até visualização do mapa', async ({page}) => {
        await loginComoAdmin(page);
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);

        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
    });

    test('CHEFE: navegar direto para subprocesso e visualizar mapa', async ({page}) => {
        await loginComoChefe(page);
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);

        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
    });

    test('deve verificar elementos obrigatórios da visualização do mapa', async ({page}) => {
        await loginComoChefe(page);
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);

        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
        await verificarCabecalhoUnidade(page, 'STIC');
        await verificarListagemAtividadesEConhecimentos(page);
    });

    test('SERVIDOR: não exibe controles de ação na visualização', async ({page}) => {
        await loginComoServidor(page);
        await irParaVisualizacaoMapa(page, processo.processo.codigo, siglaUnidade);
        await verificarModoSomenteLeitura(page);

        // Verificações explícitas para botões que não devem existir para servidor
        await esperarElementoInvisivel(page, SELETORES.BTN_VALIDAR);
        await esperarElementoInvisivel(page, SELETORES.BTN_APRESENTAR_SUGESTOES);
        await esperarElementoInvisivel(page, SELETORES.BTN_REGISTRAR_ACEITE);
    });
});
