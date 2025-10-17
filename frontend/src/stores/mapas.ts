import {defineStore} from 'pinia'
import mapasData from '../mocks/mapas.json';
import type {Mapa} from '@/types/tipos'
import {SITUACOES_MAPA} from '@/constants/situacoes'

export const useMapasStore = defineStore('mapas', {
    state: () => ({
        mapas: mapasData.map(m => ({
            ...m,
            unidade: {sigla: m.unidade}
        })) as unknown as Mapa[]
    }),
    getters: {
        getMapaByUnidadeId: (state) => (unidadeId: string, idProcesso: number): Mapa | undefined => {
            return state.mapas.find(m => m.unidade.sigla === unidadeId && m.idProcesso === idProcesso)
        },
        getMapaVigentePorUnidade: (state) => (unidadeId: string): Mapa | undefined => {
            // Considerar mapas 'vigente', 'em_andamento' e 'disponibilizado' como elegíveis para revisão
            return state.mapas.find(m =>
                m.unidade.sigla === unidadeId &&
                (m.situacao === 'VIGENTE' || m.situacao === 'EM_ANDAMENTO' || m.situacao === 'DISPONIBILIZADO')
            )
        }
    },
    actions: {
        adicionarMapa(mapa: Mapa) {
            this.mapas.push(mapa);
        },
        editarMapa(codigo: number, novosDados: Partial<Mapa>) {
            const idx = this.mapas.findIndex(m => m.codigo === codigo)
            if (idx !== -1) this.mapas[idx] = {...this.mapas[idx], ...novosDados}
        },
 
        definirMapaComoVigente(unidadeId: string, idProcesso: number) {
            // Primeiro, desmarcar qualquer mapa vigente anterior para esta unidade
            this.mapas.forEach(mapa => {
                if (mapa.unidade.sigla === unidadeId && mapa.situacao === 'VIGENTE') mapa.situacao = 'DISPONIBILIZADO';
            });
 
            // Definir o mapa do processo como vigente
            const mapaIndex = this.mapas.findIndex(m => m.unidade.sigla === unidadeId && m.idProcesso === idProcesso);
            if (mapaIndex !== -1) {
                this.mapas[mapaIndex].situacao = 'VIGENTE';
                this.mapas[mapaIndex].dataFinalizacao = new Date().toISOString();
            }
        }
    }
})