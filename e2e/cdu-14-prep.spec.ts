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
    devolverParaAjustes,
    homologarCadastro,
    verificarMensagemSucesso,
    verificarUrlDoPainel,
    gerarNomeUnico,
    TEXTOS,
    esperarUrl,
    registrarAceiteRevisao,
    verificarCadastroDevolvidoComSucesso,
    verificarAceiteRegistradoComSucesso,
} from './helpers';

// A unidade SGP é usada por ter CHEFE, GESTOR e um mapa vigente.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-14: Analisar revisão de cadastro (com preparação)', () => {

    let processoId: number;

    // ANTES DE CADA TESTE: Cria um processo de REVISAO e o CHEFE o disponibiliza,
    // deixando-o pronto para a análise do GESTOR/ADMIN.
    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'REVISAO', gerarNomeUnico('Processo CDU-14'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        await loginComoChefe(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await disponibilizarCadastro(page);
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('GESTOR deve conseguir devolver e aceitar a revisão', async ({page}) => {
        // Devolver
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await devolverParaAjustes(page, 'Ajuste necessário.');
        await verificarCadastroDevolvidoComSucesso(page);

        // Preparar para o aceite: Chefe precisa disponibilizar novamente
        await loginComoChefe(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await disponibilizarCadastro(page);

        // Aceitar
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await registrarAceiteRevisao(page, 'Revisão aceita.');
        await verificarAceiteRegistradoComSucesso(page);
    });

    test('ADMIN deve conseguir homologar a revisão após aceite do GESTOR', async ({page}) => {
        // ETAPA 1: GESTOR aceita a revisão
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await registrarAceiteRevisao(page, 'Aceite para homologação.');
        await verificarUrlDoPainel(page);

        // ETAPA 2: ADMIN homologa
        await loginComoAdmin(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await homologarCadastro(page);
        await esperarUrl(page, new RegExp(`/processo/${processoId}/${SIGLA_UNIDADE}`));
    });
});
