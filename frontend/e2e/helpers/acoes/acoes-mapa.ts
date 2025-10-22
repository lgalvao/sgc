export async function fecharModalImpactos(page: Page): Promise<void> {
    await page.getByTestId('fechar-impactos-mapa-button').click();
}

/**
 * Cria uma nova competência através do modal.
 */
export async function criarCompetencia(page: Page, descricao: string, atividades: string[]): Promise<void> {
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await expect(page.getByTestId('input-nova-competencia')).toBeVisible();
    await page.getByTestId('input-nova-competencia').fill(descricao);

    for (const atividade of atividades) {
        await page.locator('.atividade-card-item').filter({hasText: atividade}).click();
    }

    await page.getByTestId('btn-criar-competencia').click();
    await expect(page.getByTestId('input-nova-competencia')).not.toBeVisible(); // Modal deve fechar
}

/**
 * Edita uma competência existente através do modal.
 */
export async function editarCompetencia(page: Page, descricaoOriginal: string, novaDescricao: string, novasAtividades: string[]): Promise<void> {
    await page.locator('.competencia-card').filter({hasText: descricaoOriginal}).getByTestId('btn-editar-competencia').click();
    await expect(page.getByTestId('input-nova-competencia')).toBeVisible();
    await page.getByTestId('input-nova-competencia').fill(novaDescricao);

    // Desmarcar atividades antigas e marcar novas
    const atividadesAtuais = await page.locator('.atividade-card-item.checked').allTextContents();
    for (const atividade of atividadesAtuais) {
        await page.locator('.atividade-card-item').filter({hasText: atividade}).click();
    }

    for (const atividade of novasAtividades) {
        await page.locator('.atividade-card-item').filter({hasText: atividade}).click();
    }

    await page.getByTestId('btn-criar-competencia').click();
    await expect(page.getByTestId('input-nova-competencia')).not.toBeVisible(); // Modal deve fechar
}

/**
 * Exclui uma competência existente através do modal de confirmação.
 */
export async function excluirCompetencia(page: Page, descricao: string): Promise<void> {
    await page.locator('.competencia-card').filter({hasText: descricao}).getByTestId('btn-excluir-competencia').click();
    await expect(page.getByText(`Confirma a exclusão da competência "${descricao}"?`)).toBeVisible();
    await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
    await expect(page.getByText(`Confirma a exclusão da competência "${descricao}"?`)).not.toBeVisible(); // Modal deve fechar
}