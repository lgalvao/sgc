import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso, extrairProcessoId, verificarProcessoNaTabela} from './helpers/helpers-processos.js';

test.describe('CDU-04 - Iniciar processo de mapeamento', () => {
    test('Deve iniciar um processo com sucesso', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        const descricao = `Processo para Iniciar - ${Date.now()}`;

        // 1. Cria processo em estado 'Criado'
        await criarProcesso(page, {
            descricao: descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11',
            expandir: ['SECRETARIA_1']
        });

        // 2. Entra na edição
        await page.getByText(descricao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // Capturar ID do processo para cleanup
        const processoId = await extrairProcessoId(page);
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        // Aguarda carregamento dos dados
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricao);

        // 3. Clica em Iniciar
        await page.getByTestId('btn-processo-iniciar').click();

        // 4. Verifica Modal
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText('Ao iniciar o processo, não será mais possível editá-lo')).toBeVisible();

        // 5. Cancela
        await page.getByTestId('btn-iniciar-processo-cancelar').click();
        await expect(modal).not.toBeVisible();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        // 6. Inicia novamente e Confirma
        await page.getByTestId('btn-processo-iniciar').click();
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // 7. Verifica redirecionamento e Status
        await expect(page).toHaveURL(/\/painel/);

        await verificarProcessoNaTabela(page, {
            descricao: descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });
    });
});