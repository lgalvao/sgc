import { defineStore } from 'pinia';
import type { Atividade, Conhecimento, CriarAtividadeRequest, CriarConhecimentoRequest } from '@/types/tipos';
import * as atividadeService from '@/services/atividadeService';
import * as subprocessoService from '@/services/subprocessoService';
import { useNotificacoesStore } from './notificacoes';
import { mapMapaVisualizacaoToAtividades } from '@/mappers/mapas';

export const useAtividadesStore = defineStore('atividades', {
    state: () => ({
        atividadesPorSubprocesso: new Map<number, Atividade[]>(),
    }),
    getters: {
        getAtividadesPorSubprocesso: (state) => (idSubprocesso: number): Atividade[] => {
            return state.atividadesPorSubprocesso.get(idSubprocesso) || [];
        }
    },
    actions: {
        async fetchAtividadesParaSubprocesso(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore();
            try {
                const mapa = await subprocessoService.obterMapaVisualizacao(idSubprocesso);
                const atividades = mapMapaVisualizacaoToAtividades(mapa);
                this.atividadesPorSubprocesso.set(idSubprocesso, atividades);
            } catch (error) {
                notificacoes.erro('Erro ao buscar atividades', 'Não foi possível carregar as atividades do subprocesso.');
            }
        },

        async adicionarAtividade(idSubprocesso: number, request: CriarAtividadeRequest) {
            const notificacoes = useNotificacoesStore();
            try {
                // Passa o idSubprocesso para o serviço, que o adicionará ao DTO
                const novaAtividade = await atividadeService.criarAtividade(request, idSubprocesso);
                const atividades = this.atividadesPorSubprocesso.get(idSubprocesso) || [];
                atividades.push(novaAtividade);
                this.atividadesPorSubprocesso.set(idSubprocesso, atividades);
                notificacoes.sucesso('Atividade adicionada', 'A nova atividade foi adicionada com sucesso.');
                // Opcional: recarregar para garantir consistência total, mas a adição otimista já ajuda.
                await this.fetchAtividadesParaSubprocesso(idSubprocesso);
            } catch (error) {
                notificacoes.erro('Erro ao adicionar atividade', 'Não foi possível salvar a nova atividade.');
            }
        },

        async removerAtividade(idSubprocesso: number, atividadeId: number) {
            try {
                await atividadeService.excluirAtividade(atividadeId);
                let atividades = this.atividadesPorSubprocesso.get(idSubprocesso) || [];
                atividades = atividades.filter(a => a.codigo !== atividadeId);
                this.atividadesPorSubprocesso.set(idSubprocesso, atividades);
                useNotificacoesStore().sucesso('Atividade removida', 'A atividade foi removida com sucesso.');
            } catch (error) {
                useNotificacoesStore().erro('Erro ao remover atividade', 'Não foi possível remover a atividade.');
            }
        },

        async adicionarConhecimento(idSubprocesso: number, atividadeId: number, request: CriarConhecimentoRequest) {
            try {
                const novoConhecimento = await atividadeService.criarConhecimento(atividadeId, request);
                const atividades = this.atividadesPorSubprocesso.get(idSubprocesso) || [];
                const atividade = atividades.find(a => a.codigo === atividadeId);
                if (atividade) {
                    atividade.conhecimentos.push(novoConhecimento);
                    this.atividadesPorSubprocesso.set(idSubprocesso, atividades);
                }
                 useNotificacoesStore().sucesso('Conhecimento adicionado', 'O novo conhecimento foi adicionado com sucesso.');
            } catch (error) {
                useNotificacoesStore().erro('Erro ao adicionar conhecimento', 'Não foi possível salvar o novo conhecimento.');
            }
        },

        async removerConhecimento(idSubprocesso: number, atividadeId: number, conhecimentoId: number) {
            try {
                await atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
                const atividades = this.atividadesPorSubprocesso.get(idSubprocesso) || [];
                const atividade = atividades.find(a => a.codigo === atividadeId);
                if (atividade) {
                    atividade.conhecimentos = atividade.conhecimentos.filter(c => c.id !== conhecimentoId);
                    this.atividadesPorSubprocesso.set(idSubprocesso, atividades);
                }
                useNotificacoesStore().sucesso('Conhecimento removido', 'O conhecimento foi removido com sucesso.');
            } catch (error) {
                useNotificacoesStore().erro('Erro ao remover conhecimento', 'Não foi possível remover o conhecimento.');
            }
        },

        async importarAtividades(idSubprocessoDestino: number, idSubprocessoOrigem: number) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.importarAtividades(idSubprocessoDestino, idSubprocessoOrigem);
                notificacoes.sucesso('Atividades importadas', 'As atividades foram importadas com sucesso.');
                // Recarregar as atividades do subprocesso de destino para refletir a importação
                await this.fetchAtividadesParaSubprocesso(idSubprocessoDestino);
            } catch (error) {
                notificacoes.erro('Erro ao importar', 'Não foi possível importar as atividades.');
            }
        },

        async atualizarAtividade(idSubprocesso: number, atividadeId: number, data: Atividade) {
            const notificacoes = useNotificacoesStore();
            try {
                const atividadeAtualizada = await atividadeService.atualizarAtividade(atividadeId, data);
                const atividades = this.atividadesPorSubprocesso.get(idSubprocesso) || [];
                const index = atividades.findIndex(a => a.codigo === atividadeId);
                if (index !== -1) {
                    atividades[index] = atividadeAtualizada;
                    this.atividadesPorSubprocesso.set(idSubprocesso, atividades);
                }
                notificacoes.sucesso('Atividade atualizada', 'A atividade foi atualizada com sucesso.');
            } catch (error) {
                notificacoes.erro('Erro ao atualizar', 'Não foi possível atualizar a atividade.');
            }
        },

        async atualizarConhecimento(idSubprocesso: number, atividadeId: number, conhecimentoId: number, data: Conhecimento) {
            const notificacoes = useNotificacoesStore();
            try {
                const conhecimentoAtualizado = await atividadeService.atualizarConhecimento(atividadeId, conhecimentoId, data);
                const atividades = this.atividadesPorSubprocesso.get(idSubprocesso) || [];
                const atividade = atividades.find(a => a.codigo === atividadeId);
                if (atividade) {
                    const index = atividade.conhecimentos.findIndex(c => c.id === conhecimentoId);
                    if (index !== -1) {
                        atividade.conhecimentos[index] = conhecimentoAtualizado;
                        this.atividadesPorSubprocesso.set(idSubprocesso, atividades);
                    }
                }
                notificacoes.sucesso('Conhecimento atualizado', 'O conhecimento foi atualizado com sucesso.');
            } catch (error) {
                notificacoes.erro('Erro ao atualizar', 'Não foi possível atualizar o conhecimento.');
            }
        }
    }
});