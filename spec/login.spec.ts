import {expect, test} from "@playwright/test";

test.describe('Login', () => {
    test.setTimeout(5000);

    test('deve permitir o login com credenciais válidas e navegar para o painel', async ({page}) => {
        await page.goto('/');
        await page.waitForLoadState('networkidle');

        // Preencher usuário e senha
        await page.getByRole('textbox', {name: 'Título eleitoral'}).fill('1');
        await page.getByRole('textbox', {name: 'Senha'}).fill('123');

        // Clicar no botão Entrar
        await page.getByRole('button', {name: 'Entrar'}).click();

        // Verificar se a navegação para a página /painel ocorreu
        await expect(page).toHaveURL(`/painel`);
        await expect(page.getByTestId('titulo-processos')).toBeVisible(); // Verifica se um elemento da página do painel está visível
    });
});