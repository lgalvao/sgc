import {defineStore} from 'pinia'
import {useNotificacoesStore} from './notificacoes'
import * as mapaService from '@/services/mapaService';
import * as subprocessoService from '@/services/subprocessoService';
import {
    Competencia,
    ImpactoMapa,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
    SalvarAjustesRequest,
    SalvarMapaRequest,
    DisponibilizarMapaRequest
} from "@/types/tipos";

export const useMapasStore = defineStore('mapas', {
    state: () => ({
        mapaCompleto: null as MapaCompleto | null,
        mapaAjuste: null as MapaAjuste | null,
        impactoMapa: null as ImpactoMapa | null,
        mapaVisualizacao: null as MapaVisualizacao | null,
    }),

    getters: {
    },

    actions: {
        async fetchMapaVisualizacao(codSubrocesso: number) {
            const notificacoes = useNotificacoesStore()
            try {
                this.mapaVisualizacao = await mapaService.obterMapaVisualizacao(codSubrocesso);
            } catch {
                notificacoes.erro('Erro ao buscar mapa', 'Não foi possível carregar a visualização do mapa.');
                this.mapaVisualizacao = null;
            }
        },

        async fetchMapaCompleto(codSubrocesso: number) {
            const notificacoes = useNotificacoesStore()
            try {
                this.mapaCompleto = await mapaService.obterMapaCompleto(codSubrocesso);
            } catch {
                notificacoes.erro('Erro ao buscar mapa', 'Não foi possível carregar o mapa de competências.');
                this.mapaCompleto = null;
            }
        },

        async salvarMapa(codSubrocesso: number, request: SalvarMapaRequest) {
            const notificacoes = useNotificacoesStore()
            try {
                this.mapaCompleto = await mapaService.salvarMapaCompleto(codSubrocesso, request);
                notificacoes.sucesso('Mapa salvo', 'O mapa de competências foi salvo.');
            } catch {
                notificacoes.erro('Erro ao salvar', 'Não foi possível salvar o mapa de competências.');
            }
        },

        async adicionarCompetencia(codSubrocesso: number, competencia: Competencia) {
            const notificacoes = useNotificacoesStore();
            try {
                this.mapaCompleto = await subprocessoService.adicionarCompetencia(codSubrocesso, competencia);
                notificacoes.sucesso('Competência adicionada', 'A competência foi adicionada.');
            } catch {
                notificacoes.erro('Erro ao adicionar', 'Não foi possível adicionar a competência.');
            }
        },

        async atualizarCompetencia(codSubrocesso: number, competencia: Competencia) {
            const notificacoes = useNotificacoesStore();
            try {
                this.mapaCompleto = await subprocessoService.atualizarCompetencia(codSubrocesso, competencia);
                notificacoes.sucesso('Competência atualizada', 'A competência foi atualizada.');
            } catch {
                notificacoes.erro('Erro ao atualizar', 'Não foi possível atualizar a competência.');
            }
        },

        async removerCompetencia(codSubrocesso: number, idCompetencia: number) {
            const notificacoes = useNotificacoesStore();
            try {
                this.mapaCompleto = await subprocessoService.removerCompetencia(codSubrocesso, idCompetencia);
                notificacoes.sucesso('Competência removida', 'A competência foi removida.');
            } catch {
                notificacoes.erro('Erro ao remover', 'Não foi possível remover a competência.');
            }
        },

        async fetchMapaAjuste(codSubrocesso: number) {
            const notificacoes = useNotificacoesStore()
            try {
                this.mapaAjuste = await mapaService.obterMapaAjuste(codSubrocesso);
            } catch {
                notificacoes.erro('Erro ao buscar mapa para ajuste', 'Não foi possível carregar as informações para o ajuste.');
                this.mapaAjuste = null;
            }
        },

        async salvarAjustes(codSubrocesso: number, request: SalvarAjustesRequest) {
            const notificacoes = useNotificacoesStore()
            try {
                await mapaService.salvarMapaAjuste(codSubrocesso, request);
                notificacoes.sucesso('Ajustes salvos', 'Os ajustes no mapa foram salvos.');
            } catch {
                notificacoes.erro('Erro ao salvar ajustes', 'Não foi possível salvar os ajustes.');
            }
        },

        async fetchImpactoMapa(codSubrocesso: number) {
            const notificacoes = useNotificacoesStore()
            try {
                this.impactoMapa = await mapaService.verificarImpactosMapa(codSubrocesso);
            } catch {
                notificacoes.erro('Erro ao verificar impactos', 'Não foi possível carregar os impactos no mapa.');
                this.impactoMapa = null;
            }
        },

        async disponibilizarMapa(codSubrocesso: number, request: DisponibilizarMapaRequest) {
            const notificacoes = useNotificacoesStore();
            try {
                await mapaService.disponibilizarMapa(codSubrocesso, request);
                notificacoes.sucesso('Mapa disponibilizado', 'O mapa de competências foi disponibilizado para validação.');
            } catch (error: any) {
                notificacoes.erro('Erro ao disponibilizar', error.response.data.message);
                throw error;
            }
        },
    }
})