import {vueTest as test} from '../support/vue-specific-setup';
import {
    acessarAnaliseRevisaoComoAdmin,
    acessarAnaliseRevisaoComoGestor,
    cancelarModal,
    clicarBotaoHistoricoAnalise,
    devolverParaAjustes,
    registrarAceiteRevisao,
    verificarAcaoHomologarVisivel,
    verificarAceiteRegistradoComSucesso,
    verificarAcoesAnaliseGestor,
    verificarCadastroDevolvidoComSucesso,
    verificarModalFechado,
    verificarModalHistoricoAnaliseAberto,
} from './helpers';

test.describe('CDU-14: Analisar revisão de cadastro de atividades e conhecimentos', () => {
    test('deve apresentar ações adequadas para cada perfil', async ({page}) => {
        await acessarAnaliseRevisaoComoGestor(page);
        await verificarAcoesAnaliseGestor(page);

        await acessarAnaliseRevisaoComoAdmin(page);
        await verificarAcaoHomologarVisivel(page);
    });

    test('deve permitir devolver e registrar aceite da revisão', async ({page}) => {
        await acessarAnaliseRevisaoComoGestor(page);
        await devolverParaAjustes(page, 'Ajustes necessários.');
        await verificarCadastroDevolvidoComSucesso(page);

        await acessarAnaliseRevisaoComoGestor(page);
        await registrarAceiteRevisao(page, 'Aceite após análise.');
        await verificarAceiteRegistradoComSucesso(page);
    });

    test('deve exibir histórico de análise', async ({page}) => {
        await acessarAnaliseRevisaoComoGestor(page);
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnaliseAberto(page);

        await cancelarModal(page);
        await verificarModalFechado(page);
    });
});