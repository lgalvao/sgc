import {expect, type Page} from '@playwright/test';
import {verificarPaginaPainel} from './helpers-navegacao.js';

// Re-exportar para manter compatibilidade com imports existentes


/**
 * Helpers para análise de cadastro de atividades (CDU-13 e CDU-14)
 */

// Funções de Navegação

/**
 * Acessa subprocesso como GESTOR (via lista de unidades)
 */
export async function acessarSubprocessoGestor(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await expect(page).toHaveURL(/\/painel$/);
    const row = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso});
    await expect(row).toBeVisible({timeout: 15000});
    await row.click();

    await expect(page).toHaveURL(/\/processo\/\d+/);

    if (siglaUnidade) {
        const match = /\/processo\/(\d+)/.exec(page.url());
        if (match) {
            const processoId = match[1];
            const targetUrl = `/processo/${processoId}/${siglaUnidade}`;
            if (!page.url().endsWith(`/${siglaUnidade}`)) {
                await page.goto(targetUrl);
            }
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`));
            return;
        }
    }

    // Fallback original para casos sem sigla ou falha na extração
    const headingUnidades = page.getByRole('heading', {name: /Unidades participantes/i});
    if (await headingUnidades.isVisible().catch(() => false)) {
        const tabela = page.getByTestId('tbl-tree');
        const celulaUnidade = tabela.getByRole('cell', {name: new RegExp(String.raw`^\s*${siglaUnidade}\b`, 'i')}).first();
        await expect(celulaUnidade).toBeVisible();
        await celulaUnidade.click();
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`));
        return;
    } else if (/\/processo\/\d+$/.test(page.url())) {
        const primeiraLinha = page.locator('tbody tr').first();
        if (await primeiraLinha.isVisible().catch(() => false)) {
            await primeiraLinha.click();
        }
    }

    await expect(page).toHaveURL(/\/processo\/\d+(?:\/\w+)?$/);
}

/**
 * Acessa subprocesso como CHEFE (vai direto ao subprocesso)
 */
export async function acessarSubprocessoChefeDireto(page: Page, descricaoProcesso: string, siglaUnidade: string = '') {
    // Garantir que estamos no painel e que carregou
    await expect(page).toHaveURL(/\/painel$/);

    // Aguardar o processo aparecer na tabela antes de clicar
    const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descricaoProcesso)});
    await expect(linhaProcesso).toBeVisible();

    // Clicar na linha da tabela que contém o processo
    await linhaProcesso.click();

    // Aguardar navegação para uma página de processo
    await expect(page).toHaveURL(/\/processo\/\d+/);

    // Navegação robusta: extrair ID e navegar explicitamente se necessário
    if (siglaUnidade) {
        const match = /\/processo\/(\d+)/.exec(page.url());
        if (match) {
            const processoId = match[1];
            const targetUrl = `/processo/${processoId}/${siglaUnidade}`;
            // Se ainda não estiver na URL correta, forçar navegação
            if (!page.url().endsWith(`/${siglaUnidade}`)) {
                await page.goto(targetUrl);
            }
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`));
        }
    } else if (/\/processo\/\d+$/.test(page.url())) {
        const linhaUnidade = page.locator('tbody tr').first();
        if (await linhaUnidade.isVisible().catch(() => false)) {
            await linhaUnidade.click();
        }
        await expect(page).toHaveURL(/\/processo\/\d+(?:\/\w+)?$/);
    }
}

/**
 * Acessa subprocesso como ADMIN (via lista de unidades)
 */
export async function acessarSubprocessoAdmin(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    if (!await page.getByText(descricaoProcesso).first().isVisible().catch(() => false)) {
        await page.goto('/painel');
    }
    await expect(page.getByTestId('tbl-processos').getByText(descricaoProcesso).first()).toBeVisible();
    await page.getByTestId('tbl-processos').getByText(descricaoProcesso).first().click();

    const headingUnidades = page.getByRole('heading', {name: /Unidades participantes/i});
    await expect(headingUnidades).toBeVisible();

    // Navegação robusta: extrair ID e navegar explicitamente
    if (siglaUnidade) {
        const match = /\/processo\/(\d+)/.exec(page.url());
        if (match) {
            const processoId = match[1];
            const targetUrl = `/processo/${processoId}/${siglaUnidade}`;
            await page.goto(targetUrl);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`));
        } else {
            // Fallback se não conseguir extrair ID (improvável)
            const row = page.locator('tr').filter({hasText: new RegExp(String.raw`^\s*${siglaUnidade}(?:\s+-\s+|\b)`, 'i')}).first();
            await expect(row).toBeVisible();
            await row.click({force: true});
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`));
        }
    } else {
        await expect(page).toHaveURL(/\/processo\/\d+(?:\/\w+)?$/);
    }
}

// Funções de Histórico de Análise

/**
 * Abre modal de histórico de análise (tela de edição - CadAtividades)
 */
export async function abrirHistoricoAnalise(page: Page) {
    // Dropdown "Mais ações" deve ser aberto primeiro
    await page.getByTestId('btn-mais-acoes').click();
    // Aguardar o item do menu estar visível
    const itemHistorico = page.getByTestId('btn-cad-atividades-historico');
    await expect(itemHistorico).toBeVisible();
    await itemHistorico.click();

    const modal = page.locator('.modal-content').filter({hasText: 'Histórico de Análise'});
    await expect(modal).toBeVisible();
    return modal;
}

/**
 * Abre modal de histórico de análise (tela de visualização - VisAtividades)
 */
export async function abrirHistoricoAnaliseVisualizacao(page: Page) {
    await page.getByTestId('btn-vis-atividades-historico').click();
    const modal = page.locator('.modal-content').filter({hasText: 'Histórico de Análise'});
    await expect(modal).toBeVisible();
    return modal;
}

/**
 * Fecha modal de histórico de análise
 */
export async function fecharHistoricoAnalise(page: Page) {
    await page.getByRole('button', {name: 'Fechar'}).click({force: true});
    await expect(page.locator('.modal-content').filter({hasText: 'Histórico de Análise'})).toBeHidden();
}

// Funções de Devolução

/**
 * Função genérica para devolução de cadastro/revisão
 */
async function realizarDevolucao(page: Page, mensagemSucesso: string | RegExp, observacao: string = '') {
    await page.getByTestId('btn-acao-devolver').click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a devolução.*para ajustes/i)).toBeVisible();

    if (observacao) {
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(observacao);
    }

    await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
    await expect(page.getByText(mensagemSucesso).first()).toBeVisible();
    await verificarPaginaPainel(page);
}

/**
 * Devolve cadastro de mapeamento para ajustes (CDU-13)
 */
export async function devolverCadastroMapeamento(page: Page, observacao: string = '') {
    await realizarDevolucao(page, /Cadastro devolvido/i, observacao);
}

/**
 * Devolve revisão para ajustes (CDU-14)
 */
export async function devolverRevisao(page: Page, observacao: string = '') {
    await realizarDevolucao(page, /Revisão devolvida/i, observacao);
}

/**
 * Cancela devolução de cadastro
 */
export async function cancelarDevolucao(page: Page) {
    await page.getByTestId('btn-acao-devolver').click();

    // Verificar modal de devolução
    await expect(page.getByRole('dialog')).toBeVisible();

    await page.getByRole('button', {name: 'Cancelar'}).click();

    // Verificar que modal fechou
    await expect(page.getByRole('dialog')).toBeHidden();
}

// Funções de Aceite (GESTOR)

/**
 * Função genérica para aceite de cadastro/revisão (GESTOR)
 */
async function realizarAceite(page: Page, mensagemSucesso: string | RegExp, observacao: string = '') {
    await page.getByTestId('btn-acao-analisar-principal').click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma o aceite/i)).toBeVisible();

    const obsToSend = observacao || 'Aceite sem ressalvas';
    await page.getByTestId('inp-aceite-cadastro-obs').fill(obsToSend);

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(mensagemSucesso).first()).toBeVisible();
    await verificarPaginaPainel(page);
}

/**
 * Aceita cadastro de mapeamento (GESTOR - CDU-13)
 */
export async function aceitarCadastroMapeamento(page: Page, observacao: string = '') {
    await realizarAceite(page, /Cadastro aceito/i, observacao);
}

/**
 * Aceita revisão (GESTOR - CDU-14)
 */
export async function aceitarRevisao(page: Page, observacao: string = '') {
    await realizarAceite(page, /Revisão aceita/i, observacao);
}

// Funções de Homologação (ADMIN)

/**
 * Homologa cadastro (ADMIN) - Mapeamento
 */
export async function homologarCadastroMapeamento(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();

    // Modal: "Homologação do cadastro de atividades e conhecimentos"
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a homologação do cadastro de atividades e conhecimentos/i)).toBeVisible();

    await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado sem ressalvas');

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Cadastro homologado/i).first()).toBeVisible();

    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
}

export async function homologarCadastroRevisaoComImpacto(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();

    // Aguardar modal aparecer
    await expect(page.getByRole('dialog')).toBeVisible();

    await expect(page.getByText(/Confirma a homologação do cadastro de atividades e conhecimentos/i)).toBeVisible();

    await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado sem ressalvas');

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Revisão d[oe] cadastro homologada/i).first()).toBeVisible();

    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

    // Verificar situação após homologação
    await expect(page.getByTestId('subprocesso-header__txt-situacao'))
        .toHaveText(/Revisão d[oe] cadastro homologada/i);
}

/**
 * Cancela homologação
 */
export async function cancelarHomologacao(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();

    // Verificar modal de homologação
    await expect(page.getByRole('dialog')).toBeVisible();

    await page.getByRole('button', {name: 'Cancelar'}).click();

    // Verificar que modal fechou
    await expect(page.getByRole('dialog')).toBeHidden();
}

export {fazerLogout, verificarPaginaPainel} from './helpers-navegacao.js';
