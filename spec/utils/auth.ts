import {expect, Page} from "@playwright/test";

const baseUrl = "http://localhost:5173/"

export async function login(page: Page) {

    await page.goto(baseUrl);
    await page.waitForLoadState('domcontentloaded');

    // Preencher usuário e senha (usa o servidor 1 por padrão)
    await page.getByLabel('Título eleitoral').fill('1');
    await page.getByLabel('Senha').fill('123');

    // Clicar no botão Entrar
    await page.getByRole('button', {name: 'Entrar'}).click();

    // Verifica se a seleção de múltiplos perfis apareceu
    const seletorDePerfil = page.getByLabel('Selecione o Perfil e a Unidade');

    try {
        await seletorDePerfil.waitFor({state: 'visible', timeout: 1500});
    } catch (e) {
        // Seletor não apareceu, significa que é um usuário com perfil único. Segue o fluxo.
    }

    if (await seletorDePerfil.isVisible()) {
        // Se o seletor apareceu, escolhe um perfil padrão para os testes e continua
        await seletorDePerfil.selectOption({label: 'ADMIN - SEDOC'});
        await page.getByRole('button', {name: 'Entrar'}).click();
    }

    // Esperar a navegação para o painel
    await page.waitForURL(`${baseUrl}painel`);
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
}