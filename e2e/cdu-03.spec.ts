import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import type {Page} from '@playwright/test';
import type {useProcessoCleanup} from './hooks/hooks-limpeza.js';

test.describe('CDU-03 - Manter Processo', () => {

    test('Deve validar campos obrigatórios', async ({page, autenticadoComoAdmin}: {page: Page, autenticadoComoAdmin: void}) => {
        await page.getByTestId('btn-painel-criar-processo').click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // Verifica que botões estão desativados quando formulário está vazio
        await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();
        await expect(page.getByTestId('btn-processo-iniciar')).toBeDisabled();

        // Preenche descrição - botões ainda devem estar desativados (falta data e unidade)
        await page.getByTestId('inp-processo-descricao').fill('Descrição Teste');
        await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();

        // Preenche data limite - botões ainda devem estar desativados (falta unidade)
        const dataLimite = new Date();
        dataLimite.setDate(dataLimite.getDate() + 30);
        await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
        await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();

        // Seleciona tipo - botões ainda devem estar desativados (falta unidade)
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
        await expect(page.getByTestId('btn-processo-salvar')).toBeDisabled();

        // Seleciona unidade - agora botões devem estar habilitados
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();
        await page.getByTestId('chk-arvore-unidade-ASSESSORIA_11').click();
        await expect(page.getByTestId('btn-processo-salvar')).toBeEnabled();
        await expect(page.getByTestId('btn-processo-iniciar')).toBeEnabled();
    });

    test('Deve editar um processo existente', async ({page, autenticadoComoAdmin, cleanupAutomatico}: {page: Page, autenticadoComoAdmin: void, cleanupAutomatico: ReturnType<typeof useProcessoCleanup>}) => {
        const descricaoOriginal = `Processo para Edição - ${Date.now()}`;
        // Cria um processo inicial
        await criarProcesso(page, {
            descricao: descricaoOriginal,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11',
            expandir: ['SECRETARIA_1']
        });

        // Capturar ID do processo para cleanup
        await page.getByText(descricaoOriginal).click();
        await page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/);
        const url = new URL(page.url());
        const processoId = Number.parseInt(url.searchParams.get('codProcesso') || '0');
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        // Verifica que os dados foram carregados
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricaoOriginal);

        // Expandir árvore para verificar seleção
        await expect(page.getByText('Carregando unidades...')).toBeHidden();
        await page.getByTestId('btn-arvore-expand-SECRETARIA_1').click();

        await expect(page.getByTestId('chk-arvore-unidade-ASSESSORIA_11')).toBeChecked();

        // Modifica o processo
        const novaDescricao = descricaoOriginal + ' (Editado)';
        await page.getByTestId('inp-processo-descricao').fill(novaDescricao);
        await page.getByTestId('btn-processo-salvar').click();

        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByText(novaDescricao)).toBeVisible();
        await expect(page.getByText(descricaoOriginal, {exact: true})).not.toBeVisible();
    });

    test('Deve remover um processo', async ({page, autenticadoComoAdmin}: {page: Page, autenticadoComoAdmin: void}) => {
        const descricao = `Processo para Remoção - ${Date.now()}`;
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11',
            expandir: ['SECRETARIA_1']
        });

        await page.getByText(descricao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await page.getByTestId('btn-processo-remover').click();
        await expect(page.getByText(`Remover o processo '${descricao}'?`)).toBeVisible();

        const btnsRemover = page.getByRole('button', {name: 'Remover'});
        await expect(btnsRemover).toHaveCount(2);
        await btnsRemover.last().click();

        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByTestId('tbl-processos').getByText(descricao)).not.toBeVisible();
    });
});