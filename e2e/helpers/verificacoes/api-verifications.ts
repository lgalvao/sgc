import { Page } from '@playwright/test';

const BASE_URL = 'http://localhost:10000/api';

/**
 * Busca um processo pelo ID diretamente da API.
 * Requer que o usuário esteja autenticado na `page`.
 */
export async function getProcessoById(page: Page, id: number) {
    const response = await page.request.get(`${BASE_URL}/processos/${id}`);
    return response.json();
}

/**
 * Busca os subprocessos associados a um processo.
 * Requer que o usuário esteja autenticado na `page`.
 */
export async function getSubprocessosByProcessoId(page: Page, id: number) {
    const response = await page.request.get(`${BASE_URL}/processos/${id}/subprocessos`);
    return response.json();
}

/**
 * Busca as movimentações de um subprocesso.
 * Requer que o usuário esteja autenticado na `page`.
 */
export async function getMovimentacoesBySubprocessoId(page: Page, id: number) {
    // Nota: O endpoint exato pode precisar de ajuste.
    const response = await page.request.get(`${BASE_URL}/subprocessos/${id}/movimentacoes`);
    return response.json();
}

/**
 * Busca os alertas para o usuário autenticado.
 * Requer que o usuário esteja autenticado na `page`.
 */
export async function getAlertas(page: Page) {
    const response = await page.request.get(`${BASE_URL}/alertas/meus-alertas`);
    return response.json();
}

/**
 * Busca os detalhes de um subprocesso pelo ID.
 * Requer que o usuário esteja autenticado na `page`.
 */
export async function getSubprocessoById(page: Page, id: number) {
    const response = await page.request.get(`${BASE_URL}/subprocessos/${id}`);
    return response.json();
}

/**
 * Busca os detalhes de visualização de um mapa pelo ID do subprocesso.
 * Requer que o usuário esteja autenticado na `page`.
 */
export async function getMapDetailsBySubprocessoId(page: Page, id: number) {
    const response = await page.request.get(`${BASE_URL}/subprocessos/${id}/mapa-visualizacao`);
    return response.json();
}
