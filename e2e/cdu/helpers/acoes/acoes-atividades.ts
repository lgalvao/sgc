import {expect, Locator, Page} from '@playwright/test';
import {SELETORES, SELETORES_CSS} from '../dados';

/**
 * AÇÕES ESPECÍFICAS PARA ATIVIDADES E CONHECIMENTOS
 * Funções para gerenciamento de atividades e conhecimentos em testes
 */

/**
 * Adiciona uma atividade
 */
export async function adicionarAtividade(page: Page, nomeAtividade: string): Promise<void> {
    await page.getByTestId(SELETORES.INPUT_NOVA_ATIVIDADE).fill(nomeAtividade);
    await page.getByTestId(SELETORES.BTN_ADICIONAR_ATIVIDADE).click();
    await expect(page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade})).toBeVisible();
}

/**
 * Adiciona um conhecimento a uma atividade
 */
export async function adicionarConhecimento(cardAtividade: Locator, nomeConhecimento: string): Promise<void> {
    await cardAtividade.locator(`[data-testid="${SELETORES.INPUT_NOVO_CONHECIMENTO}"]`).fill(nomeConhecimento);
    await cardAtividade.locator(`[data-testid="${SELETORES.BTN_ADICIONAR_CONHECIMENTO}"]`).click();
    await expect(cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento})).toBeVisible();
}

/**
 * Edita uma atividade
 */
export async function editarAtividade(page: Page, cardAtividade: Locator, novoNome: string): Promise<void> {
    await cardAtividade.hover();
    await page.waitForTimeout(100);
    await cardAtividade.getByTestId(SELETORES.BTN_EDITAR_ATIVIDADE).click({force: true});
    await page.getByTestId(SELETORES.INPUT_EDITAR_ATIVIDADE).fill(novoNome);
    await page.getByTestId(SELETORES.BTN_SALVAR_EDICAO_ATIVIDADE).click();
}

/**
 * Remove uma atividade com confirmação
 */
export async function removerAtividade(page: Page, cardAtividade: Locator): Promise<void> {
    await cardAtividade.hover();
    await page.waitForTimeout(100);
    page.on('dialog', dialog => dialog.accept());
    await cardAtividade.getByTestId(SELETORES.BTN_REMOVER_ATIVIDADE).click({force: true});
}

/**
 * Edita um conhecimento usando o modal (novo padrão)
 */
export async function editarConhecimento(page: Page, linhaConhecimento: Locator, novoNome: string): Promise<void> {
    // Clicar no botão editar
    const btnEditar = linhaConhecimento.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO);
    await linhaConhecimento.hover();
    await btnEditar.click();

    // Aguardar o modal aparecer
    await page.getByTestId('input-conhecimento-modal').waitFor({state: 'visible'});

    // Preencher o novo nome
    await page.getByTestId('input-conhecimento-modal').fill(novoNome);

    // Salvar
    await page.getByTestId('btn-salvar-conhecimento-modal').click();

    // Aguardar o modal fechar
    await page.getByTestId('input-conhecimento-modal').waitFor({state: 'hidden'});
}

/**
 * Remove um conhecimento com confirmação
 */
export async function removerConhecimento(page: Page, linhaConhecimento: Locator): Promise<void> {
    await linhaConhecimento.hover();
    await page.waitForTimeout(100);
    page.on('dialog', dialog => dialog.accept());
    await linhaConhecimento.getByTestId(SELETORES.BTN_REMOVER_CONHECIMENTO).click();
}

/**
 * Cria uma nova competência
 */
export async function criarCompetencia(page: Page, descricao: string): Promise<void> {
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await page.getByTestId('input-nova-competencia').fill(descricao);

    // Selecionar primeira atividade disponível
    await page.locator('[data-testid="atividade-nao-associada"] label').first().click();

    await page.getByTestId('btn-criar-competencia').click();
}
