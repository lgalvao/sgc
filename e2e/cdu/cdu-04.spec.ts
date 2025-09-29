import {vueTest as test} from '../support/vue-specific-setup';
import {
    abrirModalInicializacaoProcesso,
    cancelarNoModal,
    confirmarInicializacaoNoModal,
    criarProcessoMapeamentoCompleto,
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    verificarModalConfirmacaoInicializacao,
    verificarModalFechado,
    verificarPaginaCadastroProcesso,
    verificarProcessoInicializadoComSucesso
} from './helpers';

test.describe('CDU-04: Iniciar processo de mapeamento', () => {
    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve iniciar processo de mapeamento', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await criarProcessoMapeamentoCompleto(page, 'Processo de Mapeamento Teste', '2025-12-31');

        await abrirModalInicializacaoProcesso(page);
        await verificarModalConfirmacaoInicializacao(page);
        await confirmarInicializacaoNoModal(page);

        await verificarProcessoInicializadoComSucesso(page);
    });

    test('deve cancelar o início do processo', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await criarProcessoMapeamentoCompleto(page, 'Processo de Mapeamento Teste Cancelar', '2025-12-31');

        await abrirModalInicializacaoProcesso(page);
        await verificarModalConfirmacaoInicializacao(page);
        await cancelarNoModal(page);

        await verificarModalFechado(page);
        await verificarPaginaCadastroProcesso(page);
    });
});