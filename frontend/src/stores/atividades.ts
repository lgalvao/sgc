import {defineStore} from 'pinia';
import type {Atividade, Conhecimento, CriarAtividadeRequest, CriarConhecimentoRequest} from '@/types/tipos';
import * as atividadeService from '@/services/atividadeService';
import * as subprocessoService from '@/services/subprocessoService';
import * as mapaService from '@/services/mapaService';
import {useNotificacoesStore} from './notificacoes';
import {mapMapaVisualizacaoToAtividades} from '@/mappers/mapas';

export const useAtividadesStore = defineStore('atividades', {
    state: () => ({
        atividadesPorSubprocesso: new Map<number, Atividade[]>(),
    }),
    getters: {
        getAtividadesPorSubprocesso: (state) => (codSubrocesso: number): Atividade[] => {
            return state.atividadesPorSubprocesso.get(codSubrocesso) || [];
        }
    },
    actions: {
        async fetchAtividadesParaSubprocesso(codSubrocesso: number) {
            const notificacoes = useNotificacoesStore();
            try {
                const mapa = await mapaService.obterMapaVisualizacao(codSubrocesso);
                const atividades = mapMapaVisualizacaoToAtividades(mapa);
                this.atividadesPorSubprocesso.set(codSubrocesso, atividades);
            } catch {
                notificacoes.erro('Erro ao buscar atividades', 'Não foi possível carregar as atividades do subprocesso.');
            }
        },

        async adicionarAtividade(codSubrocesso: number, request: CriarAtividadeRequest) {
            const notificacoes = useNotificacoesStore();
            try {
                // Passa o codSubrocesso para o serviço, que o adicionará ao DTO
                const novaAtividade = await atividadeService.criarAtividade(request, codSubrocesso);
                const atividades = this.atividadesPorSubprocesso.get(codSubrocesso) || [];
                atividades.push(novaAtividade);
                this.atividadesPorSubprocesso.set(codSubrocesso, atividades);
                notificacoes.sucesso('Atividade adicionada', 'A nova atividade foi adicionada.');
                // Opcional: recarregar para garantir consistência total, mas a adição otimista já ajuda.
                await this.fetchAtividadesParaSubprocesso(codSubrocesso);
            } catch {
                notificacoes.erro('Erro ao adicionar atividade', 'Não foi possível salvar a nova atividade.');
            }
        },

        async removerAtividade(codSubrocesso: number, atividadeId: number) {
            try {
                await atividadeService.excluirAtividade(atividadeId);
                let atividades = this.atividadesPorSubprocesso.get(codSubrocesso) || [];
                atividades = atividades.filter(a => a.codigo !== atividadeId);
                this.atividadesPorSubprocesso.set(codSubrocesso, atividades);
                useNotificacoesStore().sucesso('Atividade removida', 'A atividade foi removida.');
            } catch {
                useNotificacoesStore().erro('Erro ao remover atividade', 'Não foi possível remover a atividade.');
            }
        },

        async adicionarConhecimento(codSubrocesso: number, atividadeId: number, request: CriarConhecimentoRequest) {
            try {
                const novoConhecimento = await atividadeService.criarConhecimento(atividadeId, request);
                const atividades = this.atividadesPorSubprocesso.get(codSubrocesso) || [];
                const atividade = atividades.find(a => a.codigo === atividadeId);
                if (atividade) {
                    atividade.conhecimentos.push(novoConhecimento);
                    this.atividadesPorSubprocesso.set(codSubrocesso, atividades);
                }
                 useNotificacoesStore().sucesso('Conhecimento adicionado', 'O novo conhecimento foi adicionado.');
            } catch {
                useNotificacoesStore().erro('Erro ao adicionar conhecimento', 'Não foi possível salvar o novo conhecimento.');
            }
        },

        async removerConhecimento(codSubrocesso: number, atividadeId: number, conhecimentoId: number) {
            try {
                await atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
                const atividades = this.atividadesPorSubprocesso.get(codSubrocesso) || [];
                const atividade = atividades.find(a => a.codigo === atividadeId);
                if (atividade) {
                    atividade.conhecimentos = atividade.conhecimentos.filter(c => c.id !== conhecimentoId);
                    this.atividadesPorSubprocesso.set(codSubrocesso, atividades);
                }
                useNotificacoesStore().sucesso('Conhecimento removido', 'O conhecimento foi removido.');
            } catch {
                useNotificacoesStore().erro('Erro ao remover conhecimento', 'Não foi possível remover o conhecimento.');
            }
        },

        async importarAtividades(codSubrocessoDestino: number, codSubrocessoOrigem: number) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.importarAtividades(codSubrocessoDestino, codSubrocessoOrigem);
                notificacoes.sucesso('Atividades importadas', 'As atividades foram importadas.');
                // Recarregar as atividades do subprocesso de destino para refletir a importação
                await this.fetchAtividadesParaSubprocesso(codSubrocessoDestino);
            } catch {
                notificacoes.erro('Erro ao importar', 'Não foi possível importar as atividades.');
            }
        },

        async atualizarAtividade(codSubrocesso: number, atividadeId: number, data: Atividade) {
            const notificacoes = useNotificacoesStore();
            try {
                const atividadeAtualizada = await atividadeService.atualizarAtividade(atividadeId, data);
                const atividades = this.atividadesPorSubprocesso.get(codSubrocesso) || [];
                const index = atividades.findIndex(a => a.codigo === atividadeId);
                if (index !== -1) {
                    atividades[index] = atividadeAtualizada;
                    this.atividadesPorSubprocesso.set(codSubrocesso, atividades);
                }
                notificacoes.sucesso('Atividade atualizada', 'A atividade foi atualizada.');
            } catch {
                notificacoes.erro('Erro ao atualizar', 'Não foi possível atualizar a atividade.');
            }
        },

        async atualizarConhecimento(codSubrocesso: number, atividadeId: number, conhecimentoId: number, data: Conhecimento) {
            const notificacoes = useNotificacoesStore();
            try {
                const conhecimentoAtualizado = await atividadeService.atualizarConhecimento(atividadeId, conhecimentoId, data);
                const atividades = this.atividadesPorSubprocesso.get(codSubrocesso) || [];
                const atividade = atividades.find(a => a.codigo === atividadeId);
                if (atividade) {
                    const index = atividade.conhecimentos.findIndex(c => c.id === conhecimentoId);
                    if (index !== -1) {
                        atividade.conhecimentos[index] = conhecimentoAtualizado;
                        this.atividadesPorSubprocesso.set(codSubrocesso, atividades);
                    }
                }
                notificacoes.sucesso('Conhecimento atualizado', 'O conhecimento foi atualizado.');
            } catch {
                notificacoes.erro('Erro ao atualizar', 'Não foi possível atualizar o conhecimento.');
            }
        }
    }
});