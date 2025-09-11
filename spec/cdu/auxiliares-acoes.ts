import {expect, Page} from '@playwright/test';
import {SELETORES_CSS, TEXTOS} from './constantes-teste';

/**
 * Finaliza um processo com confirmação
 */
export async function finalizarProcesso(page: Page): Promise<void> {
  await page.click(`button:has-text("${TEXTOS.FINALIZAR_PROCESSO}")`);
  await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
  await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
}

/**
 * Disponibiliza cadastro com confirmação
 */
export async function disponibilizarCadastro(page: Page): Promise<void> {
  await page.click(`button:has-text("${TEXTOS.DISPONIBILIZAR}")`);
  await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
  await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
}

/**
 * Homologa um cadastro/mapa
 */
export async function homologarItem(page: Page): Promise<void> {
  await page.click(`button:has-text("${TEXTOS.HOMOLOGAR}")`);
  await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
  const confirmarBtn = page.locator(`button:has-text("${TEXTOS.CONFIRMAR}")`).last();
  await confirmarBtn.click();
}

/**
 * Devolve para ajustes
 */
export async function devolverParaAjustes(page: Page, observacao?: string): Promise<void> {
  await page.click('button:has-text("Devolver")');
  await expect(page.locator(SELETORES_CSS.MODAL_VISIVEL)).toBeVisible();
  
  if (observacao) {
    const textarea = page.locator('textarea').first();
    await textarea.waitFor({ state: 'visible' });
    await textarea.fill(observacao);
  }
  
  await page.click(`button:has-text("${TEXTOS.CONFIRMAR}")`);
}

/**
 * Valida um mapa de competências
 */
export async function validarMapa(page: Page, sugestoes?: string): Promise<void> {
  await page.getByTestId('validar-btn').click();
  await expect(page.getByTestId('modal-validar')).toBeVisible();
  
  if (sugestoes) {
    await page.fill('textarea[placeholder*="sugestões"]', sugestoes);
  }
  
  await page.getByTestId('modal-validar-confirmar').click();
}

/**
 * Cria uma nova competência
 */
export async function criarCompetencia(page: Page, descricao: string): Promise<void> {
  await page.getByTestId('btn-abrir-criar-competencia').click();
  await page.getByTestId('input-nova-competencia').fill(descricao);
  
  // Selecionar primeira atividade disponível
  await page.locator('[data-testid="atividade-nao-associada"] label').first().click();
  
  await page.getByTestId('btn-criar-competencia').click();
}

/**
 * Inicia um processo com confirmação
 */
export async function iniciarProcesso(page: Page): Promise<void> {
  await page.getByText(TEXTOS.INICIAR_PROCESSO).click();
  await page.waitForSelector('.modal.show');
  await page.getByText(TEXTOS.CONFIRMAR).click();
  await page.waitForLoadState('networkidle');
}

/**
 * Cancela um modal
 */
export async function cancelarModal(page: Page): Promise<void> {
  await page.click('button:has-text("' + TEXTOS.CANCELAR + '")');
}