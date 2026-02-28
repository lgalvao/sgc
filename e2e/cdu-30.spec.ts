import {expect, test} from './fixtures/complete-fixtures.js';

/**
 * CDU-30 - Manter Administradores
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Usuário logado como ADMIN
 */
test.describe.serial('CDU-30 - Manter Administradores', () => {

    test('Cenários CDU-30: ADMIN acessa e visualiza lista de administradores', async ({page, autenticadoComoAdmin}) => {
        // Cenario 1: Navegação para página de configurações
        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);

        // Cenario 2: Página contém seção de administradores
        await expect(page.getByRole('heading', {name: /Administradores/i})).toBeVisible();

        // Cenario 3: Lista de administradores é exibida com dados e botão de adição
        const tabela = page.locator('main table');
        await expect(tabela).toBeVisible();

        // Verificar que tabela tem dados (pelo menos o próprio admin)
        const linhas = tabela.locator('tbody tr');
        await expect(linhas.count()).not.toBe(0);

        // Verificar botão de adicionar administrador
        const btnAdicionar = page.getByRole('button', {name: /Adicionar|Novo/i});
        await expect(btnAdicionar).toBeVisible();
        await expect(btnAdicionar).toBeEnabled();
    });
});
