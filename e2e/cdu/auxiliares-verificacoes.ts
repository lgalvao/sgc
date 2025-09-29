import {expect, Locator, Page} from '@playwright/test';
import {DADOS_TESTE, SELETORES, SELETORES_CSS, TEXTOS, URLS} from './constantes-teste';

/**
 * Clica em um botão pelo nome
 */
export async function clicarBotao(page: Page, nome: string): Promise<void> {
    await page.getByRole('button', {name: nome}).click();
}

/**
 * Espera uma mensagem de sucesso aparecer
 */
export async function esperarMensagemSucesso(page: Page, mensagem: string): Promise<void> {
    const notificacao = page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO, {hasText: mensagem});
    await expect(notificacao).toBeVisible();
}

/**
 * Espera um texto ficar visível na página
 */
export async function esperarTextoVisivel(page: Page, texto: string): Promise<void> {
    await expect(page.getByText(texto)).toBeVisible();
}

/**
 * Espera um elemento com test-id ficar visível
 */
export async function esperarElementoVisivel(page: Page, testId: string): Promise<void> {
    await expect(page.getByTestId(testId).first()).toBeVisible();
}

/**
 * Verifica URL com regex
 */
export async function verificarUrl(page: Page, url: string): Promise<void> {
    const regexUrl = new RegExp(url.replace(/\*\*/g, '.*'));
    await expect(page).toHaveURL(regexUrl);
}

/**
 * Espera uma mensagem de erro aparecer
 */
export async function esperarMensagemErro(page: Page, mensagem: string): Promise<void> {
    const notificacao = page.locator(SELETORES_CSS.NOTIFICACAO_ERRO);
    await expect(notificacao).toBeVisible();
    await expect(notificacao).toContainText(mensagem);
}

/**
 * Verifica elementos comuns do painel após login
 */
export async function verificarElementosPainel(page: Page): Promise<void> {
    await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.TITULO_ALERTAS);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.COLUNA_DESCRICAO);
    await esperarElementoVisivel(page, SELETORES.COLUNA_TIPO);
    await esperarElementoVisivel(page, SELETORES.COLUNA_UNIDADES);
    await esperarElementoVisivel(page, SELETORES.COLUNA_SITUACAO);
}

/**
 * Navega para criação de processo
 */
export async function navegarParaCriacaoProcesso(page: Page): Promise<void> {
    await page.getByText(TEXTOS.CRIAR_PROCESSO).click();
    await verificarUrl(page, `**${URLS.PROCESSO_CADASTRO}`);
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
    await page.getByTestId('input-conhecimento-modal').waitFor({ state: 'visible' });
    
    // Preencher o novo nome
    await page.getByTestId('input-conhecimento-modal').fill(novoNome);
    
    // Salvar
    await page.getByTestId('btn-salvar-conhecimento-modal').click();
    
    // Aguardar o modal fechar
    await page.getByTestId('input-conhecimento-modal').waitFor({ state: 'hidden' });
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
 * Espera por um elemento ser invisível
 */
export async function esperarElementoInvisivel(page: Page, seletor: string): Promise<void> {
    await expect(page.getByTestId(seletor).first()).not.toBeVisible();
}

/**
 * Espera por uma URL específica
 */
export async function esperarUrl(page: Page, url: string | RegExp): Promise<void> {
    if (typeof url === 'string') {
        await expect(page).toHaveURL(new RegExp(url));
    } else {
        await expect(page).toHaveURL(url);
    }
}

// ==================================================================
// FUNÇÕES SEMÂNTICAS EM PORTUGUÊS
// ==================================================================

/**
 * Verifica os elementos básicos do painel de processos.
 */
export async function verificarElementosBasicosPainel(page: Page) {
    await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
}

/**
 * Garante que o botão "Criar processo" não está visível.
 */
export async function verificarAusenciaBotaoCriarProcesso(page: Page) {
    await esperarElementoInvisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
}

/**
 * Verifica a visibilidade de um processo específico na tabela de processos.
 * @param page A instância da página.
 * @param nomeProcesso O nome do processo a ser verificado (string ou RegExp).
 * @param visivel `true` se o processo deve estar visível, `false` caso contrário.
 */
export async function verificarVisibilidadeProcesso(page: Page, nomeProcesso: string | RegExp, visivel: boolean) {
    const processo = page.getByRole('row', { name: nomeProcesso });
    if (visivel) {
        await expect(processo).toBeVisible();
    } else {
        await expect(processo).toBeHidden();
    }
}

/**
 * Verifica se a navegação para a página de um subprocesso foi bem-sucedida.
 */
export async function verificarNavegacaoPaginaSubprocesso(page: Page) {
    await esperarUrl(page, /.*\/processo\/\d+\/\w+$/);
    await esperarElementoVisivel(page, SELETORES.SUBPROCESSO_HEADER);
    await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
}

/**
 * Verifica se a navegação para a página de cadastro/edição de processo foi bem-sucedida.
 */
export async function verificarNavegacaoPaginaCadastroProcesso(page: Page) {
    await esperarUrl(page, /.*\/processo\/cadastro\?idProcesso=\d+/);
    await expect(page.getByRole('heading', { name: 'Cadastro de Processo' })).toBeVisible();
}

/**
 * Verifica se a navegação para a página de detalhes de um processo foi bem-sucedida.
 */
export async function verificarNavegacaoPaginaDetalhesProcesso(page: Page) {
    await esperarUrl(page, /.*\/processo\/\d+$/);
    await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
}
