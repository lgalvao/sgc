import {Page} from '@playwright/test';
import {SELETORES} from '../dados';

export async function adicionarAtividade(page: Page, nomeAtividade: string): Promise<void> {
    await page.getByTestId(SELETORES.INPUT_NOVA_ATIVIDADE).fill(nomeAtividade);
    await page.getByTestId(SELETORES.BTN_ADICIONAR_ATIVIDADE).click();
}

export async function adicionarConhecimentoNaAtividade(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    await cardAtividade.getByTestId(SELETORES.INPUT_NOVO_CONHECIMENTO).fill(nomeConhecimento);
    await cardAtividade.getByTestId(SELETORES.BTN_ADICIONAR_CONHECIMENTO).click();
}

export async function editarAtividade(page: Page, nomeAtual: string, nomeNovo: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtual});
    await cardAtividade.getByTestId(SELETORES.BTN_EDITAR_ATIVIDADE).click();
    await cardAtividade.getByTestId(SELETORES.INPUT_EDITAR_ATIVIDADE).fill(nomeNovo);
    await cardAtividade.getByTestId(SELETORES.BTN_SALVAR_EDICAO_ATIVIDADE).click();
}

export async function removerAtividade(page: Page, nomeAtividade: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    await cardAtividade.getByTestId(SELETORES.BTN_REMOVER_ATIVIDADE).click();
}

export async function editarConhecimento(page: Page, nomeAtividade: string, nomeAtual: string, nomeNovo: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    const conhecimento = cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: nomeAtual});
    await conhecimento.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO).click();
    await conhecimento.locator('input[type="text"]').fill(nomeNovo);
    await conhecimento.locator('button[type="submit"]').click();
}

export async function removerConhecimento(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    const conhecimento = cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento});
    await conhecimento.getByTestId(SELETORES.BTN_REMOVER_CONHECIMENTO).click();
}

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
