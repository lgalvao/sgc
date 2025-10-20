import {vueTest as test} from '../support/vue-specific-setup';
import {
    acessarAnaliseRevisaoComoAdmin,
    acessarAnaliseRevisaoComoGestor,
    cancelarModal,
    clicarBotaoHistoricoAnalise,
    criarProcessoCompleto,
    devolverParaAjustes,
    gerarNomeUnico,
    registrarAceiteRevisao,
    verificarAcaoHomologarVisivel,
    verificarAceiteRegistradoComSucesso,
    verificarAcoesAnaliseGestor,
    verificarCadastroDevolvidoComSucesso,
    verificarModalFechado,
    verificarModalHistoricoAnaliseAberto,
} from './helpers';

test.describe('CDU-14: Analisar revisão de cadastro de atividades e conhecimentos', () => {
    let processo: any;

    test.beforeEach(async ({ page }) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO CDU-14');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'REVISAO', '2025-12-31', [1]);
    });

    test('deve apresentar ações adequadas para cada perfil', async ({page}) => {
        await acessarAnaliseRevisaoComoGestor(page, processo.processo.codigo, 'STIC');
        await verificarAcoesAnaliseGestor(page);

        await acessarAnaliseRevisaoComoAdmin(page, processo.processo.codigo, 'STIC');
        await verificarAcaoHomologarVisivel(page);
    });

    test('deve permitir devolver e registrar aceite da revisão', async ({page}) => {
        await acessarAnaliseRevisaoComoGestor(page, processo.processo.codigo, 'STIC');
        await devolverParaAjustes(page, 'Ajustes necessários.');
        await verificarCadastroDevolvidoComSucesso(page);

        await acessarAnaliseRevisaoComoGestor(page, processo.processo.codigo, 'STIC');
        await registrarAceiteRevisao(page, 'Aceite após análise.');
        await verificarAceiteRegistradoComSucesso(page);
    });

    test('deve exibir histórico de análise', async ({page}) => {
        await acessarAnaliseRevisaoComoGestor(page, processo.processo.codigo, 'STIC');
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnaliseAberto(page);

        await cancelarModal(page);
        await verificarModalFechado(page);
    });
});
