import { expect, type Page } from '@playwright/test';

// Função para verificar página do painel
export async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

// Função para verificar página do subprocesso
export async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}$`));
    await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toBeVisible();
}

// Função para acessar subprocesso como GESTOR (via lista de unidades)
export async function acessarSubprocessoGestor(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await page.getByText(descricaoProcesso).click();

    // GESTOR sempre vê lista de unidades participantes
    await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    await page.getByRole('row', {name: new RegExp(siglaUnidade, 'i')}).click();
}

// Função para acessar subprocesso como CHEFE (vai direto)
export async function acessarSubprocessoChefe(page: Page, descricaoProcesso: string) {
    await page.getByText(descricaoProcesso).click();
    // Se cair na lista de unidades (caso multiplas), clica na unidade
    if (await page.getByRole('heading', {name: /Unidades participantes/i}).isVisible()) {
        await page.getByRole('row', {name: /Seção 221/i}).click();
    }
}

// Função para acessar subprocesso como ADMIN (via lista de unidades)
export async function acessarSubprocessoAdmin(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await page.getByText(descricaoProcesso).click();

    // ADMIN sempre vê lista de unidades participantes
    await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    await page.getByRole('row', {name: new RegExp(siglaUnidade, 'i')}).click();
}

// Função para abrir modal de histórico de análise
export async function abrirHistoricoAnalise(page: Page) {
    await page.getByTestId('btn-cad-atividades-historico').click();
    const modal = page.locator('.modal-content').filter({ hasText: 'Histórico de Análise' });
    await expect(modal).toBeVisible();
    return modal;
}

// Função para fechar modal de histórico
export async function fecharHistoricoAnalise(page: Page) {
    await page.getByRole('button', {name: 'Fechar'}).click();
    await expect(page.locator('.modal-content').filter({ hasText: 'Histórico de Análise' })).toBeHidden();
}

// Função para devolver cadastro
export async function devolverCadastro(page: Page, observacao?: string) {
    await page.getByTestId('btn-acao-devolver').click();

    if (observacao) {
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(observacao);
    }

    await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
    await expect(page.getByText(/Devolução realizada/i)).toBeVisible();
    await verificarPaginaPainel(page);
}

// Função para aceitar cadastro (GESTOR)
export async function aceitarCadastro(page: Page, observacao?: string) {
    await page.getByTestId('btn-acao-analisar-principal').click();

    if (observacao) {
        await page.getByTestId('inp-aceite-cadastro-obs').fill(observacao);
    }

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Aceite registrado/i)).toBeVisible();
    await verificarPaginaPainel(page);
}

// Função para homologar cadastro (ADMIN) - Mapeamento
export async function homologarCadastroMapeamento(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();

    // Modal: "Homologação do cadastro de atividades e conhecimentos"
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a homologação do cadastro de atividades e conhecimentos/i)).toBeVisible();

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();

    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
}

// Função para homologar revisão (ADMIN) - COM impactos
export async function homologarRevisaoComImpactos(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();

    // Modal: "Homologação do cadastro de atividades e conhecimentos"
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a homologação do cadastro de atividades e conhecimentos/i)).toBeVisible();

    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();

    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
}

// Função para homologar revisão (ADMIN) - SEM impactos
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
}
