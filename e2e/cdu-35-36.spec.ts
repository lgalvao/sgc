import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {resetDatabase} from './hooks/hooks-limpeza';

/**
 * CDU-35 - Gerar relatório de andamento
 * CDU-36 - Gerar relatório de mapas
 * 
 * Ator: ADMIN
 * 
 * Fluxo principal (CDU-35):
 * 1. Usuário acessa Relatórios
 * 2. Usuário seleciona "Andamento de processo"
 * 3. Sistema exibe relatório com dados do processo
 * 
 * Fluxo principal (CDU-36):
 * 1. Usuário acessa Relatórios  
 * 2. Usuário seleciona "Mapas"
 * 3. Sistema gera arquivo com mapas de competências
 */
test.describe.serial('CDU-35/36 - Gerar relatórios', () => {
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
    });

    // ========================================================================
    // CENÁRIO 1: Navegação para página de relatórios
    // ========================================================================

    test('Cenario 1: ADMIN navega para página de relatórios', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        // Clicar em Relatórios na navbar
        await page.getByRole('link', {name: /Relatórios/i}).click();

        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();
    });

    // ========================================================================
    // CENÁRIO 2: Visualizar cards de relatórios disponíveis
    // ========================================================================

    test('Cenario 2: Página exibe cards de relatórios', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);

        // Verificar cards de relatórios
        await expect(page.getByTestId('card-relatorio-mapas')).toBeVisible();
        await expect(page.getByTestId('card-relatorio-andamento')).toBeVisible();
    });

    // ========================================================================
    // CENÁRIO 3: Abrir relatório de Mapas Vigentes (CDU-36)
    // ========================================================================

    test('Cenario 3: Abrir modal de Mapas Vigentes', async ({page}) => {
        // CDU-36: Passos 1-5
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);

        // Clicar no card de Mapas
        await page.getByTestId('card-relatorio-mapas').click();

        // Verificar que modal abriu
        await expect(page.getByRole('dialog')).toBeVisible();
        await expect(page.getByText(/Mapas Vigentes/i)).toBeVisible();

        // Verificar botão de exportação
        await expect(page.getByTestId('export-csv-mapas')).toBeVisible();
    });

    // ========================================================================
    // CENÁRIO 4: Abrir relatório de Andamento Geral (CDU-35)
    // ========================================================================

    test('Cenario 4: Abrir modal de Andamento Geral', async ({page}) => {
        // CDU-35: Passos 1-4
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);

        // Clicar no card de Andamento
        await page.getByTestId('card-relatorio-andamento').click();

        // Verificar que modal abriu
        await expect(page.getByRole('dialog')).toBeVisible();
        await expect(page.getByText(/Andamento Geral/i)).toBeVisible();

        // Verificar estrutura da tabela (CDU-35 passo 4)
        const tabela = page.locator('table');
        if (await tabela.count() > 0) {
            const headers = tabela.locator('th');
            // Colunas esperadas: Descrição, Tipo, Situação, Data Limite, etc.
            await expect(headers.filter({hasText: /Descrição/i}).first()).toBeVisible();
        }

        // Verificar botão de exportação
        await expect(page.getByTestId('export-csv-andamento')).toBeVisible();
    });

    // ========================================================================
    // CENÁRIO 5: Filtros de relatórios
    // ========================================================================

    test('Cenario 5: Filtros de tipo e data estão disponíveis', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);

        // Verificar filtros
        await expect(page.getByTestId('sel-filtro-tipo')).toBeVisible();
        await expect(page.getByTestId('inp-filtro-data-inicio')).toBeVisible();
        await expect(page.getByTestId('inp-filtro-data-fim')).toBeVisible();
    });
});
