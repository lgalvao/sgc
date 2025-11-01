import {vueTest as test} from './support/vue-specific-setup';
import {expect} from '@playwright/test';
import {
    loginComoAdmin,
    navegarParaCriacaoProcesso,
    selecionarUnidadesPorSigla,
    limparProcessos,
    criarProcesso,
    SELETORES,
} from './helpers';

test.describe('CDU-03: Manter processo (com preparação)', () => {
    // Limpa todos os processos antes de iniciar os testes deste arquivo.
    test.beforeAll(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    // Garante que nenhum processo criado durante um teste afete o próximo.
    test.afterEach(async ({page}) => {
        await loginComoAdmin(page);
        await limparProcessos(page);
    });

    // ===== CRIAÇÃO DE PROCESSO =====

    test('deve criar processo com sucesso e redirecionar para o Painel', async ({page}) => {
        await loginComoAdmin(page);
        const descricao = `Processo E2E ${Date.now()}`;

        await navegarParaCriacaoProcesso(page);
        await page.locator(SELETORES.CAMPO_DESCRICAO).fill(descricao);
        await page.locator(SELETORES.CAMPO_TIPO).selectOption('MAPEAMENTO');
        await page.locator(SELETORES.CAMPO_DATA_LIMITE).fill('2025-12-31');
        await selecionarUnidadesPorSigla(page, ['SGP']);

        await page.getByRole('button', {name: /Salvar/i}).click();

        await expect(page).toHaveURL(/\/painel/, {timeout: 15000});
        await expect(page.getByText(descricao)).toBeVisible({timeout: 15000});
    });

    test('deve validar descrição obrigatória', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaCriacaoProcesso(page);

        await page.locator(SELETORES.CAMPO_TIPO).selectOption('MAPEAMENTO');
        await page.locator(SELETORES.CAMPO_DATA_LIMITE).fill('2025-12-31');
        await selecionarUnidadesPorSigla(page, ['SGP']);

        await page.getByRole('button', {name: /Salvar/i}).click();

        await expect(page).toHaveURL(/\/processo\/cadastro/);
    });

    test('deve validar ao menos uma unidade selecionada', async ({page}) => {
        await loginComoAdmin(page);
        await navegarParaCriacaoProcesso(page);

        await page.locator(SELETORES.CAMPO_DESCRICAO).fill('Processo sem unidades');
        await page.locator(SELETORES.CAMPO_TIPO).selectOption('MAPEAMENTO');
        await page.locator(SELETORES.CAMPO_DATA_LIMITE).fill('2025-12-31');

        await page.getByRole('button', {name: /Salvar/i}).click();

        await expect(page).toHaveURL(/\/processo\/cadastro/);
    });

    // ===== EDIÇÃO E REMOÇÃO DE PROCESSO =====
    test.describe('Edição e Remoção', () => {
        let processoId;
        const descricaoOriginal = `Processo para Editar/Remover ${Date.now()}`;

        // Cria um processo antes de cada teste neste grupo
        test.beforeEach(async ({page}) => {
            await loginComoAdmin(page);
            processoId = await criarProcesso(page, 'MAPEAMENTO', descricaoOriginal, ['SGP']);
            await page.goto('/painel');
            await expect(page.getByText(descricaoOriginal)).toBeVisible();
        });

        test('deve editar processo e modificar descrição', async ({page}) => {
            await loginComoAdmin(page);
            await page.click(`[data-testid="tabela-processos"] tr:has-text("${descricaoOriginal}")`);
            await expect(page).toHaveURL(new RegExp(`/processo/cadastro\\?idProcesso=${processoId}`));

            const novaDescricao = `Processo Editado ${Date.now()}`;
            await page.locator(SELETORES.CAMPO_DESCRICAO).fill(novaDescricao);
            await page.getByRole('button', {name: /Salvar/i}).click();

            await expect(page).toHaveURL(/\/painel/, {timeout: 15000});
            await expect(page.getByText(novaDescricao)).toBeVisible();
            await expect(page.getByText(descricaoOriginal)).not.toBeVisible();
        });

        test('deve exibir botão Remover apenas em modo de edição', async ({page}) => {
            await loginComoAdmin(page);
            await navegarParaCriacaoProcesso(page);
            await expect(page.getByRole('button', {name: /^Remover$/i})).not.toBeVisible();

            await page.goto('/painel');
            await page.click(`[data-testid="tabela-processos"] tr:has-text("${descricaoOriginal}")`);
            await expect(page).toHaveURL(new RegExp(`/processo/cadastro\\?idProcesso=${processoId}`));
            await expect(page.getByRole('button', {name: /^Remover$/i})).toBeVisible();
        });

        test('deve remover processo com sucesso após confirmação', async ({page}) => {
            await loginComoAdmin(page);
            await page.click(`[data-testid="tabela-processos"] tr:has-text("${descricaoOriginal}")`);

            await page.getByRole('button', {name: /^Remover$/i}).click();

            const modal = page.locator('.modal.show');
            await expect(modal).toBeVisible();
            await modal.getByRole('button', {name: /Remover/i}).click();

            await expect(page).toHaveURL(/\/painel/, {timeout: 15000});
            await expect(page.getByText(descricaoOriginal)).not.toBeVisible();
        });
    });
});
