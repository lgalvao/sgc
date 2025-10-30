import {vueTest as test} from './support/vue-specific-setup';
import {
    abrirModalInicializacaoProcesso,
    cancelarNoModal,
    clicarBotao,
    clicarProcessoNaTabela,
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    preencherFormularioProcesso,
    selecionarPrimeiraUnidade,
    verificarMensagemSucesso,
    verificarModalConfirmacaoInicializacao,
    verificarModalFechado,
    verificarPaginaCadastroProcesso,
    verificarProcessoInicializadoComSucesso
} from './helpers';

test.describe('CDU-05: Iniciar processo de revisão', () => {
    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve iniciar processo de revisão com sucesso', async ({page}) => {
        const nomeProcesso = `PROCESSO REVISAO TESTE - ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarPrimeiraUnidade(page);

        // Salvar o processo primeiro
        await clicarBotao(page, 'Salvar');
        await verificarMensagemSucesso(page, 'Processo salvo com sucesso!');

        // Voltar para o processo para poder iniciar
        await clicarProcessoNaTabela(page, nomeProcesso);
        await verificarPaginaCadastroProcesso(page);

        await abrirModalInicializacaoProcesso(page);
await verificarModalConfirmacaoInicializacao(page, nomeProcesso, 'REVISAO', 1);

        await verificarProcessoInicializadoComSucesso(page);
    });

    test('deve cancelar o início do processo de revisão', async ({page}) => {
        const nomeProcesso = `PROCESSO REVISAO CANCELAR - ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, nomeProcesso, 'REVISAO', '2025-12-31');
        await selecionarPrimeiraUnidade(page);

        // Salvar o processo
        await clicarBotao(page, 'Salvar');
        await verificarMensagemSucesso(page, 'Processo salvo com sucesso!');

        // Voltar para o processo
        await clicarProcessoNaTabela(page, nomeProcesso);
        await verificarPaginaCadastroProcesso(page);

        await abrirModalInicializacaoProcesso(page);
        await verificarModalConfirmacaoInicializacao(page, nomeProcesso, 'REVISAO', 1);
        await cancelarNoModal(page);

        await verificarModalFechado(page);
        await verificarPaginaCadastroProcesso(page);
    });
});