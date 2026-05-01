import {storeToRefs} from "pinia";
import {obterMapaCompleto, verificarImpactosMapa} from "@/services/subprocessoService";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {useMapasStore} from "@/stores/mapas";

export function useMapas() {
    const mapasStore = useMapasStore();
    const {mapaCompleto, impactoMapa} = storeToRefs(mapasStore);
    const {lastError, clearError} = useErrorHandler();
    const {carregando, erro, executarSilencioso} = useAsyncAction();

    async function buscarMapaCompleto(codSubprocesso: number) {
        await executarSilencioso(async () => {
            mapaCompleto.value = await obterMapaCompleto(codSubprocesso);
        }, "Erro ao carregar mapa completo.");
    }

    async function buscarImpactoMapa(codSubprocesso: number) {
        if (!codSubprocesso) {
            return;
        }

        await executarSilencioso(async () => {
            impactoMapa.value = await verificarImpactosMapa(codSubprocesso);
        }, "Erro ao verificar impactos.");
    }

    return {
        mapaCompleto: mapaCompleto as import("vue").Ref<MapaCompleto | null>,
        impactoMapa: impactoMapa as import("vue").Ref<ImpactoMapa | null>,
        carregando,
        erro,
        lastError,
        clearError,
        buscarMapaCompleto,
        buscarImpactoMapa,
    };
}
