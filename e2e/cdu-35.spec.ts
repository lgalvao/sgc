import {expect, test} from './fixtures/auth-fixtures.js';
import {USUARIOS} from './helpers/helpers-auth.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';

/**
 * CDU-35 - Gerar relatório de andamento
 * 
 * Ator: ADMIN
 * 
 * Fluxo principal:
 * 1. Usuário acessa Relatórios no menu
 * 2. Usuário seleciona "Andamento de processo"
 * 3. Sistema exibe relatório com dados do processo
 * 4. Usuário pode exportar relatório
 */
test.describe.serial('CDU-35 - Gerar relatório de andamento', () => {
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN navega para página de relatórios', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();

        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();
    });

    test('Cenario 2: Página exibe card de relatório de andamento', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);

        await expect(page.getByTestId('card-relatorio-andamento')).toBeVisible();
    });

    test('Cenario 3: Abrir modal de Andamento Geral', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);

        await page.getByTestId('card-relatorio-andamento').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: /Andamento Geral/i})).toBeVisible();
    });

    test('Cenario 4: Modal contém tabela de dados', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);
        await page.getByTestId('card-relatorio-andamento').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();

        const tabela = modal.locator('table');
        await expect(tabela).toBeVisible();
    });

    test('Cenario 5: Botão de exportação está disponível', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await page.getByTestId('card-relatorio-andamento').click();

        await expect(page.getByRole('dialog')).toBeVisible();
        await expect(page.getByRole('button', {name: /Exportar CSV/i})).toBeVisible();
    });

    test('Cenario 6: Filtros estão disponíveis', async ({page, autenticadoComoAdmin}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);

        await expect(page.getByTestId('sel-filtro-tipo')).toBeVisible();
        await expect(page.getByTestId('inp-filtro-data-inicio')).toBeVisible();
        await expect(page.getByTestId('inp-filtro-data-fim')).toBeVisible();
    });
});