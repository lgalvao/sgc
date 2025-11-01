import {vueTest as test} from './support/vue-specific-setup';
import {
    loginComoAdmin,
    loginComoChefe,
    criarProcesso,
    submeterProcesso,
    limparProcessos,
    irParaMapaCompetencias,
    clicarBotaoImpactosMapa,
    verificarMensagemNenhumImpacto,
    navegarParaCadastroAtividades,
    adicionarAtividade,
    adicionarConhecimentoNaAtividade,
    verificarModalImpactosAberto,
    fecharModalImpactos,
    verificarModalImpactosFechado,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP é usada por ter um mapa vigente, pré-requisito para REVISAO.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-12: Verificar impactos no mapa (com preparação)', () => {

    let processoId: number;

    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'REVISAO', gerarNomeUnico('Processo CDU-12'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve exibir "Nenhum impacto" quando não houver alterações', async ({page}) => {
        // EXECUÇÃO: Como CHEFE, vai direto para o mapa e verifica os impactos.
        await loginComoChefe(page);
        await irParaMapaCompetencias(page, processoId, SIGLA_UNIDADE);
        await clicarBotaoImpactosMapa(page);

        // VERIFICAÇÃO
        await verificarMensagemNenhumImpacto(page);
    });

    test('deve exibir modal com impactos após adicionar uma nova atividade', async ({page}) => {
        // PREPARAÇÃO: Adiciona uma nova atividade para gerar um impacto.
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        const nomeAtividade = gerarNomeUnico('Atividade com Impacto');
        await adicionarAtividade(page, nomeAtividade);
        await adicionarConhecimentoNaAtividade(page, nomeAtividade, 'Conhecimento');

        // EXECUÇÃO: Vai para o mapa e verifica os impactos.
        await irParaMapaCompetencias(page, processoId, SIGLA_UNIDADE);
        await clicarBotaoImpactosMapa(page);

        // VERIFICAÇÃO
        await verificarModalImpactosAberto(page);
        await fecharModalImpactos(page);
        await verificarModalImpactosFechado(page);
    });
});
