import { test, expect, Page } from '@playwright/test';

async function login(page: Page, user: string, pass: string, profileLabel?: string) {
    await page.getByTestId('input-titulo').fill(user);
    await page.getByTestId('input-senha').fill(pass);
    await page.getByTestId('botao-entrar').click();

    const secaoPerfilUnidade = page.getByTestId('secao-perfil-unidade');
    if (await secaoPerfilUnidade.isVisible()) {
        await expect(page.getByTestId('select-perfil-unidade')).toBeVisible();
        if (profileLabel) {
            await page.getByTestId('select-perfil-unidade').selectOption({ label: profileLabel });
        } else {
            await page.getByTestId('select-perfil-unidade').selectOption({ index: 1 });
        }
        await page.waitForTimeout(500); // Espera estabilização do DOM
        await page.getByTestId('botao-entrar').click({ force: true });
    }
    await expect(page).toHaveURL(/\/painel/);
}



test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
    });

    test('Deve exibir erro com credenciais inválidas', async ({ page }) => {
        await page.getByTestId('input-titulo').fill('00000000000'); // Usuário inexistente
        await page.getByTestId('input-senha').fill('senhaerrada');
        await page.getByTestId('botao-entrar').click();
    });

    test('Deve realizar login com sucesso (Perfil Único)', async ({ page }) => {
        // Usuário 222222 (GESTOR_COORD_11) tem apenas um perfil
        await login(page, '222222', 'qualquer');
    });

    test('Deve exibir seleção de perfil se houver múltiplos', async ({ page }) => {
        // Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis
        await login(page, '111111', 'qualquer');
    });

    test('Deve exibir estrutura das telas após login', async ({ page }) => {
        // Login com ADMIN da SEDOC (111111)
        await login(page, '111111', 'qualquer', 'ADMIN - SEDOC');

        await expect(page).toHaveURL(/\/painel/);

        // Verifica Barra de Navegação
        await expect(page.getByRole('link', { name: 'SGC' })).toBeVisible();
        await expect(page.getByText('Painel')).toBeVisible();
        await expect(page.getByText('Minha unidade')).toBeVisible();
        await expect(page.getByText('Relatórios')).toBeVisible();
        await expect(page.getByText('Histórico')).toBeVisible();

        // Verifica Informações do Usuário
        await expect(page.getByText('ADMIN - SEDOC')).toBeVisible();

        // Verifica Ícone de Configurações de Admin
        await expect(page.getByTestId('btn-configuracoes')).toBeVisible();

        // Verifica Logout
        await expect(page.locator('a[href="/login"]')).toBeVisible();

        // Verifica Rodapé
        await expect(page.getByText('© SESEL/COSIS/TRE-PE')).toBeVisible();
    });

});
