import {defineStore} from "pinia";
import {ref} from "vue";
import type {ContextoEdicaoSubprocesso} from "@/types/tipos";
import {
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarContextoEdicaoPorProcessoEUnidade as serviceBuscarContextoEdicaoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import {logger} from "@/utils";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

export const useSubprocessoStore = defineStore("subprocesso", () => {
    const contextoEdicao = ref<ContextoEdicaoSubprocesso | null>(null);
    const erroIntegracaoContexto = ref<NormalizedError | null>(null);
    const codSubprocessoCarregado = ref<number | null>(null);
    const carregamentosPorCodigo = new Map<number, Promise<ContextoEdicaoSubprocesso>>();
    const carregamentosPorProcessoUnidade = new Map<string, Promise<{ codigo: number; contexto: ContextoEdicaoSubprocesso }>>();
    const mapaProcessoUnidadeParaCodigo = new Map<string, number>();

    function gerarChaveProcessoUnidade(codProcesso: number, siglaUnidade: string): string {
        return `${codProcesso}:${siglaUnidade}`;
    }

    function invalidar(): void {
        carregamentosPorCodigo.clear();
        carregamentosPorProcessoUnidade.clear();
        mapaProcessoUnidadeParaCodigo.clear();
        erroIntegracaoContexto.value = null;
        contextoEdicao.value = null;
    }

    function _registrarContexto(codigo: number, contexto: ContextoEdicaoSubprocesso): void {
        contextoEdicao.value = contexto;
        erroIntegracaoContexto.value = null;
        codSubprocessoCarregado.value = codigo;
    }

    function criarErroSubprocessoNaoEncontrado(mensagemBase: string, erroNormalizado: NormalizedError): NormalizedError {
        return {
            ...erroNormalizado,
            kind: "unexpected",
            message: `${mensagemBase} Isso indica inconsistência interna ou tentativa inválida de acesso.`,
            code: erroNormalizado.code ?? "SUBPROCESSO_NAO_ENCONTRADO_INESPERADO",
        };
    }

    async function garantirContextoEdicao(codSubprocesso: number, limparAntes = false): Promise<ContextoEdicaoSubprocesso | null> {
        if (limparAntes) {
            contextoEdicao.value = null;
        }

        const carregamentoExistente = carregamentosPorCodigo.get(codSubprocesso);
        if (carregamentoExistente) {
            return carregamentoExistente;
        }

        try {
            const promessaCarregamento = serviceBuscarContextoEdicao(codSubprocesso);
            carregamentosPorCodigo.set(codSubprocesso, promessaCarregamento);
            const data = await promessaCarregamento;
            _registrarContexto(codSubprocesso, data);
            return data;
        } catch (err) {
            const erroNormalizado = normalizeError(err);
            if (erroNormalizado.code === "REQUEST_CANCELADA") {
                erroIntegracaoContexto.value = { ...erroNormalizado, code: "REQUEST_CANCELADA" };
                return null;
            }
            
            logger.error(`Erro ao buscar contexto do subprocesso ${codSubprocesso}:`, err);
            
            if (erroNormalizado.kind === "notFound") {
                erroIntegracaoContexto.value = criarErroSubprocessoNaoEncontrado(
                        "Falha grave ao localizar o subprocesso solicitado.",
                        erroNormalizado
                );
            } else {
                erroIntegracaoContexto.value = erroNormalizado;
            }
            return null;
        } finally {
            carregamentosPorCodigo.delete(codSubprocesso);
        }
    }

    async function garantirContextoEdicaoPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
        limparAntes = false
    ): Promise<{ codigo: number; contexto: ContextoEdicaoSubprocesso } | null> {
        if (limparAntes) {
            contextoEdicao.value = null;
        }

        const chaveProcessoUnidade = gerarChaveProcessoUnidade(codProcesso, siglaUnidade);
        const codigoMapeado = mapaProcessoUnidadeParaCodigo.get(chaveProcessoUnidade);
        if (typeof codigoMapeado === "number") {
            const contexto = await garantirContextoEdicao(codigoMapeado, limparAntes);
            if (contexto) {
                return {codigo: codigoMapeado, contexto};
            }
        }

        const carregamentoExistente = carregamentosPorProcessoUnidade.get(chaveProcessoUnidade);
        if (carregamentoExistente) {
            return carregamentoExistente;
        }

        try {
            const promessaCarregamento = (async () => {
                const data = await serviceBuscarContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade);
                const codigo = data.detalhes.codigo;
                mapaProcessoUnidadeParaCodigo.set(chaveProcessoUnidade, codigo);
                _registrarContexto(codigo, data);
                return {codigo, contexto: data};
            })();
            carregamentosPorProcessoUnidade.set(chaveProcessoUnidade, promessaCarregamento);
            return await promessaCarregamento;
        } catch (err) {
            const erroNormalizado = normalizeError(err);
            if (erroNormalizado.code === "REQUEST_CANCELADA") {
                erroIntegracaoContexto.value = { ...erroNormalizado, code: "REQUEST_CANCELADA" };
                return null;
            }
            
            logger.error(`Erro ao buscar contexto por processo e unidade:`, err);
            
            if (erroNormalizado.kind === "notFound") {
                erroIntegracaoContexto.value = criarErroSubprocessoNaoEncontrado(
                        "Falha grave ao resolver subprocesso por processo e unidade.",
                        erroNormalizado
                );
            } else {
                erroIntegracaoContexto.value = erroNormalizado;
            }
            return null;
        } finally {
            carregamentosPorProcessoUnidade.delete(chaveProcessoUnidade);
        }
    }

    return {
        contextoEdicao,
        erroIntegracaoContexto,
        codSubprocessoCarregado,
        invalidar,
        garantirContextoEdicao,
        garantirContextoEdicaoPorProcessoEUnidade,
    };
});
