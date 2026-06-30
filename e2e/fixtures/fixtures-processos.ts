import type {APIRequestContext} from '@playwright/test';
import {expect} from '@playwright/test';

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

export function validarProcessoFixture(processo: ProcessoFixture, descricaoEsperada?: string): void {
    expect(processo.codigo).toBeGreaterThan(0);
    expect(processo.descricao).not.toBe('');

    if (descricaoEsperada) {
        expect(processo.descricao).toBe(descricaoEsperada);
    }

    expect(processo.situacao).not.toBe('');
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
        : tipo === 'REVISAO'
            ? '/e2e/fixtures/processo-revisao'
            : '/e2e/fixtures/processo-diagnostico';

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

export async function criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions & { servidorTitulo: string }
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-diagnostico-com-autoavaliacao-concluida', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E DIAGNOSTICO AUTOAVALIADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30,
            servidorTitulo: options.servidorTitulo
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo diagnóstico com autoavaliação concluída: ` +
            `${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

export async function criarProcessoDiagnosticoComConsensoCriadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions & { servidorTitulo: string }
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-diagnostico-com-consenso-criado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E DIAGNOSTICO CONSENSO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30,
            servidorTitulo: options.servidorTitulo
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo diagnóstico com consenso criado: ` +
            `${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

export async function criarProcessoDiagnosticoConcluidoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-diagnostico-concluido', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E DIAGNOSTICO CONCLUIDO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo diagnóstico concluído: ` +
            `${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

export async function criarProcessoDiagnosticoProntoParaConcluirFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions & { servidorTitulo: string }
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-diagnostico-pronto-para-concluir', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E DIAGNOSTICO PRONTO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30,
            servidorTitulo: options.servidorTitulo
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo diagnóstico pronto para concluir: ` +
            `${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

export async function criarProcessoDiagnosticoAceitoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-diagnostico-aceito', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E DIAGNOSTICO ACEITO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo diagnóstico aceito: ` +
            `${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

export async function criarProcessoDiagnosticoHomologadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-diagnostico-homologado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E DIAGNOSTICO HOMOLOGADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo diagnóstico homologado: ` +
            `${response.status()} ${response.statusText()}`
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
 * Cria um processo de mapeamento já com cadastro homologado via API E2E.
 */
export async function criarProcessoCadastroHomologadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-cadastro-homologado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E CADASTRO_HOMOLOGADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture com cadastro homologado: ${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

/**
 * Cria um processo de mapeamento já com cadastro disponibilizado via API E2E.
 */
export async function criarProcessoCadastroDisponibilizadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-cadastro-disponibilizado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E CADASTRO_DISPONIBILIZADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture com cadastro disponibilizado: ${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

/**
 * Cria um processo de mapeamento já com mapa disponibilizado via API E2E.
 */
export async function criarProcessoMapaDisponibilizadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-disponibilizado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E MAPA_DISPONIBILIZADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture com mapa disponibilizado: ${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

/**
 * Cria um processo de mapeamento já com mapa validado via API E2E.
 */
export async function criarProcessoMapaValidadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-validado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E MAPA_VALIDADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture com mapa validado: ${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

/**
 * Cria um processo de mapeamento já com mapa com sugestões via API E2E.
 * Usado para testar o botão "Ver sugestões" no CDU-20.
 */
export async function criarProcessoMapaComSugestoesFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-mapa-com-sugestoes', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E MAPA_COM_SUGESTOES ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture com mapa com sugestões: ${response.status()} ${response.statusText()}`
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
 * Cria um processo de revisão já com cadastro disponibilizado via API E2E.
 * Usado para testar aceite em bloco (CDU-22) e homologação em bloco (CDU-23) para revisão.
 */
export async function criarProcessoRevisaoCadastroDisponibilizadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-revisao-com-cadastro-disponibilizado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E REVISAO_CADASTRO_DISPONIBILIZADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture de revisão com cadastro disponibilizado: ${response.status()} ${response.statusText()}`
        );
    }

    return await response.json();
}

/**
 * Cria um processo de revisão já com cadastro homologado via API E2E.
 */
export async function criarProcessoRevisaoCadastroHomologadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-revisao-com-cadastro-homologado', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? true,
            descricao: options.descricao ?? `Fixture E2E REVISAO_CADASTRO_HOMOLOGADO ${Date.now()}`,
            diasLimite: options.diasLimite ?? 30
        }
    });

    if (!response.ok()) {
        throw new Error(
            `Falha ao criar processo fixture de revisão com cadastro homologado: ${response.status()} ${response.statusText()}`
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
