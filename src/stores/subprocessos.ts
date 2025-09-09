import {defineStore} from 'pinia'
import subprocessosMock from '../mocks/subprocessos.json'
import {Movimentacao, Subprocesso} from '@/types/tipos'
import {parseDate} from '@/utils/dateUtils'

function parseSubprocessoDates(pu: Omit<Subprocesso, 'dataLimiteEtapa1' | 'dataLimiteEtapa2' | 'dataFimEtapa1' | 'dataFimEtapa2' | 'movimentacoes'> & {
    dataLimiteEtapa1?: string | null,
    dataLimiteEtapa2?: string | null,
    dataFimEtapa1?: string | null,
    dataFimEtapa2?: string | null,
    movimentacoes?: Array<Omit<Movimentacao, 'dataHora'> & { dataHora: string }>
}): Subprocesso {
    return {
        ...pu,
        dataLimiteEtapa1: pu.dataLimiteEtapa1 ? parseDate(pu.dataLimiteEtapa1) || new Date() : new Date(),
        dataLimiteEtapa2: pu.dataLimiteEtapa2 ? parseDate(pu.dataLimiteEtapa2) : null,
        dataFimEtapa1: pu.dataFimEtapa1 ? parseDate(pu.dataFimEtapa1) : null,
        dataFimEtapa2: pu.dataFimEtapa2 ? parseDate(pu.dataFimEtapa2) : null,
        movimentacoes: pu.movimentacoes ? pu.movimentacoes.map(mov => ({
            ...mov,
            dataHora: parseDate(mov.dataHora) || new Date()
        })) : []
    };
}

defineStore('subprocessos', {
    state: () => {
        return {
            subprocessos: subprocessosMock.map(parseSubprocessoDates) as Subprocesso[],
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
        getSubprocessosElegiveisAceiteBloco: (state) => (idProcesso: number, siglaUnidadeUsuario: string) => {
            return state.subprocessos.filter(pu =>
                pu.idProcesso === idProcesso &&
                pu.unidadeAtual === siglaUnidadeUsuario &&
                (pu.situacao === 'Cadastro disponibilizado' || pu.situacao === 'Revisão do cadastro disponibilizada')
            );
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