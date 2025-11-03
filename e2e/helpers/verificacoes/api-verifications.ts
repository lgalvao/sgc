import { Page, expect } from '@playwright/test';

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

export async function verificarEfeitosBackendInicioProcesso(page: Page, processoId: number, descricao: string, unidadesParticipantes: string[]): Promise<void> {
    const processo = await getProcessoById(page, processoId);
    expect(processo.situacao).toBe('EM_ANDAMENTO');

    const subprocessos = await getSubprocessosByProcessoId(page, processoId);
    expect(subprocessos).toHaveLength(unidadesParticipantes.length);
    expect(subprocessos.map(s => s.unidade.sigla)).toEqual(expect.arrayContaining(unidadesParticipantes));
    subprocessos.forEach(sub => {
        expect(sub.situacao).toBe('CRIADO');
    });

    await page.goto('/painel'); // Refresh to ensure alerts are loaded
    const alertas = await getAlertas(page);
    const alertaDoProcesso = alertas.content.find(a => a.mensagem.includes(descricao));
    expect(alertaDoProcesso).toBeDefined();
    expect(alertaDoProcesso.mensagem).toContain('Um novo processo de mapeamento de competências foi iniciado');
}

export async function verificarSubprocessosCriados(page: Page, processoId: string | null): Promise<void> {
    expect(processoId).toBeTruthy();
    const response = await page.request.get(`http://localhost:10000/api/processos/${processoId}/subprocessos`);
    expect(response.ok()).toBeTruthy();

    const subprocessos = await response.json();
    expect(subprocessos.length).toBeGreaterThan(0);

    for (const subprocesso of subprocessos) {
        expect(subprocesso).toHaveProperty('dataLimiteEtapa1');
        expect(subprocesso).toHaveProperty('situacao');
        expect(subprocesso.situacao).toBe('NAO_INICIADO');
    }
}

export async function verificarAlertasCriados(page: Page): Promise<void> {
    await page.goto('http://localhost:5173/painel');
    await page.waitForSelector('[data-testid="tabela-alertas"]', { timeout: 15000 });

    const tabelaAlertas = page.locator('[data-testid="tabela-alertas"]');
    const alertaInicio = tabelaAlertas.locator('tr').filter({ hasText: /Início do processo/i });

    await expect(alertaInicio.first()).toBeVisible({ timeout: 10000 });
}

export async function verificarCopiaMapa(page: Page, processoRevisaoId: number, subprocessoOriginalId: number): Promise<void> {
    const subprocessosRevisao = await getSubprocessosByProcessoId(page, processoRevisaoId);
    expect(subprocessosRevisao).toHaveLength(1);
    const subprocessoRevisaoId = subprocessosRevisao[0].codigo;

    const mapaOriginal = await getMapDetailsBySubprocessoId(page, subprocessoOriginalId);
    const mapaCopiado = await getMapDetailsBySubprocessoId(page, subprocessoRevisaoId);

    expect(mapaCopiado.codigo).not.toEqual(mapaOriginal.codigo);

    const normalizarMapa = (mapa) => {
        mapa.codigo = null;
        mapa.competencias.forEach(c => {
            c.codigo = null;
            c.atividades.forEach(a => {
                a.codigo = null;
                a.conhecimentos.forEach(con => con.codigo = null);
                a.conhecimentos.sort((c1, c2) => c1.descricao.localeCompare(c2.descricao));
            });
            c.atividades.sort((a1, a2) => a1.descricao.localeCompare(a2.descricao));
        });
        mapa.competencias.sort((c1, c2) => c1.descricao.localeCompare(c2.descricao));
        return mapa;
    };

    const mapaOriginalNormalizado = normalizarMapa(mapaOriginal);
    const mapaCopiadoNormalizado = normalizarMapa(mapaCopiado);

    expect(mapaCopiadoNormalizado).toEqual(mapaOriginalNormalizado);
}
