import type {APIRequestContext} from '@playwright/test';

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
 * via UI com `criarProcesso()` do helpers-processos.ts.
 *
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

    const response = await request.post(endpoint, {
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
 * Cria um processo de mapeamento via API E2E que já nasce finalizado e com atividades, 
 * ignorando validações, perfeito para testes de importação.
 */
export async function criarProcessoFinalizadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const endpoint = '/e2e/fixtures/processo-finalizado-com-atividades';

    const response = await request.post(endpoint, {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E FINALIZADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture finalizado: ${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

/**
 * Cria um processo de mapeamento já com mapa homologado via API E2E.
 */
export async function criarProcessoMapaHomologadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-homologado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E MAPA_HOMOLOGADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture com mapa homologado: ${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

/**
 * Cria um processo de revisão já com mapa homologado via API E2E.
 */
export async function criarProcessoRevisaoMapaHomologadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-revisao-com-mapa-homologado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E REVISAO_MAPA_HOMOLOGADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture de revisão com mapa homologado: ${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}
