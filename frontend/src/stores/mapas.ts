import {defineStore} from "pinia";
import {ref} from "vue";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
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
        mapaVisualizacao.value =
            await mapaService.obterMapaVisualizacao(codSubrocesso);
    }

    async function buscarMapaCompleto(codSubrocesso: number) {
        mapaCompleto.value = await mapaService.obterMapaCompleto(codSubrocesso);
    }

    async function salvarMapa(codSubrocesso: number, request: SalvarMapaRequest) {
        mapaCompleto.value = await mapaService.salvarMapaCompleto(
            codSubrocesso,
            request,
        );
    }

    async function adicionarCompetencia(
        codSubrocesso: number,
        competencia: Competencia,
    ) {
        mapaCompleto.value = await subprocessoService.adicionarCompetencia(
            codSubrocesso,
            competencia,
        );
    }

    async function atualizarCompetencia(
        codSubrocesso: number,
        competencia: Competencia,
    ) {
        mapaCompleto.value = await subprocessoService.atualizarCompetencia(
            codSubrocesso,
            competencia,
        );
    }

    async function removerCompetencia(codSubrocesso: number, idCompetencia: number) {
        mapaCompleto.value = await subprocessoService.removerCompetencia(
            codSubrocesso,
            idCompetencia,
        );
    }

    async function buscarMapaAjuste(codSubrocesso: number) {
        mapaAjuste.value = await mapaService.obterMapaAjuste(codSubrocesso);
    }

    async function salvarAjustes(codSubrocesso: number, request: SalvarAjustesRequest) {
        await mapaService.salvarMapaAjuste(codSubrocesso, request);
    }

    async function buscarImpactoMapa(codSubrocesso: number) {
        impactoMapa.value =
            await mapaService.verificarImpactosMapa(codSubrocesso);
    }

    async function disponibilizarMapa(
        codSubrocesso: number,
        request: DisponibilizarMapaRequest,
    ) {
        try {
            await mapaService.disponibilizarMapa(codSubrocesso, request);
        } catch (error: any) {
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
