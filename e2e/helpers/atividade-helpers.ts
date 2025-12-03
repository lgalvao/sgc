import { expect, type Page } from '@playwright/test';

export async function navegarParaAtividades(page: Page, options?: { visualizacao?: boolean }) {
    const testId = options?.visualizacao ? 'card-subprocesso-atividades-vis' : 'card-subprocesso-atividades';
    await page.getByTestId(testId).click();
    // Assuming the title is present as h1 or similar. Vue has <h1 ...>Atividades e conhecimentos</h1>
    await expect(page.getByRole('heading', { name: 'Atividades e conhecimentos' })).toBeVisible();
}

export async function adicionarAtividade(page: Page, descricao: string) {
    await page.getByTestId('inp-nova-atividade').fill(descricao);
    await page.getByTestId('btn-adicionar-atividade').click();
    // Verifica se a atividade apareceu na lista pelo texto
    await expect(page.getByText(descricao, { exact: true })).toBeVisible();
}

export async function adicionarConhecimento(page: Page, atividadeDescricao: string, conhecimentoDescricao: string) {
    const card = page.locator('.atividade-card', { has: page.getByText(atividadeDescricao) });

    await card.getByTestId('inp-novo-conhecimento').fill(conhecimentoDescricao);
    await card.getByTestId('btn-adicionar-conhecimento').click();

    await expect(card.getByText(conhecimentoDescricao)).toBeVisible();
}

export async function editarAtividade(page: Page, descricaoAtual: string, novaDescricao: string) {
    const card = page.locator('.atividade-card', { has: page.getByText(descricaoAtual) });
    const row = card.locator('.atividade-hover-row');

    await row.hover();
    await card.getByTestId('btn-editar-atividade').click();

    await page.locator(`input[value="${descricaoAtual}"]`).fill(novaDescricao);
    await page.getByTestId('btn-salvar-edicao-atividade').click();

    await expect(page.getByText(novaDescricao)).toBeVisible();
}

export async function removerAtividade(page: Page, descricao: string) {
    const card = page.locator('.atividade-card', { has: page.getByText(descricao) });
    const row = card.locator('.atividade-hover-row');

    page.once('dialog', async dialog => {
        await dialog.accept();
    });

    await row.hover();
    await card.getByTestId('btn-remover-atividade').click();

    await expect(page.getByText(descricao)).toBeHidden();
}

export async function disponibilizarCadastro(page: Page) {
    await page.getByTestId('btn-cad-atividades-disponibilizar').click();

    // Modal confirmation
    await expect(page.getByTestId('btn-disponibilizar-cadastro-confirmar')).toBeVisible();
    await page.getByTestId('btn-disponibilizar-cadastro-confirmar').click();
}

export async function verificarSituacaoSubprocesso(page: Page, situacao: string) {
    await expect(page.getByTestId('txt-badge-situacao')).toHaveText(situacao);
}
