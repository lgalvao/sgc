import {defineStore} from "pinia";
import {ref} from "vue";
import * as analiseService from "@/services/analiseService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSingleLoading} from "@/composables/useLoadingManager";

import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

export const useAnalisesStore = defineStore("analises", () => {
    const analisesPorSubprocesso = ref(new Map<number, Analise[]>());
    const { lastError, clearError, withErrorHandling } = useErrorHandler();
    const loading = useSingleLoading();

    function obterAnalisesPorSubprocesso(codSubrocesso: number): Analise[] {
        return analisesPorSubprocesso.value.get(codSubrocesso) || [];
    }

    async function buscarAnalisesCadastro(codSubrocesso: number) {
        if (loading.isLoading.value) return; // Previne race conditions
        
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const analises = await analiseService.listarAnalisesCadastro(codSubrocesso);
                const atuais = analisesPorSubprocesso.value.get(codSubrocesso) || [];
                const outras = atuais.filter((a) => !("unidadeSigla" in a));
                analisesPorSubprocesso.value.set(codSubrocesso, [...outras, ...analises]);
            });
        });
    }

    async function buscarAnalisesValidacao(codSubrocesso: number) {
        if (loading.isLoading.value) return; // Previne race conditions
        
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const analises =
                    await analiseService.listarAnalisesValidacao(codSubrocesso);
                const atuais = analisesPorSubprocesso.value.get(codSubrocesso) || [];
                const outras = atuais.filter((a) => !("unidade" in a));
                analisesPorSubprocesso.value.set(codSubrocesso, [...outras, ...analises]);
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
