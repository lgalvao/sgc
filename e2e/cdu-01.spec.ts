import {expect, test} from './fixtures/auth-fixtures.js';
import {autenticar, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import type {Page} from '@playwright/test';

test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {
    test.beforeEach(async ({page}: { page: Page }) => {
        await page.goto('/login');
    });

    test('Deve exibir erro com credenciais inválidas', async ({page}: { page: Page }) => {
        await autenticar(page, USUARIOS.INVALIDO.titulo, USUARIOS.INVALIDO.senha);
        await expect(page.getByText('Título ou senha inválidos.')).toBeVisible();
    });

    test('Deve realizar login com sucesso (Perfil Único)', async ({page}: { page: Page }) => {
        // Usuário 222222 (GESTOR_COORD_11) tem apenas um perfil
        await autenticar(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);

        await expect(page.getByText('GESTOR - COORD_11')).toBeVisible();
    });

    test('Deve exibir seleção de perfil se houver múltiplos', async ({page}: { page: Page }) => {
        // Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis
        await loginComPerfil(page,
            USUARIOS.ADMIN_2_PERFIS.titulo,
            USUARIOS.ADMIN_2_PERFIS.senha,
            USUARIOS.ADMIN_2_PERFIS.perfil
        );

        await expect(page.getByText('ADMIN - ADMIN')).toBeVisible();
    });

    test('Deve exibir barra de navegação após login como ADMIN', async ({page, autenticadoComoAdmin}: {
        page: Page,
        autenticadoComoAdmin: void
    }) => {
        // Login como ADMIN (191919) via fixture

        await expect(page.getByRole('link', {name: 'SGC'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Painel'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Unidades'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Relatórios'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Histórico'})).toBeVisible();

        // Admin deve ver Parâmetros e Administradores
        await expect(page.getByTestId('btn-parametros')).toBeVisible();
        await expect(page.getByTestId('btn-administradores')).toBeVisible();
    });

    test('Deve exibir barra de navegação com restrições para GESTOR', async ({
                                                                                page,
                                                                                autenticadoComoGestor
                                                                            }: { page: Page, autenticadoComoGestor: void }) => {
        await expect(page.getByRole('link', {name: 'SGC'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Painel'})).toBeVisible();

        // Deve ver 'Minha unidade' em vez de 'Unidades'
        await expect(page.getByRole('link', {name: 'Minha unidade'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Unidades'})).toBeHidden();

        // Não deve ver configurações administrativas
        await expect(page.getByTestId('btn-parametros')).toBeHidden();
        await expect(page.getByTestId('btn-administradores')).toBeHidden();
    });

    test('Deve exibir barra de navegação com restrições para CHEFE', async ({
                                                                                page,
                                                                                autenticadoComoChefeSecao111
                                                                            }: {
        page: Page,
        autenticadoComoChefeSecao111: void
    }) => {
        await expect(page.getByRole('link', {name: 'SGC'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Painel'})).toBeVisible();

        // Deve ver 'Minha unidade' em vez de 'Unidades'
        await expect(page.getByRole('link', {name: 'Minha unidade'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Unidades'})).toBeHidden();

        // Não deve ver configurações administrativas
        await expect(page.getByTestId('btn-parametros')).toBeHidden();
        await expect(page.getByTestId('btn-administradores')).toBeHidden();
    });

    test('Deve exibir barra de navegação com restrições para SERVIDOR', async ({
                                                                                  page,
                                                                                  autenticadoComoServidor
                                                                              }: {
        page: Page,
        autenticadoComoServidor: void
    }) => {
        await expect(page.getByRole('link', {name: 'SGC'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Painel'})).toBeVisible();

        // Deve ver 'Minha unidade' em vez de 'Unidades'
        await expect(page.getByRole('link', {name: 'Minha unidade'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Unidades'})).toBeHidden();

        // Não deve ver configurações administrativas
        await expect(page.getByTestId('btn-parametros')).toBeHidden();
        await expect(page.getByTestId('btn-administradores')).toBeHidden();
    });

    test('Deve exibir informações do usuário e controles', async ({page, autenticadoComoAdmin}: {
        page: Page,
        autenticadoComoAdmin: void
    }) => {
        // Login como ADMIN (191919)

        await expect(page.getByText('ADMIN - ADMIN')).toBeVisible();

        await expect(page.getByTestId('btn-parametros')).toBeVisible();
        await expect(page.getByTestId('btn-administradores')).toBeVisible();

        await expect(page.getByTestId('btn-logout')).toBeVisible();
    });

    test('Deve exibir rodapé', async ({page, autenticadoComoAdmin}: { page: Page, autenticadoComoAdmin: void }) => {
        // Login como ADMIN (191919)

        await expect(page.getByText('© SESEL/COSIS/TRE-PE')).toBeVisible();
    });
});
