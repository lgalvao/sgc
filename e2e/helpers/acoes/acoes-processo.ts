import { Page } from '@playwright/test';
import { SELETORES } from '../dados';
import { selecionarUnidadesPorSigla } from './acoes-unidade';

export async function preencherFormularioProcesso(page: Page, descricao: string, tipo: string, dataLimite: string, siglasUnidades: string[]): Promise<void> {
    await page.locator(SELETORES.CAMPO_DESCRICAO).fill(descricao);
    await page.locator(SELETORES.CAMPO_TIPO).selectOption(tipo);
    await page.locator(SELETORES.CAMPO_DATA_LIMITE).fill(dataLimite);
    await selecionarUnidadesPorSigla(page, siglasUnidades);
}

export async function salvarProcesso(page: Page): Promise<void> {
    await page.getByRole('button', { name: /Salvar/i }).click();
}

export async function removerProcesso(page: Page): Promise<void> {
    await page.getByRole('button', { name: /^Remover$/i }).click();
    const modal = page.locator('.modal.show');
    await modal.getByRole('button', { name: /Remover/i }).click();
}

export async function cancelarRemocaoProcesso(page: Page): Promise<void> {
    await page.getByRole('button', { name: /^Remover$/i }).click();
    const modal = page.locator('.modal.show');
    await modal.getByRole('button', { name: /Cancelar/i }).click();
}

export async function clicarBotaoRemoverProcesso(page: Page): Promise<void> {
    await page.getByRole('button', { name: /^Remover$/i }).click();
}
