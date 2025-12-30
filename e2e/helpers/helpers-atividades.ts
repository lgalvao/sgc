import {expect, type Page} from '@playwright/test';

export async function navegarParaAtividades(page: Page) {
    const testId = 'card-subprocesso-atividades';
    
    page.on('console', msg => {
        console.log(`[Browser Console] ${msg.type()}: ${msg.text()}`);
    });
    
    await expect(page.getByTestId(testId)).toBeVisible();
    await page.getByTestId(testId).click();
    await page.waitForURL(/\/cadastro$/);

    await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos', level: 1})).toBeVisible();
    await expect(page.getByTestId('inp-nova-atividade')).toBeVisible();
}

export async function navegarParaAtividadesVisualizacao(page: Page) {
    const testId = 'card-subprocesso-atividades-vis';
    await expect(page.getByTestId(testId)).toBeVisible();
    await page.getByTestId(testId).click();
    await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
}


export async function adicionarAtividade(page: Page, descricao: string) {
    await page.getByTestId('inp-nova-atividade').fill(descricao);
    await expect(page.getByTestId('btn-adicionar-atividade')).toBeEnabled();
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
    const editButton = card.getByTestId('btn-editar-atividade');

    await row.hover();
    await expect(editButton).toBeVisible();
    await editButton.click();

    await page.locator(`input[value="${descricaoAtual}"]`).fill(novaDescricao);
    await page.getByTestId('btn-salvar-edicao-atividade').click();

    await expect(page.getByText(novaDescricao)).toBeVisible();
}

export async function removerAtividade(page: Page, descricao: string) {
    const card = page.locator('.atividade-card', {has: page.getByText(descricao)});
    const row = card.locator('.atividade-hover-row');

    await row.hover();
    await card.getByTestId('btn-remover-atividade').click({force: true});
    
    // Confirmar no modal
    await page.getByTestId('btn-modal-confirmacao-confirmar').click();

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

    await linhaConhecimento.hover();
    await linhaConhecimento.getByTestId('btn-remover-conhecimento').click({force: true});
    
    // Confirmar no modal
    await page.getByTestId('btn-modal-confirmacao-confirmar').click();

    await expect(card.getByText(conhecimento)).toBeHidden();
}

export async function disponibilizarCadastro(page: Page) {
    await page.getByTestId('btn-cad-atividades-disponibilizar').click();
    
    // Garantir que o modal apareça e o botão de confirmação esteja pronto
    const btnConfirmar = page.getByTestId('btn-confirmar-disponibilizacao');
    await expect(btnConfirmar).toBeVisible();
    await btnConfirmar.click();
}

export async function verificarSituacaoSubprocesso(page: Page, situacao: string) {
    await expect(page.getByTestId('cad-atividades__txt-badge-situacao')).toHaveText(new RegExp(situacao, 'i'), {timeout: 15000});
}

export async function verificarBotaoImpactoDropdown(page: Page) {
    const btnMaisAcoes = page.getByTestId('btn-mais-acoes');
    await expect(btnMaisAcoes).toBeVisible();
    await btnMaisAcoes.click();
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa')).toBeVisible();
    await page.keyboard.press('Escape'); // Fecha o dropdown
}

export async function verificarBotaoImpactoDireto(page: Page) {
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa')).toBeVisible();
}

export async function verificarBotaoImpactoAusente(page: Page) {
    const btnImpacto = page.getByTestId('cad-atividades__btn-impactos-mapa');
    
    // Se o botão for um item de menu (dentro de Mais ações)
    const btnMaisAcoes = page.getByTestId('btn-mais-acoes');
    if (await btnMaisAcoes.isVisible()) {
        await btnMaisAcoes.click();
        await expect(btnImpacto).toBeHidden();
        await page.keyboard.press('Escape');
    } else {
        // Se for um botão direto
        await expect(btnImpacto).toBeHidden();
    }
}

export async function abrirModalImpacto(page: Page) {
    const btnMaisAcoes = page.getByTestId('btn-mais-acoes');
    if (await btnMaisAcoes.isVisible()) {
        await btnMaisAcoes.click();
    }
    await page.getByTestId('cad-atividades__btn-impactos-mapa').click();
    await expect(page.getByRole('dialog')).toBeVisible();
}

export async function fecharModalImpacto(page: Page) {
    await page.getByTestId('btn-fechar-impacto').click();
    await expect(page.getByRole('dialog')).toBeHidden();
}
