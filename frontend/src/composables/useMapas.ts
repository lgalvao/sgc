import {computed, type MaybeRefOrGetter, ref, type Ref, toValue} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {useCacheMapa, useImpactoMapaQuery, useMapaQuery} from "@/composables/useMapaQuery";

export function useMapas(codigoSubprocesso?: MaybeRefOrGetter<number | null | undefined>) {
    const codigoSubprocessoInterno = ref<number | null>(codigoSubprocesso === undefined ? null : null);
    const codigoConsulta = computed(() =>
        codigoSubprocesso === undefined ? codigoSubprocessoInterno.value : (toValue(codigoSubprocesso) ?? null)
    );
    const mapaQuery = useMapaQuery(codigoConsulta);
    const impactoQuery = useImpactoMapaQuery(codigoConsulta);
    const cacheMapa = useCacheMapa();
    const {carregando, erro, executar} = useAsyncAction();
    const mapaCompleto = computed(() => mapaQuery.data.value ?? null);
    const impactoMapa = computed(() => impactoQuery.data.value ?? null);

    async function carregarMapa(codSubprocesso: number) {
        await executar(async () => {
            if (codigoSubprocesso === undefined) {
                codigoSubprocessoInterno.value = codSubprocesso;
            }
            const codigoAtual = codigoConsulta.value;
            if (codigoAtual === codSubprocesso && (mapaQuery.status.value === "success" || mapaQuery.status.value === "error")) {
                await mapaQuery.refresh(true);
                return;
            }
            await mapaQuery.refetch(true);
        }, "Erro ao carregar mapa completo.", {relancarErro: false});
    }

    async function carregarImpacto(codSubprocesso: number) {
        if (!codSubprocesso) {
            return;
        }

        await executar(async () => {
            if (codigoSubprocesso === undefined) {
                codigoSubprocessoInterno.value = codSubprocesso;
            }
            const codigoAtual = codigoConsulta.value;
            if (codigoAtual === codSubprocesso && (impactoQuery.status.value === "success" || impactoQuery.status.value === "error")) {
                await impactoQuery.refresh(true);
                return;
            }
            await impactoQuery.refetch(true);
        }, "Erro ao verificar impactos.", {relancarErro: false});
    }

    return {
        mapaCompleto: mapaCompleto as Ref<MapaCompleto | null>,
        impactoMapa: impactoMapa as Ref<ImpactoMapa | null>,
        carregando,
        erro,
        sincronizarMapa: cacheMapa.sincronizarMapa,
        sincronizarImpacto: cacheMapa.sincronizarImpacto,
        invalidar: cacheMapa.invalidarMapa,
        invalidarImpacto: cacheMapa.invalidarImpacto,
        resetar: () => {
            cacheMapa.invalidarMapa();
            cacheMapa.invalidarImpacto();
        },
        carregarMapa,
        carregarImpacto,
    };
}
