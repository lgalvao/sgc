import {vueTest as test} from './support/vue-specific-setup';
import {
    loginComoChefe,
    loginComoGestor,
    loginComoAdmin,
    criarProcesso,
    submeterProcesso,
    limparProcessos,
    irParaSubprocesso,
    disponibilizarCadastro,
    aceitarCadastro,
    verificarCardAcaoVisivel,
    verificarCardAcaoInvisivel,
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    navegarParaCadastroAtividades,
    verificarDetalhesSubprocesso,
} from './helpers';

// Unidade usada nos testes. A SGP (id 2) é usada por ter CHEFE e GESTOR definidos.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-07: Detalhar subprocesso (com preparação)', () => {

    test.beforeEach(async ({page}) => {
        // Garante um ambiente limpo para cada teste
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test.afterEach(async ({page}) => {
        // Limpa os dados criados pelo teste
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('CHEFE deve ver detalhes e o card "Cadastro de atividades"', async ({page}) => {
        // PREPARAÇÃO: Cria e inicia um processo de Mapeamento para a unidade SGP
        const processoId = await criarProcesso(page, 'MAPEAMENTO', 'Processo CDU-07-A', [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // EXECUÇÃO: Como CHEFE, acessa o subprocesso
        await loginComoChefe(page); // Chefe da SGP
        await irParaSubprocesso(page, processoId, SIGLA_UNIDADE);

        // VERIFICAÇÃO
        await verificarDetalhesSubprocesso(page);
        await verificarCardAcaoVisivel(page, 'Cadastro de atividades');
        await verificarCardAcaoInvisivel(page, 'Análise de cadastro');
        await verificarCardAcaoInvisivel(page, 'Mapa de competências');
    });

    test('GESTOR deve ver o card "Análise de cadastro" após CHEFE disponibilizar', async ({page}) => {
        // PREPARAÇÃO 1: Cria e inicia o processo
        const processoId = await criarProcesso(page, 'MAPEAMENTO', 'Processo CDU-07-B', [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // PREPARAÇÃO 2: CHEFE cadastra atividades e disponibiliza
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        await adicionarAtividade(page, 'Atividade Teste');
        await disponibilizarCadastro(page);

        // EXECUÇÃO: GESTOR acessa o subprocesso
        await loginComoGestor(page); // Gestor da SGP
        await irParaSubprocesso(page, processoId, SIGLA_UNIDADE);

        // VERIFICAÇÃO
        await verificarDetalhesSubprocesso(page);
        await verificarCardAcaoVisivel(page, 'Análise de cadastro');
        await verificarCardAcaoInvisivel(page, 'Cadastro de atividades');
    });

    test('CHEFE deve ver o card "Mapa de competências" após GESTOR aceitar o cadastro', async ({page}) => {
        // PREPARAÇÃO 1: Cria e inicia o processo
        const processoId = await criarProcesso(page, 'MAPEAMENTO', 'Processo CDU-07-C', [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // PREPARAÇÃO 2: CHEFE cadastra e disponibiliza
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        await adicionarAtividade(page, 'Atividade Teste');
        await adicionarConhecimentoNaAtividade(page, 'Atividade Teste', 'Conhecimento Teste');
        await disponibilizarCadastro(page);

        // PREPARAÇÃO 3: GESTOR aceita o cadastro
        await loginComoGestor(page);
        await irParaSubprocesso(page, processoId, SIGLA_UNIDADE);
        await aceitarCadastro(page, 'Análise OK');

        // EXECUÇÃO: CHEFE retorna ao subprocesso
        await loginComoChefe(page);
        await irParaSubprocesso(page, processoId, SIGLA_UNIDADE);

        // VERIFICAÇÃO
        await verificarDetalhesSubprocesso(page);
        await verificarCardAcaoVisivel(page, 'Mapa de competências');
        await verificarCardAcaoInvisivel(page, 'Cadastro de atividades');
    });
});
