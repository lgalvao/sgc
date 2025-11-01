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
    verificarAlerta,
    fecharAlerta,
    verificarMensagemSucesso,
    verificarUrlDoPainel,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP é usada por ter CHEFE, GESTOR e um mapa vigente, pré-requisito para REVISAO.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-10: Disponibilizar revisão do cadastro (com preparação)', () => {

    let processoId: number;

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
        // Cria um processo de REVISÃO para a unidade SGP
        processoId = await criarProcesso(page, 'REVISAO', gerarNomeUnico('Processo CDU-10'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve validar atividades incompletas e depois disponibilizar com sucesso', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);

        // 1. Tenta disponibilizar com uma atividade sem conhecimentos e verifica o erro
        const atividadeIncompleta = gerarNomeUnico('Atividade Incompleta');
        await adicionarAtividade(page, atividadeIncompleta);
        await disponibilizarCadastro(page);
        await verificarAlerta(page, 'Atividades Incompletas');
        await fecharAlerta(page);

        // 2. Adiciona o conhecimento e disponibiliza com sucesso
        await adicionarConhecimentoNaAtividade(page, atividadeIncompleta, 'Conhecimento');
        await disponibilizarCadastro(page);

        // 3. Verifica o resultado
        await verificarMensagemSucesso(page, 'Disponibilização solicitada');
        await verificarUrlDoPainel(page);
    });

    test('deve exibir o histórico de análise após devolução do GESTOR', async ({page}) => {
        // ETAPA 1: CHEFE preenche e disponibiliza o cadastro
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        // Em um processo de REVISÃO, o mapa já vem com atividades.
        // Apenas disponibilizamos.
        await disponibilizarCadastro(page);
        await verificarUrlDoPainel(page);

        // ETAPA 2: GESTOR devolve o cadastro
        await loginComoGestor(page);
        const motivoDevolucao = 'Revisar o conhecimento X.';
        await devolverCadastro(page, { codigo: processoId, descricao: '' }, SIGLA_UNIDADE, motivoDevolucao);
        await verificarUrlDoPainel(page);

        // ETAPA 3: CHEFE retorna e verifica o histórico
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);

        await clicarBotaoHistoricoAnalise(page);
        await verificarModalHistoricoAnalise(page, motivoDevolucao);
    });
});
