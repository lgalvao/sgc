import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso, extrairProcessoId, verificarDetalhesProcesso} from './helpers/helpers-processos.js';
import type {Page} from '@playwright/test';

test.describe('CDU-04 - Iniciar Processo', () => {

    test('Deve iniciar um processo e validar criação de subprocessos e alertas', async ({
                                                                                            page,
                                                                                            autenticadoComoAdmin,
                                                                                            cleanupAutomatico
                                                                                        }: {
        page: Page,
        autenticadoComoAdmin: void,
        cleanupAutomatico: any
    }) => {
        const descricao = `CDU-04 Iniciar - ${Date.now()}`;

        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 15,
            unidade: ['SECRETARIA_1', 'ASSESSORIA_11', 'ASSESSORIA_12'], // Interoperacional + Operacionais
            expandir: ['SECRETARIA_1'],
            iniciar: false
        });

        // Clica na tabela para abrir detalhes
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();

        // Wait for page load and capture ID
        await page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/);
        const processoId = await extrairProcessoId(page);
        cleanupAutomatico.registrar(processoId);

        // Aguarda carregamento dos dados
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricao);

        // 2. Iniciar processo
        await page.getByTestId('btn-processo-iniciar').click();

        // Validar texto do modal
        const modal = page.getByRole('dialog');
        await expect(modal.getByText(/Ao iniciar o processo, não será mais possível editá-lo ou removê-lo/i)).toBeVisible();

        // 3. Cancelar e verificar que continua na mesma tela
        await page.getByTestId('btn-iniciar-processo-cancelar').click();
        await expect(page).toHaveURL(new RegExp(`codProcesso=${processoId}`));
        await expect(page.getByTestId('btn-processo-iniciar')).toBeVisible();

        // 4. Confirmar iniciação
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // Wait for painel with process status update
        await page.waitForURL(/\/painel/);
        await expect(page.getByText(/Processo iniciado/i).first()).toBeVisible();

        const linha = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descricao)});
        await expect(linha.getByText('Em andamento')).toBeVisible();

        // 5. Verificar Detalhes
        await linha.click();
        await page.waitForURL(new RegExp(`\\/processo\\/${processoId}$`));

        // Na tela de detalhes, verificar se os subprocessos foram criados
        await verificarDetalhesProcesso(page, {
            descricao: descricao,
            tipo: 'Mapeamento',
            situacao: 'Em andamento'
        });

        // Verificar unidades na tabela de participantes
        await expect(page.locator('tr', {hasText: 'ASSESSORIA_11'})).toContainText('Não iniciado');
        await expect(page.locator('tr', {hasText: 'ASSESSORIA_12'})).toContainText('Não iniciado');

        // Secretaria 1 é interoperacional e também deve ter um subprocesso (conforme seed.sql)
        await expect(page.locator('tr', {hasText: 'SECRETARIA_1'})).toContainText('Não iniciado');
    });
});
