import { ref } from "vue";
import { useRouter } from "vue-router";
import { useProcessosStore } from "@/stores/processos";
import { useFeedbackStore } from "@/stores/feedback";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { TipoProcesso } from "@/types/tipos";
import logger from "@/utils/logger";

export function useVisMapaCrud() {
    const router = useRouter();
    const processosStore = useProcessosStore();
    const feedbackStore = useFeedbackStore();
    const subprocessosStore = useSubprocessosStore();

    const isLoading = ref(false);

    async function confirmarSugestoes(codSubprocesso: number | undefined, sugestoes: string, fecharModal: () => void) {
        if (!codSubprocesso) return;
        isLoading.value = true;
        try {
            await processosStore.apresentarSugestoes(codSubprocesso, {
                sugestoes: sugestoes,
            });
            fecharModal();
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

    async function confirmarValidacao(codSubprocesso: number | undefined, fecharModal: () => void) {
        if (!codSubprocesso) return;
        isLoading.value = true;
        try {
            await processosStore.validarMapa(codSubprocesso);
            fecharModal();
            feedbackStore.show("Mapa validado", "Mapa validado e submetido para análise da unidade superior", "success");
            await router.push({ name: "Painel" });
        } catch {
            feedbackStore.show("Erro ao validar mapa", "Ocorreu um erro. Tente novamente.", "danger");
        } finally {
            isLoading.value = false;
        }
    }

    async function confirmarAceitacao(
        codSubprocesso: number | undefined,
        podeHomologar: boolean | undefined,
        perfilSelecionado: string | null,
        tipoProcesso: TipoProcesso | undefined,
        observacoes: string | undefined,
        fecharModal: () => void
    ) {
        if (!codSubprocesso) return;
        isLoading.value = true;
        const isHomologacao = podeHomologar || perfilSelecionado === "ADMIN";

        try {
            if (isHomologacao) {
                if (tipoProcesso === TipoProcesso.REVISAO) {
                    await subprocessosStore.homologarRevisaoCadastro(codSubprocesso, {
                        observacoes: observacoes || "",
                    });
                } else {
                    await processosStore.homologarValidacao(codSubprocesso);
                }
            } else {
                await processosStore.aceitarValidacao(codSubprocesso, {
                    observacoes: observacoes || "",
                });
            }
            fecharModal();
            await router.push({ name: "Painel" });
        } catch (error) {
            logger.error(error);
            feedbackStore.show("Erro", "Erro ao realizar a operação.", "danger");
        } finally {
            isLoading.value = false;
        }
    }

    async function confirmarDevolucao(
        codSubprocesso: number | undefined,
        observacaoDevolucao: string,
        fecharModal: () => void
    ) {
        if (!codSubprocesso) return;
        isLoading.value = true;
        try {
            await subprocessosStore.devolverRevisaoCadastro(codSubprocesso, {
                observacoes: observacaoDevolucao,
            });
            fecharModal();
            await router.push({ name: "Painel" });
        } catch (error) {
            logger.error(error);
            feedbackStore.show("Erro", "Erro ao devolver.", "danger");
        } finally {
            isLoading.value = false;
        }
    }

    return {
        isLoading,
        confirmarSugestoes,
        confirmarValidacao,
        confirmarAceitacao,
        confirmarDevolucao,
    };
}
