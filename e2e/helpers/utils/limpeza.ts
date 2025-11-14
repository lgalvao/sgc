import {Page} from '@playwright/test';

/**
 * Limpa todos os processos em andamento do banco de dados.
 * @param page A instância da página do Playwright.
 */
export async function limparProcessosEmAndamento(page: Page): Promise<void> {
    try {
        const response = await page.request.post('http://localhost:10000/api/e2e/processos/em-andamento/limpar');
        if (!response.ok()) {
            const body = await response.text();
            console.warn(`[AVISO] Limpeza retornou ${response.status()}: ${body}`);
        }
    } catch (error) {
        console.warn('[AVISO] Não foi possível limpar processos EM_ANDAMENTO:', error);
        // Não falha o teste se a limpeza não funcionar
    }
}

/**
 * Recarrega os dados de teste do banco de dados (reset do estado para dados iniciais).
 * Deleta todos os dados e reinsere os dados de teste estaticamente definidos.
 * @param page A instância da página do Playwright.
 */
export async function recarregarDadosTeste(page: Page): Promise<void> {
    try {
        const response = await page.request.post('http://localhost:10000/api/e2e/dados-teste/recarregar');
        if (!response.ok()) {
            const data = await response.json();
            console.warn('[AVISO] Erro ao recarregar dados de teste:', data.mensagem);
        }
    } catch (error) {
        console.warn('[AVISO] Não foi possível recarregar dados de teste:', error);
    }
}

/**
 * Limpa todos os processos em andamento E todos os processos das unidades especificadas.
 * Recarrega a página para atualizar a lista de unidades desabilitadas.
 * 
 * ⚠️ NOTA: Esta função deleta TODOS os processos (criados e dados de teste) 
 * das unidades especificadas. Use com cuidado em testes que compartilham dados.
 * 
 * @param page A instância da página do Playwright.
 * @param codigosUnidades Array com os códigos das unidades a limpar.
 */
export async function limparProcessosPorUnidadeERecarregar(page: Page, codigosUnidades: number[]): Promise<void> {
    try {
        await page.request.post('http://localhost:10000/api/e2e/processos/unidades-e-pendentes/limpar', {
            data: codigosUnidades
        });
    } catch (error) {
        console.warn('[AVISO] Não foi possível limpar processos das unidades:', error);
    }
    // Recarrega a página para atualizar a lista de unidades desabilitadas
    await page.reload();
}

/**
 * Limpa APENAS processos em andamento de forma simples, sem recarregar a página.
 * Seguro para usar com testes que compartilham dados de teste estáticos.
 * @param page A instância da página do Playwright.
 */
export async function limparProcessosEmAndamentoSemRecarregar(page: Page): Promise<void> {
    try {
        await page.request.post('http://localhost:10000/api/e2e/processos/em-andamento/limpar');
    } catch (error) {
        console.warn('[AVISO] Não foi possível limpar processos EM_ANDAMENTO:', error);
    }
}
