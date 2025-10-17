import { test } from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarBotao,
    clicarUnidadeNaTabelaDetalhes,
    disponibilizarCadastro,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaProcessoPorId,
    SELETORES_CSS,
    TEXTOS,
    verificarMensagemErro,
    verificarMensagemSucesso,
    verificarUrlDoPainel,
} from './helpers';
import * as processoService from '../../src/services/processoService';

test.describe('CDU-10: Disponibilizar revisão do cadastro', () => {

    async function setupProcessoRevisaoIniciado() {
        const nomeProcesso = `PROCESSO REVISAO DISP TESTE - ${Date.now()}`;
        const processo = await processoService.criarProcesso({
            descricao: nomeProcesso,
            tipo: 'REVISAO',
            dataLimiteEtapa1: '2025-12-31T00:00:00',
            unidades: [2] // Unidade 2 = STIC
        });
        await processoService.iniciarProcesso(processo.codigo, 'REVISAO', [2]);
        return { nomeProcesso, processo };
    }

    test('deve disponibilizar a revisão com sucesso', async ({ page }) => {
        const { processo } = await setupProcessoRevisaoIniciado();
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        const nomeAtividade = gerarNomeUnico('Atividade para Revisão');
        await adicionarAtividade(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividade });
        const nomeConhecimento = gerarNomeUnico('Conhecimento para Revisão');
        await adicionarConhecimento(cardAtividade, nomeConhecimento);

        await disponibilizarCadastro(page);

        // A mensagem de sucesso foi padronizada na store
        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);
    });

    test('deve falhar ao disponibilizar com atividade sem conhecimento', async ({ page }) => {
        const { processo } = await setupProcessoRevisaoIniciado();
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        const nomeAtividade = gerarNomeUnico('Atividade Sem Conhecimento');
        await adicionarAtividade(page, nomeAtividade);

        // O botão de disponibilizar vai chamar o backend, que deve retornar um erro
        await clicarBotao(page, TEXTOS.DISPONIBILIZAR);

        // A UI deve mostrar a mensagem de erro vinda do backend
        await verificarMensagemErro(page, 'atividades sem conhecimentos associados');
    });
});