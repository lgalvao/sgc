import {defineStore} from "pinia";
import {ref} from "vue";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import {useFeedbackStore} from "@/stores/feedback";
import type {ImpactoMapa} from "@/types/impacto";
import type {
    Competencia,
    DisponibilizarMapaRequest,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
    SalvarAjustesRequest,
    SalvarMapaRequest,
} from "@/types/tipos";


export const useMapasStore = defineStore("mapas", () => {
    const mapaCompleto = ref<MapaCompleto | null>(null);
    const mapaAjuste = ref<MapaAjuste | null>(null);
    const impactoMapa = ref<ImpactoMapa | null>(null);
    const mapaVisualizacao = ref<MapaVisualizacao | null>(null);
    const feedbackStore = useFeedbackStore();

    async function buscarMapaVisualizacao(codSubrocesso: number) {
        try {
            mapaVisualizacao.value =
                await mapaService.obterMapaVisualizacao(codSubrocesso);
        } catch (error) {
            feedbackStore.show(
                "Erro ao buscar mapa de visualização",
                "Não foi possível carregar o mapa para visualização.",
                "danger"
            );
            mapaVisualizacao.value = null;
            throw error;
        }
    }

    async function buscarMapaCompleto(codSubrocesso: number) {
        try {
            mapaCompleto.value = await mapaService.obterMapaCompleto(codSubrocesso);
        } catch (error) {
            feedbackStore.show(
                "Erro ao buscar mapa completo",
                "Não foi possível carregar o mapa completo.",
                "danger"
            );
            mapaCompleto.value = null;
            throw error;
        }
    }

    async function salvarMapa(codSubrocesso: number, request: SalvarMapaRequest) {
        try {
            mapaCompleto.value = await mapaService.salvarMapaCompleto(
                codSubrocesso,
                request,
            );
        } catch (error) {
            feedbackStore.show("Erro ao salvar mapa", "Não foi possível salvar o mapa.", "danger");
            throw error;
        }
    }

    async function adicionarCompetencia(
        codSubrocesso: number,
        competencia: Competencia,
    ) {
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
            feedbackStore.show(
                "Erro ao adicionar competência",
                "Não foi possível adicionar a competência.",
                "danger"
            );
            throw error;
        }
    }

    async function atualizarCompetencia(
        codSubrocesso: number,
        competencia: Competencia,
    ) {
        if (!competencia || !competencia.codigo) {
            // Evitar chamada ao backend com id inválido
            feedbackStore.show(
                "Erro ao atualizar competência",
                "Código da competência inválido.",
                "danger"
            );
            throw new Error("Código da competência inválido");
        }

        try {
            mapaCompleto.value = await subprocessoService.atualizarCompetencia(
                codSubrocesso,
                competencia,
            );
        } catch (error) {
            feedbackStore.show(
                "Erro ao atualizar competência",
                "Não foi possível atualizar a competência.",
                "danger"
            );
            throw error;
        }
    }

    async function removerCompetencia(codSubrocesso: number, idCompetencia: number) {
        if (!idCompetencia || idCompetencia === 0) {
            feedbackStore.show(
                "Erro ao remover competência",
                "Código da competência inválido.",
                "danger"
            );
            throw new Error("Código da competência inválido");
        }

        try {
            mapaCompleto.value = await subprocessoService.removerCompetencia(
                codSubrocesso,
                idCompetencia,
            );
        } catch (error) {
            feedbackStore.show(
                "Erro ao remover competência",
                "Não foi possível remover a competência.",
                "danger"
            );
            throw error;
        }
    }

    async function buscarMapaAjuste(codSubrocesso: number) {
        try {
            mapaAjuste.value = await mapaService.obterMapaAjuste(codSubrocesso);
        } catch (error) {
            feedbackStore.show(
                "Erro ao buscar mapa de ajuste",
                "Não foi possível carregar o mapa para ajuste.",
                "danger"
            );
            mapaAjuste.value = null;
            throw error;
        }
    }

    async function salvarAjustes(codSubrocesso: number, request: SalvarAjustesRequest) {
        try {
            await mapaService.salvarMapaAjuste(codSubrocesso, request);
        } catch (error) {
            feedbackStore.show(
                "Erro ao salvar ajustes",
                "Não foi possível salvar os ajustes do mapa.",
                "danger"
            );
            throw error;
        }
    }

    async function buscarImpactoMapa(codSubrocesso: number) {
        try {
            impactoMapa.value =
                await mapaService.verificarImpactosMapa(codSubrocesso);
        } catch (error) {
            feedbackStore.show(
                "Erro ao buscar impacto do mapa",
                "Não foi possível carregar o impacto do mapa.",
                "danger"
            );
            impactoMapa.value = null;
            throw error;
        }
    }

    async function disponibilizarMapa(
        codSubrocesso: number,
        request: DisponibilizarMapaRequest,
    ) {
        try {
            await mapaService.disponibilizarMapa(codSubrocesso, request);
            feedbackStore.show(
                "Mapa disponibilizado",
                "O mapa de competências foi disponibilizado com sucesso.",
                "success"
            );
        } catch (error: any) {
            feedbackStore.show(
                "Erro ao disponibilizar mapa",
                "Não foi possível disponibilizar o mapa.",
                "danger"
            );
            throw error;
        }
    }

    return {
        mapaCompleto,
        mapaAjuste,
        impactoMapa,
        mapaVisualizacao,
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
    };
});
