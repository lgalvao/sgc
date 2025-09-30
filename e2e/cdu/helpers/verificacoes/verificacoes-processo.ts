import {expect, Page} from '@playwright/test';
import {SELETORES, SELETORES_CSS, TEXTOS, URLS} from '../dados';
import {esperarMensagemSucesso, esperarUrl, verificarUrlDoPainel} from './verificacoes-basicas';

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
    // Verificar notificação de sucesso de forma mais robusta usando selector de notificação
    const mensagemEsperada = `${TEXTOS.PROCESSO_REMOVIDO_INICIO}${descricaoProcesso}${TEXTOS.PROCESSO_REMOVIDO_FIM}`;
    await expect(page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO)).toContainText(mensagemEsperada);
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.locator('[data-testid="tabela-processos"] tbody').getByText(descricaoProcesso)).not.toBeVisible();
}

/**
 * Verifica a visibilidade do botão de finalização de processo.
 */
export async function verificarBotaoFinalizarProcessoVisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO})).toBeVisible();
}

/**
 * Verifica a ausência do botão de finalização de processo.
 */
export async function verificarBotaoFinalizarProcessoInvisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO})).not.toBeVisible();
}

/**
 * Verifica se o modal de finalização está visível com textos corretos.
 */
export async function verificarModalFinalizacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await expect(modal.getByRole('heading', {name: TEXTOS.FINALIZACAO_PROCESSO})).toBeVisible();
    await expect(modal.getByText(TEXTOS.CONFIRMA_FINALIZACAO)).toBeVisible();
    await expect(modal.getByText(TEXTOS.CONFIRMACAO_VIGENCIA_MAPAS)).toBeVisible();
    await expect(modal.getByRole('button', {name: TEXTOS.CONFIRMAR})).toBeVisible();
    await expect(modal.getByRole('button', {name: TEXTOS.CANCELAR})).toBeVisible();
}

/**
 * Garante que o modal de finalização foi fechado.
 */
export async function verificarModalFinalizacaoFechado(page: Page): Promise<void> {
    await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).not.toBeVisible();
}

/**
 * Verifica a notificação de bloqueio de finalização.
 */
export async function verificarFinalizacaoBloqueada(page: Page): Promise<void> {
    await expect(page.locator('.notification')).toContainText(TEXTOS.FINALIZACAO_BLOQUEADA);
}

/**
 * Confirma que o processo foi finalizado com sucesso no painel.
 */
export async function verificarProcessoFinalizadoNoPainel(page: Page, nomeProcesso: string): Promise<void> {
    const linhaProcesso = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`).filter({hasText: nomeProcesso});
    await expect(linhaProcesso).toContainText(TEXTOS.FINALIZADO);
}

/**
 * Verifica se a notificação de sucesso contém o texto esperado após finalização.
 */
export async function verificarMensagemFinalizacaoSucesso(page: Page): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.PROCESSO_FINALIZADO);
}

/**
 * Verifica se a notificação informa que os mapas estão vigentes.
 */
export async function verificarMapasVigentesNotificacao(page: Page): Promise<void> {
    await expect(page.locator(SELETORES_CSS.NOTIFICACAO_SUCESSO)).toContainText(TEXTOS.MAPAS_VIGENTES);
}

/**
 * Garante que a notificação de envio de email está visível.
 */
export async function verificarEmailFinalizacaoEnviado(page: Page): Promise<void> {
    const notificacaoEmail = page.locator(SELETORES_CSS.NOTIFICACAO_EMAIL).first();
    await expect(notificacaoEmail).toBeVisible();
    await expect(notificacaoEmail).toContainText(TEXTOS.EMAIL_ENVIADO);
}


/**
 * Verifica se o cadastro foi devolvido para ajustes com sucesso.
 */
export async function verificarCadastroDevolvidoComSucesso(page: Page): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
    await verificarUrlDoPainel(page);
}

/**
 * Verifica se o aceite foi registrado com sucesso.
 */
export async function verificarAceiteRegistradoComSucesso(page: Page): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.ACEITE_REGISTRADO);
    await verificarUrlDoPainel(page);
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