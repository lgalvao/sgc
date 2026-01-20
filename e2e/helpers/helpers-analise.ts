import {expect, type Page} from '@playwright/test';
import {fazerLogout, verificarPaginaPainel} from './helpers-navegacao';

// Re-exportar para manter compatibilidade com imports existentes
export {fazerLogout, verificarPaginaPainel};

/**
 * Helpers para análise de cadastro de atividades (CDU-13 e CDU-14)
 */

// ============================================================================
// Funções de Navegação
// ============================================================================

/**
 * Acessa subprocesso como GESTOR (via lista de unidades)
 */
export async function acessarSubprocessoGestor(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    // Garantir que estamos no painel e que carregou
    await expect(page).toHaveURL(/\/painel$/);

    // Aguardar o processo aparecer na tabela antes de clicar
    await expect(page.getByText(descricaoProcesso)).toBeVisible();
    
    await page.getByText(descricaoProcesso).click();

    // GESTOR sempre vê lista de unidades participantes
    await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

    // Clicar na linha da unidade para acessar o subprocesso
    await page.getByRole('row', {name: new RegExp(siglaUnidade, 'i')}).click();

    // Aguardar navegação para a página do subprocesso
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/\w+$`));
}

/**
 * Acessa subprocesso como CHEFE (vai direto ao subprocesso)
 */
export async function acessarSubprocessoChefeDireto(page: Page, descricaoProcesso: string, siglaUnidade: string = '') {
    // Garantir que estamos no painel e que carregou
    await expect(page).toHaveURL(/\/painel$/);
    
    // Aguardar o processo aparecer na tabela antes de clicar
    const linhaProcesso = page.locator('tr', {has: page.getByText(descricaoProcesso)});
    await expect(linhaProcesso).toBeVisible();
    
    // Clicar na linha da tabela que contém o processo
    await linhaProcesso.click();
    
    // Aguardar navegação para uma página de processo
    await expect(page).toHaveURL(/\/processo\/\d+/);

    // Se não redirecionou direto para o subprocesso (a URL não termina com a sigla da unidade)
    if (siglaUnidade && !page.url().endsWith(siglaUnidade)) {
        // Clicar na linha da unidade para acessar o subprocesso
        await page.getByRole('row', {name: new RegExp(siglaUnidade, 'i')}).click();
    }
    
    // Agora deve estar na página do subprocesso
    if (siglaUnidade) {
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${siglaUnidade}$`));
    } else {
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/\w+$`));
    }
}

/**
 * Acessa subprocesso como CHEFE (selecionando unidade na lista)
 */
export async function acessarSubprocessoChefeComSelecao(page: Page, descricaoProcesso: string, siglaUnidade: string = 'SECAO_221') {
    await page.getByText(descricaoProcesso).click();
    await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    await page.getByRole('row', {name: new RegExp(siglaUnidade, 'i')}).click();
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/\w+$`));
}

/**
 * Acessa subprocesso como ADMIN (via lista de unidades)
 */
export async function acessarSubprocessoAdmin(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await page.getByText(descricaoProcesso).click();

    // ADMIN sempre vê lista de unidades participantes
    await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    await page.getByRole('row', {name: new RegExp(siglaUnidade, 'i')}).click();

    // Aguardar navegação para a página do subprocesso
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/\w+$`));
}

// ============================================================================
// Funções de Histórico de Análise
// ============================================================================

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
    await page.getByRole('button', {name: 'Fechar'}).click();
    await expect(page.locator('.modal-content').filter({hasText: 'Histórico de Análise'})).toBeHidden();
}

// ============================================================================
// Funções de Devolução
// ============================================================================

/**
 * Função genérica para devolução de cadastro/revisão
 */
async function realizarDevolucao(page: Page, observacao: string = '', mensagemSucesso: string | RegExp) {
    await page.getByTestId('btn-acao-devolver').click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a devolução.*para ajustes/i)).toBeVisible();

    if (observacao) {
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(observacao);
    }

    await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
    await expect(page.getByRole('heading', {name: mensagemSucesso})).toBeVisible();
    await verificarPaginaPainel(page);
}

/**
 * Devolve cadastro de mapeamento para ajustes (CDU-13)
 */
export async function devolverCadastroMapeamento(page: Page, observacao: string = '') {
    await realizarDevolucao(page, observacao, /Cadastro devolvido/i);
}

/**
 * Devolve revisão para ajustes (CDU-14)
 */
export async function devolverRevisao(page: Page, observacao: string = '') {
    await realizarDevolucao(page, observacao, /Revisão devolvida/i);
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

// ============================================================================
// Funções de Aceite (GESTOR)
// ============================================================================

/**
 * Função genérica para aceite de cadastro/revisão (GESTOR)
 */
async function realizarAceite(page: Page, observacao: string = '', mensagemSucesso: string | RegExp) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma o aceite/i)).toBeVisible();

    const obsToSend = observacao || 'Aceite sem ressalvas';
    await page.getByTestId('inp-aceite-cadastro-obs').fill(obsToSend);

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByRole('heading', {name: mensagemSucesso})).toBeVisible();
    await verificarPaginaPainel(page);
}

/**
 * Aceita cadastro de mapeamento (GESTOR - CDU-13)
 */
export async function aceitarCadastroMapeamento(page: Page, observacao: string = '') {
    await realizarAceite(page, observacao, /Cadastro aceito/i);
}

/**
 * Aceita revisão (GESTOR - CDU-14)
 */
export async function aceitarRevisao(page: Page, observacao: string = '') {
    await realizarAceite(page, observacao, /Revisão aceita/i);
}

// ============================================================================
// Funções de Homologação (ADMIN)
// ============================================================================

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

    // Sistema redireciona para Detalhes do subprocesso após homologação (CDU-13 passo 11.7)
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
}

/**
 * Homologa revisão (ADMIN) - Detecta automaticamente se há impactos ou não
 */
export async function homologarCadastroRevisaoSemImpacto(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();

    // Aguardar modal aparecer
    await expect(page.getByRole('dialog')).toBeVisible();

    // Caminho SEM impactos (CDU-14 passo 12.2)
    await expect(page.getByText(/A revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade/i)).toBeVisible();
    await expect(page.getByText(/Confirma a manutenção do mapa de competências vigente/i)).toBeVisible();

    await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado sem ressalvas');

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Revisão homologada/i).first()).toBeVisible();

    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

    // Verificar situação após homologação
    await expect(page.getByTestId('subprocesso-header__txt-situacao'))
        .toHaveText(/Mapa homologado/i);
}

export async function homologarCadastroRevisaoComImpacto(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();

    // Aguardar modal aparecer
    await expect(page.getByRole('dialog')).toBeVisible();

    // Caminho COM impactos (CDU-14 passo 12.3)
    await expect(page.getByText(/Confirma a homologação do cadastro de atividades e conhecimentos/i)).toBeVisible();

    await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado sem ressalvas');

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Revisão homologada/i).first()).toBeVisible();

    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

    // Verificar situação após homologação
    await expect(page.getByTestId('subprocesso-header__txt-situacao'))
        .toHaveText(/Revisão de Cadastro Homologada/i);
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
