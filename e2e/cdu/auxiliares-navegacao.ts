
import {expect, Page} from '@playwright/test';
import {SELETORES, SELETORES_CSS} from './constantes-teste';

/**
 * Navega para visualização de atividades de um processo/unidade específica
 */
export async function navegarParaVisualizacaoAtividades(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}`));

    await page.locator(SELETORES_CSS.LINHA_TABELA).filter({hasText: unidade}).first().click();
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}`));

    await page.waitForSelector('[data-testid="atividades-card"]');
    await page.getByTestId('atividades-card').click();
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}/vis-cadastro`));
    await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
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
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}`));
}

/**
 * Navega para mapa de competências
 */
export async function irParaMapaCompetencias(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}/mapa`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}/mapa`));
}

/**
 * Navega para visualização de mapa
 */
export async function irParaVisualizacaoMapa(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}/vis-mapa`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}/vis-mapa`));
}

// ==================================================================
// FUNÇÕES SEMÂNTICAS EM PORTUGUÊS
// ==================================================================

/**
 * Clica em um processo na tabela principal do painel.
 * @param page A instância da página.
 * @param nomeProcesso O nome do processo a ser clicado (string ou RegExp).
 */
export async function clicarProcesso(page: Page, nomeProcesso: string | RegExp) {
    await page.getByTestId(SELETORES.TABELA_PROCESSOS).getByRole('row', { name: nomeProcesso }).click();
}

/**
 * Clica no cabeçalho de uma coluna da tabela de processos para ordenar.
 * @param page A instância da página.
 * @param testIdColuna O test-id da coluna a ser clicada.
 */
export async function ordenarTabelaProcessosPorColuna(page: Page, testIdColuna: string) {
    await page.getByTestId(testIdColuna).click();
}

/**
 * Clica no botão para expandir todas as unidades na árvore de um processo.
 */
export async function expandirTodasAsUnidades(page: Page) {
    await page.getByTestId('btn-expandir-todas').click();
}

/**
 * Clica em uma unidade na árvore de um processo.
 * @param page A instância da página.
 * @param nomeUnidade O nome da unidade a ser clicada.
 */
export async function clicarUnidade(page: Page, nomeUnidade: string) {
    await page.getByRole('row', { name: nomeUnidade }).click();
}
