import {
    adicionarCompetencia as adicionarCompetenciaService,
    atualizarCompetencia as atualizarCompetenciaService,
    disponibilizarMapa as disponibilizarMapaService,
    removerCompetencia as removerCompetenciaService,
    salvarMapaAjuste,
    salvarMapaCompleto,
} from "@/services/subprocessoService";
import type {DisponibilizarMapaRequest, SalvarCompetenciaRequest} from "@/types/tipos";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useMapasStore} from "@/stores/mapas";

export function useFluxoMapa() {
    const mapasStore = useMapasStore();
    const {lastError, clearError} = useErrorHandler();
    const {carregando, erro, executar} = useAsyncAction();

    async function salvarMapa(codSubprocesso: number, dados: any) {
        await executar(async () => {
            mapasStore.mapaCompleto = await salvarMapaCompleto(codSubprocesso, dados);
        }, "Erro ao salvar mapa completo.");
    }

    async function salvarAjustes(codSubprocesso: number, request: any) {
        await executar(async () => {
            await salvarMapaAjuste(codSubprocesso, request);
        }, "Erro ao salvar ajustes do mapa.");
    }

    async function disponibilizarMapa(codSubprocesso: number, request: DisponibilizarMapaRequest) {
        await executar(async () => {
            await disponibilizarMapaService(codSubprocesso, request);
        }, "Erro ao disponibilizar mapa.");
    }

    async function adicionarCompetencia(codSubprocesso: number, competencia: SalvarCompetenciaRequest) {
        await executar(async () => {
            mapasStore.mapaCompleto = await adicionarCompetenciaService(codSubprocesso, competencia);
        }, "Erro ao adicionar competência.");
    }

    async function atualizarCompetencia(codSubprocesso: number, codCompetencia: number, competencia: SalvarCompetenciaRequest) {
        await executar(async () => {
            mapasStore.mapaCompleto = await atualizarCompetenciaService(codSubprocesso, codCompetencia, competencia);
        }, "Erro ao atualizar competência.");
    }

    async function removerCompetencia(codSubprocesso: number, codCompetencia: number) {
        await executar(async () => {
            mapasStore.mapaCompleto = await removerCompetenciaService(codSubprocesso, codCompetencia);
        }, "Erro ao remover competência.");
    }

    return {
        carregando,
        erro,
        lastError,
        clearError,
        salvarMapa,
        salvarAjustes,
        disponibilizarMapa,
        adicionarCompetencia,
        atualizarCompetencia,
        removerCompetencia,
    };
}
