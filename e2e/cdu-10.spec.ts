import {vueTest as test} from './support/vue-specific-setup';
import {
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    cancelarNoModal,
    clicarBotaoHistoricoAnalise,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    devolverCadastro,
    disponibilizarCadastro,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaProcessoPorId,
    verificarAlerta,
    verificarMensagemSucesso,
    verificarModalHistoricoAnalise,
    verificarUrlDoPainel,
} from './helpers';

test.describe('CDU-10: Disponibilizar revisão do cadastro', () => {
    test('deve disponibilizar a revisão após corrigir atividades incompletas', async ({page}) => {
        const {processo} = await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-10'), 'REVISAO', '2025-12-31', ['STIC']);
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        // Adiciona uma atividade incompleta
        const nomeAtividadeIncompleta = gerarNomeUnico('Atividade Revisão Incompleta');
        await adicionarAtividade(page, nomeAtividadeIncompleta);

        // Tenta disponibilizar e verifica o alerta
        await disponibilizarCadastro(page);
        await verificarAlerta(page, 'Atividades Incompletas');
        await cancelarNoModal(page);

        // Adiciona conhecimento à atividade que estava incompleta
        await adicionarConhecimentoNaAtividade(page, nomeAtividadeIncompleta, gerarNomeUnico('Conhecimento Adicionado'));

        // Disponibiliza
        await disponibilizarCadastro(page);

        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);
    });

    test('deve exibir o histórico de análise após a devolução de um cadastro em revisão', async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO REVISAO COM DEVOLUCAO');
        const {processo} = await criarProcessoCompleto(page, nomeProcesso, 'REVISAO', '2025-12-31', ['STIC']);
        const nomeUnidade = 'STIC';
        const motivoDevolucao = 'Motivo da devolução pelo Gestor para teste CDU-10';

        // 1. CHEFE disponibiliza o cadastro em revisão
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, nomeUnidade);
        await disponibilizarCadastro(page);
        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);

        // 2. GESTOR devolve o cadastro em revisão
        await devolverCadastro(page, processo, nomeUnidade, motivoDevolucao);
        await verificarMensagemSucesso(page, 'Cadastro devolvido');
        await verificarUrlDoPainel(page);

        // 3. CHEFE verifica o histórico de análise
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, nomeUnidade);
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnalise(page, motivoDevolucao);
    });
});
