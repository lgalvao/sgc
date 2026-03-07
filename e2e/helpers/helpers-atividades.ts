import {expect, type Page} from '@playwright/test';
import {
    limparNotificacoes,
    verificarPaginaPainel,
    verificarToast
} from './helpers-navegacao.js';

function extrairRotaSubprocesso(page: Page): { codigoProcesso: string; siglaUnidade: string } {
    const match = /\/processo\/(\d+)\/([A-Z0-9_]+)/.exec(page.url());
    expect(match).not.toBeNull();
    return {
        codigoProcesso: match![1],
        siglaUnidade: match![2]
    };
}

export async function navegarParaAtividades(page: Page) {
    const card = page.getByTestId('card-subprocesso-atividades');
    await expect(card).toBeVisible();
    await card.click();
    await page.waitForURL(/\/cadastro$/);

    await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos', level: 2})).toBeVisible();
    await expect(page.getByTestId('inp-nova-atividade')).toBeVisible();
}

export async function navegarParaAtividadesVisualizacao(page: Page) {
    const {codigoProcesso, siglaUnidade} = extrairRotaSubprocesso(page);
    await page.goto(`/processo/${codigoProcesso}/${siglaUnidade}/vis-cadastro`);
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codigoProcesso}/${siglaUnidade}/vis-cadastro$`));
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
    const btnRemover = card.getByTestId('btn-remover-atividade');

    await row.hover();
    await expect(btnRemover).toBeVisible();
    await btnRemover.click();

    // Confirmar no modal
    await page.getByTestId('btn-modal-confirmacao-confirmar').click();

    await expect(page.getByText(descricao)).toBeHidden();
}

export async function editarConhecimento(page: Page, atividadeDescricao: string, conhecimentoAtual: string, novoConhecimento: string) {
    const card = page.locator('.atividade-card', {has: page.getByText(atividadeDescricao)});
    const linhaConhecimento = card.locator('.group-conhecimento', {hasText: conhecimentoAtual});
    const btnEditar = linhaConhecimento.getByTestId('btn-editar-conhecimento');

    await linhaConhecimento.hover();
    await expect(btnEditar).toBeVisible();
    await btnEditar.click();

    await card.getByTestId('inp-editar-conhecimento').fill(novoConhecimento);
    await card.getByTestId('btn-salvar-edicao-conhecimento').click();

    await expect(card.getByText(novoConhecimento)).toBeVisible();
}

export async function removerConhecimento(page: Page, atividadeDescricao: string, conhecimento: string) {
    const card = page.locator('.atividade-card', {has: page.getByText(atividadeDescricao)});
    const linhaConhecimento = card.locator('.group-conhecimento', {hasText: conhecimento});
    const btnRemover = linhaConhecimento.getByTestId('btn-remover-conhecimento');

    await linhaConhecimento.hover();
    await expect(btnRemover).toBeVisible();
    await btnRemover.click();

    // Confirmar no modal
    await page.getByTestId('btn-modal-confirmacao-confirmar').click();

    await expect(card.getByText(conhecimento)).toBeHidden();
}

export async function disponibilizarCadastro(page: Page) {
    await limparNotificacoes(page);
    await page.getByTestId('btn-cad-atividades-disponibilizar').click();

    // Garantir que o modal apareça e o botão de confirmação esteja pronto
    const modal = page.getByRole('dialog');
    await expect(modal).toBeVisible();
    
    const btnConfirmar = page.getByTestId('btn-confirmar-disponibilizacao');
    await expect(btnConfirmar).toBeVisible();
    await btnConfirmar.click();
    await verificarToast(page, /Disponibilizado com sucesso\./i);
    await verificarPaginaPainel(page);
}

export async function verificarSituacaoSubprocesso(page: Page, situacao: string) {
    await expect(page.getByTestId('cad-atividades__txt-badge-situacao')).toHaveText(new RegExp(situacao, 'i'));
}

export async function verificarBotaoImpactoDropdown(page: Page) {
    const btnMaisAcoes = page.getByTestId('btn-mais-acoes');
    await expect(btnMaisAcoes).toBeVisible();
    await btnMaisAcoes.click();
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeVisible();
    // Fechar clicando novamente ou fora
    await btnMaisAcoes.click();
}

export async function verificarBotaoImpactoDireto(page: Page) {
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-visualizacao')).toBeVisible();
}

export async function verificarBotaoImpactoAusenteEdicao(page: Page) {
    const btnMaisAcoes = page.getByTestId('btn-mais-acoes');
    await expect(btnMaisAcoes).toBeVisible();
    await btnMaisAcoes.click();
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeHidden();
    await btnMaisAcoes.click();
}

export async function verificarBotaoImpactoAusenteDireto(page: Page) {
    await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-visualizacao')).toBeHidden();
}

export async function abrirModalImpactoEdicao(page: Page) {
    await page.getByTestId('btn-mais-acoes').click();
    await page.getByTestId('cad-atividades__btn-impactos-mapa-edicao').click();
    await expect(page.getByRole('dialog')).toBeVisible();
}

export async function abrirModalImpactoVisualizacao(page: Page) {
    await page.getByTestId('cad-atividades__btn-impactos-mapa-visualizacao').click();
    await expect(page.getByRole('dialog')).toBeVisible();
}

export async function fecharModalImpacto(page: Page) {
    await page.getByTestId('btn-fechar-impacto').click();
    await expect(page.getByRole('dialog')).toBeHidden();
}

export async function selecionarAtividadesParaImportacao(page: Page, processoOrigemDescricao: string, unidadeOrigemSigla: string, atividadesDescricoes: string[]) {
    const btnEmptyState = page.getByTestId('btn-empty-state-importar');
    
    if (await btnEmptyState.isVisible()) {
        await btnEmptyState.click();
    } else {
        await page.getByTestId('btn-mais-acoes').click();
        await page.getByTestId('btn-cad-atividades-importar').click();
    }

    const modal = page.getByRole('dialog');
    await expect(modal.getByText('Importação de atividades')).toBeVisible();

    await modal.getByTestId('select-processo').selectOption({ label: processoOrigemDescricao });
    await expect(modal.getByTestId('select-unidade')).toBeEnabled();

    await modal.getByTestId('select-unidade').selectOption({ label: unidadeOrigemSigla });

    for (const desc of atividadesDescricoes) {
        await modal.getByText(desc, { exact: true }).check();
    }
}

export async function importarAtividades(page: Page, processoOrigemDescricao: string, unidadeOrigemSigla: string, atividadesDescricoes: string[]) {
    await selecionarAtividadesParaImportacao(page, processoOrigemDescricao, unidadeOrigemSigla, atividadesDescricoes);
    
    const modal = page.getByRole('dialog');
    await modal.getByTestId('btn-importar').click();
    await expect(modal).toBeHidden();

    // Validar se as atividades aparecem na tela após importar
    for (const desc of atividadesDescricoes) {
        await expect(page.getByText(desc, { exact: true }).first()).toBeVisible();
    }
}

export async function importarAtividadesComErroDuplicidade(page: Page, processoOrigemDescricao: string, unidadeOrigemSigla: string, atividadesDescricoes: string[]) {
    await selecionarAtividadesParaImportacao(page, processoOrigemDescricao, unidadeOrigemSigla, atividadesDescricoes);
    
    const modal = page.getByRole('dialog');
    const respostaImportacao = page.waitForResponse(response =>
        response.request().method() === 'POST' &&
        response.url().includes('/importar-atividades') &&
        response.status() === 422
    );
    await modal.getByTestId('btn-importar').click();
    const response = await respostaImportacao;
    const corpo = await response.text();

    // Modal continua aberto e exibe erro
    await expect(modal).toBeVisible();
    expect(corpo).toContain('"code":"VALIDACAO"');
    expect(corpo).toContain('já existentes no cadastro');
    await expect(modal.getByText(/já existente|não puderam ser importadas/i)).toBeVisible();
    
    // Fechar modal para continuar teste
    await modal.getByTestId('importar-atividades-modal__btn-modal-cancelar').click();
    await expect(modal).toBeHidden();
}
