import {defineStore} from 'pinia'
import {useNotificacoesStore} from './notificacoes'
import * as mapaService from '@/services/mapaService';
import * as subprocessoService from '@/services/subprocessoService';
import {Competencia, ImpactoMapa, MapaAjuste, MapaCompleto, SalvarAjustesRequest, SalvarMapaRequest} from "@/types/tipos";

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
                this.mapaCompleto = await mapaService.obterMapaCompleto(idSubprocesso);
            } catch {
                notificacoes.erro('Erro ao buscar mapa', 'Não foi possível carregar o mapa de competências.');
                this.mapaCompleto = null;
            }
        },

        async salvarMapa(idSubprocesso: number, request: SalvarMapaRequest) {
            const notificacoes = useNotificacoesStore()
            try {
                this.mapaCompleto = await mapaService.salvarMapaCompleto(idSubprocesso, request);
                notificacoes.sucesso('Mapa salvo', 'O mapa de competências foi salvo com sucesso.');
            } catch {
                notificacoes.erro('Erro ao salvar', 'Não foi possível salvar o mapa de competências.');
            }
        },

        async adicionarCompetencia(idSubprocesso: number, competencia: Competencia) {
            const notificacoes = useNotificacoesStore();
            try {
                this.mapaCompleto = await subprocessoService.adicionarCompetencia(idSubprocesso, competencia);
                notificacoes.sucesso('Competência adicionada', 'A competência foi adicionada com sucesso.');
            } catch {
                notificacoes.erro('Erro ao adicionar', 'Não foi possível adicionar a competência.');
            }
        },

        async atualizarCompetencia(idSubprocesso: number, competencia: Competencia) {
            const notificacoes = useNotificacoesStore();
            try {
                this.mapaCompleto = await subprocessoService.atualizarCompetencia(idSubprocesso, competencia);
                notificacoes.sucesso('Competência atualizada', 'A competência foi atualizada com sucesso.');
            } catch {
                notificacoes.erro('Erro ao atualizar', 'Não foi possível atualizar a competência.');
            }
        },

        async removerCompetencia(idSubprocesso: number, idCompetencia: number) {
            const notificacoes = useNotificacoesStore();
            try {
                this.mapaCompleto = await subprocessoService.removerCompetencia(idSubprocesso, idCompetencia);
                notificacoes.sucesso('Competência removida', 'A competência foi removida com sucesso.');
            } catch {
                notificacoes.erro('Erro ao remover', 'Não foi possível remover a competência.');
            }
        },

        async fetchMapaAjuste(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore()
            try {
                this.mapaAjuste = await mapaService.obterMapaAjuste(idSubprocesso);
            } catch {
                notificacoes.erro('Erro ao buscar mapa para ajuste', 'Não foi possível carregar as informações para o ajuste.');
                this.mapaAjuste = null;
            }
        },

        async salvarAjustes(idSubprocesso: number, request: SalvarAjustesRequest) {
            const notificacoes = useNotificacoesStore()
            try {
                await mapaService.salvarMapaAjuste(idSubprocesso, request);
                notificacoes.sucesso('Ajustes salvos', 'Os ajustes no mapa foram salvos com sucesso.');
            } catch {
                notificacoes.erro('Erro ao salvar ajustes', 'Não foi possível salvar os ajustes.');
            }
        },

        async fetchImpactoMapa(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore()
            try {
                this.impactoMapa = await mapaService.verificarImpactosMapa(idSubprocesso);
            } catch {
                notificacoes.erro('Erro ao verificar impactos', 'Não foi possível carregar os impactos no mapa.');
                this.impactoMapa = null;
            }
        },
    }
})