import {
    adicionarCompetencia as adicionarCompetenciaService,
    atualizarCompetencia as atualizarCompetenciaService,
    disponibilizarMapa as disponibilizarMapaService,
    obterMapaAjuste,
    obterMapaCompleto,
    obterMapaVisualizacao,
    removerCompetencia as removerCompetenciaService,
    salvarMapaAjuste,
    salvarMapaCompleto,
    verificarImpactosMapa,
    verificarMapaVigente
} from "@/services/subprocessoService";
import type {
    DisponibilizarMapaRequest,
    ImpactoMapa,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
    SalvarCompetenciaRequest
} from "@/types/tipos";
import {defineStore} from "pinia";
import {ref} from "vue";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useFeedbackStore} from "@/stores/feedback";
import {useAsyncAction} from "@/composables/useAsyncAction";

export const useMapasStore = defineStore("mapas", () => {
    const mapaVisualizacao = ref<MapaVisualizacao | null>(null);
    const mapaCompleto = ref<MapaCompleto | null>(null);
    const mapaAjuste = ref<MapaAjuste | null>(null);
    const impactoMapa = ref<ImpactoMapa | null>(null);
    const {lastError, clearError} = useErrorHandler();
    const feedbackStore = useFeedbackStore();
    const {carregando, erro, executar, executarSilencioso} = useAsyncAction();

    async function buscarMapaVisualizacao(codSubprocesso: number) {
        await executarSilencioso(async () => {
            if (codSubprocesso) {
                mapaVisualizacao.value = await obterMapaVisualizacao(codSubprocesso);
            }
        }, "Erro ao carregar mapa de visualização.");
    }

    async function buscarMapaCompleto(codSubprocesso: number) {
        await executarSilencioso(async () => {
            mapaCompleto.value = await obterMapaCompleto(codSubprocesso);
        }, "Erro ao carregar mapa completo.");
    }

    async function salvarMapa(codSubprocesso: number, dados: any) {
        await executar(async () => {
            mapaCompleto.value = await salvarMapaCompleto(codSubprocesso, dados);
        }, "Erro ao salvar mapa completo.");
    }

    async function buscarMapaAjuste(codSubprocesso: number) {
        await executarSilencioso(async () => {
            mapaAjuste.value = await obterMapaAjuste(codSubprocesso);
        }, "Erro ao carregar mapa para ajuste.");
    }

    async function salvarAjustes(codSubprocesso: number, request: any) {
        await executar(async () => {
            await salvarMapaAjuste(codSubprocesso, request);
        }, "Erro ao salvar ajustes do mapa.");
    }

    async function buscarImpactoMapa(codSubprocesso: number) {
        await executarSilencioso(async () => {
            if (codSubprocesso) {
                impactoMapa.value = await verificarImpactosMapa(codSubprocesso);
            }
        }, "Erro ao verificar impactos.");
    }

    async function disponibilizarMapa(codSubprocesso: number, request: DisponibilizarMapaRequest) {
        await executar(async () => {
            await disponibilizarMapaService(codSubprocesso, request);
            feedbackStore.show("Mapa disponibilizado", "Mapa de competências disponibilizado.", "success");
        }, "Erro ao disponibilizar mapa.");
    }

    async function adicionarCompetencia(codSubprocesso: number, competencia: SalvarCompetenciaRequest) {
        await executar(async () => {
            mapaCompleto.value = await adicionarCompetenciaService(codSubprocesso, competencia);
            feedbackStore.show("Competência adicionada", "Competência adicionada com sucesso.", "success");
        }, "Erro ao adicionar competência.");
    }

    async function atualizarCompetencia(codSubprocesso: number, codCompetencia: number, competencia: SalvarCompetenciaRequest) {
        await executar(async () => {
            mapaCompleto.value = await atualizarCompetenciaService(codSubprocesso, codCompetencia, competencia);
            feedbackStore.show("Competência atualizada", "Competência atualizada com sucesso.", "success");
        }, "Erro ao atualizar competência.");
    }

    async function removerCompetencia(codSubprocesso: number, codCompetencia: number) {
        await executar(async () => {
            mapaCompleto.value = await removerCompetenciaService(codSubprocesso, codCompetencia);
            feedbackStore.show("Competência removida", "Competência removida com sucesso.", "success");
        }, "Erro ao remover competência.");
    }

    async function temMapaVigente(codigoUnidade: number): Promise<boolean> {
        try {
            return await verificarMapaVigente(codigoUnidade);
        } catch {
            return false;
        }
    }

    return {
        mapaVisualizacao,
        mapaCompleto,
        mapaAjuste,
        impactoMapa,
        carregando,
        erro,
        lastError,
        clearError,
        buscarMapaVisualizacao,
        buscarMapaCompleto,
        salvarMapa,
        buscarMapaAjuste,
        salvarAjustes,
        buscarImpactoMapa,
        disponibilizarMapa,
        adicionarCompetencia,
        atualizarCompetencia,
        removerCompetencia,
        temMapaVigente,
    };
});
