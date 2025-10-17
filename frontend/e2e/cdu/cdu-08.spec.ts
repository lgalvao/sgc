import { test } from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    gerarNomeUnico,
    iniciarProcesso,
    loginComoChefe,
    navegarParaProcessoPorId,
    SELETORES_CSS,
    verificarAtividadeVisivel,
    verificarConhecimentoVisivel,
    verificarPaginaCadastroAtividades,
    editarConhecimento,
    removerConhecimento,
    verificarConhecimentoNaoVisivel,
    editarAtividade,
    removerAtividade,
    verificarAtividadeNaoVisivel,
} from './helpers';
import * as processoService from '../../src/services/processoService';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {

    async function setupProcessoIniciado() {
        const nomeProcesso = `PROCESSO ATIVIDADES TESTE - ${Date.now()}`;
        const processo = await processoService.criarProcesso({
            descricao: nomeProcesso,
            tipo: 'MAPEAMENTO',
            dataLimiteEtapa1: '2025-12-31T00:00:00',
            unidades: [2] // Unidade 2 = STIC
        });
        await processoService.iniciarProcesso(processo.codigo, 'MAPEAMENTO', [2]);
        return { nomeProcesso, processo };
    }

    test('deve adicionar uma atividade e um conhecimento', async ({ page }) => {
        const { processo } = await setupProcessoIniciado();
        await loginComoChefe(page); // Chefe da STIC
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        await verificarPaginaCadastroAtividades(page);

        const nomeAtividade = gerarNomeUnico('Atividade Teste');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);

        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividade });
        const nomeConhecimento = gerarNomeUnico('Conhecimento Teste');
        await adicionarConhecimento(cardAtividade, nomeConhecimento);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);
    });

    test('deve editar e remover atividades e conhecimentos', async ({ page }) => {
        const { processo } = await setupProcessoIniciado();
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await verificarPaginaCadastroAtividades(page);

        // Adiciona atividade e conhecimento
        const nomeAtividade = gerarNomeUnico('Atividade para Editar');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividade });
        const nomeConhecimento = gerarNomeUnico('Conhecimento para Editar');
        await adicionarConhecimento(cardAtividade, nomeConhecimento);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);

        // Edita conhecimento
        const nomeConhecimentoEditado = gerarNomeUnico('Conhecimento Editado');
        await editarConhecimento(nomeAtividade, nomeConhecimento, nomeConhecimentoEditado);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimentoEditado);

        // Remove conhecimento
        await removerConhecimento(page, nomeAtividade, nomeConhecimentoEditado);
        await verificarConhecimentoNaoVisivel(cardAtividade, nomeConhecimentoEditado);

        // Edita atividade
        const nomeAtividadeEditado = gerarNomeUnico('Atividade Editada');
        await editarAtividade(page, nomeAtividade, nomeAtividadeEditado);
        await verificarAtividadeVisivel(page, nomeAtividadeEditado);

        // Remove atividade
        await removerAtividade(page, nomeAtividadeEditado);
        await verificarAtividadeNaoVisivel(page, nomeAtividadeEditado);
    });
});