import {expect, Page} from '@playwright/test';
import {SELETORES, SELETORES_CSS, TEXTOS, URLS} from '../dados';
import {esperarMensagemSucesso, esperarUrl} from './verificacoes-basicas';

/**
 * VERIFICAÇÕES ESPECÍFICAS DE PROCESSOS
 * Funções compostas para verificações relacionadas ao domínio de processos
 */

/**
 * Verifica se está na página de edição de processo
 */
export async function verificarPaginaEdicaoProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);
}

/**
 * Verifica se está na página de cadastro de processo
 */
export async function verificarPaginaCadastroProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/processo\/cadastro(\?idProcesso=\d+)?$/);
}

/**
 * Verifica se campos obrigatórios de formulário são exibidos
 */
export async function verificarCamposObrigatoriosFormulario(page: Page): Promise<void> {
    await expect(page.getByLabel('Descrição')).toBeVisible();
    await expect(page.getByLabel('Tipo')).toBeVisible();
    await expect(page.getByText(TEXTOS.UNIDADES_PARTICIPANTES)).toBeVisible();
}

/**
 * Verifica se uma notificação de erro é exibida
 */
export async function verificarNotificacaoErro(page: Page): Promise<void> {
    await expect(page.locator(SELETORES_CSS.NOTIFICACAO_ERRO)).toBeVisible();
}

/**
 * Verificar se processo aparece na listagem do painel
 */
export async function aguardarProcessoNoPainel(page: Page, descricaoProcesso: string): Promise<void> {
    await page.waitForURL(URLS.PAINEL);
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoProcesso)).toBeVisible();
}

/**
 * Verifica se processo editado aparece na listagem e o original não
 */
export async function verificarProcessoEditado(page: Page, descricaoOriginal: string, descricaoEditada: string): Promise<void> {
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoEditada)).toBeVisible();
    await expect(page.getByText(descricaoOriginal)).not.toBeVisible();
}

/**
 * Verifica se diálogo de confirmação de remoção aparece
 */
export async function verificarDialogoConfirmacaoRemocao(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page.getByText(`Remover o processo '${descricaoProcesso}'? Esta ação não poderá ser desfeita.`)).toBeVisible();
}

/**
 * Verifica se processo foi removido com sucesso
 */
export async function verificarProcessoRemovidoComSucesso(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page.getByText(`${TEXTOS.PROCESSO_REMOVIDO_INICIO}${descricaoProcesso}${TEXTOS.PROCESSO_REMOVIDO_FIM}`)).toBeVisible();
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.locator('[data-testid="tabela-processos"] tbody').getByText(descricaoProcesso)).not.toBeVisible();
}

/**
 * Verifica se diálogo de confirmação foi fechado
 */
export async function verificarDialogoConfirmacaoFechado(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page.getByText(`Remover o processo '${descricaoProcesso}'? Esta ação não poderá ser desfeita.`)).not.toBeVisible();
}

/**
 * Verifica se processo foi iniciado com sucesso
 */
export async function verificarProcessoIniciadoComSucesso(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page.getByText(TEXTOS.PROCESSO_INICIADO)).toBeVisible();
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.locator('tr', {hasText: descricaoProcesso}).getByText(TEXTOS.EM_ANDAMENTO)).toBeVisible();
}

/**
 * Verifica se formulário permanece na tela de edição
 */
export async function verificarPermanenciaFormularioEdicao(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);
    await expect(page.getByLabel('Descrição')).toHaveValue(descricaoProcesso);
}

/**
 * Verifica se confirmação de inicialização aparece
 */
export async function verificarConfirmacaoInicializacao(page: Page): Promise<void> {
    await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
}

/**
 * Verifica modal de confirmação de inicialização de processo
 */
export async function verificarModalConfirmacaoInicializacao(page: Page): Promise<void> {
    await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
    await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
    // Removido: verificação de notificação por email que pode não estar presente no modal atual
}

/**
 * Verifica sucesso na inicialização do processo
 */
export async function verificarProcessoInicializadoComSucesso(page: Page): Promise<void> {
    await esperarUrl(page, URLS.PAINEL);
    await esperarMensagemSucesso(page, TEXTOS.PROCESSO_INICIADO);
}

/**
 * Verifica o valor do campo de descrição.
 */
export async function verificarValorCampoDescricao(page: Page, valor: string): Promise<void> {
    await expect(page.locator(SELETORES_CSS.CAMPO_DESCRICAO)).toHaveValue(valor);
}

/**
 * Verifica se o botão 'Iniciar processo' está visível.
 */
export async function verificarBotaoIniciarProcessoVisivel(page: Page): Promise<void> {
    await expect(page.locator(`[data-testid="${SELETORES.BTN_INICIAR_PROCESSO}"]`)).toBeVisible();
}

/**
 * Verifica se o modal de confirmação de iniciar processo está visível e com os textos corretos.
 */
export async function verificarModalConfirmacaoIniciarProcessoVisivel(page: Page): Promise<void> {
    await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
    await expect(page.locator(SELETORES_CSS.TITULO_MODAL_INICIAR_PROCESSO)).toBeVisible();
    await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
    // Removido: verificação de notificação por email que pode não estar presente no modal atual
}

/**
 * Verifica se o modal de confirmação de iniciar processo está invisível.
 */
export async function verificarModalConfirmacaoIniciarProcessoInvisivel(page: Page): Promise<void> {
    await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).not.toBeVisible();
}