import {vueTest as test} from './support/vue-specific-setup';
import {
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    clicarUnidadeNaTabelaDetalhes,
    criarProcessoCompleto,
    editarAtividade,
    editarConhecimento,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaProcessoPorId,
    removerAtividade,
    removerConhecimento,
    verificarAtividadeNaoVisivel,
    verificarAtividadeVisivel,
    verificarConhecimentoNaAtividade,
    verificarConhecimentoNaoVisivelNaAtividade,
    verificarPaginaCadastroAtividades,
} from '~/helpers';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
    test('deve adicionar, editar e remover atividades e conhecimentos', async ({page}) => {
        // Alterado [1] para ['STIC']
        const {processo} = await criarProcessoCompleto(page, gerarNomeUnico('PROCESSO CDU-08'), 'Mapeamento', '2025-12-31', ['STIC']);
        await loginComoChefe(page);
        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');
        await verificarPaginaCadastroAtividades(page);

        // Adiciona atividade
        const nomeAtividade = gerarNomeUnico('Atividade de Teste');
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);

        // Adiciona conhecimento
        const nomeConhecimento = gerarNomeUnico('Conhecimento de Teste');
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);

        // Edita conhecimento
        const nomeConhecimentoEditado = gerarNomeUnico('Conhecimento Editado');
        await editarConhecimento(page, nomeAtividade, nomeConhecimento, nomeConhecimentoEditado);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimentoEditado);
        await verificarConhecimentoNaoVisivelNaAtividade(page, nomeAtividade, nomeConhecimento);

        // Remove conhecimento
        await removerConhecimento(page, nomeAtividade, nomeConhecimentoEditado);
        await verificarConhecimentoNaoVisivelNaAtividade(page, nomeAtividade, nomeConhecimentoEditado);

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
