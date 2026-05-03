import logger from '@/utils/logger';
import {ehErroAxios} from "./classification";
import type {PayloadErroApi, TipoErro, ErroSimples, ErroNormalizado} from "./types";

function obterStack(e: unknown): string | undefined {
    return typeof e === "object" && e !== null && "stack" in e ? (e as ErroSimples).stack : undefined;
}

function comoPayload(d: unknown): PayloadErroApi {
    return typeof d === "object" && d !== null ? d as PayloadErroApi : {};
}

function mapearStatusParaTipo(s: number): TipoErro {
    const tipos: Record<number, TipoErro> = {400: 'validacao', 422: 'validacao', 401: 'naoAutorizado', 403: 'proibido', 404: 'naoEncontrado', 409: 'conflito'};
    return tipos[s] || 'inesperado';
}

function normalizarErroAxios(erro: import('axios').AxiosError): ErroNormalizado {
    if (erro.code === 'ERR_CANCELED' || erro.name === 'CanceledError') {
        return {tipo: 'rede', mensagem: 'Requisição cancelada.', codigo: 'REQUEST_CANCELADA', stackTrace: obterStack(erro), erroOriginal: erro};
    }

    if (!erro.response) {
        return {tipo: 'rede', mensagem: 'Não foi possível conectar ao servidor. Verifique sua conexão.', stackTrace: obterStack(erro), erroOriginal: erro};
    }

    const {status, data} = erro.response;
    const p = comoPayload(data);

    return {
        tipo: mapearStatusParaTipo(status),
        mensagem: p?.message || `Erro ${status}: O servidor não retornou uma mensagem detalhada.`,
        codigo: p?.code,
        status,
        detalhes: p?.details,
        erros: p?.erros,
        traceId: p?.traceId,
        stackTrace: p?.stackTrace || obterStack(erro),
        erroOriginal: erro
    };
}

export function normalizarErro(erro: unknown): ErroNormalizado {
    if (ehErroAxios(erro)) {
        return normalizarErroAxios(erro);
    }

    if (erro instanceof Error) {
        return {
            tipo: 'inesperado',
            mensagem: erro.message || 'Erro inesperado.',
            stackTrace: erro.stack,
            erroOriginal: erro
        };
    }

    logger.error("[normalizarErro] Erro não mapeado:", erro);
    return {
        tipo: 'inesperado',
        mensagem: 'Erro desconhecido ou não mapeado pela aplicação.',
        stackTrace: obterStack(erro) || String(erro),
        erroOriginal: erro
    };
}

export function deveNotificarGlobalmente(normalizado: ErroNormalizado): boolean {
    return ['naoAutorizado', 'rede', 'inesperado'].includes(normalizado.tipo);
}
