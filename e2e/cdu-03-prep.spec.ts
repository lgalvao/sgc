import { vueTest as test } from './support/vue-specific-setup';
import {
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    limparProcessos,
    criarProcesso,
    preencherFormularioProcesso,
    salvarProcesso,
    removerProcesso,
    verificarRedirecionamentoParaPainel,
    verificarProcessoVisivelNoPainel,
    verificarNaoRedirecionadoParaPainel,
    verificarPaginaEdicaoProcesso,
    verificarBotaoRemoverVisivel,
    verificarBotaoRemoverNaoVisivel,
    verificarProcessoNaoVisivelNoPainel,
} from './helpers';

test.describe('CDU-03: Manter processo (com preparação)', () => {
    test.beforeAll(async ({ page }) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test.afterEach(async ({ page }) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    test('deve criar processo com sucesso e redirecionar para o Painel', async ({ page }) => {
        const descricao = `Processo E2E ${Date.now()}`;

        await loginComoAdmin(page);
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, descricao, 'MAPEAMENTO', '2025-12-31', ['SGP']);
        await salvarProcesso(page);

        await verificarRedirecionamentoParaPainel(page);
        await verificarProcessoVisivelNoPainel(page, descricao);
    });

    test('deve validar descrição obrigatória', async ({ page }) => {
        await loginComoAdmin(page);
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, '', 'MAPEAMENTO', '2025-12-31', ['SGP']);
        await salvarProcesso(page);
        await verificarNaoRedirecionadoParaPainel(page);
    });

    test('deve validar ao menos uma unidade selecionada', async ({ page }) => {
        await loginComoAdmin(page);
        await navegarParaCriacaoProcesso(page);
        await preencherFormularioProcesso(page, 'Processo sem unidades', 'MAPEAMENTO', '2025-12-31', []);
        await salvarProcesso(page);
        await verificarNaoRedirecionadoParaPainel(page);
    });

    test.describe('Edição e Remoção', () => {
        const descricaoOriginal = `Processo para Editar/Remover ${Date.now()}`;

        test.beforeEach(async ({ page }) => {
            await loginComoAdmin(page);
            await criarProcesso(page, 'MAPEAMENTO', descricaoOriginal, ['SGP']);
            await page.goto('/painel');
            await verificarProcessoVisivelNoPainel(page, descricaoOriginal);
        });

        test('deve editar processo e modificar descrição', async ({ page }) => {
            const novaDescricao = `Processo Editado ${Date.now()}`;

            await loginComoAdmin(page);
            await page.click(`[data-testid="tabela-processos"] tr:has-text("${descricaoOriginal}")`);
            await verificarPaginaEdicaoProcesso(page);

            await preencherFormularioProcesso(page, novaDescricao, 'MAPEAMENTO', '2025-12-31', ['SGP']);
            await salvarProcesso(page);

            await verificarRedirecionamentoParaPainel(page);
            await verificarProcessoVisivelNoPainel(page, novaDescricao);
            await verificarProcessoNaoVisivelNoPainel(page, descricaoOriginal);
        });

        test('deve exibir botão Remover apenas em modo de edição', async ({ page }) => {
            await loginComoAdmin(page);
            await navegarParaCriacaoProcesso(page);
            await verificarBotaoRemoverNaoVisivel(page);

            await page.goto('/painel');
            await page.click(`[data-testid="tabela-processos"] tr:has-text("${descricaoOriginal}")`);
            await verificarPaginaEdicaoProcesso(page);
            await verificarBotaoRemoverVisivel(page);
        });

        test('deve remover processo com sucesso após confirmação', async ({ page }) => {
            await loginComoAdmin(page);
            await page.click(`[data-testid="tabela-processos"] tr:has-text("${descricaoOriginal}")`);

            await removerProcesso(page);

            await verificarRedirecionamentoParaPainel(page);
            await verificarProcessoNaoVisivelNoPainel(page, descricaoOriginal);
        });
    });
});
