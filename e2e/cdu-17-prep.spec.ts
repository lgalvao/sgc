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
    disponibilizarCadastro,
    navegarParaVisualizacaoAtividades,
    aceitarCadastro,
    irParaMapaCompetencias,
    criarCompetencia,
    associarTodasAtividadesACompetencia,
    abrirModalDisponibilizarMapa,
    verificarBotaoModalDisponibilizarDesabilitado,
    preencherModalDisponibilizarMapa,
    verificarBotaoModalDisponibilizarHabilitado,
    cancelarNoModal,
    verificarModalFechado,
    confirmarNoModal,
    verificarMensagemSucesso,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP é usada por ter CHEFE e GESTOR.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-17: Disponibilizar mapa de competências (com preparação)', () => {

    let processoId: number;

    // ANTES DE CADA TESTE: Orquestra o fluxo para deixar o subprocesso no estado MAPA_EM_ANDAMENTO.
    test.beforeEach(async ({page}) => {
        // 1. ADMIN: Cria e inicia o processo.
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('Processo CDU-17'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // 2. CHEFE: Cadastra atividades e disponibiliza.
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        await adicionarAtividade(page, 'Atividade para Mapa');
        await disponibilizarCadastro(page);

        // 3. GESTOR: Aceita o cadastro.
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await aceitarCadastro(page, 'Aceito para mapa.');
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve validar, preencher, cancelar e submeter a disponibilização do mapa', async ({page}) => {
        await loginComoChefe(page);
        await irParaMapaCompetencias(page, processoId, SIGLA_UNIDADE);

        // PREPARAÇÃO: Cria uma competência e associa todas as atividades a ela para passar na validação do backend.
        const nomeCompetencia = gerarNomeUnico('Competência para Submissão');
        await criarCompetencia(page, nomeCompetencia, []);
        await associarTodasAtividadesACompetencia(page, nomeCompetencia);

        // ABRIR MODAL
        await abrirModalDisponibilizarMapa(page);

        // VALIDAR
        await verificarBotaoModalDisponibilizarDesabilitado(page);
        await preencherModalDisponibilizarMapa(page, '2025-12-31', 'Mapa finalizado.');
        await verificarBotaoModalDisponibilizarHabilitado(page);

        // CANCELAR
        await cancelarNoModal(page);
        await verificarModalFechado(page);

        // SUBMETER
        await abrirModalDisponibilizarMapa(page);
        await preencherModalDisponibilizarMapa(page, '2025-12-31', 'Mapa finalizado para envio.');
        await confirmarNoModal(page);

        await verificarMensagemSucesso(page, 'Mapa disponibilizado com sucesso');
    });
});
