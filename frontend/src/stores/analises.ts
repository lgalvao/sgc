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
        getAnalisesPorSubprocesso: (state) => (idSubprocesso: number) => {
            return state.analisesPorSubprocesso.get(idSubprocesso) || [];
        },
    },
    actions: {
        async fetchAnalisesCadastro(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore();
            try {
                const analises = await analiseService.listarAnalisesCadastro(idSubprocesso);
                const atuais = this.analisesPorSubprocesso.get(idSubprocesso) || [];
                const analisesFiltradas = analises.filter(a => !atuais.some(aa => aa.codigo === a.codigo));
                this.analisesPorSubprocesso.set(idSubprocesso, [...atuais, ...analisesFiltradas]);
            } catch {
                notificacoes.erro('Erro', 'Erro ao buscar histórico de análises de cadastro.');
            }
        },

        async fetchAnalisesValidacao(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore();
            try {
                const analises = await analiseService.listarAnalisesValidacao(idSubprocesso);
                const atuais = this.analisesPorSubprocesso.get(idSubprocesso) || [];
                const analisesFiltradas = analises.filter(a => !atuais.some(aa => aa.codigo === a.codigo));
                this.analisesPorSubprocesso.set(idSubprocesso, [...atuais, ...analisesFiltradas]);
            } catch {
                notificacoes.erro('Erro', 'Erro ao buscar histórico de análises de validação.');
            }
        },
    },
});