import { defineStore } from "pinia";
import { ref } from "vue";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import type { ImpactoMapa } from "@/types/impacto";
import type {
    Competencia,
    DisponibilizarMapaRequest,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
    SalvarAjustesRequest,
    SalvarMapaRequest,
} from "@/types/tipos";
import { useNotificacoesStore } from "./notificacoes";

export const useMapasStore = defineStore("mapas", () => {
    const mapaCompleto = ref<MapaCompleto | null>(null);
    const mapaAjuste = ref<MapaAjuste | null>(null);
    const impactoMapa = ref<ImpactoMapa | null>(null);
    const mapaVisualizacao = ref<MapaVisualizacao | null>(null);

    async function fetchMapaVisualizacao(codSubrocesso: number) {
        const notificacoes = useNotificacoesStore();
        try {
            mapaVisualizacao.value =
                await mapaService.obterMapaVisualizacao(codSubrocesso);
        } catch {
            notificacoes.erro(
                "Erro ao buscar mapa",
                "Não foi possível carregar a visualização do mapa.",
            );
            mapaVisualizacao.value = null;
        }
    }

    async function fetchMapaCompleto(codSubrocesso: number) {
        const notificacoes = useNotificacoesStore();
        try {
            mapaCompleto.value = await mapaService.obterMapaCompleto(codSubrocesso);
        } catch {
            notificacoes.erro(
                "Erro ao buscar mapa",
                "Não foi possível carregar o mapa de competências.",
            );
            mapaCompleto.value = null;
        }
    }

    async function salvarMapa(codSubrocesso: number, request: SalvarMapaRequest) {
        const notificacoes = useNotificacoesStore();
        try {
            mapaCompleto.value = await mapaService.salvarMapaCompleto(
                codSubrocesso,
                request,
            );
            notificacoes.sucesso("Mapa salvo", "O mapa de competências foi salvo.");
        } catch {
            notificacoes.erro(
                "Erro ao salvar",
                "Não foi possível salvar o mapa de competências.",
            );
        }
    }

    async function adicionarCompetencia(
        codSubrocesso: number,
        competencia: Competencia,
    ) {
        const notificacoes = useNotificacoesStore();
        try {
            mapaCompleto.value = await subprocessoService.adicionarCompetencia(
                codSubrocesso,
                competencia,
            );
            notificacoes.sucesso(
                "Competência adicionada",
                "A competência foi adicionada.",
            );
        } catch {
            notificacoes.erro(
                "Erro ao adicionar",
                "Não foi possível adicionar a competência.",
            );
        }
    }

    async function atualizarCompetencia(
        codSubrocesso: number,
        competencia: Competencia,
    ) {
        const notificacoes = useNotificacoesStore();
        try {
            mapaCompleto.value = await subprocessoService.atualizarCompetencia(
                codSubrocesso,
                competencia,
            );
            notificacoes.sucesso(
                "Competência atualizada",
                "A competência foi atualizada.",
            );
        } catch {
            notificacoes.erro(
                "Erro ao atualizar",
                "Não foi possível atualizar a competência.",
            );
        }
    }

    async function removerCompetencia(codSubrocesso: number, idCompetencia: number) {
        const notificacoes = useNotificacoesStore();
        try {
            mapaCompleto.value = await subprocessoService.removerCompetencia(
                codSubrocesso,
                idCompetencia,
            );
            notificacoes.sucesso(
                "Competência removida",
                "A competência foi removida.",
            );
        } catch {
            notificacoes.erro(
                "Erro ao remover",
                "Não foi possível remover a competência.",
            );
        }
    }

    async function fetchMapaAjuste(codSubrocesso: number) {
        const notificacoes = useNotificacoesStore();
        try {
            mapaAjuste.value = await mapaService.obterMapaAjuste(codSubrocesso);
        } catch {
            notificacoes.erro(
                "Erro ao buscar mapa para ajuste",
                "Não foi possível carregar as informações para o ajuste.",
            );
            mapaAjuste.value = null;
        }
    }

    async function salvarAjustes(codSubrocesso: number, request: SalvarAjustesRequest) {
        const notificacoes = useNotificacoesStore();
        try {
            await mapaService.salvarMapaAjuste(codSubrocesso, request);
            notificacoes.sucesso(
                "Ajustes salvos",
                "Os ajustes no mapa foram salvos.",
            );
        } catch {
            notificacoes.erro(
                "Erro ao salvar ajustes",
                "Não foi possível salvar os ajustes.",
            );
        }
    }

    async function fetchImpactoMapa(codSubrocesso: number) {
        const notificacoes = useNotificacoesStore();
        try {
            impactoMapa.value =
                await mapaService.verificarImpactosMapa(codSubrocesso);
        } catch {
            notificacoes.erro(
                "Erro ao verificar impactos",
                "Não foi possível carregar os impactos no mapa.",
            );
            impactoMapa.value = null;
        }
    }

    async function disponibilizarMapa(
        codSubrocesso: number,
        request: DisponibilizarMapaRequest,
    ) {
        const notificacoes = useNotificacoesStore();
        try {
            await mapaService.disponibilizarMapa(codSubrocesso, request);
            notificacoes.sucesso(
                "Mapa disponibilizado",
                "O mapa de competências foi disponibilizado para validação.",
            );
        } catch (error: any) {
            notificacoes.erro(
                "Erro ao disponibilizar",
                error.response.data.message,
            );
            throw error;
        }
    }

    return {
        mapaCompleto,
        mapaAjuste,
        impactoMapa,
        mapaVisualizacao,
        fetchMapaVisualizacao,
        fetchMapaCompleto,
        salvarMapa,
        adicionarCompetencia,
        atualizarCompetencia,
        removerCompetencia,
        fetchMapaAjuste,
        salvarAjustes,
        fetchImpactoMapa,
        disponibilizarMapa,
    };
});
