import type { APIRequestContext } from '@playwright/test';

/**
 * Representa um processo criado via fixture
 */
export interface ProcessoFixture {
    codigo: number;
    descricao: string;
    tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO';
    situacao: string;
}

/**
 * Opções para criar um processo fixture
 */
export interface ProcessoFixtureOptions {
    /** Unidade participante (sigla) */
    unidade: string;
    /** Se true, o processo já será criado iniciado */
    iniciar?: boolean;
    /** Descrição customizada (opcional, gera automaticamente se omitido) */
    descricao?: string;
    /** Tipo do processo (padrão: MAPEAMENTO) */
    tipo?: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO';
    /** Dias até data limite (padrão: 30) */
    diasLimite?: number;
}

/**
 * Cria um processo de mapeamento via API E2E (mais rápido que navegação UI).
 * 
 * NOTA: Esta função requer que o endpoint `/e2e/fixtures/processo-mapeamento`
 * esteja implementado no backend. Se o endpoint não existir, use a criação
 * via UI com `criarProcesso()` do processo-helpers.ts.
 * 
 * @param request - Contexto de requisição do Playwright
 * @param options - Opções de configuração do processo
 * @returns Dados do processo criado
 * 
 * @example
 * ```typescript
 * test('Deve exibir processo iniciado', async ({ page, request }) => {
 *     // Criar processo via API (rápido)
 *     const processo = await criarProcessoFixture(request, {
 *         unidade: 'ASSESSORIA_11',
 *         iniciar: true
 *     });
 *     
 *     // Navegar diretamente para a tela
 *     await page.goto(`/processo/${processo.codigo}`);
 *     
 *     // Validar
 *     await expect(page.getByText('Em andamento')).toBeVisible();
 * });
 * ```
 */
export async function criarProcessoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const tipo = options.tipo ?? 'MAPEAMENTO';
    const endpoint = tipo === 'MAPEAMENTO' 
        ? '/e2e/fixtures/processo-mapeamento'
        : '/e2e/fixtures/processo-revisao';
    
    const response = await request.post(`http://localhost:10000${endpoint}`, {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? false,
            descricao: options.descricao ?? `Fixture E2E ${tipo} ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });
    
    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture: ${response.status()} ${response.statusText()}\n` +
            `Endpoint ${endpoint} pode não estar implementado no backend.`
        );
    }
    
    return await response.json();
}

/**
 * Cria múltiplos processos de uma vez.
 * Útil para testes que precisam de vários processos.
 * 
 * @example
 * ```typescript
 * const processos = await criarProcessosEmLote(request, [
 *     { unidade: 'ASSESSORIA_11', tipo: 'MAPEAMENTO' },
 *     { unidade: 'ASSESSORIA_12', tipo: 'REVISAO' },
 *     { unidade: 'ASSESSORIA_21', tipo: 'MAPEAMENTO', iniciar: true }
 * ]);
 * ```
 */
export async function criarProcessosEmLote(
    request: APIRequestContext,
    opcoes: ProcessoFixtureOptions[]
): Promise<ProcessoFixture[]> {
    return Promise.all(
        opcoes.map(opts => criarProcessoFixture(request, opts))
    );
}

/**
 * Remove um processo via API E2E (cleanup).
 * 
 * @example
 * ```typescript
 * test.afterEach(async ({ request }) => {
 *     if (processoId) {
 *         await removerProcesso(request, processoId);
 *     }
 * });
 * ```
 */
export async function removerProcesso(
    request: APIRequestContext,
    codigo: number
): Promise<void> {
    const response = await request.post(
        `http://localhost:10000/e2e/processo/${codigo}/limpar`
    );
    
    if (!response.ok()) {
        console.warn(`Falha ao remover processo ${codigo}: ${response.status()}`);
    }
}
