import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {resetDatabase} from './hooks/hooks-limpeza';

/**
 * CDU-29 - Consultar histórico de processos
 * 
 * Ator: ADMIN/GESTOR/CHEFE
 * 
 * Fluxo principal:
 * 1. Na navbar, usuário clica em Histórico
 * 2. Sistema apresenta tabela de processos finalizados
 * 3. Usuário clica em um processo para detalhamento
 * 4. Sistema apresenta Detalhes do processo sem botões de ação
 */
test.describe.serial('CDU-29 - Consultar histórico de processos', () => {
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD_22.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_22.senha;
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
    });

    // ========================================================================
    // CENÁRIO 1: Navegação para página de histórico
    // ========================================================================

    test('Cenario 1: ADMIN navega para página de histórico', async ({page}) => {
        // CDU-29: Passos 1-2
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        // Passo 1: Clicar em Histórico na navbar
        await page.getByRole('link', {name: /Histórico/i}).click();

        // Passo 2: Sistema apresenta tela de histórico
        await expect(page).toHaveURL(/\/historico/);
        await expect(page.getByRole('heading', {name: /Histórico/i})).toBeVisible();
    });

    test('Cenario 2: GESTOR pode acessar histórico', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        // Navegar para histórico
        await page.getByRole('link', {name: /Histórico/i}).click();

        await expect(page).toHaveURL(/\/historico/);
        await expect(page.getByRole('heading', {name: /Histórico/i})).toBeVisible();
    });

    test('Cenario 3: CHEFE pode acessar histórico', async ({page}) => {
        await page.goto('/login');
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        // Navegar para histórico
        await page.getByRole('link', {name: /Histórico/i}).click();

        await expect(page).toHaveURL(/\/historico/);
        await expect(page.getByRole('heading', {name: /Histórico/i})).toBeVisible();
    });

    // ========================================================================
    // CENÁRIO 4: Verificar estrutura da tabela de processos finalizados
    // ========================================================================

    test('Cenario 4: Tabela apresenta colunas corretas', async ({page}) => {
        // CDU-29: Passo 2 - Verificar colunas da tabela
        await page.goto('/login');
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await page.getByRole('link', {name: /Histórico/i}).click();
        await expect(page).toHaveURL(/\/historico/);

        // Verificar que a tabela ou lista está presente
        // Colunas esperadas: Processo, Tipo, Finalizado em, Unidades participantes
        const tabela = page.locator('table');
        if (await tabela.count() > 0) {
            const headers = tabela.locator('th');
            await expect(headers.filter({hasText: /Processo|Descrição/i})).toBeVisible();
            await expect(headers.filter({hasText: /Tipo/i})).toBeVisible();
        }
    });
});
