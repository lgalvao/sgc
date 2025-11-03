import {vueTest as test} from './support/vue-specific-setup';
import {
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    criarProcesso,
    submeterProcesso,
    limparProcessos,
    navegarParaCadastroAtividades,
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    disponibilizarCadastro,
    verificarAtividadeVisivel,
    verificarConhecimentoNaAtividade,
    verificarModoSomenteLeitura,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP é usada por ter CHEFE e GESTOR.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-11: Visualizar cadastro (somente leitura, com preparação)', () => {

    let processoId: number;
    const nomeAtividade = gerarNomeUnico('Atividade ReadOnly');
    const nomeConhecimento = gerarNomeUnico('Conhecimento ReadOnly');

    // Prepara o estado antes de cada teste: cria um processo, o CHEFE adiciona dados e disponibiliza.
    test.beforeEach(async ({page}) => {
        // Etapa 1: ADMIN cria e inicia o processo
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('Processo CDU-11'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // Etapa 2: CHEFE adiciona dados e disponibiliza o cadastro
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        await adicionarAtividade(page, nomeAtividade);
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await disponibilizarCadastro(page);
    });

    test.afterAll(async ({page}) => {
        // Limpa tudo no final
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('ADMIN deve visualizar o cadastro em modo somente leitura', async ({page}) => {
        // EXECUÇÃO: ADMIN acessa a página de cadastro
        await loginComoAdmin(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);

        // VERIFICAÇÃO
        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });

    test('GESTOR da unidade deve visualizar o cadastro em modo somente leitura', async ({page}) => {
        // EXECUÇÃO: GESTOR acessa a página de cadastro
        await loginComoGestor(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);

        // VERIFICAÇÃO
        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoNaAtividade(page, nomeAtividade, nomeConhecimento);
        await verificarModoSomenteLeitura(page);
    });
});
