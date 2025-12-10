import {expect, type Page} from '@playwright/test';

/**
 * Helpers para análise de cadastro de atividades (CDU-13 e CDU-14)
 */

// ============================================================================
// Funções de Navegação
// ============================================================================

/**
 * Faz logout do sistema
 */
export async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

/**
 * Verifica que está na página do painel
 */
export async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

/**
 * Verifica que está na página de detalhes do subprocesso
 */
export async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}$`));
}

/**
 * Acessa subprocesso como GESTOR (via lista de unidades)
 */
export async function acessarSubprocessoGestor(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await page.getByText(descricaoProcesso).click();
    
    // GESTOR sempre vê lista de unidades participantes
    await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    
    // Clicar na linha da unidade para acessar o subprocesso
    await page.getByRole('row', {name: new RegExp(siglaUnidade, 'i')}).click();
    
    // Aguardar navegação para a página do subprocesso
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/\w+$`));
}

/**
 * Acessa subprocesso como CHEFE (vai direto ou via lista)
 */
export async function acessarSubprocessoChefe(page: Page, descricaoProcesso: string) {
    await page.getByText(descricaoProcesso).click();
    
    // Se cair na lista de unidades (caso múltiplas), clica na unidade
    if (await page.getByRole('heading', {name: /Unidades participantes/i}).isVisible()) {
        await page.getByRole('row', {name: /Seção 221/i}).click();
    }
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
    await page.getByTestId('btn-cad-atividades-historico').click();
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
 * Devolve cadastro para ajustes
 */
export async function devolverCadastro(page: Page, observacao?: string) {
    await page.getByTestId('btn-acao-devolver').click();
    
    // Verificar modal de devolução
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a devolução.*para ajustes/i)).toBeVisible();
    
    if (observacao) {
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(observacao);
    }
    
    await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
    await expect(page.getByText(/Cadastro devolvido/i)).toBeVisible();
    await verificarPaginaPainel(page);
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
 * Aceita cadastro (GESTOR)
 */
export async function aceitarCadastro(page: Page, observacao?: string) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    
    // Verificar modal de aceite
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma o aceite/i)).toBeVisible();
    
    if (observacao) {
        await page.getByTestId('inp-aceite-cadastro-obs').fill(observacao);
    }
    
    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Cadastro aceito/i)).toBeVisible();
    await verificarPaginaPainel(page);
}

/**
 * Cancela aceite de cadastro
 */
export async function cancelarAceite(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    
    // Verificar modal de aceite
    await expect(page.getByRole('dialog')).toBeVisible();
    
    await page.getByRole('button', {name: 'Cancelar'}).click();
    
    // Verificar que modal fechou
    await expect(page.getByRole('dialog')).toBeHidden();
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
    
    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();
    
    // Sistema redireciona para Detalhes do subprocesso após homologação (CDU-13 passo 11.7)
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
}

/**
 * Homologa revisão (ADMIN) - COM impactos
 */
export async function homologarRevisaoComImpactos(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    
    // Modal: "Homologação do cadastro de atividades e conhecimentos"
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a homologação do cadastro de atividades e conhecimentos/i)).toBeVisible();
    
    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();
    
    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
    
    // Verificar situação após homologação
    await expect(page.getByTestId('subprocesso-header__txt-badge-situacao'))
        .toHaveText(/Revisão de Cadastro Homologada/i);
}

/**
 * Homologa revisão (ADMIN) - SEM impactos
 */
export async function homologarRevisaoSemImpactos(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    
    // Modal: "Homologação do mapa de competências"
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/A revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade/i)).toBeVisible();
    await expect(page.getByText(/Confirma a manutenção do mapa de competências vigente/i)).toBeVisible();
    
    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();
    
    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
    
    // Verificar situação após homologação
    await expect(page.getByTestId('subprocesso-header__txt-badge-situacao'))
        .toHaveText(/Mapa homologado/i);
}

/**
 * Homologa revisão (ADMIN) - Detecta automaticamente se há impactos ou não
 */
export async function homologarCadastroRevisao(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    
    // Aguardar modal aparecer
    await expect(page.getByRole('dialog')).toBeVisible();
    
    // Detectar qual tipo de modal foi aberto
    const temMensagemSemImpactos = await page.getByText(/A revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade/i).isVisible();
    
    if (temMensagemSemImpactos) {
        // Caminho SEM impactos (CDU-14 passo 12.2)
        await expect(page.getByText(/Confirma a manutenção do mapa de competências vigente/i)).toBeVisible();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();
        
        // Verifica redirecionamento para tela de detalhes do subprocesso
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
        
        // Verificar situação após homologação
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao'))
            .toHaveText(/Mapa homologado/i);
    } else {
        // Caminho COM impactos (CDU-14 passo 12.3)
        await expect(page.getByText(/Confirma a homologação do cadastro de atividades e conhecimentos/i)).toBeVisible();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();
        
        // Verifica redirecionamento para tela de detalhes do subprocesso
        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
        
        // Verificar situação após homologação
        await expect(page.getByTestId('subprocesso-header__txt-badge-situacao'))
            .toHaveText(/Revisão de Cadastro Homologada/i);
    }
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
