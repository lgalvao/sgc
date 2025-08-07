import {expect, test} from "playwright/test";

test.describe('Cadastro de Atividades e Conhecimentos', () => {
    test('deve permitir adicionar, editar e remover uma nova atividade e um novo conhecimento', async ({page}) => {
        await page.goto('http://localhost:5173/processos/4/unidade/SEDESENV/atividades');

        // Adicionar uma nova atividade
        const novaAtividadeDescricao = 'Atividade de Teste Playwright';
        await page.getByPlaceholder('Nova atividade').fill(novaAtividadeDescricao);
        await page.getByTitle('Adicionar Atividade').click();

        // Verificar se a atividade foi adicionada
        await expect(page.getByText(novaAtividadeDescricao)).toBeVisible();

        // Editar a atividade
        const atividadeCard = page.locator('.atividade-card', {hasText: novaAtividadeDescricao});
        const atividadeTitleCard = atividadeCard.locator('.atividade-titulo-card');
        await atividadeTitleCard.hover();
        await atividadeCard.getByTitle('Editar').click();
        const atividadeEditadaDescricao = 'Atividade Editada Playwright';
        await atividadeCard.locator('input.atividade-edicao-input').waitFor({
            state: 'visible'
        });
        await atividadeCard.locator('input.atividade-edicao-input').waitFor({ state: 'visible' });
        await atividadeCard.locator('input.atividade-edicao-input').fill(atividadeEditadaDescricao);
        await atividadeCard.getByTitle('Salvar').click();
        await expect(page.getByText(atividadeEditadaDescricao)).toBeVisible();

        // Adicionar um novo conhecimento à atividade recém-criada
        const novoConhecimentoDescricao = 'Conhecimento de Teste Playwright';
        await atividadeCard.getByPlaceholder('Novo conhecimento').fill(novoConhecimentoDescricao);
        await atividadeCard.getByTitle('Adicionar Conhecimento').click();

        // Verificar se o conhecimento foi adicionado
        await expect(atividadeCard.getByText(novoConhecimentoDescricao)).toBeVisible();

        // Editar o conhecimento
        const conhecimentoItem = atividadeCard.locator('.group-conhecimento', {hasText: novoConhecimentoDescricao});
        await conhecimentoItem.getByTitle('Editar').click();
        const conhecimentoEditadoDescricao = 'Conhecimento Editado Playwright';
        await conhecimentoItem.locator('input[style="max-width: 300px;"]').fill(conhecimentoEditadoDescricao);
        await conhecimentoItem.getByTitle('Salvar').click();
        await expect(atividadeCard.getByText(conhecimentoEditadoDescricao)).toBeVisible();
    });
});
