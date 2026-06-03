import type {Ref} from "vue";
import type {ConfiguracaoContexto} from "./tipos";
import {type ContextoSubprocesso, dadosValidos} from "@/stores/subprocessoStoreHelpers";
import type {ErroNormalizado} from "@/utils/apiError";
import {gerarChave, gerarChaveProcessoUnidade, registrarErroIntegracao} from "./utils";

type EstadoOrquestrador = {
    carregamentos: Map<string, Promise<object | string | number | boolean | null>>;
    erroIntegracaoContexto: Ref<ErroNormalizado | null>;
    limparContextoAtual: () => void;
};

const toAny = (val: unknown) => {
    if (typeof val === 'string') {
        try {
            return JSON.parse(val);
        } catch {
            return val;
        }
    }
    return val;
};

function limparSeNecessario(limparAntes: boolean, limparContextoAtual: () => void) {
    if (limparAntes) {
        limparContextoAtual();
    }
}

async function executarComDedupe<T extends object | string | number | boolean | null>(
    carregamentos: Map<string, Promise<object | string | number | boolean | null>>,
    chave: string,
    acao: () => Promise<T>,
): Promise<T> {
    const existente = carregamentos.get(chave);
    if (existente) {
        return toAny(existente);
    }

    const promessa = acao().finally(() => carregamentos.delete(chave));
    carregamentos.set(chave, promessa);
    return promessa;
}

async function buscarComRegistro<T extends ContextoSubprocesso>(
    buscar: () => Promise<T>,
    registrar: (contexto: T) => void,
): Promise<T> {
    const contexto = await buscar();
    registrar(contexto);
    return contexto;
}

async function garantirContextoPorCodigo<T extends ContextoSubprocesso>(
    estado: EstadoOrquestrador,
    codigoSubprocesso: number,
    opcoes: {limparAntes: boolean; config: ConfiguracaoContexto<T>},
): Promise<T | null> {
    const {limparAntes, config} = opcoes;
    limparSeNecessario(limparAntes, estado.limparContextoAtual);
    if (dadosValidos(config.contextoRef, config.contextoInvalidoRef, codigoSubprocesso)) {
        return config.contextoRef.value;
    }

    try {
        return await executarComDedupe(
            estado.carregamentos,
            gerarChave(config.tipoCodigo, codigoSubprocesso),
            () => buscarComRegistro(() => config.buscarPorCodigo(codigoSubprocesso), config.registrar),
        );
    } catch (erro) {
        return registrarErroIntegracao(toAny(erro), config.mensagemCodigo(codigoSubprocesso), estado.erroIntegracaoContexto);
    }
}

async function garantirContextoPorProcessoEUnidade<T extends ContextoSubprocesso>(
    estado: EstadoOrquestrador,
    opcoes: {
        codProcesso: number;
        siglaUnidade: string;
        limparAntes: boolean;
        config: ConfiguracaoContexto<T>;
    },
): Promise<{codigo: number; contexto: T} | null> {
    const {codProcesso, siglaUnidade, limparAntes, config} = opcoes;
    limparSeNecessario(limparAntes, estado.limparContextoAtual);

    const chaveProcessoUnidade = gerarChaveProcessoUnidade(codProcesso, siglaUnidade);
    const codigoMapeado = config.codigosPorProcessoUnidade.get(chaveProcessoUnidade);
    if (typeof codigoMapeado === "number") {
        const contexto = await garantirContextoPorCodigo(estado, codigoMapeado, {limparAntes, config});
        if (contexto) {
            return {codigo: codigoMapeado, contexto};
        }
    }

    try {
        return await executarComDedupe(
            estado.carregamentos,
            gerarChave(config.tipoProcessoUnidade, chaveProcessoUnidade),
            async () => {
                const contexto = await buscarComRegistro(
                    () => config.buscarPorProcessoEUnidade(codProcesso, siglaUnidade),
                    config.registrar,
                );
                const codigo = contexto.detalhes.codigo;
                config.codigosPorProcessoUnidade.set(chaveProcessoUnidade, codigo);
                return {codigo, contexto};
            },
        );
    } catch (erro) {
        return registrarErroIntegracao(
            toAny(erro),
            config.mensagemProcessoUnidade(codProcesso, siglaUnidade),
            estado.erroIntegracaoContexto,
        );
    }
}

export function usarOrquestradorContexto(
    carregamentos: Map<string, Promise<object | string | number | boolean | null>>,
    erroIntegracaoContexto: Ref<ErroNormalizado | null>,
    limparContextoAtual: () => void
) {
    const estado = {
        carregamentos,
        erroIntegracaoContexto,
        limparContextoAtual
    };
    return {
        garantirContextoPorCodigo: <T extends ContextoSubprocesso>(codigoSubprocesso: number, limparAntes: boolean, config: ConfiguracaoContexto<T>) =>
            garantirContextoPorCodigo(estado, codigoSubprocesso, {limparAntes, config}),
        garantirContextoPorProcessoEUnidade: <T extends ContextoSubprocesso>(codProcesso: number, siglaUnidade: string, opcoes: {limparAntes: boolean; config: ConfiguracaoContexto<T>}) =>
            garantirContextoPorProcessoEUnidade(estado, {codProcesso, siglaUnidade, ...opcoes}),
    };
}
