import { computed } from "vue";
import { useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import { useMapasStore } from "@/stores/mapas";
import { useUnidadesStore } from "@/stores/unidades";
import { useProcessosStore } from "@/stores/processos";
import { useAnalisesStore } from "@/stores/analises";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { usePerfil } from "@/composables/usePerfil";

export function useVisMapaState() {
    const route = useRoute();
    const sigla = computed(() => route.params.siglaUnidade as string);
    const codProcesso = computed(() => Number(route.params.codProcesso));

    const unidadesStore = useUnidadesStore();
    const mapaStore = useMapasStore();
    const processosStore = useProcessosStore();
    const analisesStore = useAnalisesStore();
    const subprocessosStore = useSubprocessosStore();
    const { perfilSelecionado } = usePerfil();

    const { mapaVisualizacao: mapa } = storeToRefs(mapaStore);

    const unidade = computed(() => unidadesStore.unidade);

    const subprocesso = computed(() => {
        if (!processosStore.processoDetalhe) return null;
        return processosStore.processoDetalhe.unidades.find((u) => u.sigla === sigla.value);
    });

    const processo = computed(() => processosStore.processoDetalhe);
    const codSubprocesso = computed(() => subprocesso.value?.codSubprocesso);

    const permissoes = computed(() => subprocessosStore.subprocessoDetalhe?.permissoes || null);
    const podeValidar = computed(() => permissoes.value?.podeValidarMapa || false);
    const podeAnalisar = computed(() => {
        return (
            (permissoes.value?.podeAceitarMapa || false) ||
            (permissoes.value?.podeDevolverMapa || false) ||
            (permissoes.value?.podeHomologarMapa || false)
        );
    });
    const podeVerSugestoes = computed(() => permissoes.value?.podeApresentarSugestoes || false);

    const historicoAnalise = computed(() => {
        if (!codSubprocesso.value) return [];
        return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value) || [];
    });

    const temHistoricoAnalise = computed(() => historicoAnalise.value.length > 0);

    return {
        unidadesStore,
        mapaStore,
        processosStore,
        subprocessosStore,
        analisesStore,
        sigla,
        codProcesso,
        perfilSelecionado,
        mapa,
        unidade,
        subprocesso,
        processo,
        codSubprocesso,
        permissoes,
        podeValidar,
        podeAnalisar,
        podeVerSugestoes,
        historicoAnalise,
        temHistoricoAnalise,
    };
}
