import { defineStore } from 'pinia';
import { useApi } from '@/composables/useApi';
import type { AnaliseValidacao } from '@/types/tipos';

export const useAnalisesStore = defineStore('analises', {
    state: () => ({
        items: [] as AnaliseValidacao[],
        loading: false,
        error: null as string | null,
    }),
    getters: {
        getAnalisesPorSubprocesso: (state) => (idSubprocesso: number): AnaliseValidacao[] => {
            // Este getter agora apenas filtra o estado atual, que é populado por fetchAnalises.
            // A ordenação é feita no backend ou na action, se necessário.
            return state.items.filter(analise => analise.idSubprocesso === idSubprocesso);
        }
    },
    actions: {
        async fetchAnalises(idSubprocesso: number) {
            this.loading = true;
            this.error = null;
            const api = useApi();
            try {
                const { data } = await api.get(`/api/subprocessos/${idSubprocesso}/analises`);
                // A API deve retornar as datas como strings ISO 8601, então convertemos para objetos Date.
                const analises = (data as any[]).map(item => ({
                    ...item,
                    dataHora: new Date(item.dataHora),
                }));
                // Substitui apenas as análises do subprocesso atual, para não apagar as de outros.
                this.items = [
                    ...this.items.filter(a => a.idSubprocesso !== idSubprocesso),
                    ...analises
                ].sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime());

            } catch (error: any) {
                this.error = 'Falha ao buscar o histórico de análises.';
            } finally {
                this.loading = false;
            }
        },

        async registrarAnalise(idSubprocesso: number, payload: Omit<AnaliseValidacao, 'id' | 'idSubprocesso' | 'dataHora'>) {
            const api = useApi();
            try {
                await api.post(`/api/subprocessos/${idSubprocesso}/analises`, payload);
                // Após registrar, busca a lista atualizada para garantir a consistência.
                await this.fetchAnalises(idSubprocesso);
            } catch (error: any) {
                throw new Error('Falha ao registrar a análise.');
            }
        },
    }
});