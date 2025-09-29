import {Page} from '@playwright/test';
import {TEXTOS} from '../dados/constantes-teste';

/**
 * AÇÕES ESPECÍFICAS PARA MODAIS
 * Funções para interação com modais em testes
 */

/**
 * Cancelar ação no modal
 */
export async function cancelarNoModal(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.CANCELAR }).click();
}

/**
 * Confirmar ação no modal
 */
export async function confirmarNoModal(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
}

/**
 * Confirmar remoção no modal usando botão de danger
 */
export async function confirmarRemocaoNoModal(page: Page): Promise<void> {
    await page.locator('.modal.show .btn-danger').click();
}

/**
 * Abrir diálogo de remoção
 */
export async function abrirDialogoRemocaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.REMOVER }).click();
}

/**
 * Iniciar processo através do botão
 */
export async function clicarIniciarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.INICIAR_PROCESSO }).click();
}

/**
 * Abrir modal de inicialização de processo
 */
export async function abrirModalInicializacaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.INICIAR_PROCESSO }).click();
}

/**
 * Confirmar inicialização no modal
 */
export async function confirmarInicializacaoNoModal(page: Page): Promise<void> {
    await page.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
}

/**
 * Cancela um modal clicando no botão "Cancelar", "Fechar", ou em um botão de fechar genérico.
 */
export async function cancelarModal(page: Page): Promise<void> {
  const modalVisivel = page.locator('.modal.show');
  const botaoCancelar = modalVisivel.getByRole('button', { name: TEXTOS.CANCELAR });
  const botaoFechar = modalVisivel.getByRole('button', { name: 'Fechar' });
  const botaoDismiss = modalVisivel.locator('[data-bs-dismiss="modal"]');

  if (await botaoCancelar.count() > 0) {
    await botaoCancelar.last().click();
  } else if (await botaoFechar.count() > 0) {
    await botaoFechar.last().click();
  } else {
    await botaoDismiss.last().click();
  }
}
