import {defineStore} from "pinia";
import {computed, ref} from "vue";
import * as analiseService from "@/services/analiseService";
import { useFeedbackStore } from "@/stores/feedback";

import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

export const useAnalisesStore = defineStore("analises", () => {
    const analisesPorSubprocesso = ref(new Map<number, Analise[]>());
    const feedbackStore = useFeedbackStore();

    const obterAnalisesPorSubprocesso = computed(() => (codSubrocesso: number) => {
        return analisesPorSubprocesso.value.get(codSubrocesso) || [];
    });

    async function buscarAnalisesCadastro(codSubrocesso: number) {
        
        try {
            const analises = await analiseService.listarAnalisesCadastro(codSubrocesso);
            // Substituir completamente as análises para refletir o estado atual do backend
            // (importante quando o backend exclui análises, como na disponibilização)
            analisesPorSubprocesso.value.set(codSubrocesso, analises);
        } catch {
            feedbackStore.show(
                "Erro",
                "Erro ao buscar histórico de análises de cadastro.",
                "danger"
            );
        }
    }

    async function buscarAnalisesValidacao(codSubrocesso: number) {
        
        try {
            const analises =
                await analiseService.listarAnalisesValidacao(codSubrocesso);
            // Substituir completamente as análises para refletir o estado atual do backend
            analisesPorSubprocesso.value.set(codSubrocesso, analises);
        } catch {
            feedbackStore.show(
                "Erro",
                "Erro ao buscar histórico de análises de validação.",
                "danger"
            );
        }
    }

    return {
        analisesPorSubprocesso,
        obterAnalisesPorSubprocesso,
        buscarAnalisesCadastro,
        buscarAnalisesValidacao,
    };
});
