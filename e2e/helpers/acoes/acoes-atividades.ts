import {Page} from '@playwright/test';
import {SELETORES} from '../dados';
import {extrairIdDoSeletor} from '../utils/utils';

/**
 * Adiciona uma nova atividade.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade a ser adicionada.
 */
export async function adicionarAtividade(page: Page, nomeAtividade: string): Promise<void> {
    await page.getByTestId(extrairIdDoSeletor(SELETORES.INPUT_NOVA_ATIVIDADE)).fill(nomeAtividade);
    await page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_ADICIONAR_ATIVIDADE)).click();
}

/**
 * Adiciona um novo conhecimento a uma atividade existente.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade à qual o conhecimento será adicionado.
 * @param nomeConhecimento O nome do conhecimento a ser adicionado.
 */
export async function adicionarConhecimentoNaAtividade(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    await cardAtividade.getByTestId(extrairIdDoSeletor(SELETORES.INPUT_NOVO_CONHECIMENTO)).fill(nomeConhecimento);
    await cardAtividade.getByTestId(extrairIdDoSeletor(SELETORES.BTN_ADICIONAR_CONHECIMENTO)).click();
}

/**
 * Edita o nome de uma atividade existente.
 * @param page A instância da página do Playwright.
 * @param nomeAtual O nome atual da atividade.
 * @param nomeNovo O novo nome da atividade.
 */
export async function editarAtividade(page: Page, nomeAtual: string, nomeNovo: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtual});
    await cardAtividade.getByTestId(extrairIdDoSeletor(SELETORES.BTN_EDITAR_ATIVIDADE)).click();
    await cardAtividade.getByTestId(extrairIdDoSeletor(SELETORES.INPUT_EDITAR_ATIVIDADE)).fill(nomeNovo);
    await cardAtividade.getByTestId(extrairIdDoSeletor(SELETORES.BTN_SALVAR_EDICAO_ATIVIDADE)).click();
}

/**
 * Remove uma atividade.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade a ser removida.
 */
export async function removerAtividade(page: Page, nomeAtividade: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    await cardAtividade.getByTestId(extrairIdDoSeletor(SELETORES.BTN_REMOVER_ATIVIDADE)).click();
}

/**
 * Edita o nome de um conhecimento existente em uma atividade.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade que contém o conhecimento.
 * @param nomeAtual O nome atual do conhecimento.
 * @param nomeNovo O novo nome do conhecimento.
 */
export async function editarConhecimento(page: Page, nomeAtividade: string, nomeAtual: string, nomeNovo: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    const conhecimento = cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: nomeAtual});
    await conhecimento.getByTestId(extrairIdDoSeletor(SELETORES.BTN_EDITAR_CONHECIMENTO)).click();
    await conhecimento.locator('input[type="text"]').fill(nomeNovo);
    await conhecimento.locator('button[type="submit"]').click();
}

/**
 * Remove um conhecimento de uma atividade.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade da qual o conhecimento será removido.
 * @param nomeConhecimento O nome do conhecimento a ser removido.
 */
export async function removerConhecimento(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    const conhecimento = cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento});
    await conhecimento.getByTestId(extrairIdDoSeletor(SELETORES.BTN_REMOVER_CONHECIMENTO)).click();
}

/**
 * Associa todas as atividades a uma competência.
 * @param page A instância da página do Playwright.
 * @param nomeCompetencia O nome da competência à qual as atividades serão associadas.
 */
export async function associarTodasAtividadesACompetencia(page: Page, nomeCompetencia: string): Promise<void> {
    await page.locator('.competencia-card').filter({ hasText: nomeCompetencia }).click();
    const atividades = await page.locator('.atividade-card-item').all();
    for (const atividade of atividades) {
        if (!(await atividade.isChecked())) {
            await atividade.click();
        }
    }
    await page.getByTestId('btn-criar-competencia').click();
}
