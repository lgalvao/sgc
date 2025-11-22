import { defineStore } from "pinia";
import { computed, ref } from "vue";
import * as analiseService from "@/services/analiseService";
import { useNotificacoesStore } from "@/stores/notificacoes";
import type { AnaliseCadastro, AnaliseValidacao } from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

export const useAnalisesStore = defineStore("analises", () => {
    const analisesPorSubprocesso = ref(new Map<number, Analise[]>());

    const getAnalisesPorSubprocesso = computed(() => (codSubrocesso: number) => {
        return analisesPorSubprocesso.value.get(codSubrocesso) || [];
    });

    async function fetchAnalisesCadastro(codSubrocesso: number) {
        const notificacoes = useNotificacoesStore();
        try {
            const analises =
                await analiseService.listarAnalisesCadastro(codSubrocesso);
            const atuais = analisesPorSubprocesso.value.get(codSubrocesso) || [];
            const analisesFiltradas = analises.filter(
                (a) => !atuais.some((aa) => aa.codigo === a.codigo),
            );
            analisesPorSubprocesso.value.set(codSubrocesso, [
                ...atuais,
                ...analisesFiltradas,
            ]);
        } catch {
            notificacoes.erro(
                "Erro",
                "Erro ao buscar histórico de análises de cadastro.",
            );
        }
    }

    async function fetchAnalisesValidacao(codSubrocesso: number) {
        const notificacoes = useNotificacoesStore();
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
            notificacoes.erro(
                "Erro",
                "Erro ao buscar histórico de análises de validação.",
            );
        }
    }

    return {
        analisesPorSubprocesso,
        getAnalisesPorSubprocesso,
        fetchAnalisesCadastro,
        fetchAnalisesValidacao,
    };
});
