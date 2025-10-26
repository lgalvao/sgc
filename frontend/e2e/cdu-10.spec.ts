import {test} from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarBotaoHistoricoAnalise,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    devolverCadastro,
    disponibilizarCadastro,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaProcessoPorId,
    SELETORES,
    verificarAlerta,
    verificarMensagemSucesso,
    verificarModalHistoricoAnalise,
    verificarUrlDoPainel,
} from './helpers';

test.describe('CDU-10: Disponibilizar revisão do cadastro', () => {

    test('deve disponibilizar a revisão com sucesso após corrigir atividades incompletas', async ({page}) => {
        const {processo} = await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-10'), 'REVISAO', '2025-12-31', [1]);
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        // Adiciona uma atividade incompleta
        const nomeAtividadeIncompleta = gerarNomeUnico('Atividade Revisão Incompleta');
        await adicionarAtividade(page, nomeAtividadeIncompleta);

        // Tenta disponibilizar e verifica o alerta
        await page.click(SELETORES.DISPONIBILIZAR);
        await verificarAlerta(page, 'Atividades Incompletas');
        await page.locator('.modal-dialog .btn-secondary').click(); // Clica em Cancelar no modal de confirmação

        // Adiciona conhecimento à atividade que estava incompleta
        const cardAtividadeIncompleta = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividadeIncompleta});
        await adicionarConhecimento(cardAtividadeIncompleta, gerarNomeUnico('Conhecimento Adicionado'));

        // Disponibiliza com sucesso
        await disponibilizarCadastro(page);

        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);
    });

    test('deve exibir o histórico de análise após a devolução de um cadastro em revisão', async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO REVISAO COM DEVOLUCAO');
        const {processo} = await criarProcessoCompleto(page, nomeProcesso, 'REVISAO', '2025-12-31', [1]);
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
        // A função devolverCadastro já faz o login e navegação
        await devolverCadastro(page, processo, nomeUnidade, motivoDevolucao);
        await verificarMensagemSucesso(page, 'Cadastro devolvido com sucesso');
        await verificarUrlDoPainel(page);

        // 3. CHEFE verifica o histórico de análise
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, nomeUnidade);
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnalise(page, motivoDevolucao);
    });
});