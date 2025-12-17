import {expect, type Page} from '@playwright/test';

export async function navegarParaAtividades(page: Page) {
    const testId = 'card-subprocesso-atividades';
    
    // Capturar logs do console para debug
    page.on('console', msg => {
        if (msg.text().includes('SubprocessoCards')) {
            console.log(`[Browser Console] ${msg.type()}: ${msg.text()}`);
        }
    });
    
    await expect(page.getByTestId(testId)).toBeVisible();
    
    // Capturar URL antes do clique
    const urlAntes = page.url();
    console.log('[E2E] URL antes do clique:', urlAntes);
    
    // Clicar no card
    await page.getByTestId(testId).click();
    
    // Aguardar navegação - a URL deve mudar para incluir /cadastro
    await page.waitForURL(/\/cadastro$/, {timeout: 10000});
    
    console.log('[E2E] URL após clique:', page.url());
    
    // Verificar que estamos na página de cadastro (heading level 1, não level 4 do card)
    await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos', level: 1})).toBeVisible();
    
    // Verificar que o input de nova atividade existe (só existe na página de cadastro)
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

    // Hover on the row to trigger CSS hover state
    await row.hover();
    // Wait for button to become visible (CSS transition: opacity 0.2s)
    await expect(editButton).toBeVisible();
    await editButton.click();

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

export async function abrirModalImpacto(page: Page) {
    await page.getByTestId('cad-atividades__btn-impactos-mapa').click();
    await expect(page.getByRole('dialog')).toBeVisible();
}

export async function fecharModalImpacto(page: Page) {
    await page.getByTestId('btn-fechar-impacto').click();
    await expect(page.getByRole('dialog')).toBeHidden();
}
