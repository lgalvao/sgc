import {expect, Page} from '@playwright/test';
import {DADOS_TESTE, ROTULOS, SELETORES, SELETORES_CSS, TEXTOS, URLS} from '../dados';

/**
 * FUNÇÕES DE NAVEGAÇÃO E LOGIN
 * Funções específicas para navegação entre páginas e autenticação
 */

/**
 * Verifica URL com regex (função local para evitar dependência circular)
 */
async function verificarUrl(page: Page, url: string): Promise<void> {
    const regexUrl = new RegExp(url.replace(/\*\*/g, '.*'));
    await expect(page).toHaveURL(regexUrl);
}

/**
 * Espera um texto ficar visível na página (função local para evitar dependência circular)
 */
async function esperarTextoVisivel(page: Page, texto: string): Promise<void> {
    await expect(page.getByText(texto)).toBeVisible();
}

/**
 * Navega para criação de processo
 */
export async function navegarParaCriacaoProcesso(page: Page): Promise<void> {
    await page.goto(URLS.PROCESSO_CADASTRO);
}

/**
 * Navega para detalhes de um processo
 */
export async function navegarParaDetalhesProcesso(page: Page, textoProcesso: string): Promise<void> {
    const linhaProcesso = page.locator(SELETORES_CSS.LINHA_TABELA).filter({hasText: textoProcesso}).first();
    await linhaProcesso.click();
    await expect(page).toHaveURL(/\/processo\/\d+/);
}

/**
 * Navega para cadastro de atividades
 */
export async function navegarParaCadastroAtividades(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}/cadastro`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/\/processo\/\d+\/[^/]+\/cadastro/);
    await esperarTextoVisivel(page, TEXTOS.CADASTRO_ATIVIDADES_CONHECIMENTOS);
}

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

/**
 * Navega para subprocesso específico
 */
export async function irParaSubprocesso(page: Page, idProcesso: number, unidade: string): Promise<void> {
    await page.goto(`/processo/${idProcesso}/${unidade}`);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(new RegExp(`/processo/${idProcesso}/${unidade}`));
}

/**
 * Navega para detalhes de processo por texto
 */
export async function irParaProcessoPorTexto(page: Page, textoProcesso: string): Promise<void> {
    const linhaProcesso = page.locator(SELETORES_CSS.LINHA_TABELA).filter({hasText: textoProcesso}).first();
    await linhaProcesso.click();
    await expect(page).toHaveURL(/\/processo\/\d+/);
}

// ==================================================================
// FUNÇÕES DE VERIFICAÇÃO DE NAVEGAÇÃO
// ==================================================================

/**
 * Verifica se a navegação para a página de um subprocesso foi bem-sucedida
 */
export async function verificarNavegacaoPaginaSubprocesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/.*\/processo\/\d+\/\w+$/);
    await expect(page.getByTestId('subprocesso-header').first()).toBeVisible();
    await expect(page.getByTestId('processo-info').first()).toBeVisible();
}

/**
 * Verifica se a navegação para a página de cadastro/edição de processo foi bem-sucedida
 */
export async function verificarNavegacaoPaginaCadastroProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/.*\/processo\/cadastro\?idProcesso=\d+/);
    await expect(page.getByRole('heading', {name: 'Cadastro de Processo'})).toBeVisible();
}

/**
 * Verifica se a navegação para a página de detalhes de um processo foi bem-sucedida
 */
export async function verificarNavegacaoPaginaDetalhesProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/.*\/processo\/\d+$/);
    await expect(page.getByTestId('processo-info').first()).toBeVisible();
}

// ==================================================================
// FUNÇÕES DE INTERAÇÃO COM TABELAS E ELEMENTOS
// ==================================================================

/**
 * Clica no primeiro processo da tabela após login
 */
export async function clicarPrimeiroProcesso(page: Page): Promise<void> {
    await page.locator(SELETORES_CSS.LINHA_TABELA).first().click();
}

/**
 * Clica em um processo na tabela principal do painel
 */
export async function clicarProcesso(page: Page, nomeProcesso: string | RegExp): Promise<void> {
    await page.getByTestId(SELETORES.TABELA_PROCESSOS).getByRole('row', {name: nomeProcesso}).click();
}

/**
 * Clica no cabeçalho de uma coluna da tabela de processos para ordenar
 */
export async function ordenarTabelaProcessosPorColuna(page: Page, testIdColuna: string): Promise<void> {
    await page.getByTestId(testIdColuna).click();
}

/**
 * Clica no botão para expandir todas as unidades na árvore de um processo
 */
export async function expandirTodasAsUnidades(page: Page): Promise<void> {
    await page.getByTestId('btn-expandir-todas').click();
}

/**
 * Clica em uma unidade na árvore de um processo
 */
export async function clicarUnidade(page: Page, nomeUnidade: string): Promise<void> {
    await page.getByRole('row', {name: nomeUnidade}).click();
}

// ==================================================================
// FUNÇÕES DE LOGIN E AUTENTICAÇÃO
// ==================================================================

/**
 * Login genérico para diferentes perfis
 */
async function fazerLoginComo(page: Page, perfil: keyof typeof DADOS_TESTE.PERFIS, idServidorOverride?: string): Promise<void> {
    const dadosUsuario = DADOS_TESTE.PERFIS[perfil];
    const finalIdServidor = idServidorOverride || dadosUsuario.idServidor;
    await page.context().addInitScript((dados) => {
        localStorage.setItem('idServidor', dados.idServidor);
        localStorage.setItem('perfilSelecionado', dados.perfil);
        localStorage.setItem('unidadeSelecionada', dados.unidade);
    }, {...dadosUsuario, idServidor: finalIdServidor});
    await page.goto(URLS.PAINEL);
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/\/painel/);
}

export const loginComoAdmin = (page: Page, idServidor?: string) => fazerLoginComo(page, 'ADMIN', idServidor);
export const loginComoGestor = (page: Page, idServidor?: string) => fazerLoginComo(page, 'GESTOR', idServidor);
export const loginComoChefe = (page: Page, idServidor?: string) => fazerLoginComo(page, 'CHEFE', idServidor);
export const loginComoChefeSedia = (page: Page, idServidor?: string) => fazerLoginComo(page, 'CHEFE_SEDIA', idServidor);
export const loginComoServidor = (page: Page, idServidor?: string) => fazerLoginComo(page, 'SERVIDOR', idServidor);

/**
 * Realiza o login pela UI, preenchendo título e senha.
 * Não clica em "Entrar", permitindo interações adicionais na tela de login.
 */
export async function login(page: Page, idServidor: string): Promise<void> {
    await page.goto(URLS.LOGIN);
    await page.waitForLoadState('networkidle');

    // O login mockado usa o ID do servidor como "título" e uma senha padrão
    await page.getByLabel(ROTULOS.TITULO_ELEITORAL).fill(idServidor);
    await page.getByLabel(ROTULOS.SENHA).fill('senha-padrao'); // A senha é ignorada pelo mock
}

/**
 * Navega para a página inicial (raiz do site).
 */
export async function navegarParaHome(page: Page): Promise<void> {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
}

/**
 * Clica no botão "Entrar" na tela de login.
 */
export async function clicarBotaoEntrar(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.ENTRAR }).click();
}

/**
 * Clica no botão/link "Sair" para fazer logout.
 */
export async function clicarBotaoSair(page: Page): Promise<void> {
    await page.locator('a[title="Sair"]').click();
}