export interface PayloadErroApi {
    timestamp?: string;
    status?: number;
    message?: string;
    code?: string;
    details?: Record<string, string | number | boolean | object | null>;
    traceId?: string;
    stackTrace?: string;
    erros?: Array<{
        objeto?: string;
        campo?: string;
        mensagem?: string;
    }>;
}

export type TipoErro =
    | 'validacao'       // 400, 422 - erro de validação de dados
    | 'naoEncontrado'   // 404 - recurso não encontrado
    | 'conflito'        // 409 - conflito de estado
    | 'naoAutorizado'   // 401 - não autenticado
    | 'proibido'        // 403 - sem permissão
    | 'rede'            // Erro de rede
    | 'inesperado';     // 500 ou erro desconhecido

export interface ErroNormalizado {
    tipo: TipoErro;
    mensagem: string;
    codigo?: string;
    status?: number;
    detalhes?: Record<string, string | number | boolean | object | null>;
    erros?: Array<{ mensagem?: string; campo?: string; }>;
    traceId?: string;
    stackTrace?: string;
    erroOriginal?: Error | object | string | null;
}

export type ErroSimples = {
    stack?: string;
    isAxiosError?: boolean;
    code?: string;
    name?: string;
};
