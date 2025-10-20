import {vueTest as test} from '../support/vue-specific-setup';
import {
    abrirModalFinalizacaoProcesso,
    cancelarNoModal,
    clicarBotao,
    confirmarFinalizacaoNoModal,
    criarProcessoCompleto,
    loginComoAdmin,
    loginComoGestor,
    navegarParaProcessoPorId,
    verificarBotaoFinalizarProcessoInvisivel,
    verificarBotaoFinalizarProcessoVisivel,
    verificarModalFinalizacaoFechado,
    verificarMensagemSucesso,
    verificarProcessoFinalizadoNoPainel
} from './helpers';
import * as processoService from '@/services/processoService';

test.describe('CDU-21: Finalizar processo', () => {

    async function setupProcessoEmAndamento(page) {
        const nomeProcesso = `PROCESSO FINALIZAR TESTE - ${Date.now()}`;
        const processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [1]); // Unidade 1 = SEDOC
        await processoService.iniciarProcesso(page, processo.codigo, 'MAPEAMENTO', [1]);
        // Simular a homologação para permitir a finalização
        // Esta é uma simplificação. Em um cenário real, seria necessário um setup mais complexo no backend.
        // Por agora, assumimos que o backend permitirá a finalização após o início.
        return {nomeProcesso, processo};
    }

    test.describe('Administrador', () => {
        test('deve finalizar processo com sucesso', async ({page}) => {
            const {nomeProcesso, processo} = await setupProcessoEmAndamento(page);
            await loginComoAdmin(page);
            await navegarParaProcessoPorId(page, processo.codigo);

            await verificarBotaoFinalizarProcessoVisivel(page);
            await abrirModalFinalizacaoProcesso(page);
            await confirmarFinalizacaoNoModal(page);

            await verificarMensagemSucesso(page, 'Processo finalizado com sucesso');
            await verificarProcessoFinalizadoNoPainel(page, nomeProcesso);
        });

        test('deve cancelar a finalização e permanecer na tela do processo', async ({page}) => {
            const {processo} = await setupProcessoEmAndamento(page);
            await loginComoAdmin(page);
            await navegarParaProcessoPorId(page, processo.codigo);

            await abrirModalFinalizacaoProcesso(page);
            await cancelarNoModal(page);

            await verificarModalFinalizacaoFechado(page);
            await page.waitForURL(`**/processos/${processo.codigo}`); // Garante que permaneceu na página
        });
    });

    test.describe('Restrições de perfil', () => {
        test('não deve exibir botão Finalizar para perfil Gestor', async ({page}) => {
            const {processo} = await setupProcessoEmAndamento(page);
            await loginComoGestor(page);
            await navegarParaProcessoPorId(page, processo.codigo);

            await verificarBotaoFinalizarProcessoInvisivel(page);
        });
    });
});