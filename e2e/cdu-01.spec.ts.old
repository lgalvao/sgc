import {expect, test} from './fixtures/auth-fixtures.js';
import {autenticar, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe('CDU-01 - Realizar login e exibir estrutura das telas', () => {
    test.beforeEach(async ({page}) => {
        await page.goto('/login');
    });

    test('Deve exibir erro com credenciais inválidas', async ({page}) => {
        await autenticar(page, USUARIOS.INVALIDO.titulo, USUARIOS.INVALIDO.senha);
        await expect(page.getByText(TEXTOS.login.ERRO_CREDENCIAIS)).toBeVisible();
    });

    test('Deve realizar login com sucesso (Perfil único)', async ({page}) => {
        await autenticar(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);

        await expect(page.getByText('GESTOR - COORD_11')).toBeVisible();
    });

    test('Deve exibir seleção de perfil se houver múltiplos', async ({page}) => {
        // Usuário 111111 (ADMIN_SEDOC_E_CHEFE_SEDOC) tem múltiplos perfis
        await loginComPerfil(page,
            USUARIOS.ADMIN_2_PERFIS.titulo,
            USUARIOS.ADMIN_2_PERFIS.senha,
            USUARIOS.ADMIN_2_PERFIS.perfil
        );

        // Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)
        await expect(page.locator('.user-info-text').getByText('ADMIN')).toBeVisible();
    });

    test('Deve exibir barra de navegação após login como ADMIN', async ({page, autenticadoComoAdmin}) => {
        // Login como ADMIN (191919) via fixture

        await expect(page.getByRole('link', {name: TEXTOS.comum.NOME_SISTEMA})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_RELATORIOS})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_HISTORICO})).toBeVisible();

        // Admin deve ver Parâmetros e Administradores
        await expect(page.getByTestId('btn-configuracoes')).toBeVisible();
        await expect(page.getByTestId('btn-administradores')).toBeVisible();
    });

    test('Deve exibir barra de navegação com restrições para GESTOR', async ({
                                                                                page,
                                                                                autenticadoComoGestor
                                                                            }) => {
        await expect(page.getByRole('link', {name: TEXTOS.comum.NOME_SISTEMA})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();

        // Deve ver 'Minha unidade' em vez de 'Unidades'
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_MINHA_UNIDADE})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeHidden();

        // Não deve ver configurações administrativas
        await expect(page.getByTestId('btn-configuracoes')).toBeHidden();
        await expect(page.getByTestId('btn-administradores')).toBeHidden();
    });

    test('Deve exibir barra de navegação com restrições para CHEFE', async ({
                                                                                page,
                                                                                autenticadoComoChefeSecao111
                                                                            }) => {
        await expect(page.getByRole('link', {name: TEXTOS.comum.NOME_SISTEMA})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();

        // Deve ver 'Minha unidade' em vez de 'Unidades'
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_MINHA_UNIDADE})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeHidden();

        // Não deve ver configurações administrativas
        await expect(page.getByTestId('btn-configuracoes')).toBeHidden();
        await expect(page.getByTestId('btn-administradores')).toBeHidden();
    });

    test('Deve exibir barra de navegação com restrições para SERVIDOR', async ({
                                                                                  page,
                                                                                  autenticadoComoServidor
                                                                              }) => {
        await expect(page.getByRole('link', {name: TEXTOS.comum.NOME_SISTEMA})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_PAINEL})).toBeVisible();

        // Deve ver 'Minha unidade' em vez de 'Unidades'
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_MINHA_UNIDADE})).toBeVisible();
        await expect(page.getByRole('link', {name: TEXTOS.comum.MENU_UNIDADES})).toBeHidden();

        // Não deve ver configurações administrativas
        await expect(page.getByTestId('btn-configuracoes')).toBeHidden();
        await expect(page.getByTestId('btn-administradores')).toBeHidden();
    });

    test('Deve exibir informações do usuário e controles', async ({page, autenticadoComoAdmin}) => {
        // Login como ADMIN (191919)

        // Para perfil ADMIN, a navbar exibe apenas 'ADMIN' (sem unidade)
        await expect(page.locator('.user-info-text').getByText('ADMIN')).toBeVisible();

        await expect(page.getByTestId('btn-configuracoes')).toBeVisible();
        await expect(page.getByTestId('btn-administradores')).toBeVisible();

        await expect(page.getByTestId('btn-logout')).toBeVisible();
    });

    test('Deve exibir rodapé', async ({page, autenticadoComoAdmin}) => {
        // Login como ADMIN (191919)

        await expect(page.getByText(TEXTOS.comum.RODAPE)).toBeVisible();
    });
});
