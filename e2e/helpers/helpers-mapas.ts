import {expect, type Page} from '@playwright/test';
import {calcularDataLimite} from './helpers-processos.js';
import {limparNotificacoes, verificarPaginaPainel, verificarToast} from './helpers-navegacao.js';
import {TEXTOS} from '../../frontend/src/constants/textos.js';



export async function navegarParaMapa(page: Page) {
    // Aguardar o carregamento do subprocesso antes de verificar os cards
    await expect(page.getByTestId('header-subprocesso')).toBeVisible();

    const cardEdicao = page.getByTestId('card-subprocesso-mapa-edicao');
    const cardVisualizacao = page.getByTestId('card-subprocesso-mapa-visualizacao');

    await expect(cardEdicao.or(cardVisualizacao)).toBeVisible();

    if (await cardEdicao.isVisible()) {
        await cardEdicao.click();
    } else {
        await cardVisualizacao.click();
    }

    await page.waitForURL(/\/(mapa|vis-mapa)$/);
    await expect(page.getByRole('heading', {name: TEXTOS.mapa.TITULO_TECNICO})).toBeVisible();
}

export async function abrirModalCriarCompetencia(page: Page) {
    const btnNormal = page.getByTestId('btn-abrir-criar-competencia');

    // Wait for the button to be visible
    await expect(btnNormal).toBeVisible();
    await btnNormal.click();
    await expect(page.getByTestId('mdl-criar-competencia')).toBeVisible();
}

export async function criarCompetencia(page: Page, descricao: string, atividades: string[]) {
    await limparNotificacoes(page);
    await abrirModalCriarCompetencia(page);

    const modal = page.getByTestId('mdl-criar-competencia');
    await expect(modal).toBeVisible();

    await page.getByTestId('inp-criar-competencia-descricao').fill(descricao);

    for (const atividade of atividades) {
        const checkbox = modal.getByLabel(atividade, {exact: true});
        await expect(checkbox).toBeVisible();
        await checkbox.click();
    }

    await page.getByTestId('btn-criar-competencia-salvar').click();
    await expect(modal).toBeHidden();

    await verificarCompetenciaNoMapa(page, descricao, atividades);
}

export async function editarCompetencia(page: Page, descricaoAtual: string, novaDescricao: string, novasAtividades?: string[], removerAtividades?: string[]) {
    const card = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(descricaoAtual, {exact: true})});
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
            await modal.getByLabel(atividade, {exact: true}).click();
        }
    }

    if (novasAtividades) {
        for (const atividade of novasAtividades) {
            await modal.getByLabel(atividade, {exact: true}).click();
        }
    }

    await page.getByTestId('btn-criar-competencia-salvar').click();
    await expect(modal).toBeHidden();

    await expect(page.getByText(novaDescricao)).toBeVisible();
}

export async function removerAtividadeAssociada(page: Page, descricaoCompetencia: string, descricaoAtividade: string) {
    const card = page.getByTestId('cad-mapa__card-competencia')
        .filter({has: page.getByText(descricaoCompetencia, {exact: true})});
    await expect(card).toBeVisible();

    const atividadeAssociada = card.locator('.atividade-associada-card-item')
        .filter({hasText: descricaoAtividade})
        .first();
    await expect(atividadeAssociada).toBeVisible();

    const botaoRemover = atividadeAssociada.getByTestId('btn-remover-atividade-associada');
    await expect(botaoRemover).toBeVisible();
    await botaoRemover.click({force: true});

    await expect(atividadeAssociada).toBeHidden();
}

/**
 * Exclui competência confirmando a ação no modal
 */
export async function excluirCompetenciaConfirmando(page: Page, descricao: string) {
    const card = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(descricao, {exact: true})});
    await card.hover();
    await card.getByTestId('btn-excluir-competencia').click();

    const modal = page.getByTestId('mdl-excluir-competencia');
    await expect(modal).toBeVisible();
    await expect(modal).toContainText(descricao);

    const btnConfirmar = modal.getByTestId('btn-confirmar-exclusao-competencia');
    await btnConfirmar.waitFor({ state: 'visible', timeout: 5000 });
    await btnConfirmar.scrollIntoViewIfNeeded();
    await btnConfirmar.click();
    await expect(modal).toBeHidden();
    await expect(page.getByText(descricao, {exact: true})).toBeHidden();
}

/**
 * Exclui competência cancelando a ação no modal
 */
export async function excluirCompetenciaCancelando(page: Page, descricao: string) {
    const card = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(descricao, {exact: true})});
    await card.hover();
    await card.getByTestId('btn-excluir-competencia').click();

    const modal = page.getByTestId('mdl-excluir-competencia');
    await expect(modal).toBeVisible();
    await expect(modal).toContainText(descricao);

    await modal.getByTestId('btn-modal-confirmacao-cancelar').click();
    await expect(modal).toBeHidden();
    await expect(page.getByText(descricao, {exact: true})).toBeVisible();
}

export async function verificarCompetenciaNoMapa(page: Page, descricao: string, atividades: string[]) {
    const card = page.getByTestId('cad-mapa__card-competencia').filter({has: page.getByText(descricao, {exact: true})});
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
    await verificarPaginaPainel(page);
    await verificarToast(page, TEXTOS.sucesso.MAPA_DISPONIBILIZADO);
}

/**
 * Realiza o aceite ou homologação do mapa a partir da visualização do subprocesso.
 */
export async function aceitarOuHomologarMapa(page: Page, observacao: string) {
    await page.getByTestId('card-subprocesso-mapa-visualizacao').click();
    await page.getByTestId('btn-mapa-homologar-aceite').click();
    await page.getByTestId('inp-aceite-mapa-observacao').fill(observacao);
    await page.getByTestId('btn-aceite-mapa-confirmar').click();
    await page.waitForURL(/\/painel$/);
}
