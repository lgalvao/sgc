import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json'
import subprocessosMock from '../mocks/subprocessos.json'
import type {Processo, Subprocesso} from '@/types/tipos'

function parseProcessoDates(processo: any): Processo {
    return {
        ...processo,
        dataLimite: new Date(processo.dataLimite),
        dataFinalizacao: processo.dataFinalizacao ? new Date(processo.dataFinalizacao) : null,
    };
}

function parseSubprocessoDates(pu: any): Subprocesso {
    return {
        ...pu,
        dataLimiteEtapa1: pu.dataLimiteEtapa1 ? new Date(pu.dataLimiteEtapa1) : null,
        dataLimiteEtapa2: pu.dataLimiteEtapa2 ? new Date(pu.dataLimiteEtapa2) : null,
        dataFimEtapa1: pu.dataFimEtapa1 ? new Date(pu.dataFimEtapa1) : null,
        dataFimEtapa2: pu.dataFimEtapa2 ? new Date(pu.dataFimEtapa2) : null,
    };
}

export const useProcessosStore = defineStore('processos', {
    state: () => ({
        processos: processosMock.map(parseProcessoDates) as Processo[],
        processosUnidade: subprocessosMock.map(parseSubprocessoDates) as Subprocesso[]
    }),
    getters: {
        getUnidadesDoProcesso: (state) => (idProcesso: number): Subprocesso[] => {
            return state.processosUnidade.filter(pu => pu.idProcesso === idProcesso);
        }
    },
    actions: {
        adicionarProcesso(novoProcesso: Processo) {
            this.processos.push(novoProcesso);
        },
        adicionarProcessosUnidade(processosUnidadeArray: Subprocesso[]) {
            processosUnidadeArray.forEach((pu: Subprocesso) => {
                this.processosUnidade.push(pu);
            });
        },
        finalizarProcesso(idProcesso: number) {
            const processo = this.processos.find(p => p.id === idProcesso);
            if (processo) {
                processo.situacao = 'Finalizado';
                processo.dataFinalizacao = new Date(); // Agora é um objeto Date
            }
        }
    }
})