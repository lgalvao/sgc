import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {login} from "~/utils/auth";

test.describe('Cadastro de Processo', () => {
    test.beforeEach(async ({page}) => {
        await login(page);

        // Navegar para a página de cadastro de processo
        await page.goto(`/processo/cadastro`);
        //await page.waitForLoadState('networkidle');
    });

    test('deve exibir o formulário de cadastro de processo', async ({page}) => {
        await expect(page.getByText('Cadastro de processo')).toBeVisible();
        await expect(page.getByLabel('Descrição')).toBeVisible();
        await expect(page.getByLabel('Tipo')).toBeVisible();
        await expect(page.getByLabel('Data limite')).toBeVisible();
        await expect(page.getByRole('button', {name: 'Salvar'})).toBeVisible();
        await expect(page.getByRole('link', {name: 'Cancelar'})).toBeVisible();
    });

    test('deve permitir cadastrar um novo processo', async ({page}) => {
        const descricao = `Processo de Teste ${Date.now()}`;

        await page.getByLabel('Descrição').fill(descricao);
        await page.getByLabel('Tipo').selectOption('Mapeamento');
        await page.getByLabel('Data limite').fill('2025-12-31');

        // Selecionar algumas unidades participantes
        await page.getByLabel('STIC - Secretaria de Informática e Comunicações').check();
        await page.getByLabel('COSIS - Coordenadoria de Sistemas').check();

        await page.getByRole('button', {name: 'Salvar'}).click();

        // Verificar se houve redirecionamento para o painel e se o novo processo aparece na lista
        await page.waitForURL(`/painel`);
        await expect(page.getByText(descricao)).toBeVisible();
    });
});