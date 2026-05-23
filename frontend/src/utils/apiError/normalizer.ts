import logger from '@/utils/logger';
import {ehErroAxios} from "./classification";
import type {ErroNormalizado, ErroSimples, PayloadErroApi, TipoErro} from "./types";
import type {AxiosError} from "axios";

const TIPO_REDE: TipoErro = "rede";
const TIPO_INESPERADO: TipoErro = "inesperado";

function obterStack(e: unknown): string | undefined {
    return typeof e === "object" && e !== null && "stack" in e ? (e as ErroSimples).stack : undefined;
}

function comoPayload(d: unknown): PayloadErroApi {
    return typeof d === "object" && d !== null ? d as PayloadErroApi : {};
}

function mapearStatusParaTipo(s: number): TipoErro {
    const tipos: Record<number, TipoErro> = {
        400: 'validacao',
        422: 'validacao',
        401: 'naoAutorizado',
        403: 'proibido',
        404: 'naoEncontrado',
        409: 'conflito'
    };
    return tipos[s] || 'inesperado';
}

function criarErroNormalizado(
    erroOriginal: unknown,
    dados: Omit<ErroNormalizado, "erroOriginal">,
): ErroNormalizado {
    return {
        ...dados,
        erroOriginal,
    };
}

function normalizarErroCancelado(erro: AxiosError): ErroNormalizado {
    return criarErroNormalizado(erro, {
        tipo: TIPO_REDE,
        mensagem: "Requisição cancelada.",
        codigo: "REQUEST_CANCELADA",
        stackTrace: obterStack(erro),
    });
}

function normalizarErroSemResposta(erro: AxiosError): ErroNormalizado {
    return criarErroNormalizado(erro, {
        tipo: TIPO_REDE,
        mensagem: "Não foi possível conectar ao servidor. Verifique sua conexão.",
        stackTrace: obterStack(erro),
    });
}

function normalizarErroComResposta(erro: AxiosError): ErroNormalizado {
    const {status, data} = erro.response as NonNullable<AxiosError["response"]>;
    const payload = comoPayload(data);

    return criarErroNormalizado(erro, {
        tipo: mapearStatusParaTipo(status),
        mensagem: payload.message || `Erro ${status}: O servidor não retornou uma mensagem detalhada.`,
        codigo: payload.code,
        status,
        detalhes: payload.details,
        erros: payload.erros,
        traceId: payload.traceId,
        stackTrace: payload.stackTrace || obterStack(erro),
    });
}

function normalizarErroAxios(erro: AxiosError): ErroNormalizado {
    if (erro.code === 'ERR_CANCELED' || erro.name === 'CanceledError') {
        return normalizarErroCancelado(erro);
    }

    if (!erro.response) {
        return normalizarErroSemResposta(erro);
    }

    return normalizarErroComResposta(erro);
}

export function normalizarErro(erro: unknown): ErroNormalizado {
    if (ehErroAxios(erro)) {
        return normalizarErroAxios(erro);
    }

    if (erro instanceof Error) {
        return criarErroNormalizado(erro, {
            tipo: TIPO_INESPERADO,
            mensagem: erro.message || 'Erro inesperado.',
            stackTrace: erro.stack,
        });
    }

    logger.error("[normalizarErro] Erro não mapeado:", erro);
    return criarErroNormalizado(erro, {
        tipo: TIPO_INESPERADO,
        mensagem: 'Erro desconhecido ou não mapeado pela aplicação.',
        stackTrace: obterStack(erro) || String(erro),
    });
}

export function deveNotificarGlobalmente(normalizado: ErroNormalizado): boolean {
    return ['naoAutorizado', 'rede', 'inesperado'].includes(normalizado.tipo);
}
