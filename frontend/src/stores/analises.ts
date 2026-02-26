import {defineStore} from "pinia";
import {ref} from "vue";
import * as subprocessoService from "@/services/subprocessoService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSingleLoading} from "@/composables/useLoadingManager";

import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

export const useAnalisesStore = defineStore("analises", () => {
    const analisesPorSubprocesso = ref(new Map<number, Analise[]>());
    const { lastError, clearError, withErrorHandling } = useErrorHandler();
    const loading = useSingleLoading();

    function obterAnalisesPorSubprocesso(codSubprocesso: number): Analise[] {
        return analisesPorSubprocesso.value.get(codSubprocesso) || [];
    }

    async function buscarAnalisesCadastro(codSubprocesso: number) {
        if (loading.isLoading.value) return; // Previne race conditions
        
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const analises = await subprocessoService.listarAnalisesCadastro(codSubprocesso);
                const atuais = analisesPorSubprocesso.value.get(codSubprocesso) || [];
                const outras = atuais.filter((a) => a.tipo !== "CADASTRO");
                analisesPorSubprocesso.value.set(codSubprocesso, [...outras, ...analises]);
            });
        });
    }

    async function buscarAnalisesValidacao(codSubprocesso: number) {
        if (loading.isLoading.value) return; // Previne race conditions
        
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const analises =
                    await subprocessoService.listarAnalisesValidacao(codSubprocesso);
                const atuais = analisesPorSubprocesso.value.get(codSubprocesso) || [];
                const outras = atuais.filter((a) => a.tipo !== "VALIDACAO");
                analisesPorSubprocesso.value.set(codSubprocesso, [...outras, ...analises]);
            });
        });
    }

    return {
        analisesPorSubprocesso,
        lastError,
        isLoading: loading.isLoading,
        obterAnalisesPorSubprocesso,
        buscarAnalisesCadastro,
        buscarAnalisesValidacao,
        clearError
    };
});
