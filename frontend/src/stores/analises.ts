import {defineStore} from "pinia";
import {computed, ref} from "vue";
import * as analiseService from "@/services/analiseService";
import { ToastService } from "@/services/toastService";

import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

export const useAnalisesStore = defineStore("analises", () => {
    const analisesPorSubprocesso = ref(new Map<number, Analise[]>());

    const obterAnalisesPorSubprocesso = computed(() => (codSubrocesso: number) => {
        return analisesPorSubprocesso.value.get(codSubrocesso) || [];
    });

    async function buscarAnalisesCadastro(codSubrocesso: number) {
        
        try {
            const analises = await analiseService.listarAnalisesCadastro(codSubrocesso);
            const atuais = analisesPorSubprocesso.value.get(codSubrocesso) || [];
            const analisesFiltradas = analises.filter(
                (a) => !atuais.some((aa) => aa.codigo === a.codigo),
            );
            analisesPorSubprocesso.value.set(codSubrocesso, [
                ...atuais,
                ...analisesFiltradas,
            ]);
        } catch {
            ToastService.erro(
                "Erro",
                "Erro ao buscar histórico de análises de cadastro.",
            );
        }
    }

    async function buscarAnalisesValidacao(codSubrocesso: number) {
        
        try {
            const analises =
                await analiseService.listarAnalisesValidacao(codSubrocesso);
            const atuais = analisesPorSubprocesso.value.get(codSubrocesso) || [];
            const analisesFiltradas = analises.filter(
                (a) => !atuais.some((aa) => aa.codigo === a.codigo),
            );
            analisesPorSubprocesso.value.set(codSubrocesso, [
                ...atuais,
                ...analisesFiltradas,
            ]);
        } catch {
            ToastService.erro(
                "Erro",
                "Erro ao buscar histórico de análises de validação.",
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
