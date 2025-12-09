import {expect, type Page} from '@playwright/test';

export async function navegarParaAtividades(page: Page) {
    const testId = 'card-subprocesso-atividades';
    await expect(page.getByTestId(testId)).toBeVisible();
    await page.getByTestId(testId).click();
    await expect(page).toHaveURL(/\/atividades$|\/cadastro$/);
    await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
}


export async function adicionarAtividade(page: Page, descricao: string) {
    await page.getByTestId('inp-nova-atividade').fill(descricao);
    await page.getByTestId('btn-adicionar-atividade').click();
    await expect(page.getByText(descricao, {exact: true})).toBeVisible();
}

export async function adicionarConhecimento(page: Page, atividadeDescricao: string, conhecimentoDescricao: string) {
    const card = page.locator('.atividade-card', {has: page.getByText(atividadeDescricao)});

    await card.getByTestId('inp-novo-conhecimento').fill(conhecimentoDescricao);

    const responsePromise = page.waitForResponse(resp => resp.url().includes('/conhecimentos') && resp.status() === 201);
    await card.getByTestId('btn-adicionar-conhecimento').click();
    await responsePromise;

    await expect(card.getByText(conhecimentoDescricao)).toBeVisible();
}

export async function editarAtividade(page: Page, descricaoAtual: string, novaDescricao: string) {
    const card = page.locator('.atividade-card', {has: page.getByText(descricaoAtual)});
    const row = card.locator('.atividade-hover-row');

    await row.hover();
    await card.getByTestId('btn-editar-atividade').click();

    await page.locator(`input[value="${descricaoAtual}"]`).fill(novaDescricao);
    await page.getByTestId('btn-salvar-edicao-atividade').click();

    await expect(page.getByText(novaDescricao)).toBeVisible();
}

export async function removerAtividade(page: Page, descricao: string) {
    const card = page.locator('.atividade-card', {has: page.getByText(descricao)});
    const row = card.locator('.atividade-hover-row');

    page.once('dialog', async dialog => await dialog.accept());

    await row.hover();
    await card.getByTestId('btn-remover-atividade').click({force: true});

    await expect(page.getByText(descricao)).toBeHidden();
}

export async function editarConhecimento(page: Page, atividadeDescricao: string, conhecimentoAtual: string, novoConhecimento: string) {
    const card = page.locator('.atividade-card', {has: page.getByText(atividadeDescricao)});
    const linhaConhecimento = card.locator('.group-conhecimento', {hasText: conhecimentoAtual});

    await linhaConhecimento.hover();
    await expect(linhaConhecimento.getByTestId('btn-editar-conhecimento')).toBeVisible();
    await linhaConhecimento.getByTestId('btn-editar-conhecimento').click({force: true});

    await card.getByTestId('inp-editar-conhecimento').fill(novoConhecimento);
    await card.getByTestId('btn-salvar-edicao-conhecimento').click();

    await expect(card.getByText(novoConhecimento)).toBeVisible();
}

export async function removerConhecimento(page: Page, atividadeDescricao: string, conhecimento: string) {
    const card = page.locator('.atividade-card', {has: page.getByText(atividadeDescricao)});
    const linhaConhecimento = card.locator('.group-conhecimento', {hasText: conhecimento});

    page.once('dialog', dialog => dialog.accept());

    await linhaConhecimento.hover();
    await linhaConhecimento.getByTestId('btn-remover-conhecimento').click({force: true});

    await expect(card.getByText(conhecimento)).toBeHidden();
}

export async function disponibilizarCadastro(page: Page) {
    // Clica no botão que abre o modal de confirmação
    await page.getByTestId('btn-cad-atividades-disponibilizar').click();

    // Modal confirmation
    await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
    await page.getByTestId('btn-confirmar-disponibilizacao').click();
}

export async function verificarSituacaoSubprocesso(page: Page, situacao: string) {
    await expect(page.getByTestId('cad-atividades__txt-badge-situacao')).toHaveText(new RegExp(situacao, 'i'));
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
    await expect(page.getByRole('dialog', {name: 'Importação de atividades'})).toBeVisible();
}

export async function importarAtividades(page: Page, processoDescricao: string, unidadeSigla: string, atividadesCodigos: number[]) {
    await abrirModalImportar(page);

    await page.getByTestId('select-processo').selectOption({label: processoDescricao});
    await page.getByTestId('select-unidade').selectOption({label: unidadeSigla});

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
    await page.getByTestId('btn-fechar-impacto').click();
    await expect(page.getByRole('dialog')).toBeHidden();
}
