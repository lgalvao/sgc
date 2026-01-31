import { computed, ref, type ComputedRef, type Ref } from "vue";
import { useRouter, type Router } from "vue-router";
import { storeToRefs } from "pinia";
import { useAtividadesStore } from "@/stores/atividades";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { useMapasStore } from "@/stores/mapas";
import { useUnidadesStore } from "@/stores/unidades";
import { useAnalisesStore } from "@/stores/analises";
import { usePerfil } from "@/composables/usePerfil";
import type { Atividade, AnaliseCadastro, AnaliseValidacao, SubprocessoPermissoes, SubprocessoDetalhe, ImpactoMapa } from "@/types/tipos";
import { Perfil, TipoProcesso } from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

export interface CadAtividadesState {
    router: Router;
    isChefe: ComputedRef<boolean>;
    codSubprocesso: Ref<number | null>;
    codMapa: ComputedRef<number | null>;
    subprocesso: ComputedRef<SubprocessoDetalhe | null>;
    nomeUnidade: ComputedRef<string>;
    permissoes: ComputedRef<SubprocessoPermissoes | null>;
    isRevisao: ComputedRef<boolean>;
    atividades: ComputedRef<Atividade[]>;
    historicoAnalises: ComputedRef<Analise[]>;
    podeVerImpacto: ComputedRef<boolean>;
    impactoMapa: Ref<ImpactoMapa | null>;
}

export function useCadAtividadesState(): CadAtividadesState {
    const router = useRouter();
    const atividadesStore = useAtividadesStore();
    const unidadesStore = useUnidadesStore();
    const subprocessosStore = useSubprocessosStore();
    const analisesStore = useAnalisesStore();
    const mapasStore = useMapasStore();
    const { impactoMapa } = storeToRefs(mapasStore);

    const { perfilSelecionado } = usePerfil();
    const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);

    const codSubprocesso = ref<number | null>(null);
    const codMapa = computed(() => mapasStore.mapaCompleto?.codigo || null);
    const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
    const nomeUnidade = computed(() => unidadesStore.unidade?.nome || "");
    const permissoes = computed(() => subprocesso.value?.permissoes || null);
    const isRevisao = computed(() => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO);

    const atividades = computed(() => {
        if (codSubprocesso.value === null) return [];
        return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value);
    });

    const historicoAnalises = computed(() => {
        if (!codSubprocesso.value) return [];
        return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value);
    });

    const podeVerImpacto = computed(() => permissoes.value?.podeVisualizarImpacto ?? false);

    return {
        router,
        isChefe,
        codSubprocesso,
        codMapa,
        subprocesso,
        nomeUnidade,
        permissoes,
        isRevisao,
        atividades,
        historicoAnalises,
        podeVerImpacto,
        impactoMapa,
    };
}
