import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json'
import processosUnidadesMock from '../mocks/processosUnidades.json'

/**
 * @typedef { import('../types/domain').Processo } Processo
 * @typedef { import('../types/domain').ProcessoUnidade } ProcessoUnidade
 */

export const useProcessosStore = defineStore('processos', {
    state: () => ({
        /** @type {Processo[]} */
        processos: [...processosMock],
        /** @type {ProcessoUnidade[]} */
        processosUnidade: [...processosUnidadesMock]
    }),
    getters: {
        getUnidadesDoProcesso: (state) => (processoId) => {
            const processo = state.processos.find(p => p.id === processoId);
            if (!processo) return [];
            return processo.processosUnidade.map(puId => state.processosUnidade.find(pu => pu.id === puId)).filter(Boolean);
        },
        getProcessoUnidadeById: (state) => (processoUnidadeId) => {
            return state.processosUnidade.find(pu => pu.id === processoUnidadeId);
        }
    },
    actions: {
        adicionarProcesso(novoProcesso) {
            // Adiciona o novo processo
            this.processos.push(novoProcesso);

            // Adiciona os novos ProcessoUnidade ao array separado
            novoProcesso.processosUnidade.forEach(pu => {
                this.processosUnidade.push(pu);
            });
        },
        atualizarSituacaoProcesso(processoId, novaSituacao) {
            const processo = this.processos.find(p => p.id === processoId);
            if (processo) {
                processo.situacao = novaSituacao;
            }
        },
        atualizarLocalizacaoProcesso(processoId, novaLocalizacao) {
            // Encontra o ProcessoUnidade que corresponde à nova localização
            const pu = this.processosUnidade.find(pu => pu.processoId === processoId && pu.unidadeId === novaLocalizacao);
            if (pu) {
                // Atualiza a unidadeAnterior para a unidadeAtual anterior
                pu.unidadeAnterior = pu.unidadeAtual;
                // Atualiza a unidadeAtual
                pu.unidadeAtual = novaLocalizacao;
            }
        },
        finalizarProcesso(processoId) {
            const processo = this.processos.find(p => p.id === processoId);
            if (processo) {
                processo.situacao = 'Finalizado';
                processo.dataFinalizacao = new Date().toLocaleDateString('pt-BR');
            }
        }
    }
})