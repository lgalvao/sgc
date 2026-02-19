import {expect, type Page} from '@playwright/test';
import {calcularDataLimite} from './helpers-processos.js';

async function garantirContextoSubprocesso(page: Page) {
    const cardMapa = page.getByTestId('card-subprocesso-mapa-edicao').or(page.getByTestId('card-subprocesso-mapa-visualizacao'));
    if (await cardMapa.first().isVisible()) {
        return;
    }

    if (/\/processo\/\d+$/.test(page.url())) {
        const linhaUnidade = page.getByRole('row')
            .filter({has: page.getByRole('cell')})
            .first();
        if (await linhaUnidade.isVisible().catch(() => false)) {
            await linhaUnidade.click();
        }
    }
}

export async function navegarParaMapa(page: Page) {
    if (/\/vis-mapa$/.test(page.url())) {
        await expect(page.getByRole('heading', {name: /Mapa de competências/i})).toBeVisible();
        return;
    }

    await garantirContextoSubprocesso(page);
    const cardEdicao = page.getByTestId('card-subprocesso-mapa-edicao');
    const cardVisualizacao = page.getByTestId('card-subprocesso-mapa-visualizacao');
    const cardAlvo = (await cardEdicao.isVisible()) ? cardEdicao : cardVisualizacao;

    await expect(cardAlvo).toBeVisible();
    await cardAlvo.click();
    await expect(page.getByRole('heading', {name: /Mapa de competências/i})).toBeVisible();
}

export async function abrirModalCriarCompetencia(page: Page) {
    const btnEmpty = page.getByTestId('btn-abrir-criar-competencia-empty');
    const btnNormal = page.getByTestId('btn-abrir-criar-competencia');

    // Wait for either button to be visible
    await expect(btnEmpty.or(btnNormal)).toBeVisible();

    if (await btnEmpty.isVisible()) {
        await btnEmpty.click();
    } else {
        await btnNormal.click();
    }
    await expect(page.getByTestId('mdl-criar-competencia')).toBeVisible();
}

export async function criarCompetencia(page: Page, descricao: string, atividades: string[]) {
    await abrirModalCriarCompetencia(page);

    const modal = page.getByTestId('mdl-criar-competencia');
    await expect(modal).toBeVisible();

    await page.getByTestId('inp-criar-competencia-descricao').fill(descricao);

    for (const atividade of atividades) {
        // Click on the label containing the activity text to toggle the checkbox
        await modal.locator('label').filter({hasText: atividade}).click();
    }

    await page.getByTestId('btn-criar-competencia-salvar').click();
    await expect(modal).toBeHidden();

    // Verify creation
    await verificarCompetenciaNoMapa(page, descricao, atividades);
}

export async function editarCompetencia(page: Page, descricaoAtual: string, novaDescricao: string, novasAtividades?: string[], removerAtividades?: string[]) {
    const card = page.locator('.competencia-card', {has: page.getByText(descricaoAtual, {exact: true})});
    const editButton = card.getByTestId('btn-editar-competencia');

    // Hover on the card to trigger CSS hover state (buttons use card-level hover now)
    await card.hover();
    // Wait for button to become visible (CSS transition)
    await expect(editButton).toBeVisible();
    await editButton.click();

    const modal = page.getByTestId('mdl-criar-competencia');
    await expect(modal).toBeVisible();

    // Verify current description
    await expect(page.getByTestId('inp-criar-competencia-descricao')).toHaveValue(descricaoAtual);

    await page.getByTestId('inp-criar-competencia-descricao').fill(novaDescricao);

    if (removerAtividades) {
        for (const atividade of removerAtividades) {
            // Click on the label containing the activity text to toggle the checkbox
            await modal.locator('label').filter({hasText: atividade}).click();
        }
    }

    if (novasAtividades) {
        for (const atividade of novasAtividades) {
            // Click on the label containing the activity text to toggle the checkbox
            await modal.locator('label').filter({hasText: atividade}).click();
        }
    }

    await page.getByTestId('btn-criar-competencia-salvar').click();
    await expect(modal).toBeHidden();

    await expect(page.getByText(novaDescricao)).toBeVisible();
}

/**
 * Exclui competência confirmando a ação no modal
 */
export async function excluirCompetenciaConfirmando(page: Page, descricao: string) {
    const card = page.locator('.competencia-card', {has: page.getByText(descricao, {exact: true})});
    await card.hover();
    await card.getByTestId('btn-excluir-competencia').click();

    const modal = page.getByTestId('mdl-excluir-competencia');
    await expect(modal).toBeVisible();
    await expect(modal).toContainText(descricao);

    await page.getByRole('button', {name: 'Confirmar'}).click();
    await expect(modal).toBeHidden();
    await expect(page.getByText(descricao, {exact: true})).toBeHidden();
}

/**
 * Exclui competência cancelando a ação no modal
 */
export async function excluirCompetenciaCancelando(page: Page, descricao: string) {
    const card = page.locator('.competencia-card', {has: page.getByText(descricao, {exact: true})});
    await card.hover();
    await card.getByTestId('btn-excluir-competencia').click();

    const modal = page.getByTestId('mdl-excluir-competencia');
    await expect(modal).toBeVisible();
    await expect(modal).toContainText(descricao);

    await page.getByRole('button', {name: 'Cancelar'}).click();
    await expect(modal).toBeHidden();
    await expect(page.getByText(descricao, {exact: true})).toBeVisible();
}

export async function verificarCompetenciaNoMapa(page: Page, descricao: string, atividades: string[]) {
    const card = page.locator('.competencia-card', {has: page.getByText(descricao, {exact: true})});
    await expect(card).toBeVisible();

    for (const atividade of atividades) {
        await expect(card.getByText(atividade)).toBeVisible();
    }
}

export async function verificarSituacaoSubprocesso(page: Page, situacao: string) {
    await expect(page.getByTestId('txt-badge-situacao')).toHaveText(new RegExp(situacao, 'i'));
}

export async function disponibilizarMapa(page: Page, dataLimite?: string) {
    const data = dataLimite || calcularDataLimite(30);

    await page.getByTestId('btn-cad-mapa-disponibilizar').click();
    const modal = page.getByTestId('mdl-disponibilizar-mapa');
    await expect(modal).toBeVisible();

    await page.getByTestId('inp-disponibilizar-mapa-data').fill(data);
    await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();

    await expect(modal).toBeHidden();
}
