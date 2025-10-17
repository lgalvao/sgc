import {vueTest as test} from '../support/vue-specific-setup';
import {
    cancelarNoModal,
    clicarBotaoIniciarProcesso,
    clicarProcessoNaTabela,
    iniciarProcesso,
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    preencherFormularioProcesso,
    TEXTOS,
    URLS,
    verificarBotaoIniciarProcessoVisivel,
    verificarMensagemErro,
    verificarMensagemSucesso,
    verificarModalConfirmacaoIniciarProcessoInvisivel,
    verificarModalConfirmacaoIniciarProcessoVisivel,
    verificarPaginaCadastroProcesso,
    verificarTituloProcessos,
    verificarUrlDoPainel,
    verificarValorCampoDescricao,
    verificarVisibilidadeProcesso
} from './helpers';


test.describe('CDU-05: Iniciar processo de revisão', () => {
    test.beforeEach(async ({page}) => await loginComoAdmin(page));

    test('deve navegar para processo Criado e exibir botão Iniciar', async ({page}) => {
        await verificarTituloProcessos(page);
        await clicarProcessoNaTabela(page, 'Processo teste revisão CDU-05');

        await verificarPaginaCadastroProcesso(page);
        await verificarValorCampoDescricao(page, 'Processo teste revisão CDU-05');
        await verificarBotaoIniciarProcessoVisivel(page);
    });

    test('deve exibir modal de confirmação para processo válido', async ({page}) => {
        const descricaoProcessoExistente = "Processo teste revisão CDU-05"; // Usar processo existente nos mocks
        await page.goto(URLS.PAINEL);
        await verificarVisibilidadeProcesso(page, descricaoProcessoExistente, true);

        await clicarProcessoNaTabela(page, descricaoProcessoExistente);

        // Verifica se a navegação levou à página de cadastro do processo
        await verificarPaginaCadastroProcesso(page);
        await clicarBotaoIniciarProcesso(page);

        await verificarModalConfirmacaoIniciarProcessoVisivel(page);
    });

    test('deve cancelar iniciação e permanecer na mesma tela', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, 'Teste CDU-05 Cancelar', 'Revisão', '2025-12-31', true);
        await clicarBotaoIniciarProcesso(page);
        await cancelarNoModal(page);

        await verificarModalConfirmacaoIniciarProcessoInvisivel(page);
        await verificarPaginaCadastroProcesso(page); // verificarUrlCadastroProcesso foi substituída por verificarPaginaCadastroProcesso
    });

    test('deve iniciar processo com sucesso', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, 'Teste CDU-05 Sucesso', 'Revisão', '2025-12-31', true);
        await iniciarProcesso(page, 1, 'Revisão', [1, 2, 3]);

        await verificarMensagemSucesso(page, TEXTOS.PROCESSO_INICIADO);
        await verificarUrlDoPainel(page);
    });

    test('deve validar dados antes de mostrar modal', async ({page}) => {
        await navegarParaCriacaoProcesso(page);
        await clicarBotaoIniciarProcesso(page);

        await verificarMensagemErro(page, TEXTOS.DADOS_INCOMPLETOS);
        await verificarModalConfirmacaoIniciarProcessoInvisivel(page);
    });
});