import {vueTest as test} from './support/vue-specific-setup';
import {
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    loginComoServidor,
    criarProcesso,
    submeterProcesso,
    limparProcessos,
    navegarParaCadastroAtividades,
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    disponibilizarCadastro,
    navegarParaVisualizacaoAtividades,
    aceitarCadastro,
    irParaMapaCompetencias,
    criarCompetencia,
    associarTodasAtividadesACompetencia,
    disponibilizarMapa,
    irParaVisualizacaoMapa,
    verificarCabecalhoUnidade,
    verificarListagemAtividadesEConhecimentos,
    verificarModoSomenteLeitura,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP é usada por ter CHEFE, GESTOR e SERVIDOR.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-18: Visualizar mapa de competências (com preparação)', () => {

    let processoId: number;

    // ANTES DE CADA TESTE: Orquestra o fluxo completo para deixar o subprocesso no estado MAPA_DISPONIBILIZADO.
    test.beforeEach(async ({page}) => {
        // 1. ADMIN: Cria e inicia o processo.
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('Processo CDU-18'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // 2. CHEFE: Cadastra atividades e disponibiliza.
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        await adicionarAtividade(page, 'Atividade A');
        await adicionarConhecimentoNaAtividade(page, 'Atividade A', 'Conhecimento 1');
        await disponibilizarCadastro(page);

        // 3. GESTOR: Aceita o cadastro.
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await aceitarCadastro(page, 'OK');

        // 4. CHEFE: Cria e disponibiliza o mapa.
        await loginComoChefe(page);
        await irParaMapaCompetencias(page, processoId, SIGLA_UNIDADE);
        const nomeCompetencia = gerarNomeUnico('Competência');
        await criarCompetencia(page, nomeCompetencia, []);
        await associarTodasAtividadesACompetencia(page, nomeCompetencia);
        await disponibilizarMapa(page, '2025-01-01', 'Mapa Concluído');
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('ADMIN e CHEFE devem visualizar o mapa corretamente', async ({page}) => {
        // ADMIN
        await loginComoAdmin(page);
        await irParaVisualizacaoMapa(page, processoId, SIGLA_UNIDADE);
        await verificarCabecalhoUnidade(page, SIGLA_UNIDADE);
        await verificarListagemAtividadesEConhecimentos(page);

        // CHEFE
        await loginComoChefe(page);
        await irParaVisualizacaoMapa(page, processoId, SIGLA_UNIDADE);
        await verificarCabecalhoUnidade(page, SIGLA_UNIDADE);
        await verificarListagemAtividadesEConhecimentos(page);
    });

    test('SERVIDOR deve visualizar o mapa em modo somente leitura', async ({page}) => {
        await loginComoServidor(page);
        await irParaVisualizacaoMapa(page, processoId, SIGLA_UNIDADE);

        await verificarCabecalhoUnidade(page, SIGLA_UNIDADE);
        await verificarListagemAtividadesEConhecimentos(page);
        await verificarModoSomenteLeitura(page);
    });
});
