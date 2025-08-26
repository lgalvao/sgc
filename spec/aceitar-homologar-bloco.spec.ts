import {expect, test} from "@playwright/test";

export async function loginComoUsuario(page: any, titulo: string, senha: string, perfil?: string) {
    await page.goto('/');
    await page.getByLabel('Título eleitoral').fill(titulo);
    await page.getByLabel('Senha').fill(senha);
    await page.getByRole('button', {name: 'Entrar'}).click();

    // Se houver seleção de perfil, escolher o especificado
    const seletorDePerfil = page.getByLabel('Selecione o Perfil e a Unidade');
    if (await seletorDePerfil.isVisible()) {
        if (perfil) {
            await seletorDePerfil.selectOption({label: perfil});
        } else {
            // Selecionar o primeiro disponível
            await seletorDePerfil.selectOption({index: 0});
        }
        await page.getByRole('button', {name: 'Entrar'}).click();
    }

    // Esperar a navegação para o painel
    await page.waitForURL(/.*\/painel/);
    await expect(page.getByTestId('titulo-processos')).toBeVisible();
}

test.describe('Aceitar/Homologar Cadastro em Bloco', () => {
    test('deve exibir botão "Aceitar em bloco" para perfil GESTOR quando houver subprocessos elegíveis', async ({page}) => {
        // Logar como GESTOR (exemplo: Ricardo Alves na unidade COSIS)
        await loginComoUsuario(page, '6', '123', 'GESTOR - COSIS');

        // Navegar para o processo 2 que tem subprocessos elegíveis
        await page.goto('/processo/2');
        await page.waitForLoadState('networkidle');

        // Verificar se o botão "Aceitar em bloco" está visível
        await expect(page.getByRole('button', {name: 'Aceitar em bloco'})).toBeVisible();
    });

    test('deve exibir botão "Homologar em bloco" para perfil ADMIN quando houver subprocessos elegíveis', async ({page}) => {
        // Logar como ADMIN (exemplo: Zeca Silva na unidade SEDOC)
        await loginComoUsuario(page, '7', '123', 'ADMIN - SEDOC');

        // Navegar para o processo 1 que tem subprocessos elegíveis
        await page.goto('/processo/1');
        await page.waitForLoadState('networkidle');

        // Verificar se o botão "Homologar em bloco" está visível
        await expect(page.getByRole('button', {name: 'Homologar em bloco'})).toBeVisible();
    });

    test('não deve exibir botões de ação em bloco quando não houver subprocessos elegíveis', async ({page}) => {
        // Logar como ADMIN
        await loginComoUsuario(page, '7', '123', 'ADMIN - SEDOC');

        // Navegar para o processo 5 que não tem subprocessos elegíveis
        await page.goto('/processo/5');
        await page.waitForLoadState('networkidle');

        // Verificar que os botões não estão visíveis
        await expect(page.getByRole('button', {name: 'Aceitar em bloco'})).not.toBeVisible();
        await expect(page.getByRole('button', {name: 'Homologar em bloco'})).not.toBeVisible();
    });

    test('deve abrir modal de confirmação ao clicar em "Aceitar em bloco"', async ({page}) => {
        // Logar como GESTOR (exemplo: Ricardo Alves na unidade COSIS)
        await loginComoUsuario(page, '6', '123', 'GESTOR - COSIS');

        // Navegar para o processo 2 que tem subprocessos elegíveis
        await page.goto('/processo/2');
        await page.waitForLoadState('networkidle');

        // Clicar no botão "Aceitar em bloco"
        await page.getByRole('button', {name: 'Aceitar em bloco'}).click();

        // Verificar se o modal foi aberto
        await expect(page.getByText('Aceitar cadastros em bloco')).toBeVisible();

        // Verificar se a lista de unidades está presente
        await expect(page.locator('table.table-bordered')).toBeVisible();

        // Verificar se o botão de confirmação está presente
        await expect(page.locator('button.btn-primary:has-text("Aceitar")')).toBeVisible();
    });

    test('deve abrir modal de confirmação ao clicar em "Homologar em bloco"', async ({page}) => {
        // Logar como ADMIN
        await loginComoUsuario(page, '7', '123', 'ADMIN - SEDOC');

        // Navegar para o processo 1 que tem subprocessos elegíveis
        await page.goto('/processo/1');
        await page.waitForLoadState('networkidle');

        // Clicar no botão "Homologar em bloco"
        await page.getByRole('button', {name: 'Homologar em bloco'}).click();

        // Verificar se o modal foi aberto
        await expect(page.getByText('Homologar cadastros em bloco')).toBeVisible();

        // Verificar se a lista de unidades está presente
        await expect(page.locator('table.table-bordered')).toBeVisible();

        // Verificar se o botão de confirmação está presente
        await expect(page.locator('button.btn-success:has-text("Homologar")')).toBeVisible();
    });

    test('deve permitir selecionar unidades no modal', async ({page}) => {
        // Logar como ADMIN
        await loginComoUsuario(page, '7', '123', 'ADMIN - SEDOC');

        // Navegar para o processo 1 que tem subprocessos elegíveis
        await page.goto('/processo/1');
        await page.waitForLoadState('networkidle');

        // Clicar no botão "Homologar em bloco"
        await page.getByRole('button', {name: 'Homologar em bloco'}).click();

        // Verificar que há checkboxes para selecionar unidades
        await expect(page.locator('input[type="checkbox"]')).toBeVisible();

        // Verificar que pelo menos uma unidade está selecionada por padrão
        const checkboxes = await page.locator('input[type="checkbox"]').all();
        let selected = false;
        for (const checkbox of checkboxes) {
            if (await checkbox.isChecked()) {
                selected = true;
                break;
            }
        }
        expect(selected).toBeTruthy();
    });

    test('deve fechar modal ao clicar em "Cancelar"', async ({page}) => {
        // Logar como ADMIN
        await loginComoUsuario(page, '7', '123', 'ADMIN - SEDOC');

        // Navegar para o processo 1 que tem subprocessos elegíveis
        await page.goto('/processo/1');
        await page.waitForLoadState('networkidle');

        // Clicar no botão "Homologar em bloco"
        await page.getByRole('button', {name: 'Homologar em bloco'}).click();

        // Verificar que o modal está aberto
        await expect(page.getByText('Homologar cadastros em bloco')).toBeVisible();

        // Clicar em "Cancelar"
        await page.getByRole('button', {name: 'Cancelar'}).click();

        // Verificar que o modal foi fechado
        await expect(page.getByText('Homologar cadastros em bloco')).not.toBeVisible();
    });

    test('deve processar ação em bloco e mostrar mensagem de sucesso', async ({page}) => {
        // Logar como ADMIN
        await loginComoUsuario(page, '7', '123', 'ADMIN - SEDOC');

        // Navegar para o processo 1 que tem subprocessos elegíveis
        await page.goto('/processo/1');
        await page.waitForLoadState('networkidle');

        // Clicar no botão "Homologar em bloco"
        await page.getByRole('button', {name: 'Homologar em bloco'}).click();

        // Clicar em "Homologar"
        await page.locator('button.btn-success:has-text("Homologar")').click();

        // Verificar mensagem de sucesso
        await expect(page.getByText('Cadastros homologados em bloco com sucesso!')).toBeVisible();

        // Verificar redirecionamento para o painel
        await page.waitForURL(/.*\/painel/);
    });
});