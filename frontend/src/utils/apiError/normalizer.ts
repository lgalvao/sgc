import logger from '@/utils/logger';
import {ehErroAxios} from "./classification";
import type {PayloadErroApi, TipoErro, ErroSimples, ErroNormalizado} from "./types";

function obterStack(erro: unknown): string | undefined {
    return typeof erro === "object" && erro !== null && "stack" in erro
        ? (erro as ErroSimples).stack
        : undefined;
}

function comoPayload(dados: unknown): PayloadErroApi {
    return typeof dados === "object" && dados !== null ? dados as PayloadErroApi : {};
}

function mapearStatusParaTipo(status: number): TipoErro {
    if (status === 400 || status === 422) return 'validacao';
    if (status === 401) return 'naoAutorizado';
    if (status === 403) return 'proibido';
    if (status === 404) return 'naoEncontrado';
    if (status === 409) return 'conflito';
    return 'inesperado';
}

export function normalizarErro(erro: unknown): ErroNormalizado {
    if (ehErroAxios(erro) && ((erro.code === 'ERR_CANCELED') || erro.name === 'CanceledError')) {
        return {
            tipo: 'rede',
            mensagem: 'Requisição cancelada.',
            codigo: 'REQUEST_CANCELADA',
            stackTrace: obterStack(erro),
            erroOriginal: erro
        };
    }

    if (ehErroAxios(erro) && !erro.response) {
        return {
            tipo: 'rede',
            mensagem: 'Não foi possível conectar ao servidor. Verifique sua conexão.',
            stackTrace: obterStack(erro),
            erroOriginal: erro
        };
    }

    if (ehErroAxios(erro) && erro.response) {
        const {status, data} = erro.response;
        const payload = comoPayload(data);

        return {
            tipo: mapearStatusParaTipo(status),
            mensagem: payload?.message || `Erro ${status}: O servidor não retornou uma mensagem detalhada.`,
            codigo: payload?.code,
            status: status,
            detalhes: payload?.details,
            erros: payload?.erros,
            traceId: payload?.traceId,
            stackTrace: payload?.stackTrace || obterStack(erro),
            erroOriginal: erro
        };
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
