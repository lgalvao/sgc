import {defineStore} from "pinia";
import {ref, type Ref} from "vue";
import type {ContextoCadastroAtividadesSubprocesso, ContextoEdicaoSubprocesso,} from "@/types/tipos";
import {
    buscarContextoCadastroAtividades as serviceBuscarContextoCadastroAtividades,
    buscarContextoCadastroAtividadesPorProcessoEUnidade as serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade,
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarContextoEdicaoPorProcessoEUnidade as serviceBuscarContextoEdicaoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import {
    type AtualizacaoStatusLocal,
    atualizarDetalhesContexto,
    type ContextoSubprocesso,
    dadosValidos,
    registrarContexto
} from "@/stores/subprocessoStoreHelpers";
import {logger} from "@/utils";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

type ChaveCarregamento =
    | "EDICAO_CODIGO"
    | "EDICAO_PROCESSO_UNIDADE"
    | "CADASTRO_CODIGO"
    | "CADASTRO_PROCESSO_UNIDADE";

type ConfigContexto<T extends ContextoSubprocesso> = {
    tipoCodigo: ChaveCarregamento;
    tipoProcessoUnidade: ChaveCarregamento;
    contextoRef: Ref<T | null>;
    contextoInvalidoRef: Ref<boolean>;
    codigosPorProcessoUnidade: Map<string, number>;
    buscarPorCodigo: (codigoSubprocesso: number) => Promise<T>;
    buscarPorProcessoEUnidade: (codProcesso: number, siglaUnidade: string) => Promise<T>;
    registrar: (contexto: T) => void;
    mensagemCodigo: (codigoSubprocesso: number) => string;
    mensagemProcessoUnidade: (codProcesso: number, siglaUnidade: string) => string;
};

export const useSubprocessoStore = defineStore("subprocesso", () => {
    const contextoEdicao = ref<ContextoEdicaoSubprocesso | null>(null);
    const contextoCadastro = ref<ContextoCadastroAtividadesSubprocesso | null>(null);
    const contextoEdicaoInvalido = ref(false);
    const contextoCadastroInvalido = ref(false);
    const erroIntegracaoContexto = ref<NormalizedError | null>(null);
    const carregamentos = new Map<string, Promise<unknown>>();
    const codigosEdicaoPorProcessoUnidade = new Map<string, number>();
    const codigosCadastroPorProcessoUnidade = new Map<string, number>();

    function gerarChave(tipo: ChaveCarregamento, identificador: number | string): string {
        return `${tipo}:${identificador}`;
    }

    function gerarChaveProcessoUnidade(codigoProcesso: number, siglaUnidade: string): string {
        return `${codigoProcesso}:${siglaUnidade}`;
    }

    function limparContextoAtual(): void {
        contextoEdicao.value = null;
        contextoCadastro.value = null;
        contextoEdicaoInvalido.value = false;
        contextoCadastroInvalido.value = false;
    }

    function limparErroIntegracao(): void {
        erroIntegracaoContexto.value = null;
    }

    function invalidar(): void {
        carregamentos.clear();
        contextoEdicaoInvalido.value = contextoEdicao.value !== null;
        contextoCadastroInvalido.value = contextoCadastro.value !== null;
        limparErroIntegracao();
    }

    function resetar(): void {
        carregamentos.clear();
        codigosEdicaoPorProcessoUnidade.clear();
        codigosCadastroPorProcessoUnidade.clear();
        limparErroIntegracao();
        limparContextoAtual();
    }

    function registrarContextoEdicao(contexto: ContextoEdicaoSubprocesso): void {
        registrarContexto(contextoEdicao, contextoEdicaoInvalido, contexto, limparErroIntegracao);
    }

    function registrarContextoCadastro(contexto: ContextoCadastroAtividadesSubprocesso): void {
        registrarContexto(contextoCadastro, contextoCadastroInvalido, contexto, limparErroIntegracao);
    }

    function criarErroSubprocessoNaoEncontrado(mensagemBase: string, erroNormalizado: NormalizedError): NormalizedError {
        return {
            ...erroNormalizado,
            kind: "unexpected",
            message: `${mensagemBase} Isso indica inconsistência interna ou tentativa inválida de acesso.`,
            code: erroNormalizado.code ?? "SUBPROCESSO_NAO_ENCONTRADO_INESPERADO",
        };
    }

    function registrarErroIntegracao(erro: unknown, mensagemBase: string): null {
        const erroNormalizado = normalizeError(erro);
        if (erroNormalizado.code === "REQUEST_CANCELADA") {
            erroIntegracaoContexto.value = erroNormalizado;
            return null;
        }

        logger.error(mensagemBase, erro);
        erroIntegracaoContexto.value = erroNormalizado.kind === "notFound"
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
        config: ConfigContexto<T>,
    ): Promise<T | null> {
        if (limparAntes) limparContextoAtual();
        if (dadosValidos(config.contextoRef, config.contextoInvalidoRef, codigoSubprocesso)) {
            return config.contextoRef.value;
        }

        try {
            const data = await executarComDedupe(
                gerarChave(config.tipoCodigo, codigoSubprocesso),
                () => config.buscarPorCodigo(codigoSubprocesso),
            );
            config.registrar(data);
            return data;
        } catch (erro) {
            return registrarErroIntegracao(erro, config.mensagemCodigo(codigoSubprocesso));
        }
    }

    async function garantirContextoPorProcessoEUnidade<T extends ContextoSubprocesso>(
        codProcesso: number,
        siglaUnidade: string,
        limparAntes: boolean,
        config: ConfigContexto<T>,
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

    const configContextoEdicao: ConfigContexto<ContextoEdicaoSubprocesso> = {
        tipoCodigo: "EDICAO_CODIGO",
        tipoProcessoUnidade: "EDICAO_PROCESSO_UNIDADE",
        contextoRef: contextoEdicao,
        contextoInvalidoRef: contextoEdicaoInvalido,
        codigosPorProcessoUnidade: codigosEdicaoPorProcessoUnidade,
        buscarPorCodigo: serviceBuscarContextoEdicao,
        buscarPorProcessoEUnidade: serviceBuscarContextoEdicaoPorProcessoEUnidade,
        registrar: registrarContextoEdicao,
        mensagemCodigo: (codigoSubprocesso) => `Falha grave ao localizar o subprocesso ${codigoSubprocesso}.`,
        mensagemProcessoUnidade: (codProcesso, siglaUnidade) =>
            `Falha grave ao resolver subprocesso do processo ${codProcesso} para a unidade ${siglaUnidade}.`,
    };

    const configContextoCadastro: ConfigContexto<ContextoCadastroAtividadesSubprocesso> = {
        tipoCodigo: "CADASTRO_CODIGO",
        tipoProcessoUnidade: "CADASTRO_PROCESSO_UNIDADE",
        contextoRef: contextoCadastro,
        contextoInvalidoRef: contextoCadastroInvalido,
        codigosPorProcessoUnidade: codigosCadastroPorProcessoUnidade,
        buscarPorCodigo: serviceBuscarContextoCadastroAtividades,
        buscarPorProcessoEUnidade: serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade,
        registrar: registrarContextoCadastro,
        mensagemCodigo: (codigoSubprocesso) => `Falha grave ao carregar o cadastro do subprocesso ${codigoSubprocesso}.`,
        mensagemProcessoUnidade: (codProcesso, siglaUnidade) =>
            `Falha grave ao resolver o cadastro do processo ${codProcesso} para a unidade ${siglaUnidade}.`,
    };

    async function garantirContextoEdicao(codigoSubprocesso: number, limparAntes = false): Promise<ContextoEdicaoSubprocesso | null> {
        return garantirContextoPorCodigo(codigoSubprocesso, limparAntes, configContextoEdicao);
    }

    async function garantirContextoEdicaoPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
        limparAntes = false
    ): Promise<{ codigo: number; contexto: ContextoEdicaoSubprocesso } | null> {
        return garantirContextoPorProcessoEUnidade(codProcesso, siglaUnidade, limparAntes, configContextoEdicao);
    }

    async function garantirContextoCadastroAtividades(
        codigoSubprocesso: number,
        limparAntes = false,
    ): Promise<ContextoCadastroAtividadesSubprocesso | null> {
        return garantirContextoPorCodigo(codigoSubprocesso, limparAntes, configContextoCadastro);
    }

    async function garantirContextoCadastroAtividadesPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
        limparAntes = false,
    ): Promise<ContextoCadastroAtividadesSubprocesso | null> {
        const resultado = await garantirContextoPorProcessoEUnidade(codProcesso, siglaUnidade, limparAntes, configContextoCadastro);
        return resultado?.contexto ?? null;
    }

    function atualizarStatusLocal(status: AtualizacaoStatusLocal) {
        atualizarDetalhesContexto(contextoEdicao, contextoEdicaoInvalido, status);
        atualizarDetalhesContexto(contextoCadastro, contextoCadastroInvalido, status);
    }

    return {
        contextoEdicao,
        contextoCadastro,
        erroIntegracaoContexto,
        invalidar,
        resetar,
        limparContextoAtual,
        limparErroIntegracao,
        garantirContextoEdicao,
        garantirContextoEdicaoPorProcessoEUnidade,
        garantirContextoCadastroAtividades,
        garantirContextoCadastroAtividadesPorProcessoEUnidade,
        atualizarStatusLocal,
    };
});
