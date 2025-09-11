import {expect, Page} from '@playwright/test';
import {SELETORES_CSS} from './constantes-teste';

/**
 * Navega para visualização de atividades de um processo/unidade específica
 */
export async function navegarParaVisualizacaoAtividades(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}`);
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}`));

    await page.locator(SELETORES_CSS.LINHA_TABELA).filter({hasText: unidade}).first().click();
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}`));

    await page.waitForSelector('[data-testid="atividades-card"]');
    await page.getByTestId('atividades-card').click();
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}/vis-cadastro`));
}


/**
 * Clica no primeiro processo da tabela após login
 */
export async function clicarPrimeiroProcesso(page: Page): Promise<void> {
    await page.locator(SELETORES_CSS.LINHA_TABELA).first().click();
}
/**
 * Navega para detalhes de processo por texto
 */
export async function irParaProcessoPorTexto(page: Page, textoProcesso: string): Promise<void> {
    const linhaProcesso = page.locator(SELETORES_CSS.LINHA_TABELA).filter({hasText: textoProcesso}).first();
    await linhaProcesso.click();
    await expect(page).toHaveURL(/\/processo\/\d+/);
}

/**
 * Navega para subprocesso específico
 */
export async function irParaSubprocesso(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}`);
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}`));
}

/**
 * Navega para mapa de competências
 */
export async function irParaMapaCompetencias(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}/mapa`);
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}/mapa`));
}

/**
 * Navega para visualização de mapa
 */
export async function irParaVisualizacaoMapa(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}/vis-mapa`);
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}/vis-mapa`));
}

/**
 * Navega para cadastro de atividades
 */
export async function navegarParaCadastroAtividades(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}/cadastro`);
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}/cadastro`));
}