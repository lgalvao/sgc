import { test, expect } from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    gerarNomeUnico,
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

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {

    test('deve adicionar, editar e remover atividades e conhecimentos', async ({ page }) => {
        const { processo } = await criarProcessoCompleto(gerarNomeUnico('PROCESSO CDU-08'));
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await verificarPaginaCadastroAtividades(page);

        // Adiciona atividade
        const nomeAtividade = gerarNomeUnico('Atividade de Teste');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, { hasText: nomeAtividade });

        // Adiciona conhecimento
        const nomeConhecimento = gerarNomeUnico('Conhecimento de Teste');
        await adicionarConhecimento(cardAtividade, nomeConhecimento);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);

        // Edita conhecimento
        const nomeConhecimentoEditado = gerarNomeUnico('Conhecimento Editado');
        await editarConhecimento(page, nomeAtividade, nomeConhecimento, nomeConhecimentoEditado);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimentoEditado);
        await verificarConhecimentoNaoVisivel(cardAtividade, nomeConhecimento);

        // Remove conhecimento
        await removerConhecimento(page, nomeAtividade, nomeConhecimentoEditado);
        await verificarConhecimentoNaoVisivel(cardAtividade, nomeConhecimentoEditado);

        // Edita atividade
        const nomeAtividadeEditado = gerarNomeUnico('Atividade Editada');
        await editarAtividade(page, nomeAtividade, nomeAtividadeEditado);
        await verificarAtividadeVisivel(page, nomeAtividadeEditado);
        await verificarAtividadeNaoVisivel(page, nomeAtividade);

        // Remove atividade
        await removerAtividade(page, nomeAtividadeEditado);
        await verificarAtividadeNaoVisivel(page, nomeAtividadeEditado);
    });
});