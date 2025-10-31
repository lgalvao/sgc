import { Page } from '@playwright/test';

/**
 * Limpa todos os processos EM_ANDAMENTO do banco (apenas para E2E)
 * Evita que testes deixem "lixo" que bloqueie testes subsequentes.
 * 
 * Usa um endpoint de teste especial disponível apenas no perfil E2E.
 */
export async function limparProcessosEmAndamento(page: Page): Promise<void> {
    try {
        await page.request.post('http://localhost:10000/api/e2e/limpar-processos-em-andamento');
    } catch (error) {
        console.warn('[AVISO] Não foi possível limpar processos EM_ANDAMENTO:', error);
        // Não falha o teste se a limpeza não funcionar
    }
}

/**
 * Limpa todo o banco de testes (apenas para E2E)
 * Resets para estado inicial com dados de import.sql
 */
export async function limparBancoTestes(page: Page): Promise<void> {
    try {
        await page.request.post('http://localhost:10000/api/e2e/reset');
    } catch (error) {
        console.warn('[AVISO] Não foi possível limpar o banco de testes:', error);
    }
}
