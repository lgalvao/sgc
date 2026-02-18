import {defineStore} from "pinia";
import {ref} from "vue";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import {useFeedbackStore} from "@/stores/feedback";
import type {
    Competencia,
    DisponibilizarMapaRequest,
    ImpactoMapa,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
    SalvarAjustesRequest,
    SalvarMapaRequest,
} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";


export const useMapasStore = defineStore("mapas", () => {
    const mapaCompleto = ref<MapaCompleto | null>(null);
    const mapaAjuste = ref<MapaAjuste | null>(null);
    const impactoMapa = ref<ImpactoMapa | null>(null);
    const mapaVisualizacao = ref<MapaVisualizacao | null>(null);
    const { lastError, clearError, withErrorHandling } = useErrorHandler();
    const feedbackStore = useFeedbackStore();

    async function buscarMapaVisualizacao(codSubprocesso: number) {
        return withErrorHandling(async () => {
            mapaVisualizacao.value = null; // Limpa estado anterior
            mapaVisualizacao.value =
                await mapaService.obterMapaVisualizacao(codSubprocesso);
        }, () => {
            mapaVisualizacao.value = null;
        });
    }

    async function buscarMapaCompleto(codSubprocesso: number) {
        return withErrorHandling(async () => {
            mapaCompleto.value = null; // Limpa estado anterior
            mapaCompleto.value = await mapaService.obterMapaCompleto(codSubprocesso);
        }, () => {
            mapaCompleto.value = null;
        });
    }

    async function salvarMapa(codSubprocesso: number, request: SalvarMapaRequest) {
        return withErrorHandling(async () => {
            mapaCompleto.value = await mapaService.salvarMapaCompleto(
                codSubprocesso,
                request,
            );
        });
    }

    async function adicionarCompetencia(
        codSubprocesso: number,
        competencia: Competencia,
    ) {
        return withErrorHandling(async () => {
            mapaCompleto.value = await subprocessoService.adicionarCompetencia(
                codSubprocesso,
                competencia,
            );
        });
    }

    async function atualizarCompetencia(
        codSubprocesso: number,
        competencia: Competencia,
    ) {
        if (!competencia || !competencia.codigo) {
            // Evitar chamada ao backend com id inválido
            throw new Error("Código da competência inválido");
        }

        return withErrorHandling(async () => {
            mapaCompleto.value = await subprocessoService.atualizarCompetencia(
                codSubprocesso,
                competencia,
            );
        });
    }

    async function removerCompetencia(codSubprocesso: number, idCompetencia: number) {
        if (!idCompetencia || idCompetencia === 0) {
            throw new Error("Código da competência inválido");
        }

        return withErrorHandling(async () => {
            mapaCompleto.value = await subprocessoService.removerCompetencia(
                codSubprocesso,
                idCompetencia,
            );
        });
    }

    async function buscarMapaAjuste(codSubprocesso: number) {
        return withErrorHandling(async () => {
            mapaAjuste.value = await mapaService.obterMapaAjuste(codSubprocesso);
        }, () => {
            mapaAjuste.value = null;
        });
    }

    async function salvarAjustes(codSubprocesso: number, request: SalvarAjustesRequest) {
        return withErrorHandling(async () => {
            await mapaService.salvarMapaAjuste(codSubprocesso, request);
        });
    }

    async function buscarImpactoMapa(codSubprocesso: number) {
        return withErrorHandling(async () => {
            impactoMapa.value =
                await mapaService.verificarImpactosMapa(codSubprocesso);
        }, () => {
            impactoMapa.value = null;
        });
    }

    async function disponibilizarMapa(
        codSubprocesso: number,
        request: DisponibilizarMapaRequest,
    ) {
        return withErrorHandling(async () => {
            await mapaService.disponibilizarMapa(codSubprocesso, request);
            feedbackStore.show(
                "Mapa disponibilizado",
                "O mapa de competências foi disponibilizado com sucesso.",
                "success"
            );
        });
    }

    return {
        mapaCompleto,
        mapaAjuste,
        impactoMapa,
        mapaVisualizacao,
        lastError,
        buscarMapaVisualizacao,
        buscarMapaCompleto,
        salvarMapa,
        adicionarCompetencia,
        atualizarCompetencia,
        removerCompetencia,
        buscarMapaAjuste,
        salvarAjustes,
        buscarImpactoMapa,
        disponibilizarMapa,
        clearError
    };
});
