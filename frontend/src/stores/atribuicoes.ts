import { defineStore } from 'pinia';
import { useApi } from '@/composables/useApi';
import type { AtribuicaoTemporaria } from '@/types/tipos';

export const useAtribuicaoTemporariaStore = defineStore('atribuicaoTemporaria', {
    state: () => ({
        items: [] as AtribuicaoTemporaria[],
        loading: false,
        error: null as string | null,
    }),
    getters: {
        getAtribuicoesPorServidor: (state) => (servidorId: number): AtribuicaoTemporaria[] => {
            return state.items.filter(a => Number(a.idServidor) === servidorId);
        },
        getAtribuicoesPorUnidade: (state) => (unidadeSigla: string): AtribuicaoTemporaria[] => {
            return state.items.filter(a => a.unidade === unidadeSigla);
        },
    },
    actions: {
        async fetchAtribuicoes() {
            this.loading = true;
            this.error = null;
            const api = useApi();
            try {
                const { data } = await api.get('/api/atribuicoes');
                this.items = (data as any[]).map(a => ({
                    ...a,
                    dataInicio: new Date(a.dataInicio),
                    dataTermino: new Date(a.dataTermino),
                }));
            } catch (error: any) {
                this.error = 'Falha ao buscar atribuições.';
            } finally {
                this.loading = false;
            }
        },

        async criarAtribuicao(novaAtribuicao: Omit<AtribuicaoTemporaria, 'id'>) {
            const api = useApi();
            try {
                await api.post('/api/atribuicoes', novaAtribuicao);
                await this.fetchAtribuicoes(); // Refresh a lista
            } catch (error: any) {
                throw new Error('Falha ao criar atribuição.');
            }
        },
    }
});