import type {APIRequestContext} from '@playwright/test';
import type {useProcessoCleanup} from '../hooks/hooks-limpeza.js';

type ProcessoCleanup = ReturnType<typeof useProcessoCleanup>;

async function criarProcessoDiagnosticoFixture(
    request: APIRequestContext,
    cleanup: ProcessoCleanup,
    endpoint: string,
    descricao: string,
    unidadeSigla: string,
    servidorTitulo?: string
): Promise<number> {
    const response = await request.post(endpoint, {
        data: {
            descricao,
            unidadeSigla,
            iniciar: true,
            diasLimite: 30,
            ...(servidorTitulo ? {servidorTitulo} : {})
        }
    });

    if (!response.ok()) {
        throw new Error(`Falha ao criar fixture de diagnóstico: ${response.status()} ${await response.text()}`);
    }

    const processo = await response.json() as { codigo: number };
    cleanup.registrar(processo.codigo);
    return processo.codigo;
}

export function criarProcessoDiagnosticoPorFixture(
    request: APIRequestContext,
    cleanup: ProcessoCleanup,
    descricao: string,
    unidadeSigla: string
): Promise<number> {
    return criarProcessoDiagnosticoFixture(request, cleanup, '/e2e/fixtures/processo-diagnostico', descricao, unidadeSigla);
}

export function criarProcessoDiagnosticoComAutoavaliacaoConcluidaPorFixture(
    request: APIRequestContext,
    cleanup: ProcessoCleanup,
    descricao: string,
    unidadeSigla: string,
    servidorTitulo: string
): Promise<number> {
    return criarProcessoDiagnosticoFixture(
        request,
        cleanup,
        '/e2e/fixtures/processo-diagnostico-com-autoavaliacao-concluida',
        descricao,
        unidadeSigla,
        servidorTitulo
    );
}

export function criarProcessoDiagnosticoComConsensoCriadoPorFixture(
    request: APIRequestContext,
    cleanup: ProcessoCleanup,
    descricao: string,
    unidadeSigla: string,
    servidorTitulo: string
): Promise<number> {
    return criarProcessoDiagnosticoFixture(
        request,
        cleanup,
        '/e2e/fixtures/processo-diagnostico-com-consenso-criado',
        descricao,
        unidadeSigla,
        servidorTitulo
    );
}
