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
                // TODO: O endpoint correto precisa ser verificado e implementado no backend
                // Por enquanto, vamos assumir um endpoint genérico /api/mapas
                const response = await api.get('/api/mapas');
                this.mapas = response.data as Mapa[]; // Assumindo que a API retorna um array de mapas
            } catch (error: any) {
                this.error = error.message || 'Falha ao buscar mapas.';
            } finally {
                this.loading = false;
            }
        },

        /*
        // Ações antigas que modificam o estado localmente.
        // Precisarão ser reimplementadas para chamar a API.

        adicionarMapa(mapa: Mapa) {
            this.mapas.push(mapa);
        },
        editarMapa(id: number, novosDados: Partial<Mapa>) {
            const idx = this.mapas.findIndex(m => m.id === id)
            if (idx !== -1) this.mapas[idx] = {...this.mapas[idx], ...novosDados}
        },
 
        definirMapaComoVigente(unidadeId: string, idProcesso: number) {
            // Primeiro, desmarcar qualquer mapa vigente anterior para esta unidade
            this.mapas.forEach(mapa => {
                if (mapa.unidade === unidadeId && mapa.situacao === SITUACOES_MAPA.VIGENTE) mapa.situacao = SITUACOES_MAPA.DISPONIBILIZADO;
            });
 
            // Definir o mapa do processo como vigente
            const mapaIndex = this.mapas.findIndex(m => m.unidade === unidadeId && m.idProcesso === idProcesso);
            if (mapaIndex !== -1) {
                this.mapas[mapaIndex].situacao = SITUACOES_MAPA.VIGENTE;
                this.mapas[mapaIndex].dataFinalizacao = new Date();
            }
        }
        */
    }
})