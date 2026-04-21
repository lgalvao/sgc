import {defineStore} from "pinia";
import {ref} from "vue";
import type {ContextoEdicaoSubprocesso} from "@/types/tipos";
import {
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarContextoEdicaoPorProcessoEUnidade as serviceBuscarContextoEdicaoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import {logger} from "@/utils";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

/**
 * Dedupe de sessão para contexto de edição de subprocesso.
 *
 * O contexto de edição carrega situação, permissões e ações disponíveis no momento.
 * Como esses dados mudam ao longo do workflow, não é seguro reutilizar snapshots
 * antigos entre navegações. Mantemos apenas deduplicação de requisições concorrentes,
 * além do mapeamento processo+unidade -> código do subprocesso.
 *
 * Estratégia: nunca considerar o contexto "válido" para reuso entre ativações.
 */
export const useSubprocessoStore = defineStore("subprocesso", () => {
    const contextoEdicao = ref<ContextoEdicaoSubprocesso | null>(null);
    const erroIntegracaoContexto = ref<NormalizedError | null>(null);
    const codSubprocessoCarregado = ref<number | null>(null);
    const invalido = ref(true);
    const carregamentosPorCodigo = new Map<number, Promise<ContextoEdicaoSubprocesso>>();
    const carregamentosPorProcessoUnidade = new Map<string, Promise<{ codigo: number; contexto: ContextoEdicaoSubprocesso }>>();
    const mapaProcessoUnidadeParaCodigo = new Map<string, number>();

    function gerarChaveProcessoUnidade(codProcesso: number, siglaUnidade: string): string {
        return `${codProcesso}:${siglaUnidade}`;
    }

    function criarErroSubprocessoNaoEncontrado(mensagemBase: string, erroNormalizado: NormalizedError): NormalizedError {
        return {
            ...erroNormalizado,
            kind: "unexpected",
            message: `${mensagemBase} Isso indica inconsistência interna ou tentativa inválida de acesso.`,
            code: erroNormalizado.code ?? "SUBPROCESSO_NAO_ENCONTRADO_INESPERADO",
        };
    }

    /** Contexto ainda é válido para o subprocesso dado? */
    function dadosValidos(_: number): boolean {
        return false;
    }

    /** Invalida o cache — deve ser chamado após qualquer ação de workflow. */
    function invalidar(): void {
        invalido.value = true;
        carregamentosPorCodigo.clear();
        carregamentosPorProcessoUnidade.clear();
        mapaProcessoUnidadeParaCodigo.clear();
        erroIntegracaoContexto.value = null;
    }

    /**
     * Retorna o contexto de edição do subprocesso, usando cache quando disponível.
     * @returns o contexto (potencialmente do cache) ou null em caso de erro
     */
    async function garantirContextoEdicao(codSubprocesso: number): Promise<ContextoEdicaoSubprocesso | null> {
        if (dadosValidos(codSubprocesso)) {
            return contextoEdicao.value;
        }

        const carregamentoExistente = carregamentosPorCodigo.get(codSubprocesso);
        if (carregamentoExistente) {
            return carregamentoExistente;
        }

        try {
            const promessaCarregamento = serviceBuscarContextoEdicao(codSubprocesso);
            carregamentosPorCodigo.set(codSubprocesso, promessaCarregamento);
            const data = await promessaCarregamento;
            contextoEdicao.value = data;
            erroIntegracaoContexto.value = null;
            codSubprocessoCarregado.value = codSubprocesso;
            invalido.value = true;
            return data;
        } catch (err) {
            const erroNormalizado = normalizeError(err);
            logger.error(`Erro ao buscar contexto de edição do subprocesso ${codSubprocesso}:`, err);
            if (erroNormalizado.kind === "notFound") {
                erroIntegracaoContexto.value = criarErroSubprocessoNaoEncontrado(
                        "Falha grave ao localizar o subprocesso solicitado.",
                        erroNormalizado
                );
                return null;
            }
            erroIntegracaoContexto.value = erroNormalizado;
            return null;
        } finally {
            carregamentosPorCodigo.delete(codSubprocesso);
        }
    }

    /**
     * Resolve o código de subprocesso por processo+unidade e retorna seu contexto,
     * usando cache quando disponível.
     */
    async function garantirContextoEdicaoPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string
    ): Promise<{ codigo: number; contexto: ContextoEdicaoSubprocesso } | null> {
        const chaveProcessoUnidade = gerarChaveProcessoUnidade(codProcesso, siglaUnidade);

        const codigoMapeado = mapaProcessoUnidadeParaCodigo.get(chaveProcessoUnidade);
        if (typeof codigoMapeado === "number") {
            const contexto = await garantirContextoEdicao(codigoMapeado);
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
                contextoEdicao.value = data;
                erroIntegracaoContexto.value = null;
                codSubprocessoCarregado.value = codigo;
                invalido.value = true;
                return {codigo, contexto: data};
            })();
            carregamentosPorProcessoUnidade.set(chaveProcessoUnidade, promessaCarregamento);
            const {codigo, contexto} = await promessaCarregamento;
            contextoEdicao.value = contexto;
            codSubprocessoCarregado.value = codigo;
            invalido.value = true;
            return {codigo, contexto};
        } catch (err) {
            const erroNormalizado = normalizeError(err);
            logger.error(`Erro ao buscar contexto de subprocesso para processo ${codProcesso} unidade ${siglaUnidade}:`, err);
            if (erroNormalizado.kind === "notFound") {
                erroIntegracaoContexto.value = criarErroSubprocessoNaoEncontrado(
                        "Falha grave ao resolver subprocesso por processo e unidade.",
                        erroNormalizado
                );
                return null;
            }
            erroIntegracaoContexto.value = erroNormalizado;
            return null;
        } finally {
            carregamentosPorProcessoUnidade.delete(chaveProcessoUnidade);
        }
    }

    return {
        contextoEdicao,
        erroIntegracaoContexto,
        codSubprocessoCarregado,
        invalido,
        dadosValidos,
        invalidar,
        garantirContextoEdicao,
        garantirContextoEdicaoPorProcessoEUnidade,
    };
});
