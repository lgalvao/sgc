import {storeToRefs} from "pinia";
import {computed, toValue, type MaybeRefOrGetter, type Ref} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {useMapasStore} from "@/stores/mapas";

export function useMapas(codigoSubprocesso?: MaybeRefOrGetter<number | null | undefined>) {
    const mapasStore = useMapasStore();
    const {mapaCompleto: mapaCompletoGlobal, impactoMapa: impactoMapaGlobal} = storeToRefs(mapasStore);
    const {lastError, clearError} = useErrorHandler();
    const {carregando, erro, executarSilencioso} = useAsyncAction();
    const mapaCompleto = codigoSubprocesso === undefined
        ? mapaCompletoGlobal
        : computed(() => {
            const codigoAtual = toValue(codigoSubprocesso);
            if (typeof codigoAtual !== "number") {
                return mapaCompletoGlobal.value;
            }
            return mapasStore.obterMapaCompletoCache(codigoAtual) ?? mapaCompletoGlobal.value;
        });
    const impactoMapa = codigoSubprocesso === undefined
        ? impactoMapaGlobal
        : computed(() => {
            const codigoAtual = toValue(codigoSubprocesso);
            if (typeof codigoAtual !== "number") {
                return impactoMapaGlobal.value;
            }
            return mapasStore.obterImpactoMapaCache(codigoAtual) ?? impactoMapaGlobal.value;
        });

    async function buscarMapaCompleto(codSubprocesso: number) {
        await executarSilencioso(async () => {
            await mapasStore.garantirMapaCompleto(codSubprocesso);
        }, "Erro ao carregar mapa completo.");
    }

    async function buscarImpactoMapa(codSubprocesso: number) {
        if (!codSubprocesso) {
            return;
        }

        await executarSilencioso(async () => {
            await mapasStore.garantirImpactoMapa(codSubprocesso);
        }, "Erro ao verificar impactos.");
    }

    return {
        mapaCompleto: mapaCompleto as Ref<MapaCompleto | null>,
        impactoMapa: impactoMapa as Ref<ImpactoMapa | null>,
        carregando,
        erro,
        lastError,
        clearError,
        definirMapaCompleto: mapasStore.definirMapaCompleto,
        definirImpactoMapa: mapasStore.definirImpactoMapa,
        invalidar: mapasStore.invalidar,
        invalidarImpacto: mapasStore.invalidarImpacto,
        obterMapaCompletoCache: mapasStore.obterMapaCompletoCache,
        obterImpactoMapaCache: mapasStore.obterImpactoMapaCache,
        buscarMapaCompleto,
        buscarImpactoMapa,
    };
}
