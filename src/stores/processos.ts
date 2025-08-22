import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json'
import subprocessosMock from '../mocks/subprocessos.json'
import {Processo, SituacaoProcesso, Subprocesso} from '@/types/tipos'
import {useConfiguracoesStore} from './configuracoes'; // Import the new store

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
        subprocessos: subprocessosMock.map(parseSubprocessoDates) as Subprocesso[]
    }),
    getters: {
        getUnidadesDoProcesso: (state) => (idProcesso: number): Subprocesso[] => {
            return state.subprocessos.filter(pu => pu.idProcesso === idProcesso);
        },
        isProcessoInativo: () => (processo: Processo): boolean => {
            const configuracoesStore = useConfiguracoesStore();
            if (processo.situacao === SituacaoProcesso.FINALIZADO && processo.dataFinalizacao) {
                const finalizacaoDate = new Date(processo.dataFinalizacao);
                const today = new Date();
                const diffTime = Math.abs(today.getTime() - finalizacaoDate.getTime());
                const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
                return diffDays > configuracoesStore.diasInativacaoProcesso;
            }
            return false;
        }
    },
    actions: {
        adicionarProcesso(novoProcesso: Processo) {
            this.processos.push(novoProcesso);
        },
        adicionarsubprocessos(subprocessosArray: Subprocesso[]) {
            subprocessosArray.forEach((pu: Subprocesso) => {
                this.subprocessos.push(pu);
            });
        },
        finalizarProcesso(idProcesso: number) {
            const processo = this.processos.find(p => p.id === idProcesso);
            if (processo) {
                processo.situacao = SituacaoProcesso.FINALIZADO;
                processo.dataFinalizacao = new Date(); // Agora é um objeto Date
            }
        }
    }
})