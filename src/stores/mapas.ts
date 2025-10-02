import {defineStore} from 'pinia'
import mapasData from '../mocks/mapas.json';
import type {Mapa} from '@/types/tipos'
import {SITUACOES_MAPA} from '@/constants/situacoes'
import {mapMapasArray} from '@/mappers/entidades';

export const useMapasStore = defineStore('mapas', {
    state: () => ({
        mapas: mapMapasArray(mapasData as any) as Mapa[]
    }),
    getters: {
        getMapaByUnidadeId: (state) => (unidadeId: string, idProcesso: number): Mapa | undefined => {
            return state.mapas.find(m => m.unidade === unidadeId && m.idProcesso === idProcesso)
        },
        getMapaVigentePorUnidade: (state) => (unidadeId: string): Mapa | undefined => {
            // Considerar mapas 'vigente', 'em_andamento' e 'disponibilizado' como elegíveis para revisão
            return state.mapas.find(m =>
                m.unidade === unidadeId &&
                (m.situacao === SITUACOES_MAPA.VIGENTE || m.situacao === SITUACOES_MAPA.EM_ANDAMENTO || m.situacao === SITUACOES_MAPA.DISPONIBILIZADO)
            )
        }
    },
    actions: {
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
    }
})