import { expect, type Page } from '@playwright/test';

/**
 * Calcula uma data limite N dias no futuro
 */
export function calcularDataLimite(dias: number): string {
    const dataLimite = new Date();
    dataLimite.setDate(dataLimite.getDate() + dias);
    return dataLimite.toISOString().split('T')[0];
}

/**
 * Cria um processo através da UI
 */
export async function criarProcesso(page: Page, options: {
    descricao: string;
    tipo: 'MAPEAMENTO' | 'REVISAO';
    diasLimite: number;
    unidade: string;
}): Promise<void> {
    await page.getByTestId('btn-criar-processo').click();
    await expect(page).toHaveURL(/\/processo\/cadastro/);

    await page.getByTestId('input-descricao').fill(options.descricao);
    await page.getByTestId('select-tipo').selectOption(options.tipo);
    await page.getByTestId('input-dataLimite').fill(calcularDataLimite(options.diasLimite));

    // Usar getByTestId ao invés de getByRole para respeitar disabled
    await page.getByTestId(`chk-${options.unidade}`).check();
    await page.getByTestId('btn-salvar').click();

    await expect(page).toHaveURL(/\/painel/);
}

/**
 * Verifica que um processo aparece na tabela com situação e tipo corretos
 */
export async function verificarProcessoNaTabela(page: Page, options: {
    descricao: string;
    situacao: string;
    tipo: string;
}): Promise<void> {
    await expect(page.getByTestId('tabela-processos')).toBeVisible();
    await expect(page.getByText(options.descricao)).toBeVisible();

    const linhaProcesso = page.locator('tr', { has: page.getByText(options.descricao) });
    await expect(linhaProcesso.getByText(options.situacao)).toBeVisible();
    await expect(linhaProcesso.getByText(options.tipo)).toBeVisible();
}
