import {vueTest as test} from './support/vue-specific-setup';
import {
    loginComoAdmin,
    loginComoGestor,
    criarProcesso,
    submeterProcesso,
    limparProcessos,
    navegarParaProcessoPorId,
    abrirModalFinalizacaoProcesso,
    cancelarNoModal,
    verificarModalFinalizacaoFechado,
    confirmarFinalizacaoNoModal,
    verificarMensagemSucesso,
    verificarProcessoFinalizadoNoPainel,
    verificarBotaoFinalizarProcessoVisivel,
    verificarBotaoFinalizarProcessoInvisivel,
    verificarPermanenciaNaPaginaProcesso,
    gerarNomeUnico,
} from './helpers';

test.describe('CDU-21: Finalizar processo (com preparação)', () => {

    let processoId: number;
    let nomeProcesso: string;

    // ANTES DE CADA TESTE: Cria e inicia um processo, deixando-o no estado EM_ANDAMENTO.
    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
        nomeProcesso = gerarNomeUnico('Processo CDU-21');
        processoId = await criarProcesso(page, 'MAPEAMENTO', nomeProcesso, ['SGP']);
        await submeterProcesso(page, processoId);
    });

    test.afterAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('ADMIN deve conseguir cancelar e finalizar um processo', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaProcessoPorId(page, processoId);

        // VERIFICA BOTÃO
        await verificarBotaoFinalizarProcessoVisivel(page);

        // CANCELAR
        await abrirModalFinalizacaoProcesso(page);
        await cancelarNoModal(page);
        await verificarModalFinalizacaoFechado(page);
        await verificarPermanenciaNaPaginaProcesso(page, processoId);

        // FINALIZAR
        await abrirModalFinalizacaoProcesso(page);
        await confirmarFinalizacaoNoModal(page);
        await verificarMensagemSucesso(page, 'Processo finalizado com sucesso');
        await verificarProcessoFinalizadoNoPainel(page, nomeProcesso);
    });

    test('GESTOR não deve ver o botão para finalizar o processo', async ({page}) => {
        await loginComoGestor(page);
        await navegarParaProcessoPorId(page, processoId);

        await verificarBotaoFinalizarProcessoInvisivel(page);
    });
});
