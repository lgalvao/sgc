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
    validarMapa,
    devolverParaAjustes,
    registrarAceiteRevisao, // Reutilizado para aceite de mapa
    homologarCadastro, // Reutilizado para homologar mapa
    verificarMensagemSucesso,
    verificarCadastroDevolvidoComSucesso,
    verificarAceiteRegistradoComSucesso,
    gerarNomeUnico,
} from './helpers';

// A unidade SGP é usada por ter CHEFE e GESTOR.
const SIGLA_UNIDADE = 'SGP';

test.describe('CDU-20: Analisar validação de mapa (com preparação)', () => {

    let processoId: number;

    // ANTES DE CADA TESTE: Orquestra o fluxo completo para deixar o subprocesso no estado MAPA_VALIDADO.
    test.beforeEach(async ({page}) => {
        // 1. ADMIN: Cria e inicia o processo.
        await loginComoAdmin(page);
        await limparProcessos(page);
        processoId = await criarProcesso(page, 'MAPEAMENTO', gerarNomeUnico('Processo CDU-20'), [SIGLA_UNIDADE]);
        await submeterProcesso(page, processoId);

        // 2. CHEFE: Submete cadastro de atividades.
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, processoId, SIGLA_UNIDADE);
        await adicionarAtividade(page, 'Atividade para Mapa');
        await adicionarConhecimentoNaAtividade(page, 'Atividade para Mapa', 'Conhecimento');
        await disponibilizarCadastro(page);

        // 3. GESTOR: Aceita o cadastro.
        await loginComoGestor(page);
        await navegarParaVisualizacaoAtividades(page, processoId, SIGLA_UNIDADE);
        await aceitarCadastro(page, 'OK');

        // 4. CHEFE: Submete o mapa.
        await loginComoChefe(page);
        await irParaMapaCompetencias(page, processoId, SIGLA_UNIDADE);
        const nomeCompetencia = gerarNomeUnico('Competência');
        await criarCompetencia(page, nomeCompetencia, []);
        await associarTodasAtividadesACompetencia(page, nomeCompetencia);
        await disponibilizarMapa(page, '2025-01-01', 'Mapa Concluído');

        // 5. CHEFE: Valida o mapa.
        await loginComoChefe(page);
        await irParaVisualizacaoMapa(page, processoId, SIGLA_UNIDADE);
        await validarMapa(page);
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('GESTOR deve poder devolver ou aceitar o mapa validado', async ({page}) => {
        // DEVOLVER
        await loginComoGestor(page);
        await irParaVisualizacaoMapa(page, processoId, SIGLA_UNIDADE);
        await devolverParaAjustes(page, 'Ajuste final necessário.');
        await verificarCadastroDevolvidoComSucesso(page);

        // Para testar o aceite, precisamos re-fazer o fluxo até a validação.
        // (Este fluxo é complexo demais para re-fazer aqui, será testado no teste de homologação abaixo)
    });

    test('ADMIN deve homologar o mapa após aceite do GESTOR', async ({page}) => {
        // ETAPA 1: GESTOR aceita o mapa validado.
        await loginComoGestor(page);
        await irParaVisualizacaoMapa(page, processoId, SIGLA_UNIDADE);
        await registrarAceiteRevisao(page); // Helper de aceite
        await verificarAceiteRegistradoComSucesso(page);

        // ETAPA 2: ADMIN homologa o mapa.
        await loginComoAdmin(page);
        await irParaVisualizacaoMapa(page, processoId, SIGLA_UNIDADE);
        await homologarCadastro(page); // Helper de homologação
        await verificarMensagemSucesso(page, 'Mapa homologado com sucesso');
    });
});
