import {expect, Page} from '@playwright/test';
import {SELETORES, TEXTOS} from '../dados';
import {navegarParaCriacaoProcesso} from '~/helpers';
import {aguardarTabelaProcessosCarregada} from '../verificacoes/verificacoes-processo';

/**
 * Seleciona unidades na árvore de hierarquia usando suas siglas.
 * @param page A instância da página do Playwright.
 * @param siglas Um array de siglas das unidades a serem selecionadas (ex: ['STIC', 'SGP']).
 */
export async function selecionarUnidadesPorSigla(page: Page, siglas: string[]): Promise<void> {
    for (const sigla of siglas) {
        const seletorCheckbox = `#chk-${sigla}`;
        await page.locator(seletorCheckbox).check();
    }
}

/**
 * Seleciona uma unidade disponível na árvore de hierarquia.
 * @param page A instância da página do Playwright.
 * @param index O índice da unidade a ser selecionada (padrão: 0).
 * @returns O ID da unidade selecionada.
 */
export async function selecionarUnidadeDisponivel(page: Page, index: number = 0): Promise<string> {
    const disponiveis = page.locator('.form-check-input[type="checkbox"]:not(:disabled)');
    const total = await disponiveis.count();
    // Assertion explicita em vez de throw
    expect(total, 'Nenhuma unidade disponível para seleção').toBeGreaterThan(0);

    const idx = Math.min(index, total - 1);
    const alvo = disponiveis.nth(idx);
    const id = await alvo.getAttribute('id');
    await alvo.check();
    return id || '';
}

/**
 * Clica no botão Salvar.
 * @param page A instância da página do Playwright.
 */
export async function clicarBotaoSalvar(page: Page): Promise<void> {
    await page.getByRole('button', {name: /Salvar/i}).click();
}

/**
 * Clica no botão Remover.
 * @param page A instância da página do Playwright.
 */
export async function clicarBotaoRemover(page: Page): Promise<void> {
    await page.getByRole('button', {name: /^Remover$/i}).click();
}

/**
 * Seleciona o tipo de processo.
 * @param page A instância da página do Playwright.
 * @param tipo O tipo do processo.
 */
export async function selecionarTipoProcesso(page: Page, tipo: string): Promise<void> {
    await page.locator(SELETORES.CAMPO_TIPO).selectOption(tipo);
}

/**
 * Preenche o campo descrição.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição.
 */
export async function preencherDescricao(page: Page, descricao: string): Promise<void> {
    await page.locator(SELETORES.CAMPO_DESCRICAO).fill(descricao);
}

/**
 * Preenche o campo data limite.
 * @param page A instância da página do Playwright.
 * @param data A data limite.
 */
export async function preencherDataLimite(page: Page, data: string): Promise<void> {
    await page.locator(SELETORES.CAMPO_DATA_LIMITE).fill(data);
}

/**
 * Preenche o formulário de criação de processo.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param dataLimite A data limite do processo.
 * @param sticChecked `true` se a unidade STIC deve ser selecionada.
 */
export async function preencherFormularioProcesso(
    page: Page,
    descricao: string,
    tipo: string,
    dataLimite: string,
    sticChecked: boolean = false
): Promise<void> {
    await page.fill(SELETORES.CAMPO_DESCRICAO, descricao);
    await page.selectOption(SELETORES.CAMPO_TIPO, tipo);
    await page.fill(SELETORES.CAMPO_DATA_LIMITE, dataLimite);

    // Aguarda checkboxes explicitamente se o tipo exigir carregamento dinâmico
    if (tipo === 'REVISAO' || tipo === 'DIAGNOSTICO') {
        await page.waitForSelector('.form-check-input[type="checkbox"]:not([disabled])', { timeout: 30000 });
    }
    
    if (sticChecked) {
        await page.check(SELETORES.CHECKBOX_STIC);
    }
}

/**
 * Seleciona a primeira unidade na árvore de hierarquia.
 * @param page A instância da página do Playwright.
 */
export async function selecionarPrimeiraUnidade(page: Page): Promise<void> {
    await page.waitForSelector('input[type="checkbox"]');
    const primeiroCheckbox = page.locator('input[type="checkbox"]').first();
    await primeiroCheckbox.click();
}

/**
 * Clica no primeiro processo da tabela de processos.
 * @param page A instância da página do Playwright.
 */
export async function clicarPrimeiroProcessoTabela(page: Page): Promise<void> {
    const primeiraLinha = page.locator('table tbody tr').first();
    await primeiraLinha.click();
}

/**
 * Inicia o processo de mapeamento.
 * @param page A instância da página do Playwright.
 */
export async function iniciarProcessoMapeamento(page: Page): Promise<void> {
    await page.locator(SELETORES.BTN_INICIAR_PROCESSO).click();
}

/**
 * Confirma a iniciação do processo no modal.
 * @param page A instância da página do Playwright.
 */
export async function confirmarIniciacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await expect(modal).toBeVisible();

    const confirmButton = modal.locator(SELETORES.BTN_MODAL_CONFIRMAR);
    await expect(confirmButton).toBeVisible();
    await expect(confirmButton).toBeEnabled();
    await confirmButton.click({ force: true });
}

/**
 * Cancela a iniciação do processo no modal.
 * @param page A instância da página do Playwright.
 */
export async function cancelarIniciacaoProcesso(page: Page): Promise<void> {
    const modal = page.locator('.modal.show');
    await expect(modal).toBeVisible();
    await modal.locator(SELETORES.BTN_MODAL_CANCELAR).click();
}

/**
 * Cria um processo básico.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param siglas As siglas das unidades a serem associadas ao processo.
 * @param dataLimite A data limite do processo.
 * @returns O ID do processo criado.
 */
export async function criarProcessoBasico(
    page: Page,
    descricao: string,
    tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO',
    siglas: string[],
    dataLimite: string = '2025-12-31'
): Promise<number> {
    // Intercept the API response to extract the process ID
    const responsePromise = page.waitForResponse(response =>
        response.url().includes('/api/processos') &&
        !response.url().includes('/status-unidades') &&
        response.request().method() === 'POST' &&
        response.status() === 201
    );

    await navegarParaCriacaoProcesso(page);
    await preencherFormularioProcesso(page, descricao, tipo, dataLimite);
    await selecionarUnidadesPorSigla(page, siglas);

    const btnSalvar = page.getByRole('button', {name: /salvar/i});
    await btnSalvar.click();

    const response = await responsePromise;
    const data = await response.json();
    const processoId = data.codigo;

    await page.waitForURL(/\/painel/);

    return processoId;
}

/**
 * Cria e inicia um processo básico (helper de conveniência para testes que precisam do processo já rodando).
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param siglas As siglas das unidades a serem associadas ao processo.
 * @param dataLimite A data limite do processo.
 * @returns O ID do processo criado.
 */
export async function criarEIniciarProcessoBasico(
    page: Page,
    descricao: string,
    tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO',
    siglas: string[],
    dataLimite: string = '2025-12-31'
): Promise<number> {
    const processoId = await criarProcessoBasico(page, descricao, tipo, siglas, dataLimite);
    await abrirProcessoPorNome(page, descricao);
    await iniciarProcessoMapeamento(page);
    await confirmarIniciacaoProcesso(page);
    await page.waitForURL(/\/painel/);
    return processoId;
}


/**
 * Abre um processo pelo nome na tabela de processos.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo a ser aberto.
 */
export async function abrirProcessoPorNome(page: Page, descricao: string): Promise<void> {
    await aguardarTabelaProcessosCarregada(page);
    const row = page.locator(`${SELETORES.TABELA_PROCESSOS} tr`, {hasText: descricao});
    await row.first().click();

    // Aguardar navegação para página de cadastro
    await page.waitForURL(/\/processo\/cadastro\?codProcesso=\d+/);

    // Aguardar formulário carregar
    await page.waitForSelector(SELETORES.CAMPO_DESCRICAO);
}

/**
 * Cria um processo completo e retorna seus dados.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 * @param dataLimite A data limite do processo.
 * @param siglas Uma lista de Siglas de unidades a serem associadas ao processo.
 * @returns Um objeto com o código e a descrição do processo criado.
 */
export async function criarProcessoCompleto(
    page: Page,
    descricao: string,
    tipo: string,
    dataLimite: string,
    siglas: string[]
): Promise<{ processo: { codigo: number; descricao: string; } }> {
    const responsePromise = page.waitForResponse(response =>
        response.url().includes('/api/processos') &&
        !response.url().includes('/status-unidades') &&
        response.request().method() === 'POST' &&
        response.status() === 201
    );
    
    await navegarParaCriacaoProcesso(page);
    await preencherFormularioProcesso(page, descricao, tipo, dataLimite);
    await selecionarUnidadesPorSigla(page, siglas);
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
    
    const response = await responsePromise;
    const data = await response.json();
    const processoId = data.codigo;
    
    await page.waitForURL(/\/painel/);
    
    return { processo: { codigo: processoId, descricao } };
}

/**
 * Tenta salvar um processo com o formulário vazio.
 * @param page A instância da página do Playwright.
 */
export async function tentarSalvarProcessoVazio(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}

/**
 * Cria um processo sem associar nenhuma unidade.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param tipo O tipo do processo.
 */
export async function criarProcessoSemUnidades(page: Page, descricao: string, tipo: string): Promise<void> {
    // Usa uma data padrão se não fornecida, já que dataLimite agora é obrigatório em preencherFormularioProcesso
    await preencherFormularioProcesso(page, descricao, tipo, '2025-12-31');
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}

/**
 * Navega para a página de um processo a partir da tabela de processos.
 * @param page A instância da página do Playwright.
 * @param descricaoProcesso A descrição do processo.
 */
export async function navegarParaProcessoNaTabela(page: Page, descricaoProcesso: string): Promise<void> {
    await page.getByText(descricaoProcesso).click();
}

/**
 * Edita a descrição de um processo.
 * @param page A instância da página do Playwright.
 * @param novaDescricao A nova descrição do processo.
 */
export async function editarDescricaoProcesso(page: Page, novaDescricao: string): Promise<void> {
    await page.locator(SELETORES.CAMPO_DESCRICAO).fill(novaDescricao);
    await page.getByRole('button', {name: TEXTOS.SALVAR}).click();
}


/**
 * Cria um processo de mapeamento completo.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição do processo.
 * @param dataLimite A data limite do processo.
 */
export async function criarProcessoMapeamentoCompleto(page: Page, descricao: string, dataLimite: string): Promise<void> {
    await page.getByLabel('Descrição').fill(descricao);
    await page.getByLabel('Tipo').selectOption('Mapeamento');
    await page.getByLabel('Data limite').fill(dataLimite);

    await page.waitForSelector('input[type="checkbox"]');
    const primeiroCheckbox = page.locator('input[type="checkbox"]').first();
    await primeiroCheckbox.check();
}

/**
 * Clica no botão "Finalizar processo".
 * @param page A instância da página do Playwright.
 */
export async function clicarBotaoFinalizarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.FINALIZAR_PROCESSO}).click();
}

/**
 * Abre o modal de finalização do processo.
 * @param page A instância da página do Playwright.
 */
export async function abrirModalFinalizacaoProcesso(page: Page): Promise<void> {
    await clicarBotaoFinalizarProcesso(page);
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
}

/**
 * Clica no botão "Confirmar" no modal de finalização do processo.
 * @param page A instância da página do Playwright.
 */
export async function confirmarFinalizacaoNoModal(page: Page): Promise<void> {
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Finaliza um processo.
 * @param page A instância da página do Playwright.
 */
export async function finalizarProcesso(page: Page): Promise<void> {
    await abrirModalFinalizacaoProcesso(page);
    await confirmarFinalizacaoNoModal(page);
}

/**
 * Disponibiliza o cadastro de um processo.
 * @param page A instância da página do Playwright.
 */
export async function disponibilizarCadastro(page: Page): Promise<void> {
    await page.click(`button:has-text("${TEXTOS.DISPONIBILIZAR}")`);
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
    await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
}

/**
 * Abre o modal para devolver um cadastro para ajustes.
 * @param page A instância da página do Playwright.
 */
export async function abrirModalDevolucao(page: Page): Promise<void> {
    await page.getByRole('button', {name: 'Devolver para ajustes'}).click();
}

/**
 * Devolve um cadastro para ajustes.
 * @param page A instância da página do Playwright.
 * @param observacao A observação.
 */
export async function devolverParaAjustes(page: Page, observacao: string): Promise<void> {
    await abrirModalDevolucao(page);
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    await modal.getByLabel('Observação').fill(observacao);
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Devolve um cadastro.
 * @param page A instância da página do Playwright.
 * @param processo O processo.
 * @param nomeUnidade O nome da unidade.
 * @param observacao A observação.
 */
export async function devolverCadastro(page: Page, processo: {
    codigo: any;
    descricao: string
}, nomeUnidade: string, observacao: string): Promise<void> {
    const {loginComoGestor, navegarParaProcessoPorId} = await import("../navegacao");
    await loginComoGestor(page);
    await navegarParaProcessoPorId(page, processo.codigo);
    const {acessarAnaliseRevisaoComoGestor} = await import("../navegacao");
    await acessarAnaliseRevisaoComoGestor(page, processo.codigo, nomeUnidade);
    await devolverParaAjustes(page, observacao);
}

/**
 * Aceita o cadastro de um processo.
 * @param page A instância da página do Playwright.
 * @param observacao Uma observação opcional.
 */
export async function aceitarCadastro(page: Page, observacao: string = ''): Promise<void> {
    // "Registrar aceite" OR "Validar"
    await page.getByRole('button', {name: /Registrar aceite|Validar/}).click();

    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    if (observacao) {
        await modal.getByLabel('Observação').fill(observacao);
    }

    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Registra o aceite da revisão de um cadastro.
 * @param page A instância da página do Playwright.
 * @param observacao Uma observação opcional.
 */
export async function registrarAceiteRevisao(page: Page, observacao: string = ''): Promise<void> {
    const modal = page.locator(SELETORES.MODAL_VISIVEL);

    await page.getByRole('button', {name: TEXTOS.REGISTRAR_ACEITE}).click();
    await expect(modal).toBeVisible();

    if (observacao) {
        await modal.locator('textarea, input').first().fill(observacao);
    }

    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Homologa o cadastro de um processo.
 * @param page A instância da página do Playwright.
 * @param observacao Uma observação opcional.
 */
export async function homologarCadastro(page: Page, observacao: string = ''): Promise<void> {
    // "Homologar" OR "Validar"
    await page.getByRole('button', {name: /Homologar|Validar/}).click();

    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    if (observacao) {
        await modal.getByLabel(/observa/i).fill(observacao);
    }

    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Clica no botão "Iniciar processo".
 * @param page A instância da página do Playwright.
 */
export async function clicarBotaoIniciarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
}

/**
 * Clica em um processo na tabela de processos.
 * @param page A instância da página do Playwright.
 * @param nomeProcesso O nome do processo.
 */
export async function clicarProcessoNaTabela(page: Page, nomeProcesso: string): Promise<void> {
    await page.waitForSelector(SELETORES.TABELA_PROCESSOS);
    const processo = page.locator(`${SELETORES.TABELA_PROCESSOS} tbody tr`).filter({hasText: nomeProcesso});
    await processo.click();
}

/**
 * Inicia um processo.
 * @param page A instância da página do Playwright.
 */
export async function iniciarProcesso(page: Page): Promise<void> {
    await clicarBotaoIniciarProcesso(page);
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Remove um processo com confirmação.
 * @param page A instância da página do Playwright.
 */
export async function removerProcessoComConfirmacao(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Cancela a remoção de um processo.
 * @param page A instância da página do Playwright.
 */
export async function cancelarRemocaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    await page.getByRole('button', {name: TEXTOS.CANCELAR}).click();
}

/**
 * Confirma a inicialização de um processo.
 * @param page A instância da página do Playwright.
 */
export async function confirmarInicializacaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.INICIAR_PROCESSO}).click();
    await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}

/**
 * Remove um processo confirmando no modal.
 * @param page A instância da página do Playwright.
 */
export async function removerProcessoConfirmandoNoModal(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.REMOVER}).click();
    await page.locator('.modal.show .btn-danger').click();
}

/**
 * Clica em uma unidade na tabela de detalhes de um processo.
 * @param page A instância da página do Playwright.
 * @param nomeUnidade O nome da unidade.
 */
export async function clicarUnidadeNaTabelaDetalhes(page: Page, nomeUnidade: string): Promise<void> {
    const unidadeRow = page.locator(SELETORES.LINHA_TABELA_ARVORE).filter({hasText: nomeUnidade}).first();
    await unidadeRow.click();
}

/**
 * Clica no botão de histórico de análise.
 * @param page A instância da página do Playwright.
 */
export async function clicarBotaoHistoricoAnalise(page: Page): Promise<void> {
    await page.locator(SELETORES.BTN_HISTORICO_ANALISE).click();
}

/**
 * Homologa a revisão de um cadastro.
 * @param page A instância da página do Playwright.
 * @param observacao Uma observação opcional.
 */
export async function homologarRevisaoCadastro(page: Page, observacao: string = ''): Promise<void> {
    await homologarCadastro(page, observacao);
}
