import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {buscarTodasAtribuicoes} from "@/services/atribuicaoTemporariaService";
import type {AtribuicaoTemporaria} from "@/types/tipos";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

export const useAtribuicaoTemporariaStore = defineStore(
    "atribuicaoTemporaria",
    () => {
        const atribuicoes = ref<AtribuicaoTemporaria[]>([]);
        const isLoading = ref(false);
        const error = ref<string | null>(null); // Keeping for backward compatibility
        const lastError = ref<NormalizedError | null>(null);

        function clearError() {
            lastError.value = null;
            error.value = null;
        }

        const obterAtribuicoesPorServidor = computed(
            () =>
                (servidorId: number): AtribuicaoTemporaria[] => {
                    return atribuicoes.value.filter(
                        (a) => a.servidor.codigo === servidorId,
                    );
                },
        );

        const obterAtribuicoesPorUnidade = computed(
            () =>
                (unidadeSigla: string): AtribuicaoTemporaria[] => {
                    return atribuicoes.value.filter(
                        (a) => a.unidade.sigla === unidadeSigla,
                    );
                },
        );

        async function buscarAtribuicoes() {
            isLoading.value = true;
            error.value = null;
            lastError.value = null;
            try {
                const response = await buscarTodasAtribuicoes();
                atribuicoes.value = (response as any).data.map((a: any) => ({
                    ...a,
                    dataInicio: new Date(a.dataInicio).toISOString(),
                    dataFim: new Date(a.dataTermino).toISOString(),
                    dataTermino: new Date(a.dataTermino).toISOString(),
                    servidor: a.servidor,
                    unidade: a.unidade,
                })) as AtribuicaoTemporaria[];
            } catch (err: any) {
                lastError.value = normalizeError(err);
                error.value = lastError.value.message;
                throw err;
            } finally {
                isLoading.value = false;
            }
        }

        return {
            atribuicoes,
            isLoading,
            error,
            lastError,
            clearError,
            obterAtribuicoesPorServidor,
            obterAtribuicoesPorUnidade,
            buscarAtribuicoes,
        };
    },
);
