import {expect, test} from '@playwright/test';
import {loginComoAdmin, loginComoServidor, verificarElementosPainel, verificarUrl,} from './auxiliares-verificacoes';
import {ROTULOS, TEXTOS, URLS} from './constantes-teste';
import {login} from "./auxiliares-utils";

test.describe('CDU-01: Realizar login e exibir estrutura das telas', () => {
    test('deve carregar a página de login corretamente', async ({page}) => {
        await page.goto('/');
        await page.waitForLoadState('networkidle');
        await verificarUrl(page, `${URLS.LOGIN}`);

        await expect(page.getByLabel(ROTULOS.TITULO_ELEITORAL)).toBeVisible();
        await expect(page.getByLabel(ROTULOS.SENHA)).toBeVisible();
        await expect(page.getByRole('button', {name: TEXTOS.ENTRAR})).toBeVisible();
    });

    test('deve exibir erro para usuário não encontrado', async ({page}) => {
        await login(page, '0000000000'); // ID de usuário que não existe
        await page.getByRole('button', {name: TEXTOS.ENTRAR}).click();

        const notificacao = page.locator('.notification-container');
        await expect(notificacao.getByText(TEXTOS.ERRO_LOGIN_INVALIDO)).toBeVisible();
    });

    test('deve exibir estrutura da aplicação para SERVIDOR', async ({page}) => {
        await loginComoServidor(page);
        await verificarElementosPainel(page);
        const navBar = page.getByRole('navigation');

        await expect(navBar.getByRole('link', {name: 'Painel'})).toBeVisible();
        await expect(navBar.getByRole('link', {name: 'Minha unidade'})).toBeVisible();

        await expect(page.getByText('SERVIDOR - STIC')).toBeVisible();
        await expect(page.locator('a[title="Configurações do sistema"]')).not.toBeVisible();
        await expect(page.locator('a[title="Sair"]')).toBeVisible();
    });

    test('deve exibir estrutura da aplicação para ADMIN com acesso às configurações', async ({page}) => {
        await loginComoAdmin(page);
        await verificarElementosPainel(page);

        await expect(page.getByText('ADMIN - SEDOC')).toBeVisible();
        await expect(page.locator('a[title="Configurações do sistema"]')).toBeVisible();
    });

    test('deve fazer logout e retornar para a tela de login', async ({page}) => {
        await loginComoServidor(page);
        await verificarUrl(page, `${URLS.PAINEL}`);
        await page.locator('a[title="Sair"]').click();

        await verificarUrl(page, `${URLS.LOGIN}`);
        await expect(page.getByLabel(ROTULOS.TITULO_ELEITORAL)).toBeVisible();
    });
});