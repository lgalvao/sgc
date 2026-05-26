import {storeToRefs} from "pinia";
import {computed, type MaybeRefOrGetter, type Ref, toValue} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {useMapasStore} from "@/stores/mapas";

export function useMapas(codigoSubprocesso?: MaybeRefOrGetter<number | null | undefined>) {
    const mapasStore = useMapasStore();
    const {mapaCompleto: mapaCompletoGlobal, impactoMapa: impactoMapaGlobal} = storeToRefs(mapasStore);
    const {carregando, erro, executar} = useAsyncAction();
    const mapaCompleto = codigoSubprocesso === undefined
        ? mapaCompletoGlobal
        : computed(() => {
            const codigoAtual = toValue(codigoSubprocesso);
            return typeof codigoAtual === "number"
                ? mapasStore.obterMapa(codigoAtual)
                : null;
        });
    const impactoMapa = codigoSubprocesso === undefined
        ? impactoMapaGlobal
        : computed(() => {
            const codigoAtual = toValue(codigoSubprocesso);
            return typeof codigoAtual === "number"
                ? mapasStore.obterImpacto(codigoAtual)
                : null;
        });

    async function carregarMapa(codSubprocesso: number) {
        await executar(async () => {
            await mapasStore.carregarMapa(codSubprocesso);
        }, "Erro ao carregar mapa completo.", {relancarErro: false});
    }

    async function carregarImpacto(codSubprocesso: number) {
        if (!codSubprocesso) {
            return;
        }

        await executar(async () => {
            await mapasStore.carregarImpacto(codSubprocesso);
        }, "Erro ao verificar impactos.", {relancarErro: false});
    }

    return {
        mapaCompleto: mapaCompleto as Ref<MapaCompleto | null>,
        impactoMapa: impactoMapa as Ref<ImpactoMapa | null>,
        carregando,
        erro,
        sincronizarMapa: mapasStore.sincronizarMapa,
        sincronizarImpacto: mapasStore.sincronizarImpacto,
        invalidar: mapasStore.invalidar,
        invalidarImpacto: mapasStore.invalidarImpacto,
        resetar: mapasStore.resetar,
        carregarMapa,
        carregarImpacto,
    };
}
