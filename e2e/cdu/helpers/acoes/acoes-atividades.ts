import {Locator, Page} from '@playwright/test';
import {SELETORES, SELETORES_CSS, TEXTOS} from '../dados';

// ==================================================================
// FUNÇÕES DE LOCALIZAÇÃO (INTERNAS)
// ==================================================================

/**
 * Retorna o locator de um card de atividade pelo nome.
 */
function obterCardAtividade(page: Page, nomeAtividade: string): Locator {
    return page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
}

/**
 * Retorna o locator de um item de conhecimento dentro de um card de atividade.
 */
function obterLinhaConhecimento(page: Page, nomeAtividade: string, nomeConhecimento: string): Locator {
    const card = obterCardAtividade(page, nomeAtividade);
    return card.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento});
}

// ==================================================================
// AÇÕES DE ATIVIDADES E CONHECIMENTOS
// ==================================================================

/**
 * Adiciona uma nova atividade na página de cadastro.
 */
export async function adicionarAtividade(page: Page, nomeAtividade: string): Promise<void> {
    await page.getByTestId(SELETORES.INPUT_NOVA_ATIVIDADE).fill(nomeAtividade);
    await page.getByTestId(SELETORES.BTN_ADICIONAR_ATIVIDADE).click();
}

/**
 * Adiciona um novo conhecimento a uma atividade, usando o locator do card.
 */
export async function adicionarConhecimento(cardAtividade: Locator, nomeConhecimento: string): Promise<void> {
    await cardAtividade.getByTestId(SELETORES.INPUT_NOVO_CONHECIMENTO).fill(nomeConhecimento);
    await cardAtividade.getByTestId(SELETORES.BTN_ADICIONAR_CONHECIMENTO).click();
}

/**
 * Adiciona um conhecimento na primeira atividade encontrada na página.
 * Útil para cenários de teste que precisam apenas gerar um impacto, sem se importar com a atividade específica.
 */
export async function adicionarConhecimentoPrimeiraAtividade(page: Page, nomeConhecimento: string): Promise<void> {
    const primeiroCard = page.locator(SELETORES_CSS.CARD_ATIVIDADE).first();
    await adicionarConhecimento(primeiroCard, nomeConhecimento);
}

/**
 * Tenta adicionar uma atividade com a descrição vazia (ou com espaços).
 */
export async function tentarAdicionarAtividadeVazia(page: Page): Promise<void> {
    await page.getByTestId(SELETORES.INPUT_NOVA_ATIVIDADE).fill('   ');
    await page.getByTestId(SELETORES.BTN_ADICIONAR_ATIVIDADE).click();
}

/**
 * Edita a descrição de uma atividade existente.
 */
export async function editarAtividade(page: Page, nomeOriginal: string, nomeEditado: string): Promise<void> {
    const cardAtividade = obterCardAtividade(page, nomeOriginal);
    await cardAtividade.hover();
    await cardAtividade.getByTestId(SELETORES.BTN_EDITAR_ATIVIDADE).click({force: true});
    await page.getByTestId(SELETORES.INPUT_EDITAR_ATIVIDADE).fill(nomeEditado);
    await page.getByTestId(SELETORES.BTN_SALVAR_EDICAO_ATIVIDADE).click();
}

/**
 * Inicia a edição de uma atividade e clica no botão "Cancelar".
 */
export async function cancelarEdicaoAtividade(page: Page, nomeAtividade: string, textoTemporario: string): Promise<void> {
    const cardAtividade = obterCardAtividade(page, nomeAtividade);
    await cardAtividade.hover();
    await cardAtividade.getByTestId(SELETORES.BTN_EDITAR_ATIVIDADE).click({force: true});
    await page.getByTestId(SELETORES.INPUT_EDITAR_ATIVIDADE).fill(textoTemporario);
    await page.getByTestId(SELETORES.BTN_CANCELAR_EDICAO_ATIVIDADE).click();
}

/**
 * Remove uma atividade, aceitando o diálogo de confirmação.
 */
export async function removerAtividade(page: Page, nomeAtividade: string): Promise<void> {
    const cardAtividade = obterCardAtividade(page, nomeAtividade);
    await cardAtividade.hover();
    page.on('dialog', dialog => dialog.accept());
    await cardAtividade.getByTestId(SELETORES.BTN_REMOVER_ATIVIDADE).click({force: true});
}

/**
 * Edita a descrição de um conhecimento existente usando o modal.
 */
export async function editarConhecimento(page: Page, nomeAtividade: string, nomeOriginal: string, nomeEditado: string): Promise<void> {
    const linhaConhecimento = obterLinhaConhecimento(page, nomeAtividade, nomeOriginal);
    await linhaConhecimento.hover();
    await linhaConhecimento.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO).click();

    await page.getByTestId('input-conhecimento-modal').waitFor({state: 'visible'});
    await page.getByTestId('input-conhecimento-modal').fill(nomeEditado);
    await page.getByTestId('btn-salvar-conhecimento-modal').click();
    await page.getByTestId('input-conhecimento-modal').waitFor({state: 'hidden'});
}

/**
 * Remove um conhecimento de uma atividade, aceitando o diálogo de confirmação.
 */
export async function removerConhecimento(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const linhaConhecimento = obterLinhaConhecimento(page, nomeAtividade, nomeConhecimento);
    await linhaConhecimento.hover();
    page.on('dialog', dialog => dialog.accept());
    await linhaConhecimento.getByTestId(SELETORES.BTN_REMOVER_CONHECIMENTO).click();
}

/**
 * Clica no botão para abrir o modal de importação de atividades.
 */
export async function clicarBotaoImportarAtividades(page: Page): Promise<void> {
    await page.getByRole('button', {name: TEXTOS.IMPORTAR_ATIVIDADES}).click();
}

/**
 * Clica no botão para disponibilizar o cadastro (prioriza botão da página por test-id, fallback para role).
 */
export async function clicarBotaoDisponibilizar(page: Page): Promise<void> {
    // 1) Preferir botão da página por test-id (btn-disponibilizar-page)
    try {
        const pageBtn = page.getByTestId(SELETORES.BTN_DISPONIBILIZAR_PAGE);
        if ((await pageBtn.count()) > 0) {
            await pageBtn.first().click();
            return;
        }
    } catch {
        // ignore
    }

    // 2) Fallback: botão textual na página (pode haver ambiguidade — preferimos test-id)
    try {
        await page.getByRole('button', {name: TEXTOS.DISPONIBILIZAR}).first().click();
        return;
    } catch {
        // continue para próximo fallback
    }

    // 3) Último recurso: tentar botão por test-id global (btn-disponibilizar)
    const globalBtn = page.getByTestId(SELETORES.BTN_DISPONIBILIZAR);
    if ((await globalBtn.count()) > 0) {
        await globalBtn.first().click();
        return;
    }

    throw new Error('Botão de disponibilizar não encontrado na página.');
}

/**
 * Cria uma nova competência.
 */
export async function criarCompetencia(page: Page, descricao: string): Promise<void> {
    await page.getByTestId('btn-abrir-criar-competencia').click();
    await page.getByTestId('input-nova-competencia').fill(descricao);
    await page.locator('[data-testid="atividade-nao-associada"] label').first().click();
    await page.getByTestId('btn-criar-competencia').click();
}

/**
 * Edita uma competência existente.
 * - localiza o card pela descrição original
 * - aciona o botão de editar dentro do card
 * - preenche o novo texto e confirma a edição
 */
export async function editarCompetencia(page: Page, descricaoOriginal: string, descricaoEditada: string): Promise<void> {
    const competenciaCard = page.locator('.competencia-card').filter({hasText: descricaoOriginal});
    await competenciaCard.getByTestId(SELETORES.EDITAR_COMPETENCIA).click();
    await page.getByTestId('input-nova-competencia').fill(descricaoEditada);
    await page.getByTestId('btn-criar-competencia').click();
}

/**
 * Exclui uma competência existente e confirma a ação no modal.
 * - localiza o card pela descrição
 * - aciona o botão de excluir dentro do card
 * - confirma o modal de exclusão (tenta test-id, fallback por texto)
 */
export async function excluirCompetencia(page: Page, descricao: string): Promise<void> {
    const competenciaCard = page.locator('.competencia-card').filter({hasText: descricao});
    await competenciaCard.getByTestId(SELETORES.EXCLUIR_COMPETENCIA).click();

    // Tentar confirmar pelo test-id do modal, se presente
    try {
        const confirmar = page.getByTestId(SELETORES.BTN_MODAL_CONFIRMAR);
        if ((await confirmar.count()) > 0) {
            await confirmar.first().click();
            return;
        }
    } catch {
        // ignora e tenta fallback
    }

    // Fallback por role/text (botão "Confirmar")
    await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}
/**
 * Disponibiliza o mapa preenchendo data limite e observações no modal de disponibilização.
 * Função composta para evitar que testes manipulem seletores/expect diretamente.
 */
export async function disponibilizarMapaComData(page: Page, dataLimite: string, observacoes?: string): Promise<void> {
    // Usa helper existente para abrir o modal de disponibilizar
    await clicarBotaoDisponibilizar(page);

    const modal = page.locator('.modal.show');
    await modal.waitFor({state: 'visible'});

    // Tentar preencher campo de data com múltiplos fallbacks
    if ((await modal.locator('#dataLimite').count()) > 0) {
        await modal.locator('#dataLimite').fill(dataLimite);
    } else if ((await modal.getByLabel(/data/i).count()) > 0) {
        await modal.getByLabel(/data/i).fill(dataLimite);
    } else if ((await page.locator(SELETORES_CSS.CAMPO_DATA_LIMITE).count()) > 0) {
        await page.locator(SELETORES_CSS.CAMPO_DATA_LIMITE).fill(dataLimite);
    }

    if (observacoes) {
        // Preferir test-id do input de observações
        if ((await modal.getByTestId(SELETORES.INPUT_OBSERVACOES).count()) > 0) {
            await modal.getByTestId(SELETORES.INPUT_OBSERVACOES).fill(observacoes);
        } else if ((await modal.locator('#observacoes').count()) > 0) {
            await modal.locator('#observacoes').fill(observacoes);
        } else if ((await modal.getByLabel(/observa/i).count()) > 0) {
            await modal.getByLabel(/observa/i).fill(observacoes);
        } else {
            const textbox = modal.locator('textarea, input').first();
            await textbox.fill(observacoes);
        }
    }

    // Confirmar disponibilização (pode usar texto "Disponibilizar" ou "Confirmar")
    // Preferir botão dentro do modal por test-id
    if ((await modal.getByTestId(SELETORES.BTN_DISPONIBILIZAR).count()) > 0) {
        await modal.getByTestId(SELETORES.BTN_DISPONIBILIZAR).first().click();
    } else if ((await modal.getByRole('button', {name: TEXTOS.DISPONIBILIZAR}).count()) > 0) {
        await modal.getByRole('button', {name: TEXTOS.DISPONIBILIZAR}).first().click();
    } else {
        await modal.getByRole('button', {name: TEXTOS.CONFIRMAR}).first().click();
    }

    // Esperar fechamento do modal como tentativa de estabilidade (não falhar se permanecer aberto)
    await modal.waitFor({state: 'hidden', timeout: 5000}).catch(() => {});
}
/**
 * Abre o modal de disponibilização de mapa de competências.
 * Encapsula a ação de clicar no botão "Disponibilizar" e aguardar o modal.
 */
export async function abrirModalDisponibilizacao(page: Page): Promise<void> {
    await clicarBotaoDisponibilizar(page);
    const modal = page.locator('.modal.show');
    await modal.waitFor({state: 'visible'});
}

/**
 * Preenche o campo de data do modal de disponibilização.
 */
export async function preencherDataModal(page: Page, dataLimite: string): Promise<void> {
    const modal = page.locator('.modal.show');
    // Preferir test-id do input
    if ((await modal.getByTestId(SELETORES.INPUT_DATA_LIMITE).count()) > 0) {
        await modal.getByTestId(SELETORES.INPUT_DATA_LIMITE).fill(dataLimite);
    } else if ((await modal.locator('#dataLimite').count()) > 0) {
        await modal.locator('#dataLimite').fill(dataLimite);
    } else if ((await modal.getByLabel(/data/i).count()) > 0) {
        await modal.getByLabel(/data/i).fill(dataLimite);
    } else {
        // fallback para seletor global definido nas constantes
        await page.locator(SELETORES_CSS.CAMPO_DATA_LIMITE).fill(dataLimite);
    }
}

/**
 * Preenche o campo de observações do modal de disponibilização.
 */
export async function preencherObservacoesModal(page: Page, observacoes: string): Promise<void> {
    const modal = page.locator('.modal.show');
    if ((await modal.getByTestId(SELETORES.INPUT_OBSERVACOES).count()) > 0) {
        await modal.getByTestId(SELETORES.INPUT_OBSERVACOES).fill(observacoes);
    } else if ((await modal.locator('#observacoes').count()) > 0) {
        await modal.locator('#observacoes').fill(observacoes);
    } else if ((await modal.getByLabel(/observa/i).count()) > 0) {
        await modal.getByLabel(/observa/i).fill(observacoes);
    } else {
        // fallback: tentar por input/textarea dentro do modal
        const textbox = modal.locator('textarea, input').first();
        await textbox.fill(observacoes);
    }
}