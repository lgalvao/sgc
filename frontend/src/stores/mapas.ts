import { defineStore } from 'pinia';
import type { Mapa } from '@/types/tipos';
import { useApi } from '@/composables/useApi';

export const useMapasStore = defineStore('mapas', {
    state: () => ({
        mapas: [] as Mapa[],
        loading: false,
        error: null as string | null,
    }),
    getters: {
        getMapaByUnidadeId: (state) => (unidadeId: string, idProcesso: number): Mapa | undefined => {
            return state.mapas.find(m => m.unidade === unidadeId && m.idProcesso === idProcesso)
        },
        getMapaVigentePorUnidade: (state) => (unidadeId: string): Mapa | undefined => {
            // No SGC, um mapa vigente é o que está associado à UnidadeMapa
            // Esta lógica precisará ser revista para se alinhar com a API
            return state.mapas.find(m => m.unidade === unidadeId);
        }
    },
    actions: {
        async fetchMapas() {
            this.loading = true;
            this.error = null;
            const api = useApi();
            try {
                const { data } = await api.get('/api/mapas');
                this.mapas = data as Mapa[];
            } catch (error: any) {
                this.error = 'Falha ao buscar mapas.';
            } finally {
                this.loading = false;
            }
        },

        async adicionarMapa(novoMapa: Omit<Mapa, 'id'>) {
            const api = useApi();
            try {
                await api.post('/api/mapas', novoMapa);
                await this.fetchMapas(); // Refresh a lista
            } catch (error: any) {
                throw new Error('Falha ao adicionar o mapa.');
            }
        },

        async editarMapa(id: number, dadosAtualizados: Partial<Mapa>) {
            const api = useApi();
            try {
                await api.put(`/api/mapas/${id}`, dadosAtualizados);
                await this.fetchMapas(); // Refresh a lista
            } catch (error: any) {
                throw new Error('Falha ao editar o mapa.');
            }
        },
    }
})