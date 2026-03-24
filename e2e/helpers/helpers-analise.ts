import {expect, type Page} from '@playwright/test';
import {limparNotificacoes, navegarParaSubprocesso, verificarPaginaPainel} from './helpers-navegacao.js';
import {TEXTOS} from '../../frontend/src/constants/textos.js';

/**
 * Acessa subprocesso como GESTOR (via lista de unidades)
 */
export async function acessarSubprocessoGestor(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await expect(page).toHaveURL(/\/painel$/);

    const row = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso});
    await expect(row).toBeVisible();
    await row.click();

    await navegarParaSubprocesso(page, siglaUnidade);
}

/**
 * Acessa subprocesso como CHEFE (vai direto ao subprocesso)
 */
export async function acessarSubprocessoChefeDireto(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await expect(page).toHaveURL(/\/painel$/);

    const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descricaoProcesso)});
    await expect(linhaProcesso).toBeVisible();
    await linhaProcesso.click();

    await navegarParaSubprocesso(page, siglaUnidade);
}

/**
 * Acessa subprocesso como ADMIN (via lista de unidades)
 */
export async function acessarSubprocessoAdmin(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await expect(page).toHaveURL(/\/painel$/);

    await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();
    await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();

    await expect(page.getByRole('heading', {name: TEXTOS.subprocesso.DETALHE_UNIDADES_TITULO})).toBeVisible();

    await navegarParaSubprocesso(page, siglaUnidade);
}

/**
 * Abre modal de histórico de análise (tela de edição - CadAtividades)
 */
export async function abrirHistoricoAnalise(page: Page) {
    const itemHistorico = page.getByTestId('btn-cad-atividades-historico');
    await expect(itemHistorico).toBeVisible();
    await itemHistorico.click();

    const modal = page.getByTestId('mdl-historico-analise');
    await expect(modal).toBeVisible();
    return modal;
}

/**
 * Abre modal de histórico de análise (tela de visualização - VisAtividades)
 */
export async function abrirHistoricoAnaliseVisualizacao(page: Page) {
    await page.getByTestId('btn-vis-atividades-historico').click();
    const modal = page.getByTestId('mdl-historico-analise');
    await expect(modal).toBeVisible();
    return modal;
}

export async function verificarAcoesAnaliseCadastroVisualizacao(page: Page, options: {
    rotuloPrincipal: string | RegExp;
    principalHabilitado: boolean;
    devolverHabilitado: boolean;
}) {
    const botaoHistorico = page.getByTestId('btn-vis-atividades-historico');
    const botaoDevolver = page.getByTestId('btn-acao-devolver');
    const botaoPrincipal = page.getByTestId('btn-acao-analisar-principal');

    await expect(botaoHistorico).toBeVisible();
    await expect(botaoDevolver).toBeVisible();
    await expect(botaoPrincipal).toBeVisible();
    await expect(botaoPrincipal).toHaveText(options.rotuloPrincipal);

    if (options.devolverHabilitado) {
        await expect(botaoDevolver).toBeEnabled();
    } else {
        await expect(botaoDevolver).toBeDisabled();
    }

    if (options.principalHabilitado) {
        await expect(botaoPrincipal).toBeEnabled();
    } else {
        await expect(botaoPrincipal).toBeDisabled();
    }
}

/**
 * Fecha modal de histórico de análise
 */
export async function fecharHistoricoAnalise(page: Page) {
    const modal = page.getByTestId('mdl-historico-analise');
    const btnFechar = modal.getByTestId('btn-modal-fechar');
    await expect(btnFechar).toBeVisible();
    await btnFechar.click();
    await expect(modal).toBeHidden();
}

// Funções de Devolução

/**
 * Função genérica para devolução de cadastro/revisão
 */
async function realizarDevolucao(page: Page, observacao: string = '') {
    await limparNotificacoes(page);
    const btnDevolver = page.getByTestId('btn-acao-devolver');
    await expect(btnDevolver).toBeEnabled();
    await btnDevolver.click();
    const modal = page.locator('.modal.show');
    await expect(modal).toBeVisible();

    if (observacao) {
        await modal.getByTestId('inp-devolucao-cadastro-obs').fill(observacao);
    }

    await modal.getByTestId('btn-devolucao-cadastro-confirmar').click();
    await verificarPaginaPainel(page);
}

/**
 * Devolve cadastro de mapeamento para ajustes (CDU-13)
 */
export async function devolverCadastroMapeamento(page: Page, observacao: string = '') {
    await realizarDevolucao(page, observacao);
}

/**
 * Devolve revisão para ajustes (CDU-14)
 */
export async function devolverRevisao(page: Page, observacao: string = '') {
    await realizarDevolucao(page, observacao);
}

/**
 * Cancela devolução de cadastro
 */
export async function cancelarDevolucao(page: Page) {
    const btnDevolver = page.getByTestId('btn-acao-devolver');
    await expect(btnDevolver).toBeEnabled();
    await btnDevolver.click();

    // Verificar modal de devolução
    const modal = page.locator('.modal.show');
    await expect(modal).toBeVisible();

    await modal.getByTestId('btn-modal-confirmacao-cancelar').click();

    // Verificar que modal fechou
    await expect(page.getByRole('dialog')).toBeHidden();
}

// Funções de Aceite (GESTOR)

/**
 * Função genérica para aceite de cadastro/revisão (GESTOR)
 */
async function realizarAceite(page: Page, observacao: string = '') {
    await limparNotificacoes(page);
    const btnAceitar = page.getByTestId('btn-acao-analisar-principal');
    await expect(btnAceitar).toBeEnabled();
    await btnAceitar.click();
    const modal = page.locator('.modal.show');
    await expect(modal).toBeVisible();
    await expect(modal.getByText(/Confirma o aceite/i)).toBeVisible();

    const obsToSend = observacao || 'Aceite sem ressalvas';
    await modal.getByTestId('inp-aceite-cadastro-obs').fill(obsToSend);

    await modal.getByTestId('btn-aceite-cadastro-confirmar').click();
    await verificarPaginaPainel(page);
}

/**
 * Aceita cadastro de mapeamento (GESTOR - CDU-13)
 */
export async function aceitarCadastroMapeamento(page: Page, observacao: string = '') {
    await realizarAceite(page, observacao);
}

/**
 * Aceita revisão (GESTOR - CDU-14)
 */
export async function aceitarRevisao(page: Page, observacao: string = '') {
    await realizarAceite(page, observacao);
}

/**
 * Homologa cadastro (ADMIN) - Mapeamento
 */
export async function homologarCadastroMapeamento(page: Page) {
    const btnHomologar = page.getByTestId('btn-acao-analisar-principal');
    await expect(btnHomologar).toBeEnabled();
    await btnHomologar.click();

    // Modal: "Homologação do cadastro"
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(TEXTOS.atividades.MODAL_HOMOLOGAR_TEXTO)).toBeVisible();

    await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado sem ressalvas');

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Cadastro homologado/i).first()).toBeVisible();

    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
}
export {fazerLogout, verificarPaginaPainel} from './helpers-navegacao.js';
