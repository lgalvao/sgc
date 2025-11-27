import {defineStore} from "pinia";
import {ref} from "vue";
import {mapUnidadesArray} from "@/mappers/unidades";
import {
    buscarArvoreComElegibilidade,
    buscarUnidadePorCodigo as serviceBuscarUnidadePorCodigo,
    buscarUnidadePorSigla,
} from "@/services/unidadesService";
import type {Unidade} from "@/types/tipos";

export const useUnidadesStore = defineStore("unidades", () => {
    const unidades = ref<Unidade[]>([]);
    const unidade = ref<Unidade | null>(null);
    const isLoading = ref(false);
    const error = ref<string | null>(null);

    async function fetchUnidadesParaProcesso(
        tipoProcesso: string,
        codProcesso?: number,
    ) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await buscarArvoreComElegibilidade(
                tipoProcesso,
                codProcesso,
            );
            unidades.value = mapUnidadesArray(response as any) as Unidade[];
        } catch (err: any) {
            error.value = "Falha ao carregar unidades: " + err.message;
        } finally {
            isLoading.value = false;
        }
    }

    async function fetchUnidade(sigla: string) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await buscarUnidadePorSigla(sigla);
            unidade.value = response as unknown as Unidade;
        } catch (err: any) {
            error.value = "Falha ao carregar unidade: " + err.message;
        } finally {
            isLoading.value = false;
        }
    }

    async function fetchUnidadePorCodigo(codigo: number) {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await serviceBuscarUnidadePorCodigo(codigo);
            unidade.value = response as unknown as Unidade;
        } catch (err: any) {
            error.value = "Falha ao carregar unidade: " + err.message;
        } finally {
            isLoading.value = false;
        }
    }

    function pesquisarUnidadePorCodigo(
        codigo: number,
        units: Unidade[] = unidades.value,
    ): Unidade | null {
        for (const unit of units) {
            if (unit.codigo === codigo) return unit;
            if (unit.filhas) {
                const found = pesquisarUnidadePorCodigo(codigo, unit.filhas);
                if (found) return found;
            }
        }
        return null;
    }

    function pesquisarUnidadePorSigla(
        sigla: string,
        units: Unidade[] = unidades.value,
    ): Unidade | null {
        for (const unit of units) {
            if (unit.sigla === sigla) return unit;
            if (unit.filhas) {
                const found = pesquisarUnidadePorSigla(sigla, unit.filhas);
                if (found) return found;
            }
        }
        return null;
    }

    function getUnidadesSubordinadas(siglaUnidade: string): string[] {
        const unidadesEncontradas: string[] = [];
        const stack: Unidade[] = [];

        const unidadeRaiz = pesquisarUnidadePorSigla(siglaUnidade);
        if (unidadeRaiz) {
            stack.push(unidadeRaiz);
            while (stack.length > 0) {
                const currentUnidade = stack.pop()!;
                unidadesEncontradas.push(currentUnidade.sigla);
                if (currentUnidade.filhas) {
                    for (let i = currentUnidade.filhas.length - 1; i >= 0; i--) {
                        stack.push(currentUnidade.filhas[i]);
                    }
                }
            }
        }
        return unidadesEncontradas;
    }

    function getUnidadeSuperior(siglaUnidade: string): string | null {
        const stack: { unit: Unidade; parentSigla: string | null }[] = [];

        for (const u of unidades.value) {
            stack.push({ unit: u, parentSigla: null });
        }

        while (stack.length > 0) {
            const { unit: currentUnidade, parentSigla: currentParentSigla } =
                stack.pop()!;

            if (currentUnidade.sigla === siglaUnidade) {
                return currentParentSigla;
            }

            if (currentUnidade.filhas) {
                for (let i = currentUnidade.filhas.length - 1; i >= 0; i--) {
                    stack.push({
                        unit: currentUnidade.filhas[i],
                        parentSigla: currentUnidade.sigla,
                    });
                }
            }
        }
        return null;
    }

    function getUnidadeImediataSuperior(siglaUnidade: string): string | null {
        return getUnidadeSuperior(siglaUnidade);
    }

    return {
        unidades,
        unidade,
        isLoading,
        error,
        fetchUnidadesParaProcesso,
        fetchUnidade,
        fetchUnidadePorCodigo,
        pesquisarUnidadePorCodigo,
        pesquisarUnidadePorSigla,
        getUnidadesSubordinadas,
        getUnidadeSuperior,
        getUnidadeImediataSuperior,
    };
});
