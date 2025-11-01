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
    navegarParaVisualizacaoAtividades,
    aceitarCadastro,
    irParaMapaCompetencias,
    criarCompetencia,
    associarTodasAtividadesACompetencia,
    disponibilizarMapa,
    irParaVisualizacaoMapa,
    apresentarSugestoes,
    validarMapa,
    verificarMensagemSucesso,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP é usada por ter CHEFE e GESTOR.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-19: Validar mapa de competências (com preparação)', () => {

    let processoId: number;

    // ANTES DE CADA TESTE: Orquestra o fluxo para deixar o subprocesso no estado MAPA_DISPONIBILIZADO.
    test.beforeEach(async ({page}) => {
        // 1. ADMIN: Cria e inicia o processo.
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('Processo CDU-19'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // 2. CHEFE: Cadastra atividades e disponibiliza.
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        await adicionarAtividade(page, 'Atividade para Validar');
        await adicionarConhecimentoNaAtividade(page, 'Atividade para Validar', 'Conhecimento');
        await disponibilizarCadastro(page);

        // 3. GESTOR: Aceita o cadastro.
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await aceitarCadastro(page, 'OK');

        // 4. CHEFE: Cria e disponibiliza o mapa.
        await loginComoChefe(page);
        await irParaMapaCompetencias(page, processoId, SIGLA_UNIDADE);
        const nomeCompetencia = gerarNomeUnico('Competência Validar');
        await criarCompetencia(page, nomeCompetencia, []);
        await associarTodasAtividadesACompetencia(page, nomeCompetencia);
        await disponibilizarMapa(page, '2025-01-01', 'Mapa para validação');
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve permitir ao CHEFE apresentar sugestões e validar o mapa', async ({page}) => {
        await loginComoChefe(page); // O Chefe da unidade é o validador nesta etapa

        // APRESENTAR SUGESTÕES
        await irParaVisualizacaoMapa(page, processoId, SIGLA_UNIDADE);
        const sugestao = 'Sugestão de melhoria no mapa.';
        await apresentarSugestoes(page, sugestao);
        await verificarMensagemSucesso(page, 'Sugestões apresentadas com sucesso');

        // VALIDAR MAPA
        // A navegação ocorre implicitamente dentro da ação `apresentarSugestoes` se bem-sucedida.
        // Se não, descomente a linha abaixo.
        // await irParaVisualizacaoMapa(page, processoId, SIGLA_UNIDADE);
        await validarMapa(page);
        await verificarMensagemSucesso(page, 'Mapa validado com sucesso');
    });
});
