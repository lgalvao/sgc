import {vueTest as test} from './support/vue-specific-setup';
import {
    loginComoAdmin,
    loginComoChefe,
    loginComoGestor,
    criarProcesso,
    submeterProcesso,
    limparProcessos,
    navegarParaVisualizacaoAtividades,
    disponibilizarCadastro,
    aceitarCadastro,
    irParaAjusteMapa,
    criarCompetencia,
    editarCompetencia,
    excluirCompetencia,
    submeterAjusteMapa,
    verificarCompetenciaVisivel,
    verificarCompetenciaNaoVisivel,
    associarTodasAtividadesACompetencia,
    verificarMensagemSucesso,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP é usada por ter CHEFE, GESTOR e um mapa vigente.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-16: Ajustar mapa de competências (com preparação)', () => {

    let processoId: number;

    // ANTES DE CADA TESTE: Orquestra o fluxo para deixar o subprocesso no estado REVISAO_CADASTRO_ACEITA.
    test.beforeEach(async ({page}) => {
        // 1. ADMIN: Cria e inicia um processo de REVISÃO.
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'REVISAO', gerarNomeUnico('Processo CDU-16'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // 2. CHEFE: Disponibiliza a revisão inicial.
        await loginComoChefe(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await disponibilizarCadastro(page);

        // 3. GESTOR: Aceita a revisão, liberando o ajuste do mapa para o CHEFE.
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await aceitarCadastro(page, 'Aceito para ajuste do mapa.');
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve criar, editar e excluir uma competência no ajuste do mapa', async ({page}) => {
        await loginComoChefe(page);
        await irParaAjusteMapa(page, processoId, SIGLA_UNIDADE);

        const nomeCompetencia = gerarNomeUnico('Competência Ajuste');
        const nomeCompetenciaEditado = gerarNomeUnico('Competência Ajuste Editada');

        // CRIAR
        await criarCompetencia(page, nomeCompetencia, []);
        await verificarCompetenciaVisivel(page, nomeCompetencia);

        // EDITAR
        await editarCompetencia(page, nomeCompetencia, nomeCompetenciaEditado, []);
        await verificarCompetenciaNaoVisivel(page, nomeCompetencia);
        await verificarCompetenciaVisivel(page, nomeCompetenciaEditado);

        // EXCLUIR
        await excluirCompetencia(page, nomeCompetenciaEditado);
        await verificarCompetenciaNaoVisivel(page, nomeCompetenciaEditado);
    });

    test('deve submeter o ajuste do mapa com sucesso', async ({page}) => {
        await loginComoChefe(page);
        await irParaAjusteMapa(page, processoId, SIGLA_UNIDADE);

        // Adiciona uma competência e a associa a todas as atividades para passar na validação.
        const nomeCompetencia = gerarNomeUnico('Competência Final');
        await criarCompetencia(page, nomeCompetencia, []);
        await associarTodasAtividadesACompetencia(page, nomeCompetencia);

        // Submete o ajuste
        await submeterAjusteMapa(page);

        // Verifica o sucesso
        await verificarMensagemSucesso(page, 'Mapa ajustado com sucesso');
    });
});
