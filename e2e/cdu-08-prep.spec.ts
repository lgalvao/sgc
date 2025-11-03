import {vueTest as test} from './support/vue-specific-setup';
import {
    loginComoAdmin,
    loginComoChefe,
    criarProcesso,
    submeterProcesso,
    limparProcessos,
    navegarParaCadastroAtividades,
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    editarAtividade,
    editarConhecimento,
    removerAtividade,
    removerConhecimento,
    verificarAtividadeVisivel,
    verificarAtividadeNaoVisivel,
    verificarConhecimentoNaAtividade,
    verificarConhecimentoNaoVisivelNaAtividade,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP (id 2) é usada por ter um CHEFE definido no data.sql
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-08: Manter cadastro de atividades (com preparação)', () => {

    let processoId: number;

    test.beforeEach(async ({page}) => {
        // Limpa processos antigos e cria um novo para o teste
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('Processo CDU-08'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);
    });

    test.afterAll(async ({page}) => {
        // Limpa o processo criado ao final de todos os testes do arquivo
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve adicionar, editar e remover atividades e conhecimentos', async ({page}) => {
        // LOGIN E NAVEGAÇÃO
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);

        // CRUD DE ATIVIDADE E CONHECIMENTO
        const nomeAtividade = gerarNomeUnico('Atividade');
        const nomeAtividadeEditado = gerarNomeUnico('Atividade Editada');
        const nomeConhecimento = gerarNomeUnico('Conhecimento');
        const nomeConhecimentoEditado = gerarNomeUnico('Conhecimento Editado');

        // Adicionar Atividade
        await adicionarAtividade(page, nomeAtividade);
        await verificarAtividadeVisivel(page, nomeAtividade);

        // Adicionar Conhecimento
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);

        // Editar Conhecimento
        await editarConhecimento(page, nomeAtividade, nomeConhecimento, nomeConhecimentoEditado);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimentoEditado);
        await verificarConhecimentoNaoVisivelNaAtividade(page, nomeAtividade, nomeConhecimento);

        // Remover Conhecimento
        await removerConhecimento(page, nomeAtividade, nomeConhecimentoEditado);
        await verificarConhecimentoNaoVisivelNaAtividade(page, nomeAtividade, nomeConhecimentoEditado);

        // Editar Atividade
        await editarAtividade(page, nomeAtividade, nomeAtividadeEditado);
        await verificarAtividadeVisivel(page, nomeAtividadeEditado);
        await verificarAtividadeNaoVisivel(page, nomeAtividade);

        // Remover Atividade
        await removerAtividade(page, nomeAtividadeEditado);
        await verificarAtividadeNaoVisivel(page, nomeAtividadeEditado);
    });
});
