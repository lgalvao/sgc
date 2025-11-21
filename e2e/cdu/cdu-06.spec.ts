import {vueTest as test} from '../support/vue-specific-setup';
import {
    aguardarProcessoNoPainel,
    clicarProcessoNaTabela,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    iniciarProcesso,
    loginComoAdmin,
    loginComoGestor,
    verificarBotaoFinalizarProcessoInvisivel,
    verificarBotaoFinalizarProcessoVisivel,
    verificarBotaoIniciarProcessoInvisivel,
    verificarBotaoIniciarProcessoVisivel,
    verificarElementosDetalhesProcessoVisiveis,
    verificarNavegacaoPaginaSubprocesso,
    verificarPaginaEdicaoProcesso
} from '~/helpers';

test.describe.serial('CDU-06: Detalhar processo', () => {
    test('deve mostrar detalhes do processo para ADMIN', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - ADMIN';
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [3, 4]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await clicarProcessoNaTabela(page, nomeProcesso);
        // Wait for the form to load
        await verificarPaginaEdicaoProcesso(page);
        await iniciarProcesso(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await clicarProcessoNaTabela(page, nomeProcesso);
        // Now it's in EM_ANDAMENTO and should show the detail view
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve mostrar detalhes do processo para GESTOR participante', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - GESTOR';
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [3, 4]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await clicarProcessoNaTabela(page, nomeProcesso);
        // Wait for the form to load
        await verificarPaginaEdicaoProcesso(page);
        await iniciarProcesso(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await loginComoGestor(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await clicarProcessoNaTabela(page, nomeProcesso);
        await verificarElementosDetalhesProcessoVisiveis(page);
    });

    test('deve exibir o botão "Iniciar Processo" quando o processo está em estado CRIADO', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - CRIADO';
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [3, 4]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await clicarProcessoNaTabela(page, nomeProcesso);
        // Should be in CadProcesso (CRIADO state)
        await verificarBotaoIniciarProcessoVisivel(page);
        await verificarBotaoFinalizarProcessoInvisivel(page);
    });

    test('deve exibir o botão "Finalizar Processo" quando o processo está EM ANDAMENTO', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - EM ANDAMENTO';
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [5, 6]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await clicarProcessoNaTabela(page, nomeProcesso);
        // Wait for the form to load
        await verificarPaginaEdicaoProcesso(page);
        await iniciarProcesso(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await clicarProcessoNaTabela(page, nomeProcesso);
        // Now it's EM_ANDAMENTO
        await verificarBotaoIniciarProcessoInvisivel(page);
        await verificarBotaoFinalizarProcessoVisivel(page);
    });

    test('deve permitir clicar em unidade', async ({page}) => {
        const nomeProcesso = 'Processo de Detalhes Teste - UNIDADE';
        await loginComoAdmin(page);
        await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [7, 8]);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await clicarProcessoNaTabela(page, nomeProcesso);
        // Wait for the form to load
        await verificarPaginaEdicaoProcesso(page);
        await iniciarProcesso(page);
        await aguardarProcessoNoPainel(page, nomeProcesso);
        await clicarProcessoNaTabela(page, nomeProcesso);
        await clicarUnidadeNaTabelaDetalhes(page, 'COEDE');
        await verificarNavegacaoPaginaSubprocesso(page);
    });
});
