import {expect, Page, test} from '@playwright/test';

test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {
    test.beforeEach(async ({page}) => await page.goto('/login'));

    test('Deve exibir erro com credenciais inválidas', async ({page}) => {
        await autenticar(page, '00000000000', 'senhaerrada'); // Usuário inexistente, senha errada
        await expect(page.getByText('Título ou senha inválidos.')).toBeVisible();
    });

    test('Deve realizar login com sucesso (Perfil Único)', async ({page}) => {
        // Usuário 222222 (GESTOR_COORD_11) tem apenas um perfil
        await loginUnicoPerfil(page, '222222', 'senha');
        await expect(page).toHaveURL(/\/painel/);
    });

    test('Deve exibir seleção de perfil se houver múltiplos', async ({page}) => {
        // Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis
        await loginMultiplosPerfis(page, '111111', 'senha', 'ADMIN - SEDOC');
        await expect(page).toHaveURL(/\/painel/);
    });

    test('Deve exibir estrutura das telas após login', async ({page}) => {
        // Login com ADMIN - SEDOC (111111)
        await loginMultiplosPerfis(page, '111111', 'senha', 'ADMIN - SEDOC');
        await expect(page).toHaveURL(/\/painel/);

        // Verifica Barra de Navegação
        await expect(page.getByRole('link', {name: 'SGC'})).toBeVisible();
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

    async function autenticar(page: Page, usuario: string, senha: string) {
        await page.getByTestId('input-titulo').fill(usuario);
        await page.getByTestId('input-senha').fill(senha);
        await page.getByTestId('botao-entrar').click();
    }

    async function loginUnicoPerfil(page: Page, usuario: string, senha: string) {
        await autenticar(page, usuario, senha);
    }

    async function loginMultiplosPerfis(page: Page, usuario: string, senha: string, perfilUnidade: string) {
        await autenticar(page, usuario, senha);

        // Se há mais de um perfil deve aparecer a seção de escolha de perfil-unidade
        await expect(page.getByTestId('secao-perfil-unidade')).toBeVisible();

        const selectPerfil = page.getByTestId('select-perfil-unidade');
        await expect(selectPerfil).toBeVisible();

        await selectPerfil.selectOption({label: perfilUnidade});
        await page.waitForLoadState('networkidle');
        await page.getByTestId('botao-entrar').click();
    }
});
