import { expect, test } from '@playwright/test';
import { autenticar, login, loginComPerfil, USUARIOS } from './helpers/auth';

test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
    });

    test('Deve exibir erro com credenciais inválidas', async ({ page }) => {
        await autenticar(page, USUARIOS.INVALIDO.titulo, USUARIOS.INVALIDO.senha);
        await expect(page.getByText('Ocorreu um erro ao tentar realizar o login.')).toBeVisible();
    });

    test('Deve realizar login com sucesso (Perfil Único)', async ({ page }) => {
        // Usuário 222222 (GESTOR_COORD_11) tem apenas um perfil
        await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);

        // Verifica que o usuário está logado
        await expect(page.getByText('GESTOR - COORD_11')).toBeVisible();
    });

    test('Deve exibir seleção de perfil se houver múltiplos', async ({ page }) => {
        // Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis
        await loginComPerfil(page,
            USUARIOS.ADMIN_2_PERFIS.titulo,
            USUARIOS.ADMIN_2_PERFIS.senha,
            USUARIOS.ADMIN_2_PERFIS.perfil
        );

        // Verifica que o perfil selecionado está visível
        await expect(page.getByText('ADMIN - SEDOC')).toBeVisible();
    });

    test('Deve exibir barra de navegação após login', async ({ page }) => {
        // Login como ADMIN (191919)
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Verifica Barra de Navegação
        await expect(page.getByRole('link', { name: 'SGC' })).toBeVisible();
        await expect(page.getByText('Painel')).toBeVisible();
        await expect(page.getByText('Minha unidade')).toBeVisible();
        await expect(page.getByText('Relatórios')).toBeVisible();
        await expect(page.getByText('Histórico')).toBeVisible();
    });

    test('Deve exibir informações do usuário e controles', async ({ page }) => {
        // Login como ADMIN (191919)
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Verifica Informações do Usuário
        await expect(page.getByText('ADMIN - SEDOC')).toBeVisible();

        // Verifica Ícone de Configurações de Admin
        await expect(page.getByTestId('btn-configuracoes')).toBeVisible();

        // Verifica Logout
        await expect(page.getByTestId('btn-logout')).toBeVisible();
    });

    test('Deve exibir rodapé', async ({ page }) => {
        // Login como ADMIN (191919)
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Verifica Rodapé
        await expect(page.getByText('© SESEL/COSIS/TRE-PE')).toBeVisible();
    });
});