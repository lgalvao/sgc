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
import {type NormalizedError, normalizeError} from "@/utils/apiError";


export const useMapasStore = defineStore("mapas", () => {
    const mapaCompleto = ref<MapaCompleto | null>(null);
    const mapaAjuste = ref<MapaAjuste | null>(null);
    const impactoMapa = ref<ImpactoMapa | null>(null);
    const mapaVisualizacao = ref<MapaVisualizacao | null>(null);
    const lastError = ref<NormalizedError | null>(null);
    const feedbackStore = useFeedbackStore();

    function clearError() {
        lastError.value = null;
    }

    async function buscarMapaVisualizacao(codSubrocesso: number) {
        lastError.value = null;
        try {
            mapaVisualizacao.value =
                await mapaService.obterMapaVisualizacao(codSubrocesso);
        } catch (error) {
            lastError.value = normalizeError(error);
            mapaVisualizacao.value = null;
            throw error;
        }
    }

    async function buscarMapaCompleto(codSubrocesso: number) {
        lastError.value = null;
        try {
            mapaCompleto.value = await mapaService.obterMapaCompleto(codSubrocesso);
        } catch (error) {
            lastError.value = normalizeError(error);
            mapaCompleto.value = null;
            throw error;
        }
    }

    async function salvarMapa(codSubrocesso: number, request: SalvarMapaRequest) {
        lastError.value = null;
        try {
            mapaCompleto.value = await mapaService.salvarMapaCompleto(
                codSubrocesso,
                request,
            );
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function adicionarCompetencia(
        codSubrocesso: number,
        competencia: Competencia,
    ) {
        lastError.value = null;
        try {
            mapaCompleto.value = await subprocessoService.adicionarCompetencia(
                codSubrocesso,
                competencia,
            );
            // Garantir que o mapa foi recarregado com códigos corretos
            if (mapaCompleto.value && mapaCompleto.value.competencias.some(c => !c.codigo || c.codigo === 0)) {
                await buscarMapaCompleto(codSubrocesso);
            }
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function atualizarCompetencia(
        codSubrocesso: number,
        competencia: Competencia,
    ) {
        lastError.value = null;
        if (!competencia || !competencia.codigo) {
            // Evitar chamada ao backend com id inválido
            const err = new Error("Código da competência inválido");
            lastError.value = normalizeError(err);
            throw err;
        }

        try {
            mapaCompleto.value = await subprocessoService.atualizarCompetencia(
                codSubrocesso,
                competencia,
            );
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function removerCompetencia(codSubrocesso: number, idCompetencia: number) {
        lastError.value = null;
        if (!idCompetencia || idCompetencia === 0) {
            const err = new Error("Código da competência inválido");
            lastError.value = normalizeError(err);
            throw err;
        }

        try {
            mapaCompleto.value = await subprocessoService.removerCompetencia(
                codSubrocesso,
                idCompetencia,
            );
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function buscarMapaAjuste(codSubrocesso: number) {
        lastError.value = null;
        try {
            mapaAjuste.value = await mapaService.obterMapaAjuste(codSubrocesso);
        } catch (error) {
            lastError.value = normalizeError(error);
            mapaAjuste.value = null;
            throw error;
        }
    }

    async function salvarAjustes(codSubrocesso: number, request: SalvarAjustesRequest) {
        lastError.value = null;
        try {
            await mapaService.salvarMapaAjuste(codSubrocesso, request);
        } catch (error) {
            lastError.value = normalizeError(error);
            throw error;
        }
    }

    async function buscarImpactoMapa(codSubrocesso: number) {
        lastError.value = null;
        try {
            impactoMapa.value =
                await mapaService.verificarImpactosMapa(codSubrocesso);
        } catch (error) {
            lastError.value = normalizeError(error);
            impactoMapa.value = null;
            throw error;
        }
    }

    async function disponibilizarMapa(
        codSubrocesso: number,
        request: DisponibilizarMapaRequest,
    ) {
        lastError.value = null;
        try {
            await mapaService.disponibilizarMapa(codSubrocesso, request);
            feedbackStore.show(
                "Mapa disponibilizado",
                "O mapa de competências foi disponibilizado com sucesso.",
                "success"
            );
        } catch (error: any) {
            lastError.value = normalizeError(error);
            throw error;
        }
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
