import {defineStore} from 'pinia'
import subprocessosMock from '../mocks/subprocessos.json'
import {Movimentacao, Subprocesso} from '@/types/tipos'
import {parseDate} from '@/utils'
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes'

function parseSubprocessoDates(pu: Partial<Subprocesso>): Subprocesso {
    return {
        id: pu.id || 0,
        idProcesso: pu.idProcesso || 0,
        unidade: pu.unidade || '',
        situacao: SITUACOES_SUBPROCESSO.MAPA_CRIADO,
        unidadeAtual: pu.unidadeAtual || '',
        unidadeAnterior: pu.unidadeAnterior || null,
        dataLimiteEtapa1: typeof pu.dataLimiteEtapa1 === 'string' ? parseDate(pu.dataLimiteEtapa1) || new Date() : new Date(),
        dataFimEtapa1: typeof pu.dataFimEtapa1 === 'string' ? parseDate(pu.dataFimEtapa1) : null,
        dataLimiteEtapa2: typeof pu.dataLimiteEtapa2 === 'string' ? parseDate(pu.dataLimiteEtapa2) : null,
        dataFimEtapa2: typeof pu.dataFimEtapa2 === 'string' ? parseDate(pu.dataFimEtapa2) : null,
        sugestoes: pu.sugestoes || undefined,
        observacoes: pu.observacoes || undefined,
        movimentacoes: pu.movimentacoes || [],
        analises: pu.analises || [],
        idMapaCopiado: pu.idMapaCopiado || undefined,
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
            const subprocesso = state.subprocessos.find(sp => sp.id === idSubprocesso);
            return subprocesso ? subprocesso.movimentacoes.sort((a: Movimentacao, b: Movimentacao) => b.dataHora.getTime() - a.dataHora.getTime()) : [];
        },
        getSubprocessosElegiveisHomologacaoBloco: (state) => (idProcesso: number) => {
            return state.subprocessos.filter(pu =>
                pu.idProcesso === idProcesso &&
                (pu.situacao === SITUACOES_SUBPROCESSO.CADASTRO_DISPONIBILIZADO || pu.situacao === SITUACOES_SUBPROCESSO.REVISAO_CADASTRO_DISPONIBILIZADA)
            );
        }
    },
    actions: {
        reset() {
            this.subprocessos = [];
        }
    }
});