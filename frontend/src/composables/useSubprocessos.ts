import {ref} from "vue";
import {
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarSubprocessoDetalhe as serviceBuscarSubprocessoDetalhe,
    buscarSubprocessoPorProcessoEUnidade as serviceBuscarSubprocessoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import {usePerfilStore} from "@/stores/perfil";
import {useMapas} from "@/composables/useMapas";
import type {MapaCompleto, SituacaoSubprocesso, SubprocessoDetalhe} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {normalizeError} from "@/utils/apiError";
import {logger} from "@/utils";

const subprocessoDetalhe = ref<SubprocessoDetalhe | null>(null);
const {lastError, clearError, withErrorHandling} = useErrorHandler();

function mapSubprocessoDetalheDtoToModel(dto: any): SubprocessoDetalhe {
    if (!dto) return null as any;

    const sp = dto.subprocesso || dto;

    return {
        codigo: sp.codigo,
        unidade: sp.unidade,
        titular: dto.titular,
        responsavel: dto.responsavel,
        situacao: sp.situacao,
        localizacaoAtual: dto.localizacaoAtual || sp.unidade?.sigla,
        processoDescricao: sp.processoDescricao || sp.processo?.descricao,
        dataCriacaoProcesso: sp.dataCriacaoProcesso || sp.processo?.dataCriacao,
        tipoProcesso: sp.tipoProcesso || sp.processo?.tipo,
        prazoEtapaAtual: sp.prazoEtapaAtual || sp.dataLimite || sp.dataLimiteEtapa2 || sp.dataLimiteEtapa1,
        isEmAndamento: sp.isEmAndamento ?? true,
        etapaAtual: sp.etapaAtual || 1,
        movimentacoes: dto.movimentacoes || [],
        elementosProcesso: [],
        permissoes: dto.permissoes || {},
    };
}

async function buscarSubprocessoDetalhe(codigo: number) {
    subprocessoDetalhe.value = null;

    const perfilStore = usePerfilStore();
    const perfil = perfilStore.perfilSelecionado;
    const codUnidade = perfilStore.unidadeAtual;
    const perfilGlobal = perfil === "ADMIN" || perfil === "GESTOR";

    if (!perfil || (!perfilGlobal && codUnidade === null)) {
        logger.error("Erro de pré-condição: Perfil ou unidade não disponíveis");
        const erro = new Error("Informações de perfil ou unidade não disponíveis.");
        lastError.value = normalizeError(erro);
        subprocessoDetalhe.value = null;
        return;
    }

    await withErrorHandling(async () => {
        const dto = await serviceBuscarSubprocessoDetalhe(codigo, perfil, codUnidade as number);
        subprocessoDetalhe.value = mapSubprocessoDetalheDtoToModel(dto);
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
    } catch (error) {
        logger.error("Erro ao buscar ID do subprocesso:", error);
        return null;
    }
}

async function buscarContextoEdicao(codigo: number) {
    subprocessoDetalhe.value = null;

    const perfilStore = usePerfilStore();
    const perfil = perfilStore.perfilSelecionado;
    const codUnidade = perfilStore.unidadeAtual;
    const perfilGlobal = perfil === "ADMIN" || perfil === "GESTOR";

    if (!perfil || (!perfilGlobal && codUnidade === null)) {
        const erro = new Error("Informações de perfil ou unidade não disponíveis.");
        lastError.value = normalizeError(erro);
        return;
    }

    return withErrorHandling(async () => {
        const data = await serviceBuscarContextoEdicao(codigo, perfil, codUnidade as number);
        const detalhesDto = data.detalhes || data;

        subprocessoDetalhe.value = mapSubprocessoDetalheDtoToModel(detalhesDto);

        const mapas = useMapas();
        mapas.mapaCompleto.value = data.mapa as MapaCompleto;

        return data;
    });
}

function atualizarStatusLocal(status: {
    codigo: number;
    situacao: SituacaoSubprocesso;
    permissoes?: any;
}) {
    if (!subprocessoDetalhe.value) {
        return;
    }

    subprocessoDetalhe.value = {
        ...subprocessoDetalhe.value,
        situacao: status.situacao,
        permissoes: status.permissoes || subprocessoDetalhe.value.permissoes,
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
