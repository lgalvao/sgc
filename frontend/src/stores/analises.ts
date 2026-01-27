import {defineStore} from "pinia";
import {computed, ref} from "vue";
import * as analiseService from "@/services/analiseService";
import {useErrorHandler} from "@/composables/useErrorHandler";

import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

export const useAnalisesStore = defineStore("analises", () => {
    const analisesPorSubprocesso = ref(new Map<number, Analise[]>());
    const { lastError, clearError, withErrorHandling } = useErrorHandler();
    const isLoading = ref(false);

    const obterAnalisesPorSubprocesso = computed(() => (codSubrocesso: number) => {
        return analisesPorSubprocesso.value.get(codSubrocesso) || [];
    });

    async function buscarAnalisesCadastro(codSubrocesso: number) {
        if (isLoading.value) return; // Previne race conditions
        
        isLoading.value = true;
        await withErrorHandling(async () => {
            const analises = await analiseService.listarAnalisesCadastro(codSubrocesso);
            const atuais = analisesPorSubprocesso.value.get(codSubrocesso) || [];
            const outras = atuais.filter((a) => !("unidadeSigla" in a));
            analisesPorSubprocesso.value.set(codSubrocesso, [...outras, ...analises]);
        }).finally(() => {
            isLoading.value = false;
        });
    }

    async function buscarAnalisesValidacao(codSubrocesso: number) {
        if (isLoading.value) return; // Previne race conditions
        
        isLoading.value = true;
        await withErrorHandling(async () => {
            const analises =
                await analiseService.listarAnalisesValidacao(codSubrocesso);
            const atuais = analisesPorSubprocesso.value.get(codSubrocesso) || [];
            const outras = atuais.filter((a) => !("unidade" in a));
            analisesPorSubprocesso.value.set(codSubrocesso, [...outras, ...analises]);
        }).finally(() => {
            isLoading.value = false;
        });
    }

    return {
        analisesPorSubprocesso,
        lastError,
        isLoading,
        obterAnalisesPorSubprocesso,
        buscarAnalisesCadastro,
        buscarAnalisesValidacao,
        clearError
    };
});
