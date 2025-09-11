import {expect, Page} from "@playwright/test";

const baseUrl = "http://localhost:5173/"

export async function login(page: Page) {
    await loginAsAdmin(page);
}

export async function loginAsAdmin(page: Page) {
    await page.goto(baseUrl, );
    await page.waitForLoadState('domcontentloaded', );

    // Preencher usuário e senha (usa o servidor 7 - Zeca Silva que tem perfil ADMIN)
    const tituloInput = page.getByTestId('input-titulo');
    await tituloInput.clear();
    await tituloInput.fill('7');

    const senhaInput = page.getByTestId('input-senha');
    await senhaInput.clear();
    await senhaInput.fill('123');

    // Clicar no botão Entrar
    await page.getByTestId('botao-entrar').click();

    // Verifica se a seleção de múltiplos perfis apareceu
    const seletorDePerfil = page.getByTestId('select-perfil-unidade');

    try {
        await seletorDePerfil.waitFor({state: 'visible', timeout: 2000});
    } catch {
        // Seletor não apareceu, significa que é um usuário com perfil único. Segue o fluxo.
    }

    if (await seletorDePerfil.isVisible()) {
        // Se o seletor apareceu, escolhe ADMIN para os testes
        await seletorDePerfil.selectOption({label: 'ADMIN - SEDOC'});
        await page.getByTestId('botao-entrar').click();
    }

    // Esperar a navegação para o painel
    await page.waitForURL(`${baseUrl}painel`, );
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
}