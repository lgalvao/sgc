import {vueTest as test} from '../support/vue-specific-setup';
import {
    abrirModalFinalizacaoProcesso,
    cancelarNoModal,
    confirmarFinalizacaoNoModal,
    criarProcessoCompleto,
    iniciarProcesso,
    loginComoAdmin,
    loginComoGestor,
    navegarParaProcessoPorId,
    verificarBotaoFinalizarProcessoInvisivel,
    verificarBotaoFinalizarProcessoVisivel,
    verificarMensagemSucesso,
    verificarModalFinalizacaoFechado,
    verificarPermanenciaNaPaginaProcesso,
    verificarProcessoFinalizadoNoPainel,
} from '../helpers';

test.describe('CDU-21: Finalizar processo', () => {
    async function setupProcessoEmAndamento(page) {
        const nomeProcesso = `PROCESSO FINALIZAR TESTE - ${Date.now()}`;
        const processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [1]); // Unidade 1 = SEDOC
        await navegarParaProcessoPorId(page, processo.processo.codigo);
        await iniciarProcesso(page);
        return {nomeProcesso, processo};
    }

    test.describe('Administrador', () => {
        test('deve finalizar processo', async ({page}) => {
            const {nomeProcesso, processo} = await setupProcessoEmAndamento(page);
            await loginComoAdmin(page);
            await navegarParaProcessoPorId(page, processo.processo.codigo);

            await verificarBotaoFinalizarProcessoVisivel(page);
            await abrirModalFinalizacaoProcesso(page);
            await confirmarFinalizacaoNoModal(page);

            await verificarMensagemSucesso(page, 'Processo finalizado');
            await verificarProcessoFinalizadoNoPainel(page, nomeProcesso);
        });

        test('deve cancelar a finalização e permanecer na tela do processo', async ({page}) => {
            const {processo} = await setupProcessoEmAndamento(page);
            await loginComoAdmin(page);
            await navegarParaProcessoPorId(page, processo.processo.codigo);

            await abrirModalFinalizacaoProcesso(page);
            await cancelarNoModal(page);

            await verificarModalFinalizacaoFechado(page);
            await verificarPermanenciaNaPaginaProcesso(page, processo.processo.codigo);
        });
    });

    test.describe('Restrições de perfil', () => {
        test('não deve exibir botão Finalizar para perfil Gestor', async ({page}) => {
            const {processo} = await setupProcessoEmAndamento(page);
            await loginComoGestor(page);
            await navegarParaProcessoPorId(page, processo.processo.codigo);

            await verificarBotaoFinalizarProcessoInvisivel(page);
        });
    });
});
