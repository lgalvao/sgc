import type {ConfiguracaoContexto, ChaveCarregamento} from "./tipos";
import {type ContextoSubprocesso, dadosValidos} from "@/stores/subprocessoStoreHelpers";
import {logger} from "@/utils";
import {type ErroNormalizado, normalizarErro} from "@/utils/apiError";

export function usarOrquestradorContexto(
    carregamentos: Map<string, Promise<unknown>>,
    erroIntegracaoContexto: import('vue').Ref<ErroNormalizado | null>,
    limparContextoAtual: () => void
) {
    function gerarChave(tipo: ChaveCarregamento, identificador: number | string): string {
        return `${tipo}:${identificador}`;
    }

    function gerarChaveProcessoUnidade(codigoProcesso: number, siglaUnidade: string): string {
        return `${codigoProcesso}:${siglaUnidade}`;
    }

    function criarErroSubprocessoNaoEncontrado(mensagemBase: string, erroNormalizado: ErroNormalizado): ErroNormalizado {
        return {
            ...erroNormalizado,
            tipo: "inesperado",
            mensagem: `${mensagemBase} Isso indica inconsistência interna ou tentativa inválida de acesso.`,
            codigo: erroNormalizado.codigo ?? "SUBPROCESSO_NAO_ENCONTRADO_INESPERADO",
        };
    }

    function registrarErroIntegracao(erro: unknown, mensagemBase: string): null {
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

    async function executarComDedupe<T>(chave: string, acao: () => Promise<T>): Promise<T> {
        const carregamentoExistente = carregamentos.get(chave);
        if (carregamentoExistente) {
            return carregamentoExistente as Promise<T>;
        }

        const promessa = acao().finally(() => carregamentos.delete(chave));
        carregamentos.set(chave, promessa);
        return promessa;
    }

    async function garantirContextoPorCodigo<T extends ContextoSubprocesso>(
        codigoSubprocesso: number,
        limparAntes: boolean,
        config: ConfiguracaoContexto<T>,
    ): Promise<T | null> {
        if (limparAntes) limparContextoAtual();
        if (dadosValidos(config.contextoRef, config.contextoInvalidoRef, codigoSubprocesso)) {
            return config.contextoRef.value;
        }

        try {
            const dados = await executarComDedupe(
                gerarChave(config.tipoCodigo, codigoSubprocesso),
                () => config.buscarPorCodigo(codigoSubprocesso),
            );
            config.registrar(dados);
            return dados;
        } catch (erro) {
            return registrarErroIntegracao(erro, config.mensagemCodigo(codigoSubprocesso));
        }
    }

    async function garantirContextoPorProcessoEUnidade<T extends ContextoSubprocesso>(
        codProcesso: number,
        siglaUnidade: string,
        limparAntes: boolean,
        config: ConfiguracaoContexto<T>,
    ): Promise<{ codigo: number; contexto: T } | null> {
        if (limparAntes) limparContextoAtual();
        const chaveProcessoUnidade = gerarChaveProcessoUnidade(codProcesso, siglaUnidade);
        const codigoMapeado = config.codigosPorProcessoUnidade.get(chaveProcessoUnidade);
        if (typeof codigoMapeado === "number") {
            const contexto = await garantirContextoPorCodigo(codigoMapeado, limparAntes, config);
            if (contexto) {
                return {codigo: codigoMapeado, contexto};
            }
        }

        try {
            return await executarComDedupe(
                gerarChave(config.tipoProcessoUnidade, chaveProcessoUnidade),
                async () => {
                    const contexto = await config.buscarPorProcessoEUnidade(codProcesso, siglaUnidade);
                    const codigo = contexto.detalhes.codigo;
                    config.codigosPorProcessoUnidade.set(chaveProcessoUnidade, codigo);
                    config.registrar(contexto);
                    return {codigo, contexto};
                },
            );
        } catch (erro) {
            return registrarErroIntegracao(erro, config.mensagemProcessoUnidade(codProcesso, siglaUnidade));
        }
    }

    return {
        garantirContextoPorCodigo,
        garantirContextoPorProcessoEUnidade,
    };
}
