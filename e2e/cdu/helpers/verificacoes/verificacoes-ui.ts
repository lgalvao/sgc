import {expect, Page} from '@playwright/test';
import {SELETORES, SELETORES_CSS, TEXTOS} from '../dados';
import {esperarBotaoVisivel, esperarElementoVisivel, esperarTextoVisivel} from './verificacoes-basicas';

/**
 * VERIFICAÇÕES DE INTERFACE DE USUÁRIO
 * Funções para verificações específicas de elementos da UI e comportamentos visuais
 */

/**
 * Verifica elementos comuns do painel após login
 */
export async function verificarElementosPainel(page: Page): Promise<void> {
    await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.TITULO_ALERTAS);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.COLUNA_DESCRICAO);
    await esperarElementoVisivel(page, SELETORES.COLUNA_TIPO);
    await esperarElementoVisivel(page, SELETORES.COLUNA_UNIDADES);
    await esperarElementoVisivel(page, SELETORES.COLUNA_SITUACAO);
}

/**
 * Garante que o botão "Criar processo" não está visível
 */
export async function verificarAusenciaBotaoCriarProcesso(page: Page): Promise<void> {
    await expect(page.getByTestId(SELETORES.BTN_CRIAR_PROCESSO).first()).not.toBeVisible();
}

/**
 * Verifica a visibilidade de um processo específico na tabela de processos
 */
export async function verificarVisibilidadeProcesso(page: Page, nomeProcesso: string | RegExp, visivel: boolean): Promise<void> {
    const processo = page.getByRole('row', {name: nomeProcesso});
    if (visivel) {
        await expect(processo).toBeVisible();
    } else {
        await expect(processo).toBeHidden();
    }
}

/**
 * Verifica comportamento de seleção em árvore de checkboxes
 */
export async function verificarSelecaoArvoreCheckboxes(page: Page): Promise<void> {
    const quantidadeMarcados = await page.locator(SELETORES_CSS.CHECKBOX_MARCADO).count();
    expect(quantidadeMarcados).toBeGreaterThan(0);
}

/**
 * Verifica comportamento de marcar/desmarcar checkbox
 */
export async function verificarComportamentoMarcacaoCheckbox(page: Page): Promise<void> {
    const primeiroCheckbox = page.locator(SELETORES_CSS.CHECKBOX).first();

    // Deve estar marcado inicialmente (após seleção)
    await expect(primeiroCheckbox).toBeChecked();

    // Desmarcar e verificar
    await primeiroCheckbox.click();
    await expect(primeiroCheckbox).not.toBeChecked();
}

/**
 * Verifica comportamento de checkbox específico (STIC/COSIS)
 */
export async function verificarComportamentoCheckboxInteroperacional(page: Page): Promise<void> {
    // Garantir que os checkboxes foram renderizados
    await page.waitForSelector('#chk-STIC');

    const chkStic = page.locator('#chk-STIC');
    const chkCosis = page.locator('#chk-COSIS');

    // Selecionar a unidade interoperacional raiz (STIC)
    await chkStic.click();

    // A raiz deve estar marcada
    await expect(chkStic).toBeChecked();

    // Se COSIS existe, não deve estar marcada automaticamente
    const cosisExists = await chkCosis.count() > 0;
    if (cosisExists) {
        await expect(chkCosis).not.toBeChecked();
    }

    // Desmarcar STIC não deve afetar filhos
    await chkStic.click();
    await expect(chkStic).not.toBeChecked();
    if (cosisExists) {
        await expect(chkCosis).not.toBeChecked();
    }
}

/**
 * Verifica o título da seção de processos no painel.
 */
export async function verificarTituloProcessos(page: Page): Promise<void> {
    await expect(page.locator(`[data-testid="${SELETORES.TITULO_PROCESSOS}"]`)).toContainText(TEXTOS.TITULO_PROCESSOS_LABEL);
}

/**
 * Verifica a visibilidade dos elementos da página de detalhes do processo.
 */
export async function verificarElementosDetalhesProcessoVisiveis(page: Page): Promise<void> {
    await esperarTextoVisivel(page, TEXTOS.SITUACAO_LABEL);
    await esperarTextoVisivel(page, TEXTOS.UNIDADES_PARTICIPANTES);
    await esperarBotaoVisivel(page, TEXTOS.FINALIZAR_PROCESSO);
}