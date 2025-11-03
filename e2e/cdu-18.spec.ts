import {vueTest as test} from './support/vue-specific-setup';
import { loginComoAdmin, loginComoChefe, loginComoServidor } from './helpers/auth';
import { irParaVisualizacaoMapa } from './helpers/navegacao/navegacao';
import {
    esperarElementoInvisivel,
    esperarTextoVisivel,
} from './helpers/verificacoes/verificacoes-basicas';
import {
    verificarCabecalhoUnidade,
    verificarListagemAtividadesEConhecimentos,
    verificarModoSomenteLeitura,
} from './helpers/verificacoes/verificacoes-ui';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';
import { gerarNomeUnico } from './helpers/utils/utils';
import { TEXTOS, SELETORES } from './helpers/dados/constantes-teste';

test.describe('CDU-18: Visualizar mapa de competências', () => {
    let processo: any;
    const siglaUnidade = 'STIC';

    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-18');
        const processoId = await criarProcesso(page, 'MAPEAMENTO', nomeProcesso, ['2']); // Unidade 2 = STIC
        await submeterProcesso(page, processoId);
        processo = { processo: { codigo: processoId } };
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
