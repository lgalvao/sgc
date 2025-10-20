import {defineStore} from 'pinia'
import {useNotificacoesStore} from './notificacoes'
import * as SubprocessoService from '@/services/subprocessoService'
import {ImpactoMapa, MapaAjuste, MapaCompleto, SalvarAjustesRequest, SalvarMapaRequest} from "@/types/tipos";

export const useMapasStore = defineStore('mapas', {
    state: () => ({
        mapaCompleto: null as MapaCompleto | null,
        mapaAjuste: null as MapaAjuste | null,
        impactoMapa: null as ImpactoMapa | null,
    }),

    getters: {
    },

    actions: {
        async fetchMapaCompleto(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore()
            try {
                this.mapaCompleto = await SubprocessoService.obterMapaCompleto(idSubprocesso);
            } catch (error) {
                notificacoes.erro('Erro ao buscar mapa', 'Não foi possível carregar o mapa de competências.');
                this.mapaCompleto = null;
            }
        },

        async salvarMapa(idSubprocesso: number, request: SalvarMapaRequest) {
            const notificacoes = useNotificacoesStore()
            try {
                this.mapaCompleto = await SubprocessoService.salvarMapaCompleto(idSubprocesso, request);
                notificacoes.sucesso('Mapa salvo', 'O mapa de competências foi salvo com sucesso.');
            } catch (error) {
                notificacoes.erro('Erro ao salvar', 'Não foi possível salvar o mapa de competências.');
            }
        },

        async fetchMapaAjuste(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore()
            try {
                this.mapaAjuste = await SubprocessoService.obterMapaAjuste(idSubprocesso);
            } catch (error) {
                notificacoes.erro('Erro ao buscar mapa para ajuste', 'Não foi possível carregar as informações para o ajuste.');
                this.mapaAjuste = null;
            }
        },

        async salvarAjustes(idSubprocesso: number, request: SalvarAjustesRequest) {
            const notificacoes = useNotificacoesStore()
            try {
                await SubprocessoService.salvarMapaAjuste(idSubprocesso, request);
                notificacoes.sucesso('Ajustes salvos', 'Os ajustes no mapa foram salvos com sucesso.');
            } catch (error) {
                notificacoes.erro('Erro ao salvar ajustes', 'Não foi possível salvar os ajustes.');
            }
        },

        async fetchImpactoMapa(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore()
            try {
                this.impactoMapa = await SubprocessoService.verificarImpactosMapa(idSubprocesso);
            } catch (error) {
                notificacoes.erro('Erro ao verificar impactos', 'Não foi possível carregar os impactos no mapa.');
                this.impactoMapa = null;
            }
        },
    }
})