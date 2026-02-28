import {defineStore} from "pinia";
import {ref} from "vue";
import {
    buscarArvoreComElegibilidade,
    buscarArvoreUnidade as serviceBuscarArvoreUnidade,
    buscarSubordinadas as serviceBuscarSubordinadas,
    buscarSuperior as serviceBuscarSuperior,
    buscarTodasUnidades as serviceBuscarTodasUnidades,
    buscarUnidadePorCodigo as serviceBuscarUnidadePorCodigo,
    buscarUnidadePorSigla,
    mapUnidadesArray
} from "@/services/unidadeService";
import type {Unidade} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSingleLoading} from "@/composables/useLoadingManager";

export const useUnidadesStore = defineStore("unidades", () => {
    const unidades = ref<Unidade[]>([]);
    const unidade = ref<Unidade | null>(null);
    const loading = useSingleLoading();
    const error = ref<string | null>(null);
    const {lastError, clearError: clearNormalizedError, withErrorHandling} = useErrorHandler();

    function clearError() {
        clearNormalizedError();
        error.value = null;
    }

    async function buscarUnidadesParaProcesso(
        tipoProcesso: string,
        codProcesso?: number,
    ) {
        error.value = null;
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const response = await buscarArvoreComElegibilidade(
                    tipoProcesso,
                    codProcesso,
                );
                unidades.value = mapUnidadesArray(response as any) as Unidade[];
            }).catch((err: any) => {
                error.value = lastError.value?.message || "Erro ao buscar unidades";
                throw err;
            });
        });
    }

    async function buscarTodasAsUnidades() {
        error.value = null;
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const response = await serviceBuscarTodasUnidades();
                unidades.value = mapUnidadesArray(response as any) as Unidade[];
            }).catch((err: any) => {
                error.value = lastError.value?.message || "Erro ao buscar todas as unidades";
                throw err;
            });
        });
    }

    async function buscarUnidade(sigla: string) {
        error.value = null;
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const response = await buscarUnidadePorSigla(sigla);
                unidade.value = response as unknown as Unidade;
            }).catch((err: any) => {
                error.value = lastError.value?.message || "Erro ao buscar unidade";
                throw err;
            });
        });
    }

    async function buscarUnidadePorCodigo(codigo: number) {
        error.value = null;
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const response = await serviceBuscarUnidadePorCodigo(codigo);
                unidade.value = response as unknown as Unidade;
            }).catch((err: any) => {
                error.value = lastError.value?.message || "Erro ao buscar unidade";
                throw err;
            });
        });
    }

    async function buscarArvoreUnidade(codigo: number) {
        error.value = null;
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const response = await serviceBuscarArvoreUnidade(codigo);
                unidade.value = response as unknown as Unidade;
            }).catch((err: any) => {
                error.value = lastError.value?.message || "Erro ao buscar árvore";
                throw err;
            });
        });
    }

    async function obterUnidadesSubordinadas(siglaUnidade: string): Promise<string[]> {
        return loading.withLoading(async () => {
            return withErrorHandling(async () => {
                return await serviceBuscarSubordinadas(siglaUnidade) as string[];
            }).catch((err: any) => {
                error.value = lastError.value?.message || "Erro ao buscar subordinadas";
                throw err;
            }) as Promise<string[]>;
        });
    }

    async function obterUnidadeSuperior(siglaUnidade: string): Promise<string | null> {
        return loading.withLoading(async () => {
            return withErrorHandling(async () => {
                return await serviceBuscarSuperior(siglaUnidade) as string | null;
            }).catch((err: any) => {
                error.value = lastError.value?.message || "Erro ao buscar superior";
                throw err;
            }) as Promise<string | null>;
        });
    }

    return {
        unidades,
        unidade,
        isLoading: loading.isLoading,
        error,
        lastError,
        clearError,
        buscarUnidadesParaProcesso,
        buscarTodasAsUnidades,
        buscarUnidade,
        buscarUnidadePorCodigo,
        buscarArvoreUnidade,
        obterUnidadesSubordinadas,
        obterUnidadeSuperior,
    };
});
