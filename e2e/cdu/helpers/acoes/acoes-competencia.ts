import {Page} from '@playwright/test';
import {SELETORES, TEXTOS} from '../dados';
import {verificarModalExclusaoCompetenciaAberto} from "../verificacoes";

/**
 * Clica no botão para abrir o modal de criação de competência.
 */
export async function abrirModalCriarCompetencia(page: Page): Promise<void> {
    await page.getByTestId(SELETORES.BTN_ABRIR_CRIAR_COMPETENCIA).click();
}

/**
 * Edita uma competência existente.
 */
export async function editarCompetencia(page: Page, nomeOriginal: string, nomeEditado: string): Promise<void> {
    const competenciaCard = page.locator('.competencia-card', {hasText: nomeOriginal});
    await competenciaCard.hover();
    await competenciaCard.getByTestId(SELETORES.BTN_EDITAR_COMPETENCIA).click();

    await page.getByTestId(SELETORES.INPUT_NOVA_COMPETENCIA).fill(nomeEditado);
    await page.getByTestId(SELETORES.BTN_CRIAR_COMPETENCIA).click();
}

/**
 * Exclui uma competência, incluindo a confirmação no modal.
 */
export async function excluirCompetencia(page: Page, nomeCompetencia: string): Promise<void> {
    const competenciaCard = page.locator('.competencia-card', {hasText: nomeCompetencia});
    await competenciaCard.hover();
    await competenciaCard.getByTestId(SELETORES.BTN_EXCLUIR_COMPETENCIA).click();

    await verificarModalExclusaoCompetenciaAberto(page, nomeCompetencia);
    await page.getByRole('button', {name: TEXTOS.CONFIRMAR}).click();
}
