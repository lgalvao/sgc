import {defineStore} from "pinia";
import {ref} from "vue";
import {
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarSubprocessoDetalhe as serviceFetchSubprocessoDetalhe,
    buscarSubprocessoPorProcessoEUnidade as serviceBuscarSubprocessoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import {usePerfilStore} from "@/stores/perfil";
import {useMapasStore} from "@/stores/mapas";
import type {
    MapaCompleto,
    SituacaoSubprocesso,
    SubprocessoDetalhe,
} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {normalizeError} from "@/utils/apiError";
import {logger} from "@/utils";

export const useSubprocessosStore = defineStore("subprocessos", () => {
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
            processoDescricao: sp.processoDescricao || (sp.processo?.descricao),
            tipoProcesso: sp.tipoProcesso || (sp.processo?.tipo),
            prazoEtapaAtual: sp.prazoEtapaAtual || sp.dataLimite,
            isEmAndamento: sp.isEmAndamento ?? true,
            etapaAtual: sp.etapaAtual || 1,
            movimentacoes: dto.movimentacoes || [],
            elementosProcesso: [],
            permissoes: dto.permissoes || {}
        };
    }

    async function buscarSubprocessoDetalhe(codigo: number) {
        subprocessoDetalhe.value = null;
        const perfilStore = usePerfilStore();
        const perfil = perfilStore.perfilSelecionado;
        const codUnidade = perfilStore.unidadeAtual;

        const perfilGlobal = perfil === 'ADMIN' || perfil === 'GESTOR';

        if (!perfil || (!perfilGlobal && codUnidade === null)) {
            logger.error(`Erro de pré-condição: Perfil ou unidade não disponíveis`);
            const err = new Error("Informações de perfil ou unidade não disponíveis.");
            lastError.value = normalizeError(err);
            subprocessoDetalhe.value = null;
            return;
        }

        await withErrorHandling(async () => {
            const dto = await serviceFetchSubprocessoDetalhe(
                codigo,
                perfil,
                codUnidade as number,
            );
            subprocessoDetalhe.value = mapSubprocessoDetalheDtoToModel(dto);
        }, (err) => {
            logger.error(`Erro ao buscar detalhes do subprocesso ${codigo}:`, err);
            subprocessoDetalhe.value = null;
        });
    }

    async function buscarSubprocessoPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
    ): Promise<number | null> {
        try {
            return await withErrorHandling(async () => {
                const dto = await serviceBuscarSubprocessoPorProcessoEUnidade(codProcesso, siglaUnidade);
                return dto.codigo;
            });
        } catch (err) {
            logger.error(`Erro ao buscar ID do subprocesso:`, err);
            return null;
        }
    }

    async function buscarContextoEdicao(codigo: number) {
        subprocessoDetalhe.value = null;
        const perfilStore = usePerfilStore();
        const perfil = perfilStore.perfilSelecionado;
        const codUnidade = perfilStore.unidadeAtual;
        const perfilGlobal = perfil === 'ADMIN' || perfil === 'GESTOR';

        if (!perfil || (!perfilGlobal && codUnidade === null)) {
            const err = new Error("Informações de perfil ou unidade não disponíveis.");
            lastError.value = normalizeError(err);
            return;
        }

        return withErrorHandling(async () => {
            const data = await serviceBuscarContextoEdicao(codigo, perfil, codUnidade as number);
            const detalhesDto = data.detalhes || data;
            subprocessoDetalhe.value = mapSubprocessoDetalheDtoToModel(detalhesDto);

            const mapasStore = useMapasStore();
            mapasStore.mapaCompleto = data.mapa as MapaCompleto;

            return data;
        });
    }

    function atualizarStatusLocal(status: {
        codigo: number;
        situacao: SituacaoSubprocesso;
        permissoes?: any;
    }) {
        if (subprocessoDetalhe.value) {
            subprocessoDetalhe.value = {
                ...subprocessoDetalhe.value,
                situacao: status.situacao,
                permissoes: status.permissoes || subprocessoDetalhe.value.permissoes
            };
        }
    }

    return {
        subprocessoDetalhe,
        lastError,
        clearError,
        buscarSubprocessoDetalhe,
        buscarContextoEdicao,
        buscarSubprocessoPorProcessoEUnidade,
        atualizarStatusLocal,
    };
});
