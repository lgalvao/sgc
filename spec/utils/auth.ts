import {Page} from "@playwright/test";

const baseUrl = "http://localhost:5173/"

export async function login(page: Page) {

    await page.goto(baseUrl);
    await page.waitForLoadState('networkidle');

    // Preencher usuário e senha
    await page.getByRole('textbox', {name: 'Título eleitoral'}).fill('1');
    await page.getByRole('textbox', {name: 'Senha'}).fill('123');

    // Clicar no botão Entrar
    await page.getByRole('button', {name: 'Entrar'}).click();

    // Esperar a navegação para o painel
    await page.waitForURL(`${baseUrl}painel`);
    await page.waitForSelector('[data-testid="titulo-processos"]', {state: 'visible'});
}
