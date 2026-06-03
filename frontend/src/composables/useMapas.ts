import {computed, type MaybeRefOrGetter, type Ref, toValue} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {useCacheMapa, useImpactoMapaQuery, useMapaQuery} from "@/composables/useMapaQuery";

export function useMapas(codigoSubprocesso?: MaybeRefOrGetter<number | null | undefined>) {
    const codigoConsulta = computed(() => toValue(codigoSubprocesso) ?? null);
    const mapaQuery = useMapaQuery(codigoConsulta);
    const impactoQuery = useImpactoMapaQuery(codigoConsulta);
    const cacheMapa = useCacheMapa();
    const {erro, executar} = useAsyncAction();
    const mapaCompleto = computed(() => mapaQuery.data.value ?? null);
    const impactoMapa = computed(() => impactoQuery.data.value ?? null);

    async function carregarImpacto(codSubprocesso: number) {
        if (!codSubprocesso) {
            return;
        }

        await executar(async () => {
            await impactoQuery.refetch();
        }, "Erro ao verificar impactos.", {relancarErro: false});
    }

    return {
        mapaCompleto: mapaCompleto as Ref<MapaCompleto | null>,
        impactoMapa: impactoMapa as Ref<ImpactoMapa | null>,
        erro,
        sincronizarMapa: cacheMapa.sincronizarMapa,
        invalidar: cacheMapa.invalidarMapa,
        invalidarImpacto: cacheMapa.invalidarImpacto,
        carregarImpacto,
    };
}
