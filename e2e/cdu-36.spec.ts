import type { Page } from '@playwright/test';
import {expect, test} from './fixtures/auth-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';

/**
 * CDU-36 - Gerar relatório de mapas
 * 
 * Ator: ADMIN
 * 
 * Fluxo principal:
 * 1. Usuário acessa Relatórios no menu
 * 2. Usuário seleciona "Mapas"
 * 3. Sistema gera relatório com mapas de competências
 * 4. Usuário pode exportar relatório
 */
test.describe.serial('CDU-36 - Gerar relatório de mapas', () => {
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
    });

    // ========================================================================
    // TESTES PRINCIPAIS
    // ========================================================================

    test('Cenario 1: ADMIN navega para página de relatórios', (async ({page: Page, autenticadoComoAdmin: void}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();

        await expect(page).toHaveURL(/\/relatorios/);
        await expect(page.getByRole('heading', {name: /Relatórios/i})).toBeVisible();
    });

    test('Cenario 2: Página exibe card de relatório de mapas', (async ({page: Page, autenticadoComoAdmin: void}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);

        await expect(page.getByTestId('card-relatorio-mapas')).toBeVisible();
    });

    test('Cenario 3: Abrir modal de Mapas Vigentes', (async ({page: Page, autenticadoComoAdmin: void}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await expect(page).toHaveURL(/\/relatorios/);

        await page.getByTestId('card-relatorio-mapas').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByRole('heading', {name: /Mapas Vigentes/i})).toBeVisible();
    });

    test('Cenario 4: Botão de exportação está disponível', (async ({page: Page, autenticadoComoAdmin: void}) => {
        

        await page.getByRole('link', {name: /Relatórios/i}).click();
        await page.getByTestId('card-relatorio-mapas').click();

        await expect(page.getByRole('dialog')).toBeVisible();
        await expect(page.getByRole('button', {name: /Exportar CSV/i})).toBeVisible();
    });
});
