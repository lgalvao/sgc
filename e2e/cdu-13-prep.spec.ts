import {vueTest as test} from './support/vue-specific-setup';
import {
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    criarProcesso,
    submeterProcesso,
    limparProcessos,
    navegarParaCadastroAtividades,
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    disponibilizarCadastro,
    navegarParaVisualizacaoAtividades,
    aceitarCadastro,
    devolverParaAjustes,
    homologarCadastro,
    clicarBotaoHistoricoAnalise,
    verificarModalHistoricoAnaliseAberto,
    cancelarModal,
    verificarModalFechado,
    verificarMensagemSucesso,
    verificarUrlDoPainel,
    gerarNomeUnico,
    TEXTOS,
} from './helpers';

// A unidade SGP é usada por ter CHEFE e GESTOR.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-13: Analisar cadastro (com preparação)', () => {

    let processoId: number;

    // ANTES DE CADA TESTE: Cria um processo, o CHEFE submete o cadastro,
    // deixando-o no estado "CADASTRO_DISPONIBILIZADO", pronto para análise.
    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('Processo CDU-13'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        await adicionarAtividade(page, 'Atividade para Análise');
        await adicionarConhecimentoNaAtividade(page, 'Atividade para Análise', 'Conhecimento');
        await disponibilizarCadastro(page);
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('GESTOR deve conseguir devolver o cadastro para ajustes', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);

        await devolverParaAjustes(page, 'Favor corrigir a atividade X.');

        await verificarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
        await verificarUrlDoPainel(page);
    });

    test('GESTOR deve conseguir aceitar o cadastro', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);

        await aceitarCadastro(page, 'Cadastro aceito.');

        await verificarMensagemSucesso(page, TEXTOS.ANALISE_REGISTRADA_SUCESSO);
        await verificarUrlDoPainel(page);
    });

    test('ADMIN deve conseguir homologar o cadastro diretamente', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);

        await homologarCadastro(page);

        await verificarMensagemSucesso(page, TEXTOS.CADASTRO_HOMOLOGADO_SUCESSO);
    });

    test('ADMIN deve conseguir devolver o cadastro para ajustes', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);

        await devolverParaAjustes(page, 'Devolvido pelo Admin.');

        await verificarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
        await verificarUrlDoPainel(page);
    });

    test('deve exibir e fechar o modal de Histórico de análise', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);

        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnaliseAberto(page);

        await cancelarModal(page);
        await verificarModalFechado(page);
    });
});
