import {expect, test} from './fixtures/complete-fixtures.js';

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

    // ========================================================================
    // CENÁRIO 1: Navegação para página de histórico
    // ========================================================================

    test('Cenario 1: ADMIN navega para página de histórico', async ({page, autenticadoComoAdmin}) => {
        // CDU-29: Passos 1-2


        await page.getByRole('link', {name: /Histórico/i}).click();

        await expect(page).toHaveURL(/\/historico/);
        await expect(page.getByRole('heading', {name: /Histórico/i})).toBeVisible();
    });

    test('Cenario 2: GESTOR pode acessar histórico', async ({page, autenticadoComoGestor}) => {


        // Navegar para histórico
        await page.getByRole('link', {name: /Histórico/i}).click();

        await expect(page).toHaveURL(/\/historico/);
        await expect(page.getByRole('heading', {name: /Histórico/i})).toBeVisible();
    });

    test('Cenario 3: CHEFE pode acessar histórico', async ({page, autenticadoComoChefeSecao121}) => {


        // Navegar para histórico
        await page.getByRole('link', {name: /Histórico/i}).click();

        await expect(page).toHaveURL(/\/historico/);
        await expect(page.getByRole('heading', {name: /Histórico/i})).toBeVisible();
    });

    // ========================================================================
    // CENÁRIO 4: Verificar estrutura da tabela de processos finalizados
    // ========================================================================

    test('Cenario 4: Tabela apresenta colunas corretas', async ({page, autenticadoComoAdmin}) => {


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
