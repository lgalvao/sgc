import { vueTest as test } from './support/vue-specific-setup';
import {
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    limparProcessosEmAndamento,
    preencherFormularioProcesso,
    salvarProcesso,
    removerProcesso,
    cancelarRemocaoProcesso,
    clicarBotaoRemoverProcesso,
    verificarRedirecionamentoParaPainel,
    verificarProcessoVisivelNoPainel,
    verificarNaoRedirecionadoParaPainel,
    verificarPaginaEdicaoProcesso,
    verificarBotaoRemoverVisivel,
    verificarBotaoRemoverNaoVisivel,
    verificarModalRemocaoVisivel,
    verificarModalRemocaoNaoVisivel,
    verificarProcessoNaoVisivelNoPainel,
} from './helpers';

test.describe('CDU-03: Manter processo (Expandido)', () => {
    test.beforeEach(async ({ page }) => {
        await limparProcessosEmAndamento(page);
        await loginComoAdmin(page);
    });

    test.afterEach(async ({ page }) => {
        await limparProcessosEmAndamento(page);
    });

    test('deve criar processo com sucesso e redirecionar para o Painel', async ({ page }) => {
        const descricao = `Processo E2E ${Date.now()}`;

        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, descricao, 'MAPEAMENTO', '2025-12-31', ['STIC']);
        await salvarProcesso(page);

        await verificarRedirecionamentoParaPainel(page);
        await verificarProcessoVisivelNoPainel(page, descricao);
    });

    test('deve validar descrição obrigatória', async ({ page }) => {
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, '', 'MAPEAMENTO', '2025-12-31', ['STIC']);
        await salvarProcesso(page);
        await verificarNaoRedirecionadoParaPainel(page);
    });

    test('deve validar ao menos uma unidade selecionada', async ({ page }) => {
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, 'Processo sem unidades', 'MAPEAMENTO', '2025-12-31', []);
        await salvarProcesso(page);
        await verificarNaoRedirecionadoParaPainel(page);
    });

    test('deve exibir botão Remover apenas em modo de edição', async ({ page }) => {
        await navegarParaCriacaoProcesso(page);
        await verificarBotaoRemoverNaoVisivel(page);

        await page.goto('http://localhost:5173/painel');
        await page.click('[data-testid="tabela-processos"] tr:has-text("Processo teste revisão CDU-05")');
        await verificarPaginaEdicaoProcesso(page);
        await verificarBotaoRemoverVisivel(page);
    });

    test('deve abrir modal de confirmação ao clicar em Remover', async ({ page }) => {
        await page.goto('http://localhost:5173/painel');
        await page.click('[data-testid="tabela-processos"] tr:has-text("Processo teste revisão CDU-05")');
        await verificarPaginaEdicaoProcesso(page);

        await clicarBotaoRemoverProcesso(page);

        await verificarModalRemocaoVisivel(page);
    });

    test('deve cancelar remoção e permanecer na tela de edição', async ({ page }) => {
        await page.goto('http://localhost:5173/painel');
        await page.click('[data-testid="tabela-processos"] tr:has-text("Processo teste revisão CDU-05")');
        await verificarPaginaEdicaoProcesso(page);

        await cancelarRemocaoProcesso(page);

        await verificarModalRemocaoNaoVisivel(page);
        await verificarPaginaEdicaoProcesso(page);
    });

    test('deve remover processo com sucesso após confirmação', async ({ page }) => {
        const descricao = `Processo para Remover ${Date.now()}`;
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, descricao, 'MAPEAMENTO', '2025-12-31', ['STIC']);
        await salvarProcesso(page);
        await verificarRedirecionamentoParaPainel(page);

        await page.click(`[data-testid="tabela-processos"] tr:has-text("${descricao}")`);
        await verificarPaginaEdicaoProcesso(page);

        await removerProcesso(page);

        await verificarRedirecionamentoParaPainel(page);
        await verificarProcessoNaoVisivelNoPainel(page, descricao);
    });
});
