import {expect, test} from './fixtures/complete-fixtures.js';

/**
 * CDU-30 - Manter Administradores
 * 
 * Ator: ADMIN
 * 
 * Fluxo principal:
 * 1. O usuário acessa Configurações -> Administradores
 * 2. Sistema exibe lista de administradores
 * 3. Sistema apresenta opções para adicionar/remover
 * 
 * Regras de Negócio:
 * - Apenas usuários cadastrados no SGRH podem ser tornados administradores
 * - Um administrador não pode remover seu próprio acesso
 */
test.describe.serial('CDU-30 - Manter Administradores', () => {

    // ========================================================================
    // CENÁRIO 1: Navegação para página de administradores
    // ========================================================================

    test('Cenario 1: ADMIN acessa página de configurações', async ({page, autenticadoComoAdmin}) => {
        // Acessar configurações
        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);
    });

    // ========================================================================
    // CENÁRIO 2: Verificar seção de administradores
    // ========================================================================

    test('Cenario 2: Página de configurações contém seção de administradores', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);

        // Verificar se existe seção ou link para administradores
        await expect(page.getByRole('heading', {name: /Administradores/i})).toBeVisible();
    });

    // ========================================================================
    // CENÁRIO 3: Verificar lista de administradores
    // ========================================================================

    test('Cenario 3: Lista de administradores é exibida', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('btn-configuracoes').click();
        await expect(page).toHaveURL(/\/configuracoes/);

        // Procurar por tabela ou lista de administradores
        const tabela = page.locator('main table');
        await expect(tabela).toBeVisible();

        // Verificar que tabela tem dados (pelo menos o proprio admin)
        const linhas = tabela.locator('tbody tr');
        await expect(linhas).not.toHaveCount(0);

        // Verificar botão de adicionar administrador
        const btnAdicionar = page.getByRole('button', {name: /Adicionar|Novo/i});
        await expect(btnAdicionar).toBeVisible();
        await expect(btnAdicionar).toBeEnabled();
    });
});
