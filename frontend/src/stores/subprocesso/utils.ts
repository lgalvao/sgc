import type {ChaveCarregamento} from "./tipos";
import {type ErroNormalizado, normalizarErro} from "@/utils/apiError";
import {logger} from "@/utils";

export function gerarChave(tipo: ChaveCarregamento, identificador: number | string): string {
    return `${tipo}:${identificador}`;
}

export function gerarChaveProcessoUnidade(codigoProcesso: number, siglaUnidade: string): string {
    return `${codigoProcesso}:${siglaUnidade}`;
}

export function criarErroSubprocessoNaoEncontrado(mensagemBase: string, erroNormalizado: ErroNormalizado): ErroNormalizado {
    return {
        ...erroNormalizado,
        tipo: "inesperado",
        mensagem: `${mensagemBase} Isso indica inconsistência interna ou tentativa inválida de acesso.`,
        codigo: erroNormalizado.codigo ?? "SUBPROCESSO_NAO_ENCONTRADO_INESPERADO",
    };
}

export function registrarErroIntegracao(
    erro: unknown,
    mensagemBase: string,
    erroIntegracaoContexto: import('vue').Ref<ErroNormalizado | null>
): null {
    const erroNormalizado = normalizarErro(erro);
    if (erroNormalizado.codigo === "REQUEST_CANCELADA") {
        erroIntegracaoContexto.value = erroNormalizado;
        return null;
    }

    logger.error(mensagemBase, erro);
    erroIntegracaoContexto.value = erroNormalizado.tipo === "naoEncontrado"
        ? criarErroSubprocessoNaoEncontrado(mensagemBase, erroNormalizado)
        : erroNormalizado;
    return null;
}
