import { defineStore } from 'pinia';
import type { Atividade, Conhecimento, CriarAtividadeRequest, CriarConhecimentoRequest } from '@/models/atividade';
import * as atividadeService from '@/services/atividadeService';
import * as subprocessoService from '@/services/subprocessoService';
import { useNotificacoesStore } from './notificacoes';

export const useAtividadesStore = defineStore('atividades', {
    state: () => ({
        atividades: [] as Atividade[],
        atividadesPorSubprocesso: new Map<number, Atividade[]>(),
    }),
    getters: {
        getAtividadesPorSubprocesso: (state) => (idSubprocesso: number): Atividade[] => {
            return state.atividadesPorSubprocesso.get(idSubprocesso) || [];
        }
    },
    actions: {
        async fetchAtividades() {
            try {
                this.atividades = await atividadeService.listarAtividades();
            } catch (error) {
                useNotificacoesStore().erro('Erro ao buscar atividades', 'Não foi possível carregar a lista de atividades.');
            }
        },

        async fetchAtividadesParaSubprocesso(idSubprocesso: number) {
            // No backend atual, não há um endpoint direto para buscar atividades por subprocesso.
            // A lógica de negócio associa atividades a um mapa, que por sua vez está ligado a um subprocesso.
            // Esta action precisará ser ajustada quando a lógica de busca de mapa estiver implementada.
            // Por enquanto, vamos simular buscando todas e filtrando (ineficiente, para desenvolvimento).
            if (this.atividades.length === 0) {
                await this.fetchAtividades();
            }
            // A filtragem real dependerá da estrutura de dados do subprocesso.
            // Ex: this.atividadesPorSubprocesso.set(idSubprocesso, this.atividades.filter(a => a.mapaCodigo === subprocesso.mapaCodigo));
            // Por enquanto, apenas para demonstração:
            this.atividadesPorSubprocesso.set(idSubprocesso, [...this.atividades]);
        },

        async adicionarAtividade(request: CriarAtividadeRequest) {
            try {
                const novaAtividade = await atividadeService.criarAtividade(request);
                this.atividades.push(novaAtividade);
                // O ideal seria associar a atividade ao subprocesso correto aqui.
            } catch (error) {
                useNotificacoesStore().erro('Erro ao adicionar atividade', 'Não foi possível salvar a nova atividade.');
            }
        },

        async removerAtividade(atividadeId: number) {
            try {
                await atividadeService.excluirAtividade(atividadeId);
                this.atividades = this.atividades.filter(a => a.codigo !== atividadeId);
                // Também remover de todos os maps de subprocesso
                this.atividadesPorSubprocesso.forEach((atividades, id) => {
                    this.atividadesPorSubprocesso.set(id, atividades.filter(a => a.codigo !== atividadeId));
                });
            } catch (error) {
                useNotificacoesStore().erro('Erro ao remover atividade', 'Não foi possível remover a atividade.');
            }
        },

        async adicionarConhecimento(atividadeId: number, request: CriarConhecimentoRequest) {
            try {
                const novoConhecimento = await atividadeService.criarConhecimento(atividadeId, request);
                const atividade = this.atividades.find(a => a.codigo === atividadeId);
                if (atividade) {
                    atividade.conhecimentos.push(novoConhecimento);
                }
            } catch (error) {
                useNotificacoesStore().erro('Erro ao adicionar conhecimento', 'Não foi possível salvar o novo conhecimento.');
            }
        },

        async removerConhecimento(atividadeId: number, conhecimentoId: number) {
            try {
                await atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
                const atividade = this.atividades.find(a => a.codigo === atividadeId);
                if (atividade) {
                    atividade.conhecimentos = atividade.conhecimentos.filter(c => c.codigo !== conhecimentoId);
                }
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
        }
    }
});