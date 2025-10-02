import {expect, Page} from '@playwright/test';

/**
 * Valida um mapa de competências
 */
export async function validarMapa(page: Page, sugestoes?: string): Promise<void> {
    await page.getByTestId('validar-btn').click();
    await expect(page.getByTestId('modal-validar')).toBeVisible();

    if (sugestoes) {
        await page.fill('textarea[placeholder*="sugestões"]', sugestoes);
    }

    await page.getByTestId('modal-validar-confirmar').click();
}

/**
 * Apresenta sugestões para o mapa (ação composta: abrir modal, preencher e confirmar).
 * Fica na camada de ações porque descreve a intenção de negócio "apresentar sugestões".
 */
export async function apresentarSugestoes(page: Page, sugestao: string): Promise<void> {
    await page.getByTestId('apresentar-sugestoes-btn').click();
    // O modal pode demorar a aparecer; usar expectation simples para estabilidade
    await expect(page.getByTestId('modal-apresentar-sugestoes')).toBeVisible();
    await page.getByTestId('sugestoes-textarea').fill(sugestao);
    await page.getByTestId('modal-apresentar-sugestoes-confirmar').click();
}

/**
 * Clica no botão "Impactos no mapa" para abrir o modal de impactos.
 */
export async function clicarBotaoImpactosMapa(page: Page): Promise<void> {
    await page.getByTestId('impactos-mapa-button').click();
}

/**
 * Fecha o modal de impactos no mapa.
 */
export async function fecharModalImpactos(page: Page): Promise<void> {
    await page.getByTestId('fechar-impactos-mapa-button').click();
}