import {ref} from "vue";
import {
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarSubprocessoDetalhe as serviceBuscarSubprocessoDetalhe,
    buscarSubprocessoPorProcessoEUnidade as serviceBuscarSubprocessoPorProcessoEUnidade,
    mapSubprocessoDetalheResponseParaModel,
} from "@/services/subprocessoService";
import {useMapas} from "@/composables/useMapas";
import type {
    ContextoEdicaoSubprocesso,
    PermissoesSubprocesso,
    SituacaoSubprocesso,
    SubprocessoDetalhe,
} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {logger} from "@/utils";

const subprocessoDetalhe = ref<SubprocessoDetalhe | null>(null);
const {lastError, clearError, withErrorHandling} = useErrorHandler();

function atualizarDetalheLocal(detalhe: SubprocessoDetalhe) {
    subprocessoDetalhe.value = detalhe;
}

async function buscarSubprocessoDetalhe(codigo: number) {
    subprocessoDetalhe.value = null;

    await withErrorHandling(async () => {
        const dto = await serviceBuscarSubprocessoDetalhe(codigo);
        atualizarDetalheLocal(mapSubprocessoDetalheResponseParaModel(dto));
    }, (erro) => {
        logger.error(`Erro ao buscar detalhes do subprocesso ${codigo}:`, erro);
        subprocessoDetalhe.value = null;
    });
}

async function buscarSubprocessoPorProcessoEUnidade(codProcesso: number, siglaUnidade: string): Promise<number | null> {
    try {
        return await withErrorHandling(async () => {
            const dto = await serviceBuscarSubprocessoPorProcessoEUnidade(codProcesso, siglaUnidade);
            return dto.codigo;
        });
    } catch (error: unknown) {
        logger.error("Erro ao buscar ID do subprocesso:", error);
        return null;
    }
}

async function buscarContextoEdicao(codigo: number) {
    subprocessoDetalhe.value = null;

    return withErrorHandling(async () => {
        const data = await serviceBuscarContextoEdicao(codigo);
        atualizarContextoEdicaoLocal(data);

        return data;
    });
}

function atualizarContextoEdicaoLocal(data: ContextoEdicaoSubprocesso) {
    atualizarDetalheLocal(data.detalhes);

    const {mapaCompleto} = useMapas();
    mapaCompleto.value = data.mapa;
}

function atualizarStatusLocal(status: {
    codigo: number;
    situacao: SituacaoSubprocesso;
    permissoes?: PermissoesSubprocesso;
}) {
    if (!subprocessoDetalhe.value) {
        return;
    }

    subprocessoDetalhe.value = {
        ...subprocessoDetalhe.value,
        situacao: status.situacao,
        permissoes: status.permissoes ?? subprocessoDetalhe.value.permissoes,
    };
}

export function useSubprocessos() {
    return {
        get subprocessoDetalhe() {
            return subprocessoDetalhe.value;
        },
        set subprocessoDetalhe(valor: SubprocessoDetalhe | null) {
            subprocessoDetalhe.value = valor;
        },
        get lastError() {
            return lastError.value;
        },
        set lastError(valor: typeof lastError.value) {
            lastError.value = valor;
        },
        clearError,
        buscarSubprocessoDetalhe,
        buscarContextoEdicao,
        buscarSubprocessoPorProcessoEUnidade,
        atualizarStatusLocal,
    };
}
