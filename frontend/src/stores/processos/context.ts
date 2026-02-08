import {defineStore} from "pinia";
import {ref} from "vue";
import type {ProcessoResumo, SubprocessoElegivel,} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import * as processoService from "@/services/processoService";
import {useProcessosCoreStore} from "./core";

export const useProcessosContextStore = defineStore("processos-context", () => {
    const subprocessosElegiveis = ref<SubprocessoElegivel[]>([]);
    const { lastError, clearError, withErrorHandling } = useErrorHandler();
    const coreStore = useProcessosCoreStore();

    function obterUnidadesDoProcesso(codigoProcesso: number): ProcessoResumo[] {
        if (coreStore.processoDetalhe && coreStore.processoDetalhe.codigo === codigoProcesso) {
            return coreStore.processoDetalhe.resumoSubprocessos;
        }
        return [];
    }

    async function buscarContextoCompleto(codigoProcesso: number) {
        return withErrorHandling(async () => {
            coreStore.setProcessoDetalhe(null); // Limpa estado anterior
            const data = await processoService.buscarContextoCompleto(codigoProcesso);
            coreStore.setProcessoDetalhe(data);
            subprocessosElegiveis.value = data.elegiveis;
        });
    }

    async function buscarSubprocessosElegiveis(codigoProcesso: number) {
        return withErrorHandling(async () => {
            subprocessosElegiveis.value =
                await processoService.buscarSubprocessosElegiveis(codigoProcesso);
        });
    }

    return {
        subprocessosElegiveis,
        lastError,
        obterUnidadesDoProcesso,
        buscarContextoCompleto,
        buscarSubprocessosElegiveis,
        clearError,
    };
});
