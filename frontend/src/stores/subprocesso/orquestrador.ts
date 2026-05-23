import type {ConfiguracaoContexto} from "./tipos";
import {type ContextoSubprocesso, dadosValidos} from "@/stores/subprocessoStoreHelpers";
import {type ErroNormalizado} from "@/utils/apiError";
import {gerarChave, gerarChaveProcessoUnidade, registrarErroIntegracao} from "./utils";

export function usarOrquestradorContexto(
    carregamentos: Map<string, Promise<unknown>>,
    erroIntegracaoContexto: import('vue').Ref<ErroNormalizado | null>,
    limparContextoAtual: () => void
) {
    async function executarComDedupe<T>(chave: string, acao: () => Promise<T>): Promise<T> {
        const existente = carregamentos.get(chave);
        if (existente) return existente as Promise<T>;
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
            return registrarErroIntegracao(erro, config.mensagemCodigo(codigoSubprocesso), erroIntegracaoContexto);
        }
    }

    async function garantirContextoPorProcessoEUnidade<T extends ContextoSubprocesso>(
        codProcesso: number,
        siglaUnidade: string,
        opcoes: {
            limparAntes: boolean;
            config: ConfiguracaoContexto<T>;
        },
    ): Promise<{ codigo: number; contexto: T } | null> {
        const { limparAntes, config } = opcoes;
        if (limparAntes) limparContextoAtual();
        const chaveProcessoUnidade = gerarChaveProcessoUnidade(codProcesso, siglaUnidade);
        const codigoMapeado = config.codigosPorProcessoUnidade.get(chaveProcessoUnidade);
        if (typeof codigoMapeado === "number") {
            const contexto = await garantirContextoPorCodigo(codigoMapeado, limparAntes, config);
            if (contexto) return {codigo: codigoMapeado, contexto};
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
            return registrarErroIntegracao(erro, config.mensagemProcessoUnidade(codProcesso, siglaUnidade), erroIntegracaoContexto);
        }
    }

    return {garantirContextoPorCodigo, garantirContextoPorProcessoEUnidade};
}
