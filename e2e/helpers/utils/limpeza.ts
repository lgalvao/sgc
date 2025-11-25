import {Page} from '@playwright/test';

const BACKEND_URL = 'http://localhost:10000';

/**
 * Limpa todos os processos em andamento via endpoint de teste.
 * @param page A instância da página do Playwright.
 */
export async function limparProcessosEmAndamento(page: Page): Promise<void> {
    const response = await page.request.post(`${BACKEND_URL}/api/e2e/processos/em-andamento/limpar`);
    if (!response.ok()) {
        console.error(`Erro ao limpar processos em andamento: ${response.status()} ${response.statusText()}`);
        try {
            console.error(await response.text());
        } catch (e) {
            // ignore
        }
    } else {
        console.log('Processos em andamento limpos com sucesso.');
    }
}

/**
 * Limpa todos os processos de uma unidade específica via endpoint de teste.
 * @param page A instância da página do Playwright.
 * @param unidadeCodigo O código da unidade.
 */
export async function limparProcessosPorUnidade(page: Page, unidadeCodigo: number): Promise<void> {
    const response = await page.request.post(`${BACKEND_URL}/api/e2e/processos/unidade/${unidadeCodigo}/limpar`);
    if (!response.ok()) {
        console.error(`Erro ao limpar processos da unidade ${unidadeCodigo}: ${response.status()} ${response.statusText()}`);
    }
}