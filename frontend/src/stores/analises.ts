import {defineStore} from 'pinia';
import type {AnaliseCadastro, AnaliseValidacao} from '@/types/tipos';
import * as analiseService from '@/services/analiseService';
import {useNotificacoesStore} from "@/stores/notificacoes";

type Analise = AnaliseCadastro | AnaliseValidacao;

export const useAnalisesStore = defineStore('analises', {
    state: () => ({
        analisesPorSubprocesso: new Map<number, Analise[]>(),
    }),
    getters: {
        getAnalisesPorSubprocesso: (state) => (codSubrocesso: number) => {
            return state.analisesPorSubprocesso.get(codSubrocesso) || [];
        },
    },
    actions: {
        async fetchAnalisesCadastro(codSubrocesso: number) {
            const notificacoes = useNotificacoesStore();
            try {
                const analises = await analiseService.listarAnalisesCadastro(codSubrocesso);
                const atuais = this.analisesPorSubprocesso.get(codSubrocesso) || [];
                const analisesFiltradas = analises.filter(a => !atuais.some(aa => aa.codigo === a.codigo));
                this.analisesPorSubprocesso.set(codSubrocesso, [...atuais, ...analisesFiltradas]);
            } catch {
                notificacoes.erro('Erro', 'Erro ao buscar histórico de análises de cadastro.');
            }
        },

        async fetchAnalisesValidacao(codSubrocesso: number) {
            const notificacoes = useNotificacoesStore();
            try {
                const analises = await analiseService.listarAnalisesValidacao(codSubrocesso);
                const atuais = this.analisesPorSubprocesso.get(codSubrocesso) || [];
                const analisesFiltradas = analises.filter(a => !atuais.some(aa => aa.codigo === a.codigo));
                this.analisesPorSubprocesso.set(codSubrocesso, [...atuais, ...analisesFiltradas]);
            } catch {
                notificacoes.erro('Erro', 'Erro ao buscar histórico de análises de validação.');
            }
        },
    },
});