import { test } from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarUnidadeNaTabelaDetalhes,
    disponibilizarCadastro,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaProcessoPorId,
    SELETORES_CSS,
    verificarAtividadeVisivel,
    verificarConhecimentoVisivel,
    verificarMensagemSucesso,
    verificarUrlDoPainel,
} from './helpers';
import * as processoService from '../../src/services/processoService';

test.describe('CDU-09: Disponibilizar cadastro de atividades', () => {

    async function setupProcessoIniciado() {
        const nomeProcesso = `PROCESSO DISPONIBILIZAR TESTE - ${Date.now()}`;
        const processo = await processoService.criarProcesso({
            descricao: nomeProcesso,
            tipo: 'MAPEAMENTO',
            dataLimiteEtapa1: '2025-12-31T00:00:00',
            unidades: [2] // Unidade 2 = STIC
        });
        await processoService.iniciarProcesso(processo.codigo, 'MAPEAMENTO', [2]);
        return { nomeProcesso, processo };
    }

    test('deve disponibilizar o cadastro com sucesso', async ({ page }) => {
        const { processo } = await setupProcessoIniciado();
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        const nomeAtividade = gerarNomeUnico('Atividade para Disponibilizar');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);

        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividade });
        const nomeConhecimento = gerarNomeUnico('Conhecimento para Disponibilizar');
        await adicionarConhecimento(cardAtividade, nomeConhecimento);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);

        await disponibilizarCadastro(page);

        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);
    });
});
