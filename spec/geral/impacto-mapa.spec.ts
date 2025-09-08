import {expect, Locator, Page, test} from "@playwright/test";
import {login} from "~/utils/auth";

// Helper para aguardar a notificação de sucesso desaparecer
async function waitForNotification(page: Page) {
    const notif = page.locator('.notification-content');
    try {
        await notif.waitFor({state: 'visible', timeout: 1500});
        await notif.waitFor({state: 'hidden', timeout: 3000});
    } catch (e) {
        // Ignora o erro se a notificação nunca aparecer
    }
}

async function adicionarAtividade(page: Page, nomeAtividade: string) {
    await page.getByTestId('input-nova-atividade').fill(nomeAtividade);
    await page.getByTestId('btn-adicionar-atividade').click();
    await waitForNotification(page);
    await expect(page.locator('.atividade-card', {hasText: nomeAtividade})).toBeVisible();
}

async function adicionarConhecimento(page: Page, atividadeCard: Locator, nomeConhecimento: string) {
    await atividadeCard.locator('[data-testid="input-novo-conhecimento"]').fill(nomeConhecimento);
    const button = atividadeCard.locator('[data-testid="btn-adicionar-conhecimento"]');
    await expect(button).toBeEnabled();
    await button.click();
    await waitForNotification(page);
    await expect(atividadeCard.locator('.group-conhecimento', {hasText: nomeConhecimento})).toBeVisible();
}


test.describe('Impacto no Mapa de Competências', () => {
    test.slow(); // Triplica o timeout para todos os testes neste describe

    test.beforeEach(async ({page}) => {
        await login(page);
        await page.goto(`/processo/1/SESEL/cadastro`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir tela vazia quando não há mudanças', async ({page}) => {
        const impactoButton = page.locator('button', {hasText: 'Impacto no mapa'});
        await impactoButton.click();

        await expect(page.getByText('Impacto no Mapa de Competências')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas')).not.toBeVisible();
        await expect(page.getByTestId('msg-nenhuma-competencia')).toBeVisible();
    });

    test('deve exibir atividades inseridas sem impacto em competências', async ({page}) => {
        await adicionarAtividade(page, 'Atividade Nova Sem Impacto');

        const impactoButton = page.locator('button', {hasText: 'Impacto no mapa'});
        await impactoButton.click();

        await expect(page.getByTestId('titulo-atividades-inseridas')).toBeVisible();
        await expect(page.getByTestId('secao-atividades-inseridas').getByText('Atividade Nova Sem Impacto')).toBeVisible();
        await expect(page.getByTestId('msg-nenhuma-competencia')).toBeVisible();
    });

    test('deve exibir impacto de CONHECIMENTO ADICIONADO', async ({page}) => {
        const atividadeCard = page.locator('.atividade-card', {hasText: 'Manutenção de sistemas'});
        const novoConhecimento = `Conhecimento Novo ${Date.now()}`;
        await adicionarConhecimento(page, atividadeCard, novoConhecimento);

        await page.locator('button', {hasText: 'Impacto no mapa'}).click();

        const secaoImpactadas = page.locator('.card.mb-3').filter({has: page.locator('.card-header', {hasText: new RegExp(/^Implantação de sistemas$/)})});
        await expect(secaoImpactadas.getByText('Conhecimento adicionado')).toBeVisible();
        await expect(secaoImpactadas.getByText(novoConhecimento)).toBeVisible();
    });

    test('deve exibir impacto de CONHECIMENTO REMOVIDO', async ({page}) => {
        const atividadeCard = page.locator('.atividade-card', {hasText: 'Manutenção de sistemas'});
        const conhecimentoParaRemover = 'Programação em Java';

        const conhecimentoRow = atividadeCard.locator('.group-conhecimento', {hasText: new RegExp(`^${conhecimentoParaRemover}$`)});
        await conhecimentoRow.hover();
        await conhecimentoRow.getByTestId('btn-remover-conhecimento').click();
        await waitForNotification(page);

        await page.locator('button', {hasText: 'Impacto no mapa'}).click();

        const secaoImpactadas = page.locator('.card.mb-3').filter({has: page.locator('.card-header', {hasText: new RegExp(/^Implantação de sistemas$/)})});
        await expect(secaoImpactadas.getByText('Conhecimento removido')).toBeVisible();
        await expect(secaoImpactadas.getByText(conhecimentoParaRemover)).toBeVisible();
    });

    test('deve exibir impacto de CONHECIMENTO ALTERADO', async ({page}) => {
        const atividadeCard = page.locator('.atividade-card', {hasText: 'Manutenção de sistemas'});
        const conhecimentoOriginal = 'Programação em Java';
        const conhecimentoEditado = `Conhecimento Alterado ${Date.now()}`;

        const conhecimentoRow = atividadeCard.locator('.group-conhecimento', {hasText: new RegExp(`^${conhecimentoOriginal}$`)});
        await conhecimentoRow.hover();
        await conhecimentoRow.getByTestId('btn-editar-conhecimento').click();
        await page.getByTestId('input-editar-conhecimento').fill(conhecimentoEditado);
        await page.getByTestId('btn-salvar-edicao-conhecimento').click();
        await waitForNotification(page);

        await page.locator('button', {hasText: 'Impacto no mapa'}).click();

        const secaoImpactadas = page.locator('.card.mb-3').filter({has: page.locator('.card-header', {hasText: new RegExp(/^Implantação de sistemas$/)})});
        await expect(secaoImpactadas).toContainText('Conhecimento alterado');
        await expect(secaoImpactadas).toContainText(conhecimentoEditado);
        await expect(secaoImpactadas).toContainText(`De "${conhecimentoOriginal}"`);
    });

    test('deve exibir impacto de ATIVIDADE REMOVIDA', async ({page}) => {
        const atividadeParaRemover = 'Suporte e acompanhamento dos procedimentos eleitorais';
        const atividadeCard = page.locator('.atividade-card', {hasText: atividadeParaRemover});
        await atividadeCard.hover();
        await atividadeCard.getByTestId('btn-remover-atividade').click();
        await waitForNotification(page);

        await page.locator('button', {hasText: 'Impacto no mapa'}).click();

        const secaoImpactadas = page.locator('.card.mb-3').filter({has: page.locator('.card-header', {hasText: new RegExp(/^Acompanhamento de processos eleitorais$/)})});
        await expect(secaoImpactadas.getByText('Atividade removida')).toBeVisible();
        await expect(secaoImpactadas.getByText(atividadeParaRemover)).toBeVisible();
    });

    test('deve exibir impacto de ATIVIDADE ALTERADA', async ({page}) => {
        const atividadeOriginal = 'Suporte e acompanhamento dos procedimentos eleitorais';
        const atividadeAlterada = `Atividade Alterada ${Date.now()}`;
        const atividadeCard = page.locator('.atividade-card', {hasText: atividadeOriginal});

        await atividadeCard.hover();
        await atividadeCard.getByTestId('btn-editar-atividade').click();
        await page.getByTestId('input-editar-atividade').fill(atividadeAlterada);
        await page.getByTestId('btn-salvar-edicao-atividade').click();
        await waitForNotification(page);

        await page.locator('button', {hasText: 'Impacto no mapa'}).click();

        const secaoImpactadas = page.locator('.card.mb-3').filter({has: page.locator('.card-header', {hasText: new RegExp(/^Acompanhamento de processos eleitorais$/)})});
        await expect(secaoImpactadas).toContainText('Atividade alterada');
        await expect(secaoImpactadas).toContainText(atividadeAlterada);
        await expect(secaoImpactadas).toContainText(`De "${atividadeOriginal}"`);
    });
});
