import {test} from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    editarAtividade,
    editarConhecimento,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaProcessoPorId,
    removerAtividade,
    removerConhecimento,
    SELETORES,
    verificarAtividadeNaoVisivel,
    verificarAtividadeVisivel,
    verificarConhecimentoNaoVisivel,
    verificarConhecimentoVisivel,
    verificarPaginaCadastroAtividades,
} from './helpers';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {

    test('deve adicionar, editar e remover atividades e conhecimentos', async ({ page }) => {
        const { processo } = await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-08'), 'Mapeamento', '2025-12-31', [1]);
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await verificarPaginaCadastroAtividades(page);

        // Adiciona atividade
        const nomeAtividade = gerarNomeUnico('Atividade de Teste');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, { hasText: nomeAtividade });

        // Adiciona conhecimento
        const nomeConhecimento = gerarNomeUnico('Conhecimento de Teste');
        await adicionarConhecimento(cardAtividade, nomeConhecimento);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimento);

        // Edita conhecimento
        const nomeConhecimentoEditado = gerarNomeUnico('Conhecimento Editado');
        await editarConhecimento(page, nomeAtividade, nomeConhecimento, nomeConhecimentoEditado);
        await verificarConhecimentoVisivel(cardAtividade, nomeConhecimentoEditado);
        await verificarConhecimentoNaoVisivel(page, nomeAtividade, nomeConhecimento);

        // Remove conhecimento
        await removerConhecimento(page, nomeAtividade, nomeConhecimentoEditado);
        await verificarConhecimentoNaoVisivel(page, nomeAtividade, nomeConhecimentoEditado);

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