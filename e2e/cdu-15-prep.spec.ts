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
    editarCompetencia,
    excluirCompetencia,
    verificarCompetenciaVisivel,
    verificarCompetenciaNaoVisivel,
    verificarAtividadesAssociadas,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP é usada por ter CHEFE e GESTOR.
const SIGLA_UNIDADE = 'SGP';
const ATIVIDADES_CRIADAS = [gerarNomeUnico('Atividade A'), gerarNomeUnico('Atividade B')];

test.describe('CDU-15: Manter Mapa de Competências (com preparação)', () => {

    let processoId: number;

    // ANTES DE CADA TESTE: Orquestra o fluxo completo para deixar o subprocesso no estado MAPA_EM_ANDAMENTO.
    test.beforeEach(async ({page}) => {
        // 1. ADMIN: Cria e inicia o processo.
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('Processo CDU-15'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // 2. CHEFE: Cadastra atividades e disponibiliza.
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        for (const atividade of ATIVIDADES_CRIADAS) {
            await adicionarAtividade(page, atividade);
        }
        await disponibilizarCadastro(page);

        // 3. GESTOR: Aceita o cadastro, movendo o subprocesso para a etapa de mapa.
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await aceitarCadastro(page, 'Aceito para iniciar mapa.');
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve criar, editar e excluir uma competência', async ({page}) => {
        await loginComoChefe(page);
        await irParaMapaCompetencias(page, processoId, SIGLA_UNIDADE);

        const nomeCompetencia = gerarNomeUnico('Competência Original');
        const nomeCompetenciaEditado = gerarNomeUnico('Competência Editada');

        // CRIAR
        await criarCompetencia(page, nomeCompetencia, [ATIVIDADES_CRIADAS[0]]);
        await verificarCompetenciaVisivel(page, nomeCompetencia);
        await verificarAtividadesAssociadas(page, nomeCompetencia, [ATIVIDADES_CRIADAS[0]]);

        // EDITAR
        await editarCompetencia(page, nomeCompetencia, nomeCompetenciaEditado, [ATIVIDADES_CRIADAS[1]]);
        await verificarCompetenciaNaoVisivel(page, nomeCompetencia);
        await verificarCompetenciaVisivel(page, nomeCompetenciaEditado);
        await verificarAtividadesAssociadas(page, nomeCompetenciaEditado, [ATIVIDADES_CRIADAS[1]]);

        // EXCLUIR
        await excluirCompetencia(page, nomeCompetenciaEditado);
        await verificarCompetenciaNaoVisivel(page, nomeCompetenciaEditado);
    });
});
