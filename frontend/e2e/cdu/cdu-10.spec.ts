import {test} from '@playwright/test';
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
    verificarMensagemSucesso,
    verificarUrlDoPainel,
} from './helpers';

test.describe('CDU-10: Disponibilizar revisão do cadastro', () => {

    test('deve disponibilizar a revisão com sucesso após corrigir atividades incompletas', async ({ page }) => {
        const { processo } = await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-10'), 'REVISAO', '2025-12-31', [1]);
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        // Adiciona uma atividade incompleta
        const nomeAtividadeIncompleta = gerarNomeUnico('Atividade Revisão Incompleta');
        await adicionarAtividade(page, nomeAtividadeIncompleta);

        // Tenta disponibilizar e verifica o alerta
        await page.click(SELETORES_CSS.BTN_DISPONIBILIZAR);
        await verificarAlerta(page, 'Atividades Incompletas');
        await page.locator('.modal-dialog .btn-secondary').click(); // Clica em Cancelar no modal de confirmação

        // Adiciona conhecimento à atividade que estava incompleta
        const cardAtividadeIncompleta = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividadeIncompleta });
        await adicionarConhecimento(cardAtividadeIncompleta, gerarNomeUnico('Conhecimento Adicionado'));

        // Disponibiliza com sucesso
        await disponibilizarCadastro(page);

        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);
    });
});