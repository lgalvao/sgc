import {defineStore} from "pinia";
import {ref} from "vue";
import type {ContextoEdicaoSubprocesso} from "@/types/tipos";
import {
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarContextoEdicaoPorProcessoEUnidade as serviceBuscarContextoEdicaoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import {logger} from "@/utils";

/**
 * Cache de sessão para contexto de edição de subprocesso.
 *
 * Problema: SubprocessoView e CadastroVisualizacaoView buscam `subprocessos/buscar` +
 * `subprocessos/{codigo}/contexto-edicao` em toda ativação (onMounted + onActivated),
 * mesmo quando o contexto não mudou. Isso explica 7-9 chamadas repetidas por jornada.
 *
 * Estratégia: cache por código de subprocesso, invalidado explicitamente após ações
 * de workflow (disponibilizar, aceitar, homologar, etc.).
 */
export const useSubprocessoStore = defineStore("subprocesso", () => {
    const contextoEdicao = ref<ContextoEdicaoSubprocesso | null>(null);
    const codSubprocessoCarregado = ref<number | null>(null);
    const invalido = ref(true);
    const carregamentosPorCodigo = new Map<number, Promise<ContextoEdicaoSubprocesso>>();
    const carregamentosPorProcessoUnidade = new Map<string, Promise<{ codigo: number; contexto: ContextoEdicaoSubprocesso }>>();
    const mapaProcessoUnidadeParaCodigo = new Map<string, number>();

    function gerarChaveProcessoUnidade(codProcesso: number, siglaUnidade: string): string {
        return `${codProcesso}:${siglaUnidade}`;
    }

    /** Contexto ainda é válido para o subprocesso dado? */
    function dadosValidos(codSubprocesso: number): boolean {
        return !invalido.value && codSubprocessoCarregado.value === codSubprocesso && contextoEdicao.value !== null;
    }

    /** Invalida o cache — deve ser chamado após qualquer ação de workflow. */
    function invalidar(): void {
        invalido.value = true;
        carregamentosPorCodigo.clear();
        carregamentosPorProcessoUnidade.clear();
        mapaProcessoUnidadeParaCodigo.clear();
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
            codSubprocessoCarregado.value = codSubprocesso;
            invalido.value = false;
            return data;
        } catch (err) {
            logger.error(`Erro ao buscar contexto de edição do subprocesso ${codSubprocesso}:`, err);
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
                codSubprocessoCarregado.value = codigo;
                invalido.value = false;
                return {codigo, contexto: data};
            })();
            carregamentosPorProcessoUnidade.set(chaveProcessoUnidade, promessaCarregamento);
            const {codigo, contexto} = await promessaCarregamento;
            contextoEdicao.value = contexto;
            codSubprocessoCarregado.value = codigo;
            invalido.value = false;
            return {codigo, contexto};
        } catch (err) {
            logger.error(`Erro ao buscar contexto de subprocesso para processo ${codProcesso} unidade ${siglaUnidade}:`, err);
            return null;
        } finally {
            carregamentosPorProcessoUnidade.delete(chaveProcessoUnidade);
        }
    }

    return {
        contextoEdicao,
        codSubprocessoCarregado,
        invalido,
        dadosValidos,
        invalidar,
        garantirContextoEdicao,
        garantirContextoEdicaoPorProcessoEUnidade,
    };
});
