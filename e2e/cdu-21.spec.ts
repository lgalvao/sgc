import {vueTest as test} from './support/vue-specific-setup';
import { loginComoAdmin, loginComoGestor } from './helpers/auth';
import { navegarParaProcessoPorId } from './helpers/navegacao/navegacao';
import {
    abrirModalFinalizacaoProcesso,
    confirmarFinalizacaoNoModal,
} from './helpers/acoes/acoes-processo';
import { cancelarNoModal } from './helpers/acoes/acoes-modais';
import {
    verificarMensagemSucesso,
    verificarPermanenciaNaPaginaProcesso,
} from './helpers/verificacoes/verificacoes-basicas';
import {
    verificarBotaoFinalizarProcessoInvisivel,
    verificarBotaoFinalizarProcessoVisivel,
    verificarModalFinalizacaoFechado,
    verificarProcessoFinalizadoNoPainel,
} from './helpers/verificacoes/verificacoes-processo';
import { criarProcesso, submeterProcesso } from './helpers/acoes/api-helpers';

test.describe('CDU-21: Finalizar processo', () => {
    async function setupProcessoEmAndamento(page) {
        const nomeProcesso = `PROCESSO FINALIZAR TESTE - ${Date.now()}`;
        const processoId = await criarProcesso(page, 'MAPEAMENTO', nomeProcesso, ['1']); // Unidade 1 = SEDOC
        await submeterProcesso(page, processoId);
        const processo = { processo: { codigo: processoId } };
        await navegarParaProcessoPorId(page, processo.processo.codigo);
        return {nomeProcesso, processo};
    }

    test.describe('Administrador', () => {
        test('deve finalizar processo com sucesso', async ({page}) => {
            const {nomeProcesso, processo} = await setupProcessoEmAndamento(page);
            await loginComoAdmin(page);
            await navegarParaProcessoPorId(page, processo.processo.codigo);

            await verificarBotaoFinalizarProcessoVisivel(page);
            await abrirModalFinalizacaoProcesso(page);
            await confirmarFinalizacaoNoModal(page);

            await verificarMensagemSucesso(page, 'Processo finalizado com sucesso');
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
