import {vueTest as test} from '../support/vue-specific-setup';
import {
    clicarUnidadeNaTabelaDetalhes,
    loginComoAdmin,
    navegarParaDetalhesProcesso,
    verificarElementosDetalhesProcessoVisiveis,
    verificarNavegacaoPaginaSubprocesso
} from './helpers';

test.describe('CDU-06: Detalhar processo', () => {
    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve mostrar detalhes do processo para ADMIN', async ({page}) => {
        await navegarParaDetalhesProcesso(page, 'STIC/COINF');
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve permitir clicar em unidade', async ({page}) => {
        await navegarParaDetalhesProcesso(page, 'STIC/COINF');
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await verificarNavegacaoPaginaSubprocesso(page);
    });
});