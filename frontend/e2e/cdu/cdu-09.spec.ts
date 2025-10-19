import { test, expect } from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    disponibilizarCadastro,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaProcessoPorId,
    SELETORES_CSS,
    verificarAlerta,
    verificarAtividadeVisivel,
    verificarConhecimentoVisivel,
    verificarMensagemSucesso,
    verificarUrlDoPainel,
} from './helpers';

test.describe('CDU-09: Disponibilizar cadastro de atividades', () => {

    test('deve avisar sobre atividades sem conhecimentos e depois disponibilizar com sucesso', async ({ page }) => {
        const { processo } = await criarProcessoCompleto(gerarNomeUnico('PROCESSO CDU-09'));
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        // Adiciona uma atividade sem conhecimento
        const nomeAtividadeIncompleta = gerarNomeUnico('Atividade Incompleta');
        await adicionarAtividade(page, nomeAtividadeIncompleta);
        await verificarAtividadeVisivel(page, nomeAtividadeIncompleta);

        // Tenta disponibilizar e verifica o alerta
        await page.click(SELETORES_CSS.BTN_DISPONIBILIZAR);
        await verificarAlerta(page, 'Atividades Incompletas');
        await page.click('.btn-close'); // Fecha o alerta para continuar

        // Adiciona uma atividade completa
        const nomeAtividadeCompleta = gerarNomeUnico('Atividade Completa');
        await adicionarAtividade(page, nomeAtividadeCompleta);
        await verificarAtividadeVisivel(page, nomeAtividadeCompleta);
        const cardAtividadeCompleta = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividadeCompleta });
        const nomeConhecimento = gerarNomeUnico('Conhecimento');
        await adicionarConhecimento(cardAtividadeCompleta, nomeConhecimento);
        await verificarConhecimentoVisivel(cardAtividadeCompleta, nomeConhecimento);

        // Adiciona conhecimento à atividade que estava incompleta
        const cardAtividadeIncompleta = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividadeIncompleta });
        await adicionarConhecimento(cardAtividadeIncompleta, gerarNomeUnico('Conhecimento Adicionado'));

        // Disponibiliza com sucesso
        await disponibilizarCadastro(page);

        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);
    });

    test('deve exibir o histórico de análise após devolução', async ({page}) => {
        const processo = await criarProcessoMapeamentoCompleto(page, gerarNomeUnico('PROCESSO-CDU-09'), '2025-12-31');
        await iniciarProcesso(page);
        await verificarMensagemSucesso(page, 'Processo iniciado');

        // Chefe da unidade STIC acessa e preenche o cadastro
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        const atividade = gerarNomeUnico('Atividade');
        const conhecimento = gerarNomeUnico('Conhecimento');
        await adicionarAtividade(page, atividade);
        await adicionarConhecimento(page.locator(SELETORES_CSS.CARD_ATIVIDADE).first(), conhecimento);
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
