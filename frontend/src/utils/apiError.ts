import logger from '@/utils/logger';

type ErrorLike = {
    stack?: string;
    isAxiosError?: boolean;
};

function obterStack(erro: unknown): string | undefined {
    return typeof erro === "object" && erro !== null && "stack" in erro
        ? (erro as ErrorLike).stack
        : undefined;
}

function asPayload(data: unknown): ApiErrorPayload {
    return typeof data === "object" && data !== null ? data as ApiErrorPayload : {};
}

export interface ApiErrorPayload {
    timestamp?: string;
    status?: number;
    message?: string;
    code?: string;
    details?: Record<string, unknown>;
    traceId?: string;
    stackTrace?: string;
    subErrors?: Array<{
        object?: string;
        field?: string;
        rejectedValue?: unknown;
        message?: string;
    }>;
}

export type ErrorKind =
    | 'validation'      // 400, 422 - erro de validação de dados
    | 'notFound'        // 404 - recurso não encontrado
    | 'conflict'        // 409 - conflito de estado
    | 'unauthorized'    // 401 - não autenticado
    | 'forbidden'       // 403 - sem permissão
    | 'network'         // Erro de rede
    | 'unexpected';     // 500 ou erro desconhecido

export interface NormalizedError {
    kind: ErrorKind;
    message: string;
    code?: string;
    status?: number;
    details?: Record<string, unknown>;
    subErrors?: Array<{ message?: string; field?: string; }>;
    traceId?: string;
    stackTrace?: string;
    originalError?: unknown;
}

export function normalizeError(err: unknown): NormalizedError {
    // Erro de rede (sem response)
    if (isAxiosError(err) && !err.response) {
        return {
            kind: 'network',
            message: 'Não foi possível conectar ao servidor. Verifique sua conexão.',
            stackTrace: obterStack(err),
            originalError: err
        };
    }

    // Erro HTTP com resposta da API
    if (isAxiosError(err) && err.response) {
        const {status, data} = err.response;
        const payload = asPayload(data);

        return {
            kind: mapStatusToKind(status),
            message: payload?.message || `Erro ${status}: O servidor não retornou uma mensagem detalhada.`,
            code: payload?.code,
            status: status,
            details: payload?.details,
            subErrors: payload?.subErrors,
            traceId: payload?.traceId,
            stackTrace: payload?.stackTrace || obterStack(err),
            originalError: err
        };
    }

    // Erro genérico (Error, string, etc.)
    if (err instanceof Error) {
        return {
            kind: 'unexpected',
            message: err.message || 'Erro inesperado.',
            stackTrace: err.stack,
            originalError: err
        };
    }

    logger.error("[normalizeError] Erro não mapeado:", err);
    return {
        kind: 'unexpected',
        message: 'Erro desconhecido ou não mapeado pela aplicação.',
        stackTrace: obterStack(err) || String(err),
        originalError: err
    };
}

function mapStatusToKind(status: number): ErrorKind {
    if (status === 400 || status === 422) return 'validation';
    if (status === 401) return 'unauthorized';
    if (status === 403) return 'forbidden';
    if (status === 404) return 'notFound';
    if (status === 409) return 'conflict';
    if (status >= 500) return 'unexpected';
    return 'unexpected';
}

export function isAxiosError(error: unknown): error is import('axios').AxiosError {
    return (error !== null &&
        typeof error === 'object' &&
        'isAxiosError' in error && (error as ErrorLike).isAxiosError);
}

/**
 * Decide se o erro deve ser notificado globalmente ou tratado inline.
 * Erros inline: validação, notFound, conflict (contexto de formulário)
 * Erros globais: unauthorized, network, unexpected
 */
export function shouldNotifyGlobally(normalized: NormalizedError): boolean {
    return ['unauthorized', 'network', 'unexpected'].includes(normalized.kind);
}

/**
 * Executa uma chamada de API e retorna true se sucesso, false se 404.
 * Outros erros são propagados.
 */
export async function existsOrFalse<T>(
    apiCall: () => Promise<T>
): Promise<boolean> {
    try {
        await apiCall();
        return true;
    } catch (error) {
        const normalized = normalizeError(error);
        if (normalized.kind === 'notFound') {
            return false;
        }
        throw error;
    }
}

/**
 * Executa uma chamada de API e retorna null se 404.
 * Outros erros são propagados.
 */
export async function getOrNull<T>(
    apiCall: () => Promise<T>
): Promise<T | null> {
    try {
        return await apiCall();
    } catch (error) {
        const normalized = normalizeError(error);
        if (normalized.kind === 'notFound') {
            return null;
        }
        throw error;
    }
}
