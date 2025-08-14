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
    state: () => {
        const storedProcessosUnidade = localStorage.getItem('processosUnidade');
        const processosUnidade = storedProcessosUnidade
            ? JSON.parse(storedProcessosUnidade).map(parseProcessoUnidadeDates)
            : processosUnidadesMock.map(parseProcessoUnidadeDates);

        return {
            processos: processosMock.map(parseProcessoDates) as Processo[],
            processosUnidade: processosUnidade as ProcessoUnidade[]
        };
    },
    getters: {
        getUnidadesDoProcesso: (state) => (processoId: number): ProcessoUnidade[] => {
            return state.processosUnidade.filter(pu => pu.processoId === processoId);
        },
        getProcessoUnidadeById: (state) => (subprocessoId: number): ProcessoUnidade | undefined => {
            return state.processosUnidade.find(pu => pu.id === subprocessoId);
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
        finalizarProcesso(processoId: number) {
            const processo = this.processos.find(p => p.id === processoId);
            if (processo) {
                processo.situacao = 'Finalizado';
                processo.dataFinalizacao = new Date(); // Agora é um objeto Date
            }
        }
    }
})