import {expect, test} from './fixtures/complete-fixtures.js';

/**
 * CDU-36 - Gerar relatório de mapas
 * 
 * Ator: ADMIN
 * 
 * Pré-condições:
 * - Usuário logado como ADMIN
 */
test.describe.serial('CDU-36 - Gerar relatório de mapas', () => {

    test('Cenários CDU-36: ADMIN navega e gera relatórios de mapas', async ({page, autenticadoComoAdmin}) => {
        // Cenario 1: Navegação para página de relatórios
        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();

        // Cenario 2: Exibir card de relatório de mapas
        await expect(page.getByTestId('card-relatorio-mapas')).toBeVisible();

        // Cenario 3: Abrir modal de Mapas Vigentes
        await page.getByTestId('card-relatorio-mapas').click();
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: /Mapas Vigentes/i})).toBeVisible();

        // Cenario 4: Botão de exportação está disponível
        await expect(page.getByRole('button', {name: /Exportar CSV/i})).toBeVisible();
    });
});
