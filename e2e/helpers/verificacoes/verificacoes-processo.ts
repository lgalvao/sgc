import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS, URLS} from '../dados';
import {esperarMensagemSucesso, esperarUrl, verificarUrlDoPainel} from './verificacoes-basicas';

/**
 * Verifica se a página atual é a de edição de processo.
 * @param page A instância da página do Playwright.
 */
export async function verificarPaginaEdicaoProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/\/processo\/cadastro\?codProcesso=\d+$/);
}

/**
 * Verifica se a página atual é a de cadastro de processo.
 * @param page A instância da página do Playwright.
 */
export async function verificarPaginaCadastroProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/processo\/cadastro(\?codProcesso=\d+)?$/);
}

/**
 * Verifica se os campos obrigatórios do formulário de processo estão visíveis.
 * @param page A instância da página do Playwright.
 */
export async function verificarCamposObrigatoriosFormulario(page: Page): Promise<void> {
    await expect(page.getByLabel('Descrição')).toBeVisible();
    await expect(page.getByLabel('Tipo')).toBeVisible();
    await expect(page.getByText(TEXTOS.UNIDADES_PARTICIPANTES)).toBeVisible();
}

/**
 * Verifica se uma notificação de erro está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarNotificacaoErro(page: Page): Promise<void> {
    // Utiliza o seletor padrão de erro. Se houver múltiplos tipos de erro, o teste deve ser específico.
    // Para este helper genérico, assumimos o seletor mais comum.
    const notificacao = page.locator('[data-testid="notificacao-error"]');
    await expect(notificacao).toBeVisible();
}

/**
 * Aguarda que um processo apareça na tabela de processos.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function aguardarProcessoNoPainel(page: Page, descricaoProcesso: string): Promise<void> {
    await page.waitForURL(URLS.PAINEL);
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoProcesso)).toBeVisible();
}

/**
 * Verifica se um processo editado aparece na tabela e o original não.
 * @param page A instância da página do Playwright.
 * @param descricaoOriginal A descrição original do processo.
 * @param descricaoEditada A nova descrição do processo.
 */
export async function verificarProcessoEditado(page: Page, descricaoOriginal: string, descricaoEditada: string): Promise<void> {
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoEditada)).toBeVisible();
    await expect(page.getByText(descricaoOriginal)).not.toBeVisible();
}

/**
 * Verifica se o diálogo de confirmação de remoção está visível.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarDialogoConfirmacaoRemocao(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page.getByText(`Remover o processo '${descricaoProcesso}'? Esta ação não poderá ser desfeita.`)).toBeVisible();
}

/**
 * Verifica se um processo foi removido.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarProcessoRemovidoComSucesso(page: Page, descricaoProcesso: string): Promise<void> {
    const mensagemEsperada = `${TEXTOS.PROCESSO_REMOVIDO_INICIO}${descricaoProcesso}${TEXTOS.PROCESSO_REMOVIDO_FIM}`;

    // 1) Verificar notificação de sucesso específica
    await expect(page.locator(`.notification:has-text("${mensagemEsperada}")`)).toBeVisible();

    // 2) Retorno ao painel
    await expect(page).toHaveURL(URLS.PAINEL);

    // 3) Ausência na tabela
    const linhaProcesso = page.locator('[data-testid="tabela-processos"] tbody tr').filter({hasText: descricaoProcesso});
    await expect(linhaProcesso).not.toBeVisible();
}

/**
 * Verifica se o botão "Finalizar processo" está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoFinalizarProcessoVisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO})).toBeVisible();
}

/**
 * Verifica se o botão "Finalizar processo" não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoFinalizarProcessoInvisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO})).not.toBeVisible();
}

/**
 * Verifica se o modal de finalização de processo está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalFinalizacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await expect(modal.getByRole('heading', {name: TEXTOS.FINALIZACAO_PROCESSO})).toBeVisible();
    await expect(modal.getByText(TEXTOS.CONFIRMA_FINALIZACAO)).toBeVisible();
    await expect(modal.getByText(TEXTOS.CONFIRMACAO_VIGENCIA_MAPAS)).toBeVisible();
    await expect(modal.getByRole('button', {name: TEXTOS.CONFIRMAR})).toBeVisible();
    await expect(modal.getByRole('button', {name: TEXTOS.CANCELAR})).toBeVisible();
}

/**
 * Verifica se o modal de finalização de processo não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalFinalizacaoFechado(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).not.toBeVisible();
}

/**
 * Verifica se a notificação de bloqueio de finalização está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarFinalizacaoBloqueada(page: Page): Promise<void> {
    await expect(page.locator('.notification')).toContainText(TEXTOS.FINALIZACAO_BLOQUEADA);
}

/**
 * Verifica se um processo está com o status "Finalizado" no painel.
 * @param page A instância da página do Playwright.
 * @param nomeProcesso O nome do processo.
 */
export async function verificarProcessoFinalizadoNoPainel(page: Page, nomeProcesso: string): Promise<void> {
    const linhaProcesso = page.locator(`table[data-testid="${SELETORES.TABELA_PROCESSOS}"] tbody tr`).filter({hasText: nomeProcesso});
    await expect(linhaProcesso).toContainText(TEXTOS.FINALIZADO);
}

/**
 * Verifica se a mensagem de sucesso de finalização está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarMensagemFinalizacaoSucesso(page: Page): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.PROCESSO_FINALIZADO);
}

/**
 * Verifica se a notificação de mapas vigentes está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarMapasVigentesNotificacao(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.NOTIFICACAO_SUCESSO)).toContainText(TEXTOS.MAPAS_VIGENTES);
}

/**
 * Verifica se a notificação de e-mail enviado está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarEmailFinalizacaoEnviado(page: Page): Promise<void> {
    const notificacaoEmail = page.locator(SELETORES.NOTIFICACAO_EMAIL).first();
    await expect(notificacaoEmail).toBeVisible();
    await expect(notificacaoEmail).toContainText(TEXTOS.EMAIL_ENVIADO);
}

/**
 * Verifica se o cadastro foi devolvido.
 * @param page A instância da página do Playwright.
 */
export async function verificarCadastroDevolvidoComSucesso(page: Page): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.CADASTRO_DEVOLVIDO_AJUSTES);
    await verificarUrlDoPainel(page);
}

/**
 * Verifica se o aceite foi registrado.
 * @param page A instância da página do Playwright.
 */
export async function verificarAceiteRegistradoComSucesso(page: Page): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.ACEITE_REGISTRADO);
    await verificarUrlDoPainel(page);
}

/**
 * Verifica se o diálogo de confirmação de remoção não está visível.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarDialogoConfirmacaoFechado(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page.getByText(`Remover o processo '${descricaoProcesso}'? Esta ação não poderá ser desfeita.`)).not.toBeVisible();
}

/**
 * Verifica se um processo foi iniciado.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarProcessoIniciadoComSucesso(page: Page, descricaoProcesso: string): Promise<void> {
    await esperarMensagemSucesso(page, TEXTOS.PROCESSO_INICIADO);
    await expect(page).toHaveURL(URLS.PAINEL);
    const processoNaTabelaComStatus = page.locator('tr', {hasText: descricaoProcesso}).getByText(TEXTOS.EM_ANDAMENTO);
    await processoNaTabelaComStatus.waitFor({ state: 'visible' });
    await expect(processoNaTabelaComStatus).toBeVisible();
}

/**
 * Verifica se a página permanece no formulário de edição.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function verificarPermanenciaFormularioEdicao(page: Page, descricaoProcesso: string): Promise<void> {
    await expect(page).toHaveURL(/\/processo\/cadastro\?codProcesso=\d+$/);
    await expect(page.getByLabel('Descrição')).toHaveValue(descricaoProcesso);
}

/**
 * Verifica se o modal de confirmação de inicialização está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarConfirmacaoInicializacao(page: Page): Promise<void> {
    await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
}

/**
 * Verifica o conteúdo do modal de confirmação de inicialização.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param numUnidades O número de unidades.
 */
export async function verificarModalConfirmacaoInicializacao(page: Page, descricao: string, tipo: string, numUnidades: number): Promise<void> {
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await expect(modal.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
    await expect(modal.locator('p')).toContainText(`Descrição: ${descricao}`);
    await expect(modal.locator('p')).toContainText(`Tipo: ${tipo}`);
    await expect(modal.locator('p')).toContainText(`Unidades selecionadas: ${numUnidades}`);
}

/**
 * Verifica se um processo foi inicializado.
 * @param page A instância da página do Playwright.
 */
export async function verificarProcessoInicializadoComSucesso(page: Page): Promise<void> {
    await esperarUrl(page, URLS.PAINEL);
    await esperarMensagemSucesso(page, TEXTOS.PROCESSO_INICIADO);
}

/**
 * Verifica o valor do campo de descrição.
 * @param page A instância da página do Playwright.
 * @param valor O valor esperado.
 */
export async function verificarValorCampoDescricao(page: Page, valor: string): Promise<void> {
    await expect(page.locator(SELETORES.CAMPO_DESCRICAO)).toHaveValue(valor);
}

/**
 * Verifica se o botão "Iniciar processo" está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoIniciarProcessoVisivel(page: Page): Promise<void> {
    await expect(page.locator(`[data-testid="${SELETORES.BTN_INICIAR_PROCESSO}"]`)).toBeVisible();
}

/**
 * Verifica se o botão "Iniciar processo" não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoIniciarProcessoInvisivel(page: Page): Promise<void> {
    await expect(page.locator(`[data-testid="btn-iniciar-processo"]`)).not.toBeVisible();
}

/**
 * Verifica se o modal de confirmação de iniciar processo está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalConfirmacaoIniciarProcessoVisivel(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
    await expect(page.locator(SELETORES.TITULO_MODAL_INICIAR_PROCESSO)).toBeVisible();
    await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();
}

/**
 * Verifica se o modal de confirmação de iniciar processo não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalConfirmacaoIniciarProcessoInvisivel(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).not.toBeVisible();
}

/**
 * Verifica o conteúdo do modal de confirmação de iniciação.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalConfirmacaoIniciacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await expect(modal).toBeVisible();
    await expect(modal.locator('.modal-title')).toContainText(/iniciar processo/i);
    await expect(modal.locator('.modal-body')).toContainText(/não será mais possível editá-lo ou removê-lo/i);
    await expect(modal.locator('.modal-body')).toContainText(/todas as unidades participantes serão notificadas/i);
    await expect(modal.getByRole('button', {name: /confirmar/i})).toBeVisible();
    await expect(modal.getByRole('button', {name: /cancelar/i})).toBeVisible();
}

/**
 * Verifica se um processo está bloqueado para edição.
 * @param page A instância da página do Playwright.
 */
export async function verificarProcessoBloqueadoParaEdicao(page: Page): Promise<void> {
    // Verificar que botões de edição não estão disponíveis
    await expect(page.getByRole('button', {name: /^salvar$/i})).not.toBeVisible();
    await expect(page.getByRole('button', {name: /^remover$/i})).not.toBeVisible();
    await expect(page.getByRole('button', {name: /^iniciar processo$/i})).not.toBeVisible();
}

/**
 * Verifica se a página permanece na página de um processo.
 * @param page A instância da página do Playwright.
 * @param codProcesso O ID do processo.
 */
export async function verificarPermanenciaNaPaginaProcesso(page: Page, codProcesso: number): Promise<void> {
    await expect(page).toHaveURL(new RegExp(`/processo/${codProcesso}`));
}

/**
 * Aguarda que a tabela de processos seja carregada.
 * @param page A instância da página do Playwright.
 */
export async function aguardarTabelaProcessosCarregada(page: Page): Promise<void> {
    await page.waitForSelector(`${SELETORES.TABELA_PROCESSOS} tbody tr`);
}

/**
 * Verifica a quantidade de processos visíveis na tabela.
 * @param page A instância da página do Playwright.
 * @param quantidade Esperada.
 */
export async function verificarQuantidadeProcessosNaTabela(page: Page, quantidade: number): Promise<void> {
    const tabela = page.getByTestId('tabela-processos');
    const linhas = tabela.locator('tbody tr');
    await expect(linhas).toHaveCount(quantidade);
}

/**
 * Verifica se o botão Remover está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoRemoverVisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: /^Remover$/i})).toBeVisible();
}

/**
 * Verifica se o botão Remover não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoRemoverInvisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: /^Remover$/i})).not.toBeVisible();
}

/**
 * Verifica se o checkbox da unidade está marcado.
 * @param page A instância da página do Playwright.
 * @param sigla A sigla da unidade.
 */
export async function verificarCheckboxUnidadeMarcado(page: Page, sigla: string): Promise<void> {
     await expect(page.locator(`#chk-${sigla}`)).toBeChecked();
}

/**
 * Verifica se o valor do campo Data Limite está correto.
 * @param page A instância da página do Playwright.
 * @param data A data esperada.
 */
export async function verificarValorCampoDataLimite(page: Page, data: string): Promise<void> {
    await expect(page.locator(SELETORES.CAMPO_DATA_LIMITE)).toHaveValue(data);
}

/**
 * Verifica se o campo Tipo está visível e opcionalmente verifica seu valor.
 * @param page A instância da página do Playwright.
 */
export async function verificarCampoTipoVisivel(page: Page): Promise<void> {
    const selectTipo = page.locator(SELETORES.CAMPO_TIPO);
    await expect(selectTipo).toBeVisible();
}

/**
 * Verifica se o campo Tipo tem o valor esperado.
 * @param page A instância da página do Playwright.
 * @param valor O valor esperado.
 */
export async function verificarCampoTipoValor(page: Page, valor: string): Promise<void> {
    const selectTipo = page.locator(SELETORES.CAMPO_TIPO);
    await expect(selectTipo).toHaveValue(valor);
}

/**
 * Verifica se um processo não está visível no painel.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 */
export async function verificarProcessoNaoVisivel(page: Page, descricao: string): Promise<void> {
    await expect(page.getByText(descricao)).not.toBeVisible();
}

/**
 * Verifica se a página atual é a de detalhes do processo (visualização somente leitura).
 * @param page A instância da página do Playwright.
 */
export async function verificarPaginaDetalheProcesso(page: Page): Promise<void> {
    await expect(page).toHaveURL(/\/processo\/\d+$/);
}

/**
 * Verifica via API se os subprocessos foram criados e os retorna.
 * @param page A instância da página do Playwright.
 * @param processoId O ID do processo.
 * @returns A lista de subprocessos criados.
 */
export async function verificarCriacaoSubprocessos(page: Page, processoId: string): Promise<any[]> {
    const response = await page.request.get(`http://localhost:10000/api/processos/${processoId}/subprocessos`);
    if (!response.ok()) {
        console.error(`Erro ao buscar subprocessos: ${response.status()} ${response.statusText()}`);
        try { console.error(await response.text()); } catch(e) {}
    }
    expect(response.ok()).toBeTruthy();
    const subprocessos = await response.json();
    expect(subprocessos.length).toBeGreaterThan(0);
    return subprocessos;
}

/**
 * Verifica se os campos do processo estão desabilitados (somente leitura).
 * @param page A instância da página do Playwright.
 */
export async function verificarCamposProcessoDesabilitados(page: Page): Promise<void> {
    const campoDescricao = page.locator(SELETORES.CAMPO_DESCRICAO);
    // Apenas verifica se o campo existe antes de afirmar estado
    const count = await campoDescricao.count();
    if (count > 0) {
        const isDisabled = await campoDescricao.isDisabled();
        const isReadonly = await campoDescricao.getAttribute('readonly');
        expect(isDisabled || isReadonly !== null, 'Campo descrição deve estar desabilitado ou readonly').toBeTruthy();
    }
}

/**
 * Verifica a situação de um processo na tabela.
 * @param page A instância da página do Playwright.
 * @param nomeProcesso O nome do processo.
 * @param situacao A situação esperada.
 */
export async function verificarSituacaoProcesso(page: Page, nomeProcesso: string, situacao: string | RegExp): Promise<void> {
    const tabela = page.getByTestId('tabela-processos');
    const linha = tabela.locator('tr').filter({hasText: nomeProcesso});
    await expect(linha).toBeVisible();
    await expect(linha).toContainText(situacao);
}

/**
 * Verifica via API se um mapa de competências vazio foi criado para um subprocesso.
 * @param page A instância da página do Playwright.
 * @param subprocessoId O ID do subprocesso.
 */
export async function verificarMapaVazioCriadoParaSubprocesso(page: Page, subprocessoId: number): Promise<void> {
    const response = await page.request.get(`/api/subprocessos/${subprocessoId}/mapa`);
    expect(response.ok()).toBeTruthy();
    const mapa = await response.json();
    expect(mapa.competencias).toEqual([]);
}

/**
 * Verifica via API a movimentação inicial de um subprocesso.
 * @param page A instância da página do Playwright.
 * @param subprocessoId O ID do subprocesso.
 */
export async function verificarMovimentacaoInicialSubprocesso(page: Page, subprocessoId: number): Promise<void> {
    const perfilAdmin = 'ADMIN';
    const codUnidadeAdmin = 2; // Correção: A unidade STIC tem o código 2 (conforme e2e/helpers/utils/utils.ts)

    // Recuperar o token JWT do localStorage da página
    const jwtToken = await page.evaluate(() => localStorage.getItem('jwtToken'));

    const response = await page.request.get(
        `/api/subprocessos/${subprocessoId}?perfil=${perfilAdmin}&unidadeUsuario=${codUnidadeAdmin}`,
        {
            headers: {
                'Authorization': `Bearer ${jwtToken}`,
            },
        }
    );
    if (!response.ok()) {
        console.error(`Erro ao buscar movimentação inicial: ${response.status()} ${response.statusText()}`);
        try { console.error(await response.text()); } catch(e) {}
    }
    expect(response.ok()).toBeTruthy();
    const subprocesso = await response.json();
    expect(subprocesso.movimentacoes).toHaveLength(1);
    const movimentacao = subprocesso.movimentacoes[0];
    expect(movimentacao.descricao).toMatch(/Processo.*iniciado/);
    expect(movimentacao.unidadeOrigemSigla).toBeNull();
}

/**
 * Verifica via API o alerta de início de processo para uma unidade.
 * @param page A instância da página do Playwright.
 * @param unidadeSigla A sigla da unidade.
 * @param processoId O ID do processo.
 */
export async function verificarAlertaInicioProcesso(page: Page, unidadeSigla: string, processoId: number): Promise<void> {
    const unidadeResponse = await page.request.get(`/api/unidades/sigla/${unidadeSigla}`);
    expect(unidadeResponse.ok()).toBeTruthy();
    const unidade = await unidadeResponse.json();
    const unidadeId = unidade.codigo;

    const response = await page.request.get(`/api/painel/alertas?unidade=${unidadeId}`);
    expect(response.ok()).toBeTruthy();
    const alertas = await response.json();

    const alertaProcesso = alertas.content.find(alerta => alerta.codProcesso === processoId);
    expect(alertaProcesso).toBeDefined();
    expect(alertaProcesso.descricao).toContain('Início do processo');
}
