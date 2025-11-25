import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS, URLS} from '../dados';
import {loginComoAdmin, loginComoGestor} from '../auth';

/**
 * Espera um texto ficar visível na página.
 * @param page A instância da página do Playwright.
 * @param texto O texto a ser esperado.
 */
async function esperarTextoVisivel(page: Page, texto: string): Promise<void> {
    await expect(page.getByText(texto)).toBeVisible();
}

/**
 * Navega para a página de criação de processo.
 * @param page A instância da página do Playwright.
 */
export async function navegarParaCriacaoProcesso(page: Page): Promise<void> {
    await page.goto(URLS.PROCESSO_CADASTRO);
    await page.waitForSelector(SELETORES.CAMPO_DESCRICAO, { state: 'visible' });
}

/**
 * Navega para a página de detalhes de um processo.
 * @param page A instância da página do Playwright.
 * @param textoProcesso O texto que identifica o processo na tabela.
 */
export async function navegarParaDetalhesProcesso(page: Page, textoProcesso: string): Promise<void> {
    const linhaProcesso = page.locator(SELETORES.LINHA_TABELA).filter({hasText: textoProcesso}).first();
    await linhaProcesso.click();
    await expect(page).toHaveURL(/\/processo\/\d+/);
}

/**
 * Navega para a página de cadastro de atividades de um subprocesso.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param unidade A sigla da unidade.
 */
export async function navegarParaCadastroAtividades(page: Page, codProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${codProcesso}/${unidade}/cadastro`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+\/cadastro/);
    await esperarTextoVisivel(page, TEXTOS.CADASTRO_ATIVIDADES_CONHECIMENTOS);
}

/**
 * Navega para a página de visualização de atividades de um subprocesso.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param unidade A sigla da unidade.
 */
export async function navegarParaVisualizacaoAtividades(page: Page, codProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${codProcesso}`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}`));

    await page.locator(SELETORES.LINHA_TABELA).filter({hasText: unidade}).first().click();
    await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}/${unidade}`));

    await page.waitForSelector('[data-testid="atividades-card"]');
    await page.getByTestId('atividades-card').click();
    await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}/${unidade}/vis-cadastro`));
    await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
}

/**
 * Acessa a tela de análise da revisão de cadastro de atividades e conhecimentos.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param unidade A sigla da unidade.
 */
export async function acessarAnaliseRevisaoCadastro(page: Page, codProcesso: number, unidade: string): Promise<void> {
    await irParaSubprocesso(page, codProcesso, unidade);
    await page.goto(`/processo/${codProcesso}/${unidade}/vis-cadastro`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}/${unidade}/vis-cadastro`));
    await expect(page.getByRole('heading', {name: TEXTOS.CADASTRO_ATIVIDADES_CONHECIMENTOS})).toBeVisible();
}

/**
 * Realiza o login como gestor e acessa a análise de revisão do cadastro.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param unidade A sigla da unidade.
 */
export async function acessarAnaliseRevisaoComoGestor(page: Page, codProcesso: number, unidade: string): Promise<void> {
    await loginComoGestor(page);
    await acessarAnaliseRevisaoCadastro(
        page,
        codProcesso,
        unidade
    );
}

/**
 * Realiza o login como administrador e acessa a análise de revisão do cadastro.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param unidade A sigla da unidade.
 */
export async function acessarAnaliseRevisaoComoAdmin(page: Page, codProcesso: number, unidade: string): Promise<void> {
    await loginComoAdmin(page);
    await acessarAnaliseRevisaoCadastro(
        page,
        codProcesso,
        unidade
    );
}

/**
 * Navega para a página do mapa de competências de um subprocesso.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param unidade A sigla da unidade.
 */
export async function irParaMapaCompetencias(page: Page, codProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${codProcesso}/${unidade}/mapa`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}/${unidade}/mapa`));
}

/**
 * Navega para a página de visualização do mapa de um subprocesso.
 * @param page A instância da página do Playwright.
 * @param idProcesso O ID do processo.
 * @param unidade A sigla da unidade.
 */
export async function irParaVisualizacaoMapa(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}/vis-mapa`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}/vis-mapa`));
}

/**
 * Navega para a página de edição do mapa de um subprocesso.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param siglaUnidade A sigla da unidade.
 */
export async function navegarParaEdicaoMapa(page: Page, codProcesso: number, siglaUnidade: string): Promise<void> {
    await page.goto(`/processo/${codProcesso}/${siglaUnidade}/mapa`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}/${siglaUnidade}/mapa`));
    await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
}

/**
 * Navega para a página do mapa de competências de um processo de revisão.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param siglaUnidade A sigla da unidade.
 */
export async function navegarParaMapaRevisao(page: Page, codProcesso: number, siglaUnidade: string): Promise<void> {
    await loginComoAdmin(page);
    await irParaMapaCompetencias(page, codProcesso, siglaUnidade);
    await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
}

/**
 * Navega para a página do mapa de competências de um processo de mapeamento.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param siglaUnidade A sigla da unidade.
 */
export async function navegarParaMapaMapeamento(page: Page, codProcesso: number, siglaUnidade: string): Promise<void> {
    await loginComoAdmin(page);
    await irParaMapaCompetencias(page, codProcesso, siglaUnidade);
    await esperarTextoVisivel(page, TEXTOS.MAPA_COMPETENCIAS_TECNICAS);
}

/**
 * Navega para a página de um subprocesso.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 * @param unidade A sigla da unidade.
 */
export async function irParaSubprocesso(page: Page, codProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${codProcesso}/${unidade}`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}/${unidade}`));
}

/**
 * Navega para a página de detalhes de um processo a partir da tabela de processos.
 * @param page A instância da página do Playwright.
 * @param textoProcesso O texto que identifica o processo na tabela.
 */
export async function irParaProcessoPorTexto(page: Page, textoProcesso: string): Promise<void> {
    const linhaProcesso = page.locator(SELETORES.LINHA_TABELA).filter({hasText: textoProcesso}).first();
    await linhaProcesso.click();
    await expect(page).toHaveURL(/\/processo\/\d+/);
}

/**
 * Navega para a página de um processo pelo seu ID.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 */
export async function navegarParaProcessoPorId(page: Page, codProcesso: number): Promise<void> {
    await page.goto(`/processo/${codProcesso}`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}`));
}

/**
 * Verifica se a navegação para a página de um subprocesso foi bem-sucedida.
 * @param page A instância da página do Playwright.
 */
export async function verificarNavegacaoPaginaSubprocesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
    await expect(page.getByTestId('subprocesso-header').first()).toBeVisible();
    await expect(page.getByTestId('processo-info').first()).toBeVisible();
}

/**
 * Verifica se a navegação para a página de cadastro de processo foi bem-sucedida.
 * @param page A instância da página do Playwright.
 */
export async function verificarNavegacaoPaginaCadastroProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/.*\/processo\/cadastro\?codProcesso=\d+/);
    await expect(page.getByRole('heading', {name: 'Cadastro de Processo'})).toBeVisible({ timeout: 2000 });
}

/**
 * Verifica se a navegação para a página de detalhes de um processo foi bem-sucedida.
 * @param page A instância da página do Playwright.
 */
export async function verificarNavegacaoPaginaDetalhesProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/.*\/processo\/\d+$/);
    await expect(page.getByTestId('processo-info').first()).toBeVisible();
}

/**
 * Clica no primeiro processo da tabela.
 * @param page A instância da página do Playwright.
 */
export async function clicarPrimeiroProcesso(page: Page): Promise<void> {
    await page.locator(SELETORES.LINHA_TABELA).first().click();
}

/**
 * Clica em um processo na tabela de processos.
 * @param page A instância da página do Playwright.
 * @param nomeProcesso O nome do processo.
 */
export async function clicarProcesso(page: Page, nomeProcesso: string | RegExp): Promise<void> {
    await page.locator(SELETORES.TABELA_PROCESSOS).locator('tr', {hasText: nomeProcesso}).click();
}

/**
 * Ordena a tabela de processos por uma coluna.
 * @param page A instância da página do Playwright.
 * @param testIdColuna O test-id da coluna.
 */
export async function ordenarTabelaProcessosPorColuna(page: Page, testIdColuna: string): Promise<void> {
    await page.getByTestId(testIdColuna).click();
}

/**
 * Expande todas as unidades na árvore de hierarquia.
 * @param page A instância da página do Playwright.
 */
export async function expandirTodasAsUnidades(page: Page): Promise<void> {
    await page.getByTestId('btn-expandir-todas').click();
}

/**
 * Clica em uma unidade na árvore de hierarquia.
 * @param page A instância da página do Playwright.
 * @param nomeUnidade O nome da unidade.
 */
export async function clicarUnidade(page: Page, nomeUnidade: string): Promise<void> {
    await page.getByRole('row', {name: nomeUnidade}).click();
}

/**
 * Navega para a página inicial.
 * @param page A instância da página do Playwright.
 */
export async function navegarParaHome(page: Page): Promise<void> {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
}

/**
 * Clica no botão "Sair".
 * @param page A instância da página do Playwright.
 */
export async function clicarBotaoSair(page: Page): Promise<void> {
    await page.locator('a[title="Sair"]').click();
}
