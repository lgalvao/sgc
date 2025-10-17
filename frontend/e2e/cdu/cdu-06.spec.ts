import {vueTest as test} from '../support/vue-specific-setup';
import {
    clicarUnidadeNaTabelaDetalhes,
    loginComoAdmin,
    criarProcessoCompleto,
    navegarParaProcessoNaTabela,
    verificarElementosDetalhesProcessoVisiveis,
    verificarNavegacaoPaginaSubprocesso,
    aguardarProcessoNoPainel
} from './helpers';

test.describe('CDU-06: Detalhar processo', () => {
    const nomeProcesso = 'Processo de Detalhes Teste';

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        // Cria um processo para garantir que o teste seja autossuficiente
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [1, 2]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
    });

    test('deve mostrar detalhes do processo para ADMIN', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve permitir clicar em unidade', async ({page}) => {
        await navegarParaProcessoNaTabela(page, nomeProcesso);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await verificarNavegacaoPaginaSubprocesso(page);
    });
});