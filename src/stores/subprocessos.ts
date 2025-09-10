import {defineStore} from 'pinia'
import subprocessosMock from '../mocks/subprocessos.json'
import {Movimentacao, Subprocesso} from '@/types/tipos'
import {parseDate} from '@/utils/dateUtils'
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes'

function parseSubprocessoDates(pu: Partial<Subprocesso>): Subprocesso {
    return {
        id: pu.id || 0,
        idProcesso: pu.idProcesso || 0,
        unidade: pu.unidade || '',
        situacao: SITUACOES_SUBPROCESSO.MAPA_CRIADO,
        unidadeAtual: pu.unidadeAtual || '',
        unidadeAnterior: pu.unidadeAnterior || null,
        dataLimiteEtapa1: pu.dataLimiteEtapa1 ? parseDate(pu.dataLimiteEtapa1 as any) || new Date() : new Date(),
        dataFimEtapa1: pu.dataFimEtapa1 ? parseDate(pu.dataFimEtapa1 as any) : null,
        dataLimiteEtapa2: pu.dataLimiteEtapa2 ? parseDate(pu.dataLimiteEtapa2 as any) : null,
        dataFimEtapa2: pu.dataFimEtapa2 ? parseDate(pu.dataFimEtapa2 as any) : null,
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
            subprocessos: (subprocessosMock as Partial<Subprocesso>[]).map(parseSubprocessoDates) as Subprocesso[],
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
                (pu.situacao === 'Cadastro disponibilizado' || pu.situacao === 'Revisão do cadastro disponibilizada')
            );
        }
    },
    actions: {
        reset() {
            this.subprocessos = [];
        }
    }
});