import {defineStore} from "pinia";
import {ref} from "vue";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import { ToastService } from "@/services/toastService"; // Import ToastService
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

    async function buscarMapaVisualizacao(codSubrocesso: number) {
        try {
            mapaVisualizacao.value =
                await mapaService.obterMapaVisualizacao(codSubrocesso);
        } catch (error) {
            ToastService.erro(
                "Erro ao buscar mapa de visualização",
                "Não foi possível carregar o mapa para visualização.",
            );
            mapaVisualizacao.value = null;
            throw error;
        }
    }

    async function buscarMapaCompleto(codSubrocesso: number) {
        try {
            mapaCompleto.value = await mapaService.obterMapaCompleto(codSubrocesso);
        } catch (error) {
            ToastService.erro(
                "Erro ao buscar mapa completo",
                "Não foi possível carregar o mapa completo.",
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
            ToastService.erro("Erro ao salvar mapa", "Não foi possível salvar o mapa.");
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
        } catch (error) {
            ToastService.erro(
                "Erro ao adicionar competência",
                "Não foi possível adicionar a competência.",
            );
            throw error;
        }
    }

    async function atualizarCompetencia(
        codSubrocesso: number,
        competencia: Competencia,
    ) {
        try {
            mapaCompleto.value = await subprocessoService.atualizarCompetencia(
                codSubrocesso,
                competencia,
            );
        } catch (error) {
            ToastService.erro(
                "Erro ao atualizar competência",
                "Não foi possível atualizar a competência.",
            );
            throw error;
        }
    }

    async function removerCompetencia(codSubrocesso: number, idCompetencia: number) {
        try {
            mapaCompleto.value = await subprocessoService.removerCompetencia(
                codSubrocesso,
                idCompetencia,
            );
        } catch (error) {
            ToastService.erro(
                "Erro ao remover competência",
                "Não foi possível remover a competência.",
            );
            throw error;
        }
    }

    async function buscarMapaAjuste(codSubrocesso: number) {
        try {
            mapaAjuste.value = await mapaService.obterMapaAjuste(codSubrocesso);
        } catch (error) {
            ToastService.erro(
                "Erro ao buscar mapa de ajuste",
                "Não foi possível carregar o mapa para ajuste.",
            );
            mapaAjuste.value = null;
            throw error;
        }
    }

    async function salvarAjustes(codSubrocesso: number, request: SalvarAjustesRequest) {
        try {
            await mapaService.salvarMapaAjuste(codSubrocesso, request);
        } catch (error) {
            ToastService.erro(
                "Erro ao salvar ajustes",
                "Não foi possível salvar os ajustes do mapa.",
            );
            throw error;
        }
    }

    async function buscarImpactoMapa(codSubrocesso: number) {
        try {
            impactoMapa.value =
                await mapaService.verificarImpactosMapa(codSubrocesso);
        } catch (error) {
            ToastService.erro(
                "Erro ao buscar impacto do mapa",
                "Não foi possível carregar o impacto do mapa.",
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
        } catch (error: any) {
            ToastService.erro(
                "Erro ao disponibilizar mapa",
                "Não foi possível disponibilizar o mapa.",
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
