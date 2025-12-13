import { useFeedbackStore } from '@/stores/feedback';

/**
 * Payload de erro retornado pela API (backend)
 */
export interface ApiErrorPayload {
  timestamp?: string;
  status?: number;
  message?: string;
  code?: string;
  details?: Record<string, any>;
  traceId?: string;
  subErrors?: Array<{
    object?: string;
    field?: string;
    rejectedValue?: any;
    message?: string;
  }>;
}

/**
 * Categorias de erro para decisão de UX
 */
export type ErrorKind =
  | 'validation'      // 400, 422 - erro de validação de dados
  | 'notFound'        // 404 - recurso não encontrado
  | 'conflict'        // 409 - conflito de estado
  | 'unauthorized'    // 401 - não autenticado
  | 'forbidden'       // 403 - sem permissão
  | 'network'         // Erro de rede
  | 'unexpected';     // 500 ou erro desconhecido

/**
 * Erro normalizado para consumo pelo frontend
 */
export interface NormalizedError {
  kind: ErrorKind;
  message: string;
  code?: string;
  status?: number;
  details?: Record<string, any>;
  subErrors?: Array<{ message?: string; field?: string; }>;
  traceId?: string;
  originalError?: unknown;
}

/**
 * Normaliza um erro (axios, Error, ou unknown) em uma estrutura previsível
 */
export function normalizeError(err: unknown): NormalizedError {
  // Erro de rede (sem response)
  if (isAxiosError(err) && !err.response) {
    return {
      kind: 'network',
      message: 'Não foi possível conectar ao servidor. Verifique sua conexão.',
      originalError: err
    };
  }

  // Erro HTTP com resposta da API
  if (isAxiosError(err) && err.response) {
    const { status, data } = err.response;
    // data can be unknown, cast to ApiErrorPayload if it matches
    const payload = (data || {}) as ApiErrorPayload;

    return {
      kind: mapStatusToKind(status),
      message: payload?.message || 'Erro desconhecido.',
      code: payload?.code,
      status: status,
      details: payload?.details,
      subErrors: payload?.subErrors,
      traceId: payload?.traceId,
      originalError: err
    };
  }

  // Erro genérico (Error, string, etc.)
  if (err instanceof Error) {
    return {
      kind: 'unexpected',
      message: err.message || 'Erro inesperado.',
      originalError: err
    };
  }

  // Fallback
  return {
    kind: 'unexpected',
    message: 'Erro desconhecido.',
    originalError: err
  };
}

/**
 * Mapeia status HTTP para categoria de erro
 */
function mapStatusToKind(status: number): ErrorKind {
  if (status === 400 || status === 422) return 'validation';
  if (status === 401) return 'unauthorized';
  if (status === 403) return 'forbidden';
  if (status === 404) return 'notFound';
  if (status === 409) return 'conflict';
  if (status >= 500) return 'unexpected';
  return 'unexpected';
}

/**
 * Type guard para AxiosError
 */
function isAxiosError(error: unknown): error is import('axios').AxiosError {
  return (
    error !== null &&
    typeof error === 'object' &&
    'isAxiosError' in error &&
    (error as any).isAxiosError === true
  );
}

/**
 * Exibe notificação de erro global (toast) baseado no tipo de erro.
 * Use para erros que devem ser mostrados globalmente (não inline).
 */
export function notifyError(normalized: NormalizedError): void {
  const feedbackStore = useFeedbackStore();

  // Títulos padrão por tipo
  const titles: Record<ErrorKind, string> = {
    validation: 'Erro de Validação',
    notFound: 'Não Encontrado',
    conflict: 'Conflito',
    unauthorized: 'Não Autorizado',
    forbidden: 'Acesso Negado',
    network: 'Erro de Rede',
    unexpected: 'Erro Inesperado'
  };

  const title = titles[normalized.kind];
  feedbackStore.show(title, normalized.message, 'danger');
}

/**
 * Decide se o erro deve ser notificado globalmente ou tratado inline.
 * Erros inline: validação, notFound, conflict (contexto de formulário)
 * Erros globais: unauthorized, network, unexpected
 */
export function shouldNotifyGlobally(normalized: NormalizedError): boolean {
  return ['unauthorized', 'forbidden', 'network', 'unexpected'].includes(normalized.kind);
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
