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
                (usuarioCodigo: number): AtribuicaoTemporaria[] => {
                    return atribuicoes.value.filter(
                        (a) => a.usuario.codigo === usuarioCodigo,
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
                // response is the array directly from the service
                const data = Array.isArray(response) ? response : (response as any).data;
                
                if (Array.isArray(data)) {
                    atribuicoes.value = data.map((a: any) => ({
                        codigo: a.id,
                        dataInicio: new Date(a.dataInicio).toISOString(),
                        dataFim: new Date(a.dataTermino).toISOString(),
                        dataTermino: new Date(a.dataTermino).toISOString(),
                        usuario: a.usuario || a.servidor, // Handle both just in case
                        unidade: a.unidade,
                        justificativa: a.justificativa
                    })) as AtribuicaoTemporaria[];
                } else {
                    console.error("Expected array but got:", data);
                    atribuicoes.value = [];
                }
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
