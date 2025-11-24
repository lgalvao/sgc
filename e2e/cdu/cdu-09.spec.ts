import {vueTest as test} from '../support/vue-specific-setup';
import {
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    clicarBotaoHistoricoAnalise,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    devolverCadastro,
    disponibilizarCadastro,
    fecharAlerta,
    gerarNomeUnico,
    iniciarProcesso,
    loginComoChefe,
    navegarParaProcessoPorId,
    verificarAlerta,
    verificarAtividadeVisivel,
    verificarBotaoHistoricoAnaliseVisivel,
    verificarConhecimentoNaAtividade,
    verificarMensagemSucesso,
    verificarModalHistoricoAnalise,
    verificarUrlDoPainel,
} from '../helpers';

test.describe('CDU-09: Disponibilizar cadastro de atividades', () => {
    test('deve avisar sobre atividades sem conhecimentos e depois disponibilizar', async ({page}) => {
        const {processo} = await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-09'), 'Mapeamento', '2025-12-31', ['STIC']);
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        // Adiciona uma atividade sem conhecimento
        const nomeAtividadeIncompleta = gerarNomeUnico('Atividade Incompleta');
        await adicionarAtividade(page, nomeAtividadeIncompleta);
        await verificarAtividadeVisivel(page, nomeAtividadeIncompleta);

        // Tenta disponibilizar e verifica o alerta
        await disponibilizarCadastro(page);
        await verificarAlerta(page, 'Atividades Incompletas');
        await fecharAlerta(page);

        // Adiciona uma atividade completa
        const nomeAtividadeCompleta = gerarNomeUnico('Atividade Completa');
        await adicionarAtividade(page, nomeAtividadeCompleta);
        await verificarAtividadeVisivel(page, nomeAtividadeCompleta);
        const nomeConhecimento = gerarNomeUnico('Conhecimento');
        await adicionarConhecimentoNaAtividade(page, nomeAtividadeCompleta, nomeConhecimento);
        await verificarConhecimentoNaAtividade(page, nomeAtividadeCompleta, nomeConhecimento);

        // Adiciona conhecimento à atividade que estava incompleta
        await adicionarConhecimentoNaAtividade(page, nomeAtividadeIncompleta, gerarNomeUnico('Conhecimento Adicionado'));

        // Disponibiliza
        await disponibilizarCadastro(page);

        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);
    });

    test('deve exibir o histórico de análise após devolução', async ({page}) => {
        const {processo} = await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO-CDU-09'), 'Mapeamento', '2025-12-31', ['STIC']);
        await iniciarProcesso(page);
        await verificarMensagemSucesso(page, 'Processo iniciado');

        // Chefe da unidade STIC acessa e preenche o cadastro
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        const atividade = gerarNomeUnico('Atividade');
        const conhecimento = gerarNomeUnico('Conhecimento');
        await adicionarAtividade(page, atividade);
        await adicionarConhecimentoNaAtividade(page, atividade, conhecimento);
        await disponibilizarCadastro(page);
        await verificarMensagemSucesso(page, 'Disponibilização solicitada');

        // Gestor da unidade SEDES devolve o cadastro
        await devolverCadastro(page, processo, 'STIC', 'Favor revisar o item X.');

        // Chefe da STIC verifica o botão de histórico
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await verificarBotaoHistoricoAnaliseVisivel(page);
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnalise(page, 'Favor revisar o item X.');
    });
});
