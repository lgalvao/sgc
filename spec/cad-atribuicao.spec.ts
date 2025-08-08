import { expect, test } from "@playwright/test";
import { login } from "../utils/auth";

test.describe('Cadastro de Atribuição Temporária', () => {
    test.setTimeout(5000);

    test.beforeEach(async ({ page }) => {
        await login(page);

        // Navegar para a página de cadastro de atribuição (unidade STIC)
        await page.goto(`/unidade/STIC/atribuicao`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir o título da página e os campos do formulário', async ({ page }) => {
        await expect(page.getByRole('heading', { name: 'Criar atribuição temporária' })).toBeVisible();
        await expect(page.getByLabel('Servidor')).toBeVisible();
        await expect(page.getByLabel('Data de término')).toBeVisible();
        await expect(page.getByLabel('Justificativa')).toBeVisible();
        await expect(page.getByRole('button', { name: 'Criar' })).toBeVisible();
        await expect(page.getByRole('button', { name: 'Cancelar' })).toBeVisible();
    });

    test('deve permitir criar uma nova atribuição temporária', async ({ page }) => {
        // Selecionar um servidor (assumindo que há servidores elegíveis)
        await page.getByLabel('Servidor').selectOption({ label: 'Servidor Teste STIC' });

        // Preencher data de término
        await page.getByLabel('Data de término').fill('2025-12-31');

        // Preencher justificativa
        await page.getByLabel('Justificativa').fill(`Atribuição de Teste ${Date.now()}`);

        // Clicar em Criar
        await page.getByRole('button', { name: 'Criar' }).click();

        // Verificar mensagem de sucesso ou redirecionamento
        await expect(page.getByText('Atribuição criada com sucesso!')).toBeVisible();
        // Ou verificar redirecionamento para a página da unidade
        await page.waitForURL(`/unidade/STIC`);
    });
});