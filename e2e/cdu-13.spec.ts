import {vueTest as test} from './support/vue-specific-setup';
import {
    aceitarCadastro,
    cancelarModal,
    clicarBotaoHistoricoAnalise,
    devolverParaAjustes,
    homologarCadastro,
    loginComoAdmin,
    loginComoGestor,
    navegarParaVisualizacaoAtividades,
    TEXTOS,
    verificarMensagemSucesso,
    verificarModalFechado,
    verificarModalHistoricoAnaliseAberto,
    verificarUrl,
    verificarUrlDoPainel,
    criarProcessoCompleto,
    iniciarProcesso,
    disponibilizarCadastro,
    loginComoChefe,
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    gerarNomeUnico,
    navegarParaCadastroAtividades,
} from './helpers';

test.describe('CDU-13: Analisar cadastro de atividades e conhecimentos', () => {
    let processo: any;
    const SIGLA_STIC = 'STIC';

    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO-CDU-13');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [2]); // Unidade 2 = STIC
        await iniciarProcesso(page);

        // Chefe da STIC preenche e disponibiliza o cadastro
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processo.processo.codigo, SIGLA_STIC);
        const nomeAtividade = gerarNomeUnico('Atividade CDU-13');
        await adicionarAtividade(page, nomeAtividade);
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, gerarNomeUnico('Conhecimento CDU-13'));
        await disponibilizarCadastro(page);
    });

    test('deve exibir modal de Histórico de análise', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnaliseAberto(page);

        await cancelarModal(page);
        await verificarModalFechado(page);
    });

    test('GESTOR deve conseguir devolver cadastro para ajustes', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await devolverParaAjustes(page, 'Devolução para correção de detalhes.');

        await verificarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
        await verificarUrlDoPainel(page);
    });

    test('ADMIN deve conseguir devolver cadastro para ajustes', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await devolverParaAjustes(page); // Sem observação

        await verificarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
        await verificarUrlDoPainel(page);
    });

    test('GESTOR deve conseguir registrar aceite do cadastro', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await aceitarCadastro(page, 'Aceite do cadastro de atividades.');

        await verificarMensagemSucesso(page, TEXTOS.ANALISE_REGISTRADA_SUCESSO);
        await verificarUrlDoPainel(page);
    });

    test('ADMIN deve conseguir homologar o cadastro', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, processo.processo.codigo, SIGLA_STIC);

        await homologarCadastro(page);

        await verificarMensagemSucesso(page, TEXTOS.CADASTRO_HOMOLOGADO_SUCESSO);
        await verificarUrl(page, `/processo/${processo.processo.codigo}/${SIGLA_STIC}`);
    });
});
