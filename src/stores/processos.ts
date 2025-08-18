import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json'
import processosUnidadesMock from '../mocks/subprocessos.json'
import type {Processo, ProcessoUnidade} from '@/types/tipos'

function parseProcessoDates(processo: any): Processo {
    return {
        ...processo,
        dataLimite: new Date(processo.dataLimite),
        dataFinalizacao: processo.dataFinalizacao ? new Date(processo.dataFinalizacao) : null,
    };
}

function parseProcessoUnidadeDates(pu: any): ProcessoUnidade {
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
        processosUnidade: processosUnidadesMock.map(parseProcessoUnidadeDates) as ProcessoUnidade[]
    }),
    getters: {
        getUnidadesDoProcesso: (state) => (idProcesso: number): ProcessoUnidade[] => {
            return state.processosUnidade.filter(pu => pu.idProcesso === idProcesso);
        },
        getProcessoUnidadeById: (state) => (subidProcesso: number): ProcessoUnidade | undefined => {
            return state.processosUnidade.find(pu => pu.id === subidProcesso);
        }
    },
    actions: {
        adicionarProcesso(novoProcesso: Processo) {
            this.processos.push(novoProcesso);
        },
        adicionarProcessosUnidade(processosUnidadeArray: ProcessoUnidade[]) {
            processosUnidadeArray.forEach((pu: ProcessoUnidade) => {
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