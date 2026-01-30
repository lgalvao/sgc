import {defineStore} from "pinia";
import {ref} from "vue";
import {mapUnidadesArray} from "@/mappers/unidades";
import {
    buscarArvoreComElegibilidade,
    buscarArvoreUnidade as serviceBuscarArvoreUnidade,
    buscarSubordinadas as serviceBuscarSubordinadas,
    buscarSuperior as serviceBuscarSuperior,
    buscarUnidadePorCodigo as serviceBuscarUnidadePorCodigo,
    buscarUnidadePorSigla,
} from "@/services/unidadeService";
import type {Unidade} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";

export const useUnidadesStore = defineStore("unidades", () => {
    const unidades = ref<Unidade[]>([]);
    const unidade = ref<Unidade | null>(null);
    const isLoading = ref(false);
    const error = ref<string | null>(null);
    const { lastError, clearError: clearNormalizedError, withErrorHandling } = useErrorHandler();

    function clearError() {
        clearNormalizedError();
        error.value = null;
    }

    async function buscarUnidadesParaProcesso(
        tipoProcesso: string,
        codProcesso?: number,
    ) {
        isLoading.value = true;
        error.value = null;
        await withErrorHandling(async () => {
            const response = await buscarArvoreComElegibilidade(
                tipoProcesso,
                codProcesso,
            );
            unidades.value = mapUnidadesArray(response as any) as Unidade[];
        }).catch((err: any) => {
            error.value = lastError.value?.message || "Erro ao buscar unidades";
            throw err;
        }).finally(() => {
            isLoading.value = false;
        });
    }

    async function buscarUnidade(sigla: string) {
        isLoading.value = true;
        error.value = null;
        await withErrorHandling(async () => {
            const response = await buscarUnidadePorSigla(sigla);
            unidade.value = response as unknown as Unidade;
        }).catch((err: any) => {
            error.value = lastError.value?.message || "Erro ao buscar unidade";
            throw err;
        }).finally(() => {
            isLoading.value = false;
        });
    }

    async function buscarUnidadePorCodigo(codigo: number) {
        isLoading.value = true;
        error.value = null;
        await withErrorHandling(async () => {
            const response = await serviceBuscarUnidadePorCodigo(codigo);
            unidade.value = response as unknown as Unidade;
        }).catch((err: any) => {
            error.value = lastError.value?.message || "Erro ao buscar unidade";
            throw err;
        }).finally(() => {
            isLoading.value = false;
        });
    }

    async function buscarArvoreUnidade(codigo: number) {
        isLoading.value = true;
        error.value = null;
        await withErrorHandling(async () => {
            const response = await serviceBuscarArvoreUnidade(codigo);
            unidade.value = response as unknown as Unidade;
        }).catch((err: any) => {
            error.value = lastError.value?.message || "Erro ao buscar árvore";
            throw err;
        }).finally(() => {
            isLoading.value = false;
        });
    }

    async function obterUnidadesSubordinadas(siglaUnidade: string): Promise<string[]> {
        isLoading.value = true;
        return withErrorHandling(async () => {
            return await serviceBuscarSubordinadas(siglaUnidade) as string[];
        }).catch((err: any) => {
            error.value = lastError.value?.message || "Erro ao buscar subordinadas";
            throw err;
        }).finally(() => {
            isLoading.value = false;
        }) as Promise<string[]>;
    }

    async function obterUnidadeSuperior(siglaUnidade: string): Promise<string | null> {
        isLoading.value = true;
        return withErrorHandling(async () => {
            return await serviceBuscarSuperior(siglaUnidade) as string | null;
        }).catch((err: any) => {
            error.value = lastError.value?.message || "Erro ao buscar superior";
            throw err;
        }).finally(() => {
            isLoading.value = false;
        }) as Promise<string | null>;
    }

    return {
        unidades,
        unidade,
        isLoading,
        error,
        lastError,
        clearError,
        buscarUnidadesParaProcesso,
        buscarUnidade,
        buscarUnidadePorCodigo,
        buscarArvoreUnidade,
        obterUnidadesSubordinadas,
        obterUnidadeSuperior,
    };
});
