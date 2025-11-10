import {Page} from '@playwright/test';

/**
 * Limpa todos os processos em andamento do banco de dados.
 * @param page A instância da página do Playwright.
 */
export async function limparProcessosEmAndamento(page: Page): Promise<void> {
    try {
        await page.request.post('http://localhost:10000/api/e2e/limpar-processos-em-andamento');
    } catch (error) {
        console.warn('[AVISO] Não foi possível limpar processos EM_ANDAMENTO:', error);
        // Não falha o teste se a limpeza não funcionar
    }
}
