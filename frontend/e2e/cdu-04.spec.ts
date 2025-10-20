import {vueTest as test} from './support/vue-specific-setup';
import {
    abrirModalInicializacaoProcesso,
    cancelarNoModal,
    clicarBotao,
    clicarProcessoNaTabela,
    confirmarInicializacaoNoModal,
    criarProcessoMapeamentoCompleto,
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    verificarMensagemSucesso,
    verificarModalConfirmacaoInicializacao,
    verificarModalFechado,
    verificarPaginaCadastroProcesso,
    verificarProcessoInicializadoComSucesso
} from './helpers';

test.describe('CDU-04: Iniciar processo de mapeamento', () => {
    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve iniciar processo de mapeamento', async ({page}) => {
        const nomeProcesso = `PROCESSO MAPA TESTE - ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await criarProcessoMapeamentoCompleto(page, nomeProcesso, '2025-12-31');

        // Salvar o processo primeiro
        await clicarBotao(page, 'Salvar');
        await verificarMensagemSucesso(page, 'Processo salvo com sucesso!');

        // Voltar para o processo para poder iniciar
        await clicarProcessoNaTabela(page, nomeProcesso);
        await verificarPaginaCadastroProcesso(page);

        await abrirModalInicializacaoProcesso(page);
        await verificarModalConfirmacaoInicializacao(page, nomeProcesso, 'Mapeamento', 1);
        await confirmarInicializacaoNoModal(page);

        await verificarProcessoInicializadoComSucesso(page);
    });

    test('deve cancelar o início do processo', async ({page}) => {
        const nomeProcesso = `PROCESSO MAPA CANCELAR - ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await criarProcessoMapeamentoCompleto(page, nomeProcesso, '2025-12-31');

        // Salvar o processo
        await clicarBotao(page, 'Salvar');
        await verificarMensagemSucesso(page, 'Processo salvo com sucesso!');

        // Voltar para o processo
        await clicarProcessoNaTabela(page, nomeProcesso);
        await verificarPaginaCadastroProcesso(page);

        await abrirModalInicializacaoProcesso(page);
        await verificarModalConfirmacaoInicializacao(page, nomeProcesso, 'Mapeamento', 1);
        await cancelarNoModal(page);

        await verificarModalFechado(page);
        await verificarPaginaCadastroProcesso(page);
    });
});