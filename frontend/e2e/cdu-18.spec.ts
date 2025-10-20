import {vueTest as test} from './support/vue-specific-setup';
import {
    DADOS_TESTE,
    esperarElementoInvisivel,
    esperarTextoVisivel,
    irParaVisualizacaoMapa,
    loginComoAdmin,
    loginComoChefe,
    loginComoServidor,
    TEXTOS,
    verificarCabecalhoUnidade,
    verificarListagemAtividadesEConhecimentos,
    verificarModoSomenteLeitura
} from './helpers';

test.describe('CDU-18: Visualizar mapa de competências', () => {
    const ID_PROCESSO = DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id;

    test('ADMIN: navegar até visualização do mapa', async ({page}) => {
        await loginComoAdmin(page);
        await irParaVisualizacaoMapa(page, ID_PROCESSO, DADOS_TESTE.UNIDADES.STIC);

        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
    });

    test('CHEFE: navegar direto para subprocesso e visualizar mapa', async ({page}) => {
        await loginComoChefe(page);
        await irParaVisualizacaoMapa(page, ID_PROCESSO, DADOS_TESTE.UNIDADES.STIC);

        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
    });

    test('deve verificar elementos obrigatórios da visualização do mapa', async ({page}) => {
        await loginComoChefe(page);
        await irParaVisualizacaoMapa(page, ID_PROCESSO, DADOS_TESTE.UNIDADES.STIC);

        await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
        await verificarCabecalhoUnidade(page, 'STIC');
        await verificarListagemAtividadesEConhecimentos(page);
    });

    test('SERVIDOR: não exibe controles de ação na visualização', async ({page}) => {
        await loginComoServidor(page);
        await irParaVisualizacaoMapa(page, ID_PROCESSO, DADOS_TESTE.UNIDADES.STIC);
        await verificarModoSomenteLeitura(page);

        // Verificações explícitas para botões que não devem existir para servidor
        await esperarElementoInvisivel(page, 'validar-btn');
        await esperarElementoInvisivel(page, 'apresentar-sugestoes-btn');
        await esperarElementoInvisivel(page, 'registrar-aceite-btn');
    });
});