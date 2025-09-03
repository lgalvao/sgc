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

export async function loginAsGestor(page: Page) {
    await page.goto(baseUrl, );
    await page.waitForLoadState('domcontentloaded', );

    // Preencher usuário e senha (usa o servidor 2 - Carlos Lima que tem perfil GESTOR)
    const tituloInputGestor = page.getByTestId('input-titulo');
    await tituloInputGestor.clear();
    await tituloInputGestor.fill('2');

    const senhaInputGestor = page.getByTestId('input-senha');
    await senhaInputGestor.clear();
    await senhaInputGestor.fill('123');

    // Clicar no botão Entrar
    await page.getByTestId('botao-entrar').click();

    // GESTOR tem perfil único, deve ir direto para o painel
    await page.waitForURL(`${baseUrl}painel`, );
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
}

export async function loginAsChefe(page: Page) {
    await page.goto(baseUrl, );
    await page.waitForLoadState('domcontentloaded', );

    // Preencher usuário e senha (usa o servidor 9 - Giuseppe Corleone que tem perfil CHEFE)
    const tituloInputChefe = page.getByTestId('input-titulo');
    await tituloInputChefe.clear();
    await tituloInputChefe.fill('9');

    const senhaInputChefe = page.getByTestId('input-senha');
    await senhaInputChefe.clear();
    await senhaInputChefe.fill('123');

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
        // Se o seletor apareceu, escolhe CHEFE para os testes
        await seletorDePerfil.selectOption({label: 'CHEFE - SEDIA'});
        await page.getByTestId('botao-entrar').click();
    }

    // Esperar a navegação para o painel
    await page.waitForURL(`${baseUrl}painel`, );
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
}

export async function loginAsGestorCOSIS(page: Page) {
    await page.goto(baseUrl, );
    await page.waitForLoadState('domcontentloaded', );

    // Preencher usuário e senha (usa o servidor 6 - Ricardo Alves que tem perfil GESTOR de STIC)
    const tituloInputGestorCOSIS = page.getByTestId('input-titulo');
    await tituloInputGestorCOSIS.clear();
    await tituloInputGestorCOSIS.fill('6');

    const senhaInputGestorCOSIS = page.getByTestId('input-senha');
    await senhaInputGestorCOSIS.clear();
    await senhaInputGestorCOSIS.fill('123');

    // Clicar no botão Entrar
    await page.getByTestId('botao-entrar').click();

    // GESTOR tem perfil único, deve ir direto para o painel
    await page.waitForURL(`${baseUrl}painel`, );
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
}

export async function loginAsServidor(page: Page) {
    await page.goto(baseUrl, );
    await page.waitForLoadState('domcontentloaded', );

    // Preencher usuário e senha (usa o servidor 1 - Ana Paula Souza que tem perfil SERVIDOR)
    const tituloInputServidor = page.getByTestId('input-titulo');
    await tituloInputServidor.clear();
    await tituloInputServidor.fill('1');

    const senhaInputServidor = page.getByTestId('input-senha');
    await senhaInputServidor.clear();
    await senhaInputServidor.fill('123');

    // Clicar no botão Entrar
    await page.getByTestId('botao-entrar').click();

    // SERVIDOR tem perfil único, deve ir direto para o painel
    await page.waitForURL(`${baseUrl}painel`, );
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
}