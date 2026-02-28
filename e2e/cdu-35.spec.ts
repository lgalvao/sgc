import {expect, test} from './fixtures/complete-fixtures.js';

/**
 * CDU-35 - Gerar relatório de andamento
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Usuário logado como ADMIN
 */
test.describe.serial('CDU-35 - Gerar relatório de andamento', () => {

    test('Cenários CDU-35: ADMIN navega e gera relatórios de andamento', async ({page, autenticadoComoAdmin}) => {
        // Cenario 1: Navegação para página de relatórios
        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();

        // Cenario 6: Filtros estão disponíveis
        await expect(page.getByLabel('Filtrar por Tipo')).toBeVisible();
        await expect(page.getByLabel('Data Início')).toBeVisible();
        await expect(page.getByLabel('Data Fim')).toBeVisible();

        // Cenario 2: Exibir card de relatório de andamento
        await expect(page.getByTestId('card-relatorio-andamento')).toBeVisible();

        // Cenario 3: Abrir modal de Andamento Geral
        await page.getByTestId('card-relatorio-andamento').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: /Andamento Geral/i})).toBeVisible();

        // Cenario 4: Modal contém tabela de dados
        const tabela = modal.locator('table');
        await expect(tabela).toBeVisible();

        // Cenario 5: Botão de exportação está disponível
        await expect(page.getByRole('button', {name: /Exportar CSV/i})).toBeVisible();
    });
});
