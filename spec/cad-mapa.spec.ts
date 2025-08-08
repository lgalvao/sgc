import { expect, test, Page } from "@playwright/test";
import { login } from "../utils/auth";

async function adicionarCompetenciaEGerarMapa(page: Page, competenciaDescricao: string, checkboxLocator?: string) {
    if (checkboxLocator) {
        await page.locator(checkboxLocator).first().check();
    } else {
        await page.locator('input[type="checkbox"]').first().check();
    }
    await page.getByPlaceholder('Descreva a competência').fill(competenciaDescricao);
    await page.getByRole('button', { name: 'Adicionar competência' }).click();
    await expect(page.getByTestId('competencia-item')).toContainText(competenciaDescricao);
    await page.getByRole('button', { name: 'Gerar Mapa' }).click();
    await page.waitForURL(`/finalizacao-mapa?sigla=STIC&processoId=1`);
}

test.describe('Criação e Finalização de Mapa de Competências', () => {
    test.setTimeout(10000);
    const competenciaDescricao = `Competência de Teste E2E - ${Date.now()}`;
    const competenciaDescricaoExclusao = `Competência para Excluir - ${Date.now()}`;

    // Hook para fazer login antes de cada teste
    test.beforeEach(async ({ page }) => {
        await login(page);
    });

    // Teste principal do caminho feliz
    test('deve criar, finalizar e disponibilizar um mapa com sucesso', async ({ page }) => {
        // 1. Navega para a página de edição e cria uma competência
        await page.goto(`/unidade/STIC/mapa?processoId=1`);
        await expect(page.getByRole('heading', { name: 'Edição de Mapa de Competências' })).toBeVisible();

        await adicionarCompetenciaEGerarMapa(page, competenciaDescricao);

        // 3. Finaliza o Mapa
        await page.waitForURL(`/finalizacao-mapa?sigla=STIC&processoId=1`);
        await expect(page.getByRole('heading', { name: 'Finalização de Mapa de Competências' })).toBeVisible();
        await expect(page.locator('.card-body')).toContainText(competenciaDescricao);

        // Preenche a data e disponibiliza
        await page.getByLabel('Data limite para validação').fill('2025-12-31');
        await page.getByRole('button', { name: 'Disponibilizar' }).click();

        // Verifica a notificação de sucesso
        await expect(page.locator('.alert')).toContainText('foi disponibilizado para validação');
    });

    // Teste para o botão de exclusão de competência
    test('deve permitir excluir uma competência adicionada', async ({ page }) => {
        await page.goto(`/unidade/STIC/mapa?processoId=1`);
        await expect(page.getByRole('heading', { name: 'Edição de Mapa de Competências' })).toBeVisible();

        // Adiciona uma competência para ser excluída
        await page.locator('[data-testid="atividade-checkbox"]').first().check();
        await page.getByPlaceholder('Descreva a competência').fill(competenciaDescricaoExclusao);
        await page.getByRole('button', { name: 'Adicionar competência' }).click();
        const competenciaItem = page.getByTestId('competencia-item').filter({ hasText: competenciaDescricaoExclusao });
        await expect(competenciaItem).toBeVisible();

        // Exclui a competência
        await competenciaItem.getByRole('button', { name: 'Excluir' }).click();

        // Verifica se a competência foi removida
        await expect(competenciaItem).not.toBeVisible();
    });

    // Teste para a validação do botão 'Disponibilizar'
    test('botão Disponibilizar deve estar desabilitado sem data de validação', async ({ page }) => {
        await page.goto(`/unidade/STIC/mapa?processoId=1`);
        await adicionarCompetenciaEGerarMapa(page, 'Competencia para teste de habilitacao');

        const btnDisponibilizar = page.getByRole('button', { name: 'Disponibilizar' });

        // Verifica se o botão está desabilitado
        await expect(btnDisponibilizar).toBeDisabled();

        // Preenche a data e verifica se o botão habilita
        await page.getByLabel('Data limite para validação').fill('2025-12-31');
        await expect(btnDisponibilizar).toBeEnabled();
    });

    // Teste para o botão 'Voltar'
    test('botão Voltar na finalização deve retornar para a edição do mapa', async ({ page }) => {
        await page.goto(`/unidade/STIC/mapa?processoId=1`);
        await adicionarCompetenciaEGerarMapa(page, 'Competencia para teste de voltar');

        await page.getByRole('button', { name: 'Voltar' }).click();

        // Verifica se retornou para a página de edição
        await page.waitForURL(`/unidade/STIC/mapa?processoId=1`);
        await expect(page.getByRole('heading', { name: 'Edição de Mapa de Competências' })).toBeVisible();
    });

    // Teste para a checkbox de incluir atividades
    test('deve exibir/ocultar atividades ao marcar/desmarcar a checkbox', async ({ page }) => {
        // Adiciona uma competência com atividade associada
        await page.goto(`/unidade/STIC/mapa?processoId=1`);
        await adicionarCompetenciaEGerarMapa(page, competenciaDescricao);

        await page.waitForURL(`/finalizacao-mapa?sigla=STIC&processoId=1`);
        const checkboxAtividades = page.getByLabel('Incluir descrição das atividades no mapa gerado');
        const listaAtividades = page.locator('.card-body ul');

        // Atividades devem estar visíveis por padrão
        await expect(listaAtividades).toBeVisible();

        // Desmarca a checkbox e verifica se as atividades somem
        await checkboxAtividades.uncheck();
        await expect(listaAtividades).not.toBeVisible();

        // Marca novamente e verifica se as atividades aparecem
        await checkboxAtividades.check();
        await expect(listaAtividades).toBeVisible();
    });
});