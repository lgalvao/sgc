import { computed, onMounted, ref, type ComputedRef, type Ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useMapasStore } from "@/stores/mapas";
import { useUnidadesStore } from "@/stores/unidades";
import { useProcessosStore } from "@/stores/processos";
import { useAnalisesStore } from "@/stores/analises";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { useFeedbackStore } from "@/stores/feedback";
import { usePerfil } from "@/composables/usePerfil";
import type { AnaliseCadastro, AnaliseValidacao, MapaVisualizacao, Unidade, SubprocessoPermissoes } from "@/types/tipos";
import { TipoProcesso } from "@/types/tipos";
import logger from "@/utils/logger";

type Analise = AnaliseCadastro | AnaliseValidacao;

export interface UseVisMapa {
    // Estado
    perfilSelecionado: ComputedRef<string | null>;
    permissoes: ComputedRef<SubprocessoPermissoes | null>;
    mapa: Ref<MapaVisualizacao | null>;
    unidade: ComputedRef<Unidade | null>;
    subprocesso: ComputedRef<any>;
    podeValidar: ComputedRef<boolean>;
    podeAnalisar: ComputedRef<boolean>;
    podeVerSugestoes: ComputedRef<boolean>;
    temHistoricoAnalise: ComputedRef<boolean>;
    historicoAnalise: ComputedRef<Analise[]>;

    // Modais
    mostrarModalAceitar: Ref<boolean>;
    mostrarModalSugestoes: Ref<boolean>;
    mostrarModalVerSugestoes: Ref<boolean>;
    mostrarModalValidar: Ref<boolean>;
    mostrarModalDevolucao: Ref<boolean>;
    mostrarModalHistorico: Ref<boolean>;
    sugestoes: Ref<string>;
    sugestoesVisualizacao: Ref<string>;
    observacaoDevolucao: Ref<string>;

    // Loading
    isLoading: Ref<boolean>;

    // Métodos CRUD
    confirmarSugestoes: () => Promise<void>;
    confirmarValidacao: () => Promise<void>;
    confirmarAceitacao: (observacoes?: string) => Promise<void>;
    confirmarDevolucao: () => Promise<void>;

    // Métodos de modais
    abrirModalAceitar: () => void;
    fecharModalAceitar: () => void;
    abrirModalSugestoes: () => void;
    fecharModalSugestoes: () => void;
    verSugestoes: () => void;
    fecharModalVerSugestoes: () => void;
    abrirModalValidar: () => void;
    fecharModalValidar: () => void;
    abrirModalDevolucao: () => void;
    fecharModalDevolucao: () => void;
    abrirModalHistorico: () => Promise<void>;
    fecharModalHistorico: () => void;
}

/**
 * Composable unificado para visualização de mapa.
 * 
 * Consolida funcionalidades de:
 * - Estado do subprocesso e mapa
 * - Validação e aceitação de mapa
 * - Apresentação de sugestões
 * - Devolução de mapa
 * - Gerenciamento de modais
 */
export function useVisMapa(): UseVisMapa {
    // Stores
    const route = useRoute();
    const router = useRouter();
    const unidadesStore = useUnidadesStore();
    const mapaStore = useMapasStore();
    const processosStore = useProcessosStore();
    const analisesStore = useAnalisesStore();
    const subprocessosStore = useSubprocessosStore();
    const feedbackStore = useFeedbackStore();
    const { perfilSelecionado } = usePerfil();
    const { mapaVisualizacao: mapa } = storeToRefs(mapaStore);

    // Estado base
    const sigla = computed(() => route.params.siglaUnidade as string);
    const codProcesso = computed(() => Number(route.params.codProcesso));

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

    // Modais
    const mostrarModalAceitar = ref(false);
    const mostrarModalSugestoes = ref(false);
    const mostrarModalVerSugestoes = ref(false);
    const mostrarModalValidar = ref(false);
    const mostrarModalDevolucao = ref(false);
    const mostrarModalHistorico = ref(false);
    const sugestoes = ref("");
    const sugestoesVisualizacao = ref("");
    const observacaoDevolucao = ref("");

    // Loading
    const isLoading = ref(false);

    // ========== CRUD - Sugestões ==========

    async function confirmarSugestoes() {
        if (!codSubprocesso.value) return;
        isLoading.value = true;
        try {
            await processosStore.apresentarSugestoes(codSubprocesso.value, {
                sugestoes: sugestoes.value,
            });
            fecharModalSugestoes();
            feedbackStore.show(
                "Sugestões apresentadas",
                "Sugestões submetidas para análise da unidade superior",
                "success"
            );
            await router.push({ name: "Painel" });
        } catch {
            feedbackStore.show("Erro ao apresentar sugestões", "Ocorreu um erro. Tente novamente.", "danger");
        } finally {
            isLoading.value = false;
        }
    }

    // ========== CRUD - Validação ==========

    async function confirmarValidacao() {
        if (!codSubprocesso.value) return;
        isLoading.value = true;
        try {
            await processosStore.validarMapa(codSubprocesso.value);
            fecharModalValidar();
            feedbackStore.show("Mapa validado", "Mapa validado e submetido para análise da unidade superior", "success");
            await router.push({ name: "Painel" });
        } catch {
            feedbackStore.show("Erro ao validar mapa", "Ocorreu um erro. Tente novamente.", "danger");
        } finally {
            isLoading.value = false;
        }
    }

    // ========== CRUD - Aceitação ==========

    async function confirmarAceitacao(observacoes?: string) {
        if (!codSubprocesso.value) return;
        isLoading.value = true;
        const isHomologacao = permissoes.value?.podeHomologarMapa || perfilSelecionado.value === "ADMIN";

        try {
            if (isHomologacao) {
                if (processo.value?.tipo === TipoProcesso.REVISAO) {
                    await subprocessosStore.homologarRevisaoCadastro(codSubprocesso.value, {
                        observacoes: observacoes || "",
                    });
                } else {
                    await processosStore.homologarValidacao(codSubprocesso.value);
                }
            } else {
                await processosStore.aceitarValidacao(codSubprocesso.value, {
                    observacoes: observacoes || "",
                });
            }
            fecharModalAceitar();
            await router.push({ name: "Painel" });
        } catch (error) {
            logger.error(error);
            feedbackStore.show("Erro", "Erro ao realizar a operação.", "danger");
        } finally {
            isLoading.value = false;
        }
    }

    // ========== CRUD - Devolução ==========

    async function confirmarDevolucao() {
        if (!codSubprocesso.value) return;
        isLoading.value = true;
        try {
            await subprocessosStore.devolverRevisaoCadastro(codSubprocesso.value, {
                observacoes: observacaoDevolucao.value,
            });
            fecharModalDevolucao();
            await router.push({ name: "Painel" });
        } catch (error) {
            logger.error(error);
            feedbackStore.show("Erro", "Erro ao devolver.", "danger");
        } finally {
            isLoading.value = false;
        }
    }

    // ========== Modais ==========

    function abrirModalAceitar() {
        mostrarModalAceitar.value = true;
    }

    function fecharModalAceitar() {
        mostrarModalAceitar.value = false;
    }

    function abrirModalSugestoes() {
        mostrarModalSugestoes.value = true;
    }

    function fecharModalSugestoes() {
        mostrarModalSugestoes.value = false;
        sugestoes.value = "";
    }

    function verSugestoes() {
        sugestoesVisualizacao.value = mapa.value?.sugestoes || "Nenhuma sugestão registrada.";
        mostrarModalVerSugestoes.value = true;
    }

    function fecharModalVerSugestoes() {
        mostrarModalVerSugestoes.value = false;
        sugestoesVisualizacao.value = "";
    }

    function abrirModalValidar() {
        mostrarModalValidar.value = true;
    }

    function fecharModalValidar() {
        mostrarModalValidar.value = false;
    }

    function abrirModalDevolucao() {
        mostrarModalDevolucao.value = true;
    }

    function fecharModalDevolucao() {
        mostrarModalDevolucao.value = false;
        observacaoDevolucao.value = "";
    }

    async function abrirModalHistorico() {
        if (codSubprocesso.value) {
            await analisesStore.buscarAnalisesCadastro(codSubprocesso.value);
        }
        mostrarModalHistorico.value = true;
    }

    function fecharModalHistorico() {
        mostrarModalHistorico.value = false;
    }

    // ========== Inicialização ==========

    onMounted(async () => {
        await unidadesStore.buscarUnidade(sigla.value);
        await processosStore.buscarProcessoDetalhe(codProcesso.value);
        if (codSubprocesso.value) {
            await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value);
            await mapaStore.buscarMapaVisualizacao(codSubprocesso.value);
        }
    });

    return {
        // Estado
        perfilSelecionado,
        permissoes,
        mapa,
        unidade,
        subprocesso,
        podeValidar,
        podeAnalisar,
        podeVerSugestoes,
        temHistoricoAnalise,
        historicoAnalise,

        // Modais
        mostrarModalAceitar,
        mostrarModalSugestoes,
        mostrarModalVerSugestoes,
        mostrarModalValidar,
        mostrarModalDevolucao,
        mostrarModalHistorico,
        sugestoes,
        sugestoesVisualizacao,
        observacaoDevolucao,

        // Loading
        isLoading,

        // CRUD
        confirmarSugestoes,
        confirmarValidacao,
        confirmarAceitacao,
        confirmarDevolucao,

        // Modais
        abrirModalAceitar,
        fecharModalAceitar,
        abrirModalSugestoes,
        fecharModalSugestoes,
        verSugestoes,
        fecharModalVerSugestoes,
        abrirModalValidar,
        fecharModalValidar,
        abrirModalDevolucao,
        fecharModalDevolucao,
        abrirModalHistorico,
        fecharModalHistorico,
    };
}
