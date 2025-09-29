import {test} from '@playwright/test';
import {
    aceitarCadastro,
    cancelarModal,
    clicarBotaoHistoricoAnalise,
    DADOS_TESTE,
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
} from './helpers';

test.describe('CDU-13: Analisar cadastro de atividades e conhecimentos', () => {
    const ID_PROCESSO_STIC = DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id;
    const SIGLA_STIC = DADOS_TESTE.UNIDADES.STIC;

    test('deve exibir modal de Histórico de análise', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, ID_PROCESSO_STIC, SIGLA_STIC);

        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnaliseAberto(page);
        await cancelarModal(page);
        await verificarModalFechado(page);
    });

    test('GESTOR deve conseguir devolver cadastro para ajustes', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, ID_PROCESSO_STIC, SIGLA_STIC);

        await devolverParaAjustes(page, 'Devolução para correção de detalhes.');

        await verificarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
        await verificarUrlDoPainel(page);
    });

    test('ADMIN deve conseguir devolver cadastro para ajustes', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, ID_PROCESSO_STIC, SIGLA_STIC);

        await devolverParaAjustes(page); // Sem observação

        await verificarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
        await verificarUrlDoPainel(page);
    });

    test('GESTOR deve conseguir registrar aceite do cadastro', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, ID_PROCESSO_STIC, SIGLA_STIC);

        await aceitarCadastro(page, 'Aceite do cadastro de atividades.');

        await verificarMensagemSucesso(page, TEXTOS.ANALISE_REGISTRADA_SUCESSO);
        await verificarUrlDoPainel(page);
    });

    test('ADMIN deve conseguir homologar o cadastro', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, ID_PROCESSO_STIC, SIGLA_STIC);

        await homologarCadastro(page);

        await verificarMensagemSucesso(page, TEXTOS.CADASTRO_HOMOLOGADO_SUCESSO);
        await verificarUrl(page, `/processo/${ID_PROCESSO_STIC}/${SIGLA_STIC}`);
    });
});
