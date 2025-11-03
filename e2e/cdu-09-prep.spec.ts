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
    devolverCadastro,
    clicarBotaoHistoricoAnalise,
    verificarModalHistoricoAnalise,
    verificarBotaoHistoricoAnaliseVisivel,
    verificarAlerta,
    fecharAlerta,
    verificarMensagemSucesso,
    verificarUrlDoPainel,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP (id 2) é usada por ter CHEFE e GESTOR definidos.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-09: Disponibilizar cadastro (com preparação)', () => {

    let processoId: number;

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('Processo CDU-09'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve validar atividades incompletas e depois disponibilizar com sucesso', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);

        // 1. Adiciona atividade sem conhecimento e tenta disponibilizar
        const atividadeIncompleta = gerarNomeUnico('Incompleta');
        await adicionarAtividade(page, atividadeIncompleta);
        await disponibilizarCadastro(page);

        // 2. Verifica o alerta de erro
        await verificarAlerta(page, 'Atividades Incompletas');
        await fecharAlerta(page);

        // 3. Adiciona conhecimento à atividade e disponibiliza com sucesso
        await adicionarConhecimentoNaAtividade(page, atividadeIncompleta, 'Conhecimento');
        await disponibilizarCadastro(page);

        // 4. Verifica a mensagem de sucesso e o redirecionamento
        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);
    });

    test('deve exibir o histórico de análise após devolução do GESTOR', async ({page}) => {
        // ETAPA 1: CHEFE preenche e disponibiliza o cadastro
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        const atividade = gerarNomeUnico('Atividade para Devolver');
        await adicionarAtividade(page, atividade);
        await adicionarConhecimentoNaAtividade(page, atividade, 'Conhecimento');
        await disponibilizarCadastro(page);
        await verificarUrlDoPainel(page);

        // ETAPA 2: GESTOR devolve o cadastro com uma justificativa
        await loginComoGestor(page);
        const motivoDevolucao = 'Favor revisar a descrição da atividade.';
        await devolverCadastro(page, { codigo: processoId, descricao: '' }, SIGLA_UNIDADE, motivoDevolucao);

        // ETAPA 3: CHEFE retorna e verifica o histórico de análise
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);

        await verificarBotaoHistoricoAnaliseVisivel(page);
        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnalise(page, motivoDevolucao);
    });
});
