import { Page } from '@playwright/test';
import { preencherFormularioProcesso, selecionarUnidadesPorSigla } from './acoes-processo';
import { navegarParaCriacaoProcesso } from '../navegacao';

/**
 * Cria um processo via UI e retorna o ID do processo criado.
 */
export async function criarProcesso(page: Page, tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO', descricao: string, siglasUnidades: string[]): Promise<number> {
    await navegarParaCriacaoProcesso(page);
    await preencherFormularioProcesso(page, descricao, tipo, '2025-12-31');
    await selecionarUnidadesPorSigla(page, siglasUnidades);
    await page.getByRole('button', { name: /Salvar/i }).click();
    await page.waitForURL(/\/painel/);

    // Adiciona um data-testid para facilitar a limpeza
    await page.evaluate(({ descricao }) => {
        const rows = Array.from(document.querySelectorAll('table tbody tr'));
        const row = rows.find(r => r.textContent.includes(descricao));
        if (row) {
            row.setAttribute('data-testid', 'processo-teste');
        }
    }, { descricao });

    // Extrai o ID do processo da API a partir do texto na tabela
    const response = await page.waitForResponse(response => response.url().includes('/api/processos') && response.request().method() === 'GET');
    const processos = await response.json();
    const processoCriado = processos.find(p => p.descricao === descricao);
    return processoCriado.codigo;
}

/**
 * Submete um processo que está no estado "CRIADO".
 */
export async function submeterProcesso(page: Page, processoId: number): Promise<void> {
    await page.goto(`/processo/cadastro?idProcesso=${processoId}`);
    await page.getByRole('button', { name: /Iniciar processo/i }).click();
    await page.locator('.modal.show').getByRole('button', { name: /Confirmar/i }).click();
    await page.waitForURL(/\/painel/);
}

/**
 * Limpa todos os processos criados durante os testes.
 */
export async function limparProcessos(page: Page): Promise<void> {
    await page.goto('/painel');
    const processosParaLimpar = await page.locator('tr[data-testid="processo-teste"]').all();
    for (const processo of processosParaLimpar) {
        await processo.click();
        await page.waitForURL(/\/processo\/cadastro\?idProcesso=\d+/);
        await page.getByRole('button', { name: /Remover/i }).click();
        await page.locator('.modal.show').getByRole('button', { name: /Remover/i }).click();
        await page.waitForURL(/\/painel/);
    }
}

/**
 * Abre um processo pela sua descrição na tabela do painel.
 */
export async function abrirProcessoPorDescricao(page: Page, descricao: string): Promise<void> {
    await page.getByText(descricao).click();
}
