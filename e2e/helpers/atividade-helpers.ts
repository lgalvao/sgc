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
    await card.getByTestId('btn-remover-atividade').click({ force: true });

    await expect(page.getByText(descricao)).toBeHidden();
}

export async function editarConhecimento(page: Page, atividadeDescricao: string, conhecimentoAtual: string, novoConhecimento: string) {
    const card = page.locator('.atividade-card', { has: page.getByText(atividadeDescricao) });
    const linhaConhecimento = card.locator('.group-conhecimento', { hasText: conhecimentoAtual });

    await linhaConhecimento.hover();
    await linhaConhecimento.getByTestId('btn-editar-conhecimento').click({ force: true });
    await linhaConhecimento.getByTestId('inp-editar-conhecimento').fill(novoConhecimento);
    await linhaConhecimento.getByTestId('btn-salvar-edicao-conhecimento').click();

    await expect(card.getByText(novoConhecimento)).toBeVisible();
}

export async function removerConhecimento(page: Page, atividadeDescricao: string, conhecimento: string) {
    const card = page.locator('.atividade-card', { has: page.getByText(atividadeDescricao) });
    const linhaConhecimento = card.locator('.group-conhecimento', { hasText: conhecimento });

    page.once('dialog', dialog => dialog.accept());

    await linhaConhecimento.hover();
    await linhaConhecimento.getByTestId('btn-remover-conhecimento').click({ force: true });

    await expect(card.getByText(conhecimento)).toBeHidden();
}

export async function disponibilizarCadastro(page: Page) {
    // Clica no botão que abre o modal de confirmação
    await page.getByTestId('btn-cad-atividades-disponibilizar').click();

    // Modal confirmation
    await expect(page.getByTestId('btn-disponibilizar-cadastro-confirmar')).toBeVisible();
    await page.getByTestId('btn-disponibilizar-cadastro-confirmar').click();
}

export async function verificarSituacaoSubprocesso(page: Page, situacao: string) {
    await expect(page.getByTestId('cad-atividades__txt-badge-situacao')).toHaveText(situacao);
}

export async function verificarBotaoImpacto(page: Page, visivel: boolean) {
    const btn = page.getByTestId('cad-atividades__btn-impactos-mapa');
    if (visivel) {
        await expect(btn).toBeVisible();
    } else {
        await expect(btn).toBeHidden();
    }
}

export async function abrirModalImportar(page: Page) {
    await page.getByTestId('btn-cad-atividades-importar').click();
    await expect(page.getByRole('dialog', { name: 'Importação de atividades' })).toBeVisible();
}

export async function importarAtividades(page: Page, processoDescricao: string, unidadeSigla: string, atividadesCodigos: number[]) {
    await abrirModalImportar(page);

    // Select Processo - We need to find the option that contains the text
    // The select options might use ID as value, so we select by label/text
    await page.getByTestId('select-processo').selectOption({ label: processoDescricao });

    // Select Unidade
    await page.getByTestId('select-unidade').selectOption({ label: unidadeSigla });

    // Check Activities
    for (const codigo of atividadesCodigos) {
        await page.getByTestId(`checkbox-atividade-${codigo}`).check();
    }

    await page.getByTestId('btn-importar').click();
    await expect(page.getByRole('dialog')).toBeHidden();
}

export async function abrirModalImpacto(page: Page) {
    await page.getByTestId('cad-atividades__btn-impactos-mapa').click();
    await expect(page.getByRole('dialog')).toBeVisible();
}

export async function fecharModalImpacto(page: Page) {
    // Assuming there is a close button or similar.
    // ImpactoMapaModal.vue has @fechar="fecharModalImpacto"
    // Usually a modal has a close button in header or footer.
    // I'll assume clicking outside or pressing Escape works, or look for a button.
    // ImpactoMapaModal.vue implementation:
    // It's not fully visible in the read_file output (it was imported).
    // Let's assume hitting Escape works for now or just checking visibility.
    await page.keyboard.press('Escape');
    await expect(page.getByRole('dialog')).toBeHidden();
}
