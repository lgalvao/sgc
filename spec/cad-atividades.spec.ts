import {expect, Page, test} from "@playwright/test";
import {login} from "./utils/auth";

async function adicionarAtividade(page: Page, nomeAtividade: string) {
    await page.getByTestId('input-nova-atividade').fill(nomeAtividade);
    await page.getByTestId('btn-adicionar-atividade').click();
    await expect(page.getByText(nomeAtividade)).toBeVisible();
}

async function adicionarConhecimento(page: Page, atividadeCard: any, nomeConhecimento: string) {
    await atividadeCard.locator('[data-testid="input-novo-conhecimento"]').fill(nomeConhecimento);
    await atividadeCard.locator('[data-testid="btn-adicionar-conhecimento"]').click();
    await expect(page.getByText(nomeConhecimento)).toBeVisible();
}

test.describe('Cadastro de Atividades e Conhecimentos', () => {
    test.setTimeout(5000);
    test.beforeEach(async ({page}) => {
        await login(page);

        // Navegar para a página de cadastro de atividades (novo padrão: /processo/:idProcesso/:siglaUnidade/cadastro)
        await page.goto(`/processo/1/STIC/cadastro`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir o título da página e os campos de entrada', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
        await expect(page.getByTestId('input-nova-atividade')).toBeVisible();
        await expect(page.getByTestId('btn-adicionar-atividade')).toBeVisible();
    });

    test('deve permitir adicionar uma nova atividade', async ({page}) => {
        const novaAtividade = `Atividade de Teste ${Date.now()}`;
        await adicionarAtividade(page, novaAtividade);
    });

    test('deve permitir editar uma atividade existente', async ({page}) => {
        // Adiciona uma atividade para editar
        const atividadeOriginal = `Atividade para Editar ${Date.now()}`;
        await adicionarAtividade(page, atividadeOriginal);

        // Localiza o card da atividade recém-adicionada
        const atividadeCard = page.locator('.atividade-card', {hasText: atividadeOriginal});
        await atividadeCard.hover(); // Simula o hover para exibir os botões
        await atividadeCard.getByTestId('btn-editar-atividade').waitFor({state: 'visible'}); // Espera o botão de edição ficar visível

        // Inicia a edição
        await atividadeCard.getByTestId('btn-editar-atividade').click({force: true});
        const atividadeEditada = `Atividade Editada ${Date.now()}`;
        await page.getByTestId('input-editar-atividade').fill(atividadeEditada);
        await page.getByTestId('btn-salvar-edicao-atividade').click();
        await expect(page.getByText(atividadeEditada)).toBeVisible();
    });

    test('deve permitir remover uma atividade', async ({page}) => {
        // Adiciona uma atividade para remover
        const atividadeParaRemover = `Atividade para Remover ${Date.now()}`;
        await adicionarAtividade(page, atividadeParaRemover);

        // Localiza o card da atividade recém-adicionada
        const atividadeCard = page.locator('.atividade-card', {hasText: atividadeParaRemover});
        await atividadeCard.hover(); // Simula o hover para exibir os botões
        await atividadeCard.getByTestId('btn-remover-atividade').waitFor({state: 'visible'}); // Espera o botão de remover ficar visível

        // Remove a atividade
        await atividadeCard.getByTestId('btn-remover-atividade').click({force: true});
        await expect(page.getByText(atividadeParaRemover)).not.toBeAttached();
    });

    test('deve permitir adicionar um novo conhecimento a uma atividade', async ({page}) => {
        // Adiciona uma atividade para adicionar conhecimento
        const atividadeComConhecimento = `Atividade com Conhecimento ${Date.now()}`;
        await adicionarAtividade(page, atividadeComConhecimento);

        // Adiciona um conhecimento
        const novoConhecimento = `Conhecimento de Teste ${Date.now()}`;
        const atividadeCard = page.locator('.atividade-card', {hasText: atividadeComConhecimento});
        await adicionarConhecimento(page, atividadeCard, novoConhecimento);
    });

    test('deve permitir editar um conhecimento existente', async ({page}) => {
        // Adiciona uma atividade e um conhecimento para editar
        const atividadeParaEditarConhecimento = `Atividade para Editar Conhecimento ${Date.now()}`;
        await adicionarAtividade(page, atividadeParaEditarConhecimento);

        const conhecimentoOriginal = `Conhecimento Original ${Date.now()}`;
        const atividadeCard = page.locator('.atividade-card', {hasText: atividadeParaEditarConhecimento});
        await adicionarConhecimento(page, atividadeCard, conhecimentoOriginal);

        // Localiza o conhecimento recém-adicionado e simula o hover
        const conhecimentoRow = atividadeCard.locator('.group-conhecimento', {hasText: conhecimentoOriginal});
        await conhecimentoRow.hover();
        await conhecimentoRow.getByTestId('btn-editar-conhecimento')
            .waitFor({state: 'visible'}); // Espera o botão de edição do conhecimento ficar visível

        // Inicia a edição do conhecimento
        await conhecimentoRow.getByTestId('btn-editar-conhecimento').click({force: true});
        const conhecimentoEditado = `Conhecimento Editado ${Date.now()}`;
        await page.getByTestId('input-editar-conhecimento').fill(conhecimentoEditado);
        await page.getByTestId('btn-salvar-edicao-conhecimento').click();
        await expect(page.getByText(conhecimentoEditado)).toBeVisible();
    });

    test('deve permitir remover um conhecimento', async ({page}) => {
        // Adiciona uma atividade e um conhecimento para remover
        const atividadeParaRemoverConhecimento = `Atividade para Remover Conhecimento ${Date.now()}`;
        await adicionarAtividade(page, atividadeParaRemoverConhecimento);

        const conhecimentoParaRemover = `Conhecimento para Remover ${Date.now()}`;
        const atividadeCard = page.locator('.atividade-card', {hasText: atividadeParaRemoverConhecimento});
        await adicionarConhecimento(page, atividadeCard, conhecimentoParaRemover);

        // Localiza o conhecimento recém-adicionado e simula o hover
        const conhecimentoRow = atividadeCard.locator('.group-conhecimento', {hasText: conhecimentoParaRemover});
        await conhecimentoRow.hover();
        await conhecimentoRow.getByTestId('btn-remover-conhecimento')
            .waitFor({state: 'visible'}); // Espera o botão de remover conhecimento ficar visível

        // Remove o conhecimento
        await conhecimentoRow.getByTestId('btn-remover-conhecimento').click({force: true});
        await expect(page.getByText(conhecimentoParaRemover)).not.toBeAttached();
    });
});