import {expect} from '@playwright/test';
import {vueTest as test} from '../support/vue-specific-setup';
import {
    cancelarNoModal,
    clicarBotaoIniciarProcesso,
    clicarProcessoNaTabela,
    iniciarProcesso,
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    preencherFormularioProcesso,
    SELETORES,
    TEXTOS,
    URLS,
    verificarBotaoIniciarProcessoVisivel,
    verificarMensagemErro,
    verificarMensagemSucesso,
    verificarModalConfirmacaoIniciarProcessoInvisivel,
    verificarModalConfirmacaoIniciarProcessoVisivel,
    verificarPaginaCadastroProcesso,
    verificarTituloProcessos, verificarUrlDoPainel,
    verificarValorCampoDescricao
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
        await expect(page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"]`).getByText(descricaoProcessoExistente)).toBeVisible();

        await clicarProcessoNaTabela(page, descricaoProcessoExistente);

        // Verifica se a URL é a da página de detalhes do processo
        await expect(page).toHaveURL(new RegExp(`/processo/cadastro\\?idProcesso=100`));
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
        await iniciarProcesso(page);

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