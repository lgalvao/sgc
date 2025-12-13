import {defineStore} from "pinia";
import {computed, ref} from "vue";
import * as analiseService from "@/services/analiseService";
import {useFeedbackStore} from "@/stores/feedback";
import { normalizeError, type NormalizedError } from "@/utils/apiError";

import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

export const useAnalisesStore = defineStore("analises", () => {
    const analisesPorSubprocesso = ref(new Map<number, Analise[]>());
    const lastError = ref<NormalizedError | null>(null);
    const feedbackStore = useFeedbackStore();

    function clearError() {
        lastError.value = null;
    }

    const obterAnalisesPorSubprocesso = computed(() => (codSubrocesso: number) => {
        return analisesPorSubprocesso.value.get(codSubrocesso) || [];
    });

    async function buscarAnalisesCadastro(codSubrocesso: number) {
        lastError.value = null;
        try {
            const analises = await analiseService.listarAnalisesCadastro(codSubrocesso);
            const atuais = analisesPorSubprocesso.value.get(codSubrocesso) || [];
            const outras = atuais.filter((a) => !("unidadeSigla" in a));
            analisesPorSubprocesso.value.set(codSubrocesso, [...outras, ...analises]);
        } catch (error) {
            lastError.value = normalizeError(error);
            // Before it was swallowing the error and just showing feedback.
            // Now we store the error. Should we throw?
            // The plan says "Deixar componente/view decidir UX".
            // So we should populate lastError and probably not throw if it wasn't rethrowing before?
            // Wait, the previous code was NOT rethrowing.
            // But if we don't rethrow, the component won't know it failed unless it checks lastError.
            // Let's assume components are not currently checking for errors here since it was swallowed.
            // But to be safe and consistent with other stores, let's keep it swallowing but populating lastError.
        }
    }

    async function buscarAnalisesValidacao(codSubrocesso: number) {
        lastError.value = null;
        try {
            const analises =
                await analiseService.listarAnalisesValidacao(codSubrocesso);
            const atuais = analisesPorSubprocesso.value.get(codSubrocesso) || [];
            const outras = atuais.filter((a) => !("unidade" in a));
            analisesPorSubprocesso.value.set(codSubrocesso, [...outras, ...analises]);
        } catch (error) {
            lastError.value = normalizeError(error);
        }
    }

    return {
        analisesPorSubprocesso,
        lastError,
        obterAnalisesPorSubprocesso,
        buscarAnalisesCadastro,
        buscarAnalisesValidacao,
        clearError
    };
});
