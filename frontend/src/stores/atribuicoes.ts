import {defineStore} from "pinia";
import {ref} from "vue";
import {
    buscarTodasAtribuicoes,
    criarAtribuicaoTemporaria as serviceCriarAtribuicao,
    type CriarAtribuicaoTemporariaRequest,
} from "@/services/atribuicaoTemporariaService";
import type {AtribuicaoTemporaria} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSingleLoading} from "@/composables/useLoadingManager";
import {logger} from "@/utils";

export const useAtribuicaoTemporariaStore = defineStore(
    "atribuicaoTemporaria",
    () => {
        const atribuicoes = ref<AtribuicaoTemporaria[]>([]);
        const loading = useSingleLoading();
        const error = ref<string | null>(null); // Keeping for backward compatibility
        const { lastError, clearError: clearNormalizedError, withErrorHandling } = useErrorHandler();

        function clearError() {
            clearNormalizedError();
            error.value = null;
        }

        function obterAtribuicoesPorServidor(usuarioCodigo: number): AtribuicaoTemporaria[] {
            return atribuicoes.value.filter(
                (a) => a.usuario.codigo === usuarioCodigo,
            );
        }

        function obterAtribuicoesPorUnidade(unidadeSigla: string): AtribuicaoTemporaria[] {
            return atribuicoes.value.filter(
                (a) => a.unidade.sigla === unidadeSigla,
            );
        }

        async function buscarAtribuicoes() {
            error.value = null;
            await loading.withLoading(async () => {
                await withErrorHandling(async () => {
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
                        logger.error("Expected array but got:", data);
                        atribuicoes.value = [];
                    }
                }, () => {
                    error.value = lastError.value?.message || "Erro ao buscar atribuições";
                });
            });
        }

        async function criarAtribuicaoTemporaria(
            codUnidade: number,
            request: CriarAtribuicaoTemporariaRequest
        ) {
            error.value = null;
            await loading.withLoading(async () => {
                await withErrorHandling(async () => {
                    await serviceCriarAtribuicao(codUnidade, request);
                    await buscarAtribuicoes();
                }, () => {
                    error.value = lastError.value?.message || "Erro ao criar atribuição";
                });
            });
        }

        return {
            atribuicoes,
            isLoading: loading.isLoading,
            error,
            lastError,
            clearError,
            obterAtribuicoesPorServidor,
            obterAtribuicoesPorUnidade,
            buscarAtribuicoes,
            criarAtribuicaoTemporaria,
        };
    },
);
