import {defineStore} from 'pinia'
import subprocessosMock from '../mocks/subprocessos.json'
import {Movimentacao, Subprocesso} from '@/types/tipos'
import {parseDate} from '@/utils'
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes'

function parseSubprocessoDates(pu: any): Subprocesso {
    return {
        ...pu,
        dataLimite: new Date(pu.dataLimite).toISOString(),
        unidade: {
            ...pu.unidade,
            dataFimEtapa1: pu.unidade.dataFimEtapa1 ? new Date(pu.unidade.dataFimEtapa1).toISOString() : undefined,
            dataLimiteEtapa2: pu.unidade.dataLimiteEtapa2 ? new Date(pu.unidade.dataLimiteEtapa2).toISOString() : undefined,
            dataFimEtapa2: pu.unidade.dataFimEtapa2 ? new Date(pu.unidade.dataFimEtapa2).toISOString() : undefined,
        }
    };
}

export const useSubprocessosStore = defineStore('subprocessos', {
    state: () => {
        return {
             
            subprocessos: (subprocessosMock as any[]).map(parseSubprocessoDates) as Subprocesso[],
        };
    },
    getters: {
        getUnidadesDoProcesso: (state) => (idProcesso: number): Subprocesso[] => {
            return state.subprocessos.filter(pu => pu.idProcesso === idProcesso);
        },
        getMovementsForSubprocesso: (state) => (idSubprocesso: number) => {
            const subprocesso = state.subprocessos.find(sp => sp.codigo === idSubprocesso);
            return subprocesso ? []: [];
        },
        getSubprocessosElegiveisHomologacaoBloco: (state) => (idProcesso: number) => {
            return state.subprocessos.filter(pu =>
                pu.idProcesso === idProcesso &&
                (pu.situacao === SituacaoSubprocesso.ATIVIDADES_EM_HOMOLOGACAO || pu.situacao === SituacaoSubprocesso.ATIVIDADES_REVISADAS)
            );
        }
    },
    actions: {
        reset() {
            this.subprocessos = [];
        }
    }
});