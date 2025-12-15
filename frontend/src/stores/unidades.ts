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
} from "@/services/unidadesService";
import type {Unidade} from "@/types/tipos";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

export const useUnidadesStore = defineStore("unidades", () => {
    const unidades = ref<Unidade[]>([]);
    const unidade = ref<Unidade | null>(null);
    const isLoading = ref(false);
    const error = ref<string | null>(null);
    const lastError = ref<NormalizedError | null>(null);

    function clearError() {
        lastError.value = null;
        error.value = null;
    }

    async function buscarUnidadesParaProcesso(
        tipoProcesso: string,
        codProcesso?: number,
    ) {
        isLoading.value = true;
        error.value = null;
        lastError.value = null;
        try {
            const response = await buscarArvoreComElegibilidade(
                tipoProcesso,
                codProcesso,
            );
            unidades.value = mapUnidadesArray(response as any) as Unidade[];
        } catch (err: any) {
            lastError.value = normalizeError(err);
            error.value = lastError.value.message;
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    async function buscarUnidade(sigla: string) {
        isLoading.value = true;
        error.value = null;
        lastError.value = null;
        try {
            const response = await buscarUnidadePorSigla(sigla);
            unidade.value = response as unknown as Unidade;
        } catch (err: any) {
            lastError.value = normalizeError(err);
            error.value = lastError.value.message;
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    async function buscarUnidadePorCodigo(codigo: number) {
        isLoading.value = true;
        error.value = null;
        lastError.value = null;
        try {
            const response = await serviceBuscarUnidadePorCodigo(codigo);
            unidade.value = response as unknown as Unidade;
        } catch (err: any) {
            lastError.value = normalizeError(err);
            error.value = lastError.value.message;
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    async function buscarArvoreUnidade(codigo: number) {
        isLoading.value = true;
        error.value = null;
        lastError.value = null;
        try {
            const response = await serviceBuscarArvoreUnidade(codigo);
            unidade.value = response as unknown as Unidade;
        } catch (err: any) {
            lastError.value = normalizeError(err);
            error.value = lastError.value.message;
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    async function obterUnidadesSubordinadas(siglaUnidade: string): Promise<string[]> {
        isLoading.value = true;
        lastError.value = null;
        try {
            return await serviceBuscarSubordinadas(siglaUnidade);
        } catch (err: any) {
            lastError.value = normalizeError(err);
            error.value = lastError.value.message;
            throw err;
        } finally {
            isLoading.value = false;
        }
    }

    async function obterUnidadeSuperior(siglaUnidade: string): Promise<string | null> {
        isLoading.value = true;
        lastError.value = null;
        try {
            return await serviceBuscarSuperior(siglaUnidade);
        } catch (err: any) {
            lastError.value = normalizeError(err);
            error.value = lastError.value.message;
            throw err;
        } finally {
            isLoading.value = false;
        }
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
