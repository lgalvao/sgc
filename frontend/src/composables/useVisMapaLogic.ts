import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useMapasStore } from "@/stores/mapas";
import { useUnidadesStore } from "@/stores/unidades";
import { useProcessosStore } from "@/stores/processos";
import { useAnalisesStore } from "@/stores/analises";
import { useFeedbackStore } from "@/stores/feedback";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { usePerfil } from "@/composables/usePerfil";
import { SituacaoSubprocesso, TipoProcesso } from "@/types/tipos";
import logger from "@/utils/logger";


export function useVisMapaLogic() {
    const route = useRoute();
    const router = useRouter();
    const sigla = computed(() => route.params.siglaUnidade as string);
    const codProcesso = computed(() => Number(route.params.codProcesso));

    const unidadesStore = useUnidadesStore();
    const mapaStore = useMapasStore();
    const processosStore = useProcessosStore();
    const feedbackStore = useFeedbackStore();
    const analisesStore = useAnalisesStore();
    const subprocessosStore = useSubprocessosStore();
    const { perfilSelecionado } = usePerfil();

    const { mapaVisualizacao: mapa } = storeToRefs(mapaStore);

    const mostrarModalAceitar = ref(false);
    const mostrarModalSugestoes = ref(false);
    const mostrarModalVerSugestoes = ref(false);
    const mostrarModalValidar = ref(false);
    const mostrarModalDevolucao = ref(false);
    const mostrarModalHistorico = ref(false);
    const sugestoes = ref("");
    const sugestoesVisualizacao = ref("");
    const observacaoDevolucao = ref("");
    const isLoading = ref(false);

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
        return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value);
    });

    const temHistoricoAnalise = computed(() => historicoAnalise.value.length > 0);

    onMounted(async () => {
        await unidadesStore.buscarUnidade(sigla.value);
        await processosStore.buscarProcessoDetalhe(codProcesso.value);
        if (codSubprocesso.value) {
            await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value);
            await mapaStore.buscarMapaVisualizacao(codSubprocesso.value);
        }
    });

    async function confirmarSugestoes() {
        if (!codSubprocesso.value) return;
        isLoading.value = true;
        try {
            await processosStore.apresentarSugestoes(codSubprocesso.value, {
                sugestoes: sugestoes.value,
            });
            mostrarModalSugestoes.value = false;
            sugestoes.value = "";
            feedbackStore.show(
                "Sugestões apresentadas",
                "Sugestões submetidas para análise da unidade superior",
                "success",
            );
            await router.push({ name: "Painel" });
        } catch {
            feedbackStore.show("Erro ao apresentar sugestões", "Ocorreu um erro. Tente novamente.", "danger");
        } finally {
            isLoading.value = false;
        }
    }

    async function confirmarValidacao() {
        if (!codSubprocesso.value) return;
        isLoading.value = true;
        try {
            await processosStore.validarMapa(codSubprocesso.value);
            mostrarModalValidar.value = false;
            feedbackStore.show("Mapa validado", "Mapa validado e submetido para análise da unidade superior", "success");
            await router.push({ name: "Painel" });
        } catch {
            feedbackStore.show("Erro ao validar mapa", "Ocorreu um erro. Tente novamente.", "danger");
        } finally {
            isLoading.value = false;
        }
    }

    async function confirmarAceitacao(observacoes?: string) {
        if (!codSubprocesso.value) return;
        isLoading.value = true;
        const perfil = perfilSelecionado.value;
        const isHomologacao = permissoes.value?.podeHomologarMapa || perfil === "ADMIN";
        const tipoProcessoEnv = processo.value?.tipo;

        try {
            if (isHomologacao) {
                if (tipoProcessoEnv === TipoProcesso.REVISAO) {
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
            mostrarModalAceitar.value = false;
            await router.push({ name: "Painel" });
        } catch (error) {
            logger.error(error);
            feedbackStore.show("Erro", "Erro ao realizar a operação.", "danger");
        } finally {

            isLoading.value = false;
        }
    }

    async function confirmarDevolucao() {
        if (!codSubprocesso.value) return;
        isLoading.value = true;
        try {
            await subprocessosStore.devolverRevisaoCadastro(codSubprocesso.value, {
                observacoes: observacaoDevolucao.value,
            });
            mostrarModalDevolucao.value = false;
            observacaoDevolucao.value = "";
            await router.push({ name: "Painel" });
        } catch (error) {
            logger.error(error);
            feedbackStore.show("Erro", "Erro ao devolver.", "danger");
        } finally {

            isLoading.value = false;
        }
    }

    return {
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
        mostrarModalAceitar,
        mostrarModalSugestoes,
        mostrarModalVerSugestoes,
        mostrarModalValidar,
        mostrarModalDevolucao,
        mostrarModalHistorico,
        sugestoes,
        sugestoesVisualizacao,
        observacaoDevolucao,
        isLoading,
        confirmarSugestoes,
        confirmarValidacao,
        confirmarAceitacao,
        confirmarDevolucao,
        abrirModalAceitar: () => (mostrarModalAceitar.value = true),
        fecharModalAceitar: () => (mostrarModalAceitar.value = false),
        abrirModalSugestoes: () => (mostrarModalSugestoes.value = true),
        fecharModalSugestoes: () => {
            mostrarModalSugestoes.value = false;
            sugestoes.value = "";
        },
        verSugestoes: () => {
            sugestoesVisualizacao.value = mapa.value?.sugestoes || "Nenhuma sugestão registrada.";
            mostrarModalVerSugestoes.value = true;
        },
        fecharModalVerSugestoes: () => {
            mostrarModalVerSugestoes.value = false;
            sugestoesVisualizacao.value = "";
        },
        abrirModalValidar: () => (mostrarModalValidar.value = true),
        fecharModalValidar: () => (mostrarModalValidar.value = false),
        abrirModalDevolucao: () => (mostrarModalDevolucao.value = true),
        fecharModalDevolucao: () => {
            mostrarModalDevolucao.value = false;
            observacaoDevolucao.value = "";
        },
        abrirModalHistorico: async () => {
          if (codSubprocesso.value) {
            await analisesStore.buscarAnalisesCadastro(codSubprocesso.value);
          }
          mostrarModalHistorico.value = true;
        },
        fecharModalHistorico: () => (mostrarModalHistorico.value = false),
    };
}
