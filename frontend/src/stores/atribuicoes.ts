import {defineStore} from 'pinia'
import {AtribuicaoTemporaria} from "@/types/tipos";
import {buscarTodasAtribuicoes} from "@/services/atribuicaoTemporariaService";

export const useAtribuicaoTemporariaStore = defineStore('atribuicaoTemporaria', {
    state: () => ({
        atribuicoes: [] as AtribuicaoTemporaria[],
        isLoading: false,
        error: null as string | null
    }),
    getters: {
        getAtribuicoesPorServidor: (state) => (servidorId: number): AtribuicaoTemporaria[] => {
            return state.atribuicoes.filter(a => a.servidor.codigo === servidorId)
        },
        getAtribuicoesPorUnidade: (state) => (unidadeSigla: string): AtribuicaoTemporaria[] => {
            return state.atribuicoes.filter(a => a.unidade.sigla === unidadeSigla)
        },
    },
    actions: {
        async fetchAtribuicoes() {
            this.isLoading = true;
            this.error = null;
            try {
                const response = await buscarTodasAtribuicoes();
                this.atribuicoes = (response as any).data.map(a => ({
                    ...a,
                    dataInicio: new Date(a.dataInicio).toISOString(),
                    dataFim: new Date(a.dataTermino).toISOString(),
                    dataTermino: new Date(a.dataTermino).toISOString(),
                    servidor: a.servidor,
                    unidade: a.unidade
                })) as any as AtribuicaoTemporaria[];
            } catch (err: any) {
                this.error = 'Falha ao carregar atribuições: ' + err.message;
            } finally {
                this.isLoading = false;
            }
        },
        criarAtribuicao(novaAtribuicao: AtribuicaoTemporaria) {
            this.atribuicoes.push(novaAtribuicao);
        }
    }
})