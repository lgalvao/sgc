import {defineStore} from 'pinia'
import subprocessosMock from '../mocks/subprocessos.json'
import {Movimentacao, Subprocesso} from '@/types/tipos'
import {parseDate} from '@/utils/dateUtils'
import {generateUniqueId} from '@/utils/idGenerator'
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes'

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

export const useSubprocessosStore = defineStore('subprocessos', {
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
        adicionarSubprocessos(subprocessosArray: Subprocesso[]) {
            subprocessosArray.forEach((pu: Subprocesso) => {
                this.subprocessos.push(pu);
            });
        },
        criarSubprocesso(idProcesso: number, siglaUnidade: string, dataLimiteEtapa1: Date): Subprocesso {
            const novoSubprocesso: Subprocesso = {
                id: generateUniqueId(),
                idProcesso: idProcesso,
                unidade: siglaUnidade,
                situacao: SITUACOES_SUBPROCESSO.NAO_INICIADO,
                unidadeAtual: siglaUnidade,
                unidadeAnterior: null,
                dataLimiteEtapa1: dataLimiteEtapa1,
                dataFimEtapa1: null,
                dataLimiteEtapa2: null,
                dataFimEtapa2: null,
                sugestoes: '',
                observacoes: '',
                idMapaCopiado: undefined,
                movimentacoes: []
            };
            this.subprocessos.push(novoSubprocesso);
            return novoSubprocesso;
        },
        adicionarMovimentacao(idSubprocesso: number, movement: Omit<Movimentacao, 'id' | 'dataHora' | 'idSubprocesso'>) {
            const subprocesso = this.subprocessos.find(sp => sp.id === idSubprocesso);
            if (subprocesso) {
                const newMovement: Movimentacao = {
                    id: generateUniqueId(),
                    dataHora: new Date(),
                    idSubprocesso: idSubprocesso,
                    ...movement
                };
                subprocesso.movimentacoes.push(newMovement);
            }
        },
        reset() {
            this.subprocessos = [];
        }
    }
})