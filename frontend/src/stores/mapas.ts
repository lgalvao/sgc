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
    Competencia,
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

export const useMapasStore = defineStore("mapas", () => {
    const mapaVisualizacao = ref<MapaVisualizacao | null>(null);
    const mapaCompleto = ref<MapaCompleto | null>(null);
    const mapaAjuste = ref<MapaAjuste | null>(null);
    const impactoMapa = ref<ImpactoMapa | null>(null);
    const carregando = ref(false);
    const erro = ref<string | null>(null);
    const { lastError, clearError } = useErrorHandler();
    const feedbackStore = useFeedbackStore();

    async function buscarMapaVisualizacao(codSubprocesso: number) {
        carregando.value = true;
        erro.value = null;
        try {
            if (codSubprocesso) {
                mapaVisualizacao.value = await obterMapaVisualizacao(codSubprocesso);
            }
        } catch (e: any) {
            erro.value = e.message || "Erro ao carregar mapa de visualização.";
        } finally {
            carregando.value = false;
        }
    }

    async function buscarMapaCompleto(codSubprocesso: number) {
        carregando.value = true;
        erro.value = null;
        try {
            mapaCompleto.value = await obterMapaCompleto(codSubprocesso);
        } catch (e: any) {
            erro.value = e.message || "Erro ao carregar mapa completo.";
        } finally {
            carregando.value = false;
        }
    }

    async function salvarMapa(codSubprocesso: number, dados: any) {
        carregando.value = true;
        erro.value = null;
        try {
            mapaCompleto.value = await salvarMapaCompleto(
                codSubprocesso,
                dados,
            );
        } catch (e: any) {
            erro.value = e.message || "Erro ao salvar mapa completo.";
            throw e;
        } finally {
            carregando.value = false;
        }
    }

    async function buscarMapaAjuste(codSubprocesso: number) {
        carregando.value = true;
        erro.value = null;
        try {
            mapaAjuste.value = await obterMapaAjuste(codSubprocesso);
        } catch (e: any) {
            erro.value = e.message || "Erro ao carregar mapa para ajuste.";
        } finally {
            carregando.value = false;
        }
    }

    async function salvarAjustes(codSubprocesso: number, request: any) {
        carregando.value = true;
        erro.value = null;
        try {
            await salvarMapaAjuste(codSubprocesso, request);
        } catch (e: any) {
            erro.value = e.message || "Erro ao salvar ajustes do mapa.";
            throw e;
        } finally {
            carregando.value = false;
        }
    }

    async function buscarImpactoMapa(codSubprocesso: number) {
        carregando.value = true;
        erro.value = null;
        try {
            if (codSubprocesso) {
                impactoMapa.value = await verificarImpactosMapa(codSubprocesso);
            }
        } catch (e: any) {
            erro.value = e.message || "Erro ao verificar impactos.";
            // Não relançar erro para não quebrar a UI, apenas logar/mostrar msg
        } finally {
            carregando.value = false;
        }
    }

    async function disponibilizarMapa(
        codSubprocesso: number,
        request: DisponibilizarMapaRequest,
    ) {
        carregando.value = true;
        erro.value = null;
        try {
            await disponibilizarMapaService(codSubprocesso, request);
            feedbackStore.show("Mapa disponibilizado", "Mapa de competências disponibilizado.", "success");
        } catch (e: any) {
            erro.value = e.message || "Erro ao disponibilizar mapa.";
            throw e;
        } finally {
            carregando.value = false;
        }
    }

    async function adicionarCompetencia(
        codSubprocesso: number,
        competencia: SalvarCompetenciaRequest,
    ) {
        carregando.value = true;
        erro.value = null;
        try {
            mapaCompleto.value = await adicionarCompetenciaService(
                codSubprocesso,
                competencia,
            );
            feedbackStore.show("Competência adicionada", "Competência adicionada com sucesso.", "success");
        } catch (e: any) {
            erro.value = e.message || "Erro ao adicionar competência.";
            throw e;
        } finally {
            carregando.value = false;
        }
    }

    async function atualizarCompetencia(
        codSubprocesso: number,
        codCompetencia: number,
        competencia: SalvarCompetenciaRequest,
    ) {
        carregando.value = true;
        erro.value = null;
        try {
            mapaCompleto.value = await atualizarCompetenciaService(
                codSubprocesso,
                codCompetencia,
                competencia,
            );
            feedbackStore.show("Competência atualizada", "Competência atualizada com sucesso.", "success");
        } catch (e: any) {
            erro.value = e.message || "Erro ao atualizar competência.";
            throw e;
        } finally {
            carregando.value = false;
        }
    }

    async function removerCompetencia(
        codSubprocesso: number,
        codCompetencia: number,
    ) {
        carregando.value = true;
        erro.value = null;
        try {
            mapaCompleto.value = await removerCompetenciaService(
                codSubprocesso,
                codCompetencia,
            );
            feedbackStore.show("Competência removida", "Competência removida com sucesso.", "success");
        } catch (e: any) {
            erro.value = e.message || "Erro ao remover competência.";
            throw e;
        } finally {
            carregando.value = false;
        }
    }

    async function temMapaVigente(codigoUnidade: number): Promise<boolean> {
        // Não gerencia estado de carregando/erro global aqui para ser mais leve
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
