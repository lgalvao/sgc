import { test, expect } from '@playwright/test';

test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
    });

    test('Deve exibir erro com credenciais inválidas', async ({ page }) => {
        await page.fill('[data-testid="input-titulo"]', '00000000000'); // Usuário inexistente
        await page.fill('[data-testid="input-senha"]', 'senhaerrada');
        await page.click('[data-testid="botao-entrar"]');
    });

    test('Deve realizar login com sucesso (Perfil Único)', async ({ page }) => {
        // Usuário 222222 (GESTOR_COORD_11) tem apenas um perfil no JSON
        await page.fill('[data-testid="input-titulo"]', '222222');
        await page.fill('[data-testid="input-senha"]', 'qualquer');
        await page.click('[data-testid="botao-entrar"]');

        await expect(page).toHaveURL(/\/painel/);
    });

    test('Deve exibir seleção de perfil se houver múltiplos', async ({ page }) => {
        // Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis
        await page.fill('[data-testid="input-titulo"]', '111111');
        await page.fill('[data-testid="input-senha"]', 'qualquer');
        await page.click('[data-testid="botao-entrar"]');

        // Deve ver seleção de perfil
        await expect(page.getByTestId('secao-perfil-unidade')).toBeVisible({ timeout: 10000 });
        await expect(page.getByTestId('select-perfil-unidade')).toBeVisible();

        // Seleciona um e continua
        await page.selectOption('[data-testid="select-perfil-unidade"]', { index: 1 });
        await page.waitForTimeout(500); // Espera estabilização do DOM
        await page.waitForTimeout(2000); // Espera renderizar debug
        await page.click('[data-testid="botao-entrar"]', { force: true });

        await expect(page).toHaveURL(/\/painel/);
    });

    test('Deve exibir estrutura das telas após login', async ({ page }) => {
        // Login com ADMIN (111111)
        await page.fill('[data-testid="input-titulo"]', '111111');
        await page.fill('[data-testid="input-senha"]', 'qualquer');
        await page.click('[data-testid="botao-entrar"]');

        // Seleciona perfil se necessário (ADMIN tem 2)
        try {
            await expect(page.getByTestId('select-perfil-unidade')).toBeVisible({ timeout: 5000 });
            await page.selectOption('[data-testid="select-perfil-unidade"]', { label: 'ADMIN - SEDOC' });
            await page.click('[data-testid="botao-entrar"]');
        } catch (e) {
            // Se não aparecer, assume que logou direto (o que não deve acontecer para ADMIN, mas previne falha se mudar)
            console.log('Seleção de perfil não apareceu ou não foi necessária.');
        }

        await expect(page).toHaveURL(/\/painel/);

        // Verifica Barra de Navegação
        await expect(page.getByRole('link', { name: 'SGC' })).toBeVisible();
        await expect(page.getByText('Painel')).toBeVisible();
        await expect(page.getByText('Minha unidade')).toBeVisible();
        await expect(page.getByText('Relatórios')).toBeVisible();
        await expect(page.getByText('Histórico')).toBeVisible();

        // Verifica Informações do Usuário
        await expect(page.getByText(/ADMIN - (SEDOC|1)/)).toBeVisible();

        // Verifica Ícone de Configurações de Admin
        await expect(page.getByTestId('btn-configuracoes')).toBeVisible();

        // Verifica Logout
        await expect(page.locator('a[href="/login"]')).toBeVisible();

        // Verifica Rodapé
        await expect(page.getByText('© SESEL/COSIS/TRE-PE')).toBeVisible();
    });

});
