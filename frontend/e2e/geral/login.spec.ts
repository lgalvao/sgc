import { expect } from '@playwright/test';
import { vueTest as test } from '../support/vue-specific-setup';
import { testUsers } from '../support/helpers';

test.describe('Login', () => {

    test('deve permitir o login com credenciais válidas e navegar para o painel', async ({ page }) => {
        const chefeUser = testUsers.chefe;

        // Navegar para a página de login
        await page.goto('/');

        // Preencher o formulário de login com as credenciais do usuário de teste
        await page.getByLabel('Título eleitoral').fill(chefeUser.tituloEleitoral);
        await page.getByLabel('Senha').fill(chefeUser.senha);

        // Clicar no botão de login
        await page.getByRole('button', { name: 'Entrar' }).click();

        // Verificar se a navegação para a página /painel ocorreu
        await expect(page).toHaveURL(/.*\/painel/);
        await expect(page.getByRole('heading', { name: /Processos de Mapeamento/i })).toBeVisible();
    });

    test('deve mostrar uma mensagem de erro com credenciais inválidas', async ({ page }) => {
        const chefeUser = testUsers.chefe;
        // Navegar para a página de login
        await page.goto('/');

        // Preencher com credenciais inválidas
        await page.getByLabel('Título eleitoral').fill(chefeUser.tituloEleitoral);
        await page.getByLabel('Senha').fill('senha-super-errada');

        // Clicar no botão de login
        await page.getByRole('button', { name: 'Entrar' }).click();

        // Verificar se a mensagem de erro de autenticação é exibida
        const mensagemErro = page.getByTestId('mensagem-erro-autenticacao');
        await expect(mensagemErro).toBeVisible();
        await expect(mensagemErro).toContainText('Título eleitoral ou senha inválidos.');
    });
});