import { defineStore } from 'pinia';
import { useApi } from '@/composables/useApi';
import { useMapasStore } from './mapas';

export enum TipoMudanca {
    AtividadeAdicionada = 'AtividadeAdicionada',
    AtividadeRemovida = 'AtividadeRemovida',
    AtividadeAlterada = 'AtividadeAlterada',
    ConhecimentoAdicionado = 'ConhecimentoAdicionado',
    ConhecimentoRemovido = 'ConhecimentoRemovido',
    ConhecimentoAlterado = 'ConhecimentoAlterado',
}

export interface Mudanca {
    id: number;
    tipo: TipoMudanca;
    idAtividade?: number;
    idConhecimento?: number;
    descricaoAtividade?: string;
    descricaoConhecimento?: string;
    valorAntigo?: string;
    valorNovo?: string;
    competenciasImpactadasIds?: number[];
}

export const useRevisaoStore = defineStore('revisao', {
    state: () => ({
        items: [] as Mudanca[],
        mudancasParaImpacto: [] as Mudanca[], // Mantido para lógica de UI de impacto
        loading: false,
        error: null as string | null,
    }),
    actions: {
        async fetchMudancas(idRevisao: number) {
            this.loading = true;
            this.error = null;
            const api = useApi();
            try {
                const { data } = await api.get(`/api/revisoes/${idRevisao}/mudancas`);
                this.items = data as Mudanca[];
            } catch (error) {
                this.error = 'Falha ao buscar mudanças da revisão.';
            } finally {
                this.loading = false;
            }
        },

        async registrarMudanca(idRevisao: number, mudanca: Omit<Mudanca, 'id'>) {
            const api = useApi();
            try {
                await api.post(`/api/revisoes/${idRevisao}/mudancas`, mudanca);
                await this.fetchMudancas(idRevisao);
            } catch (error) {
                throw new Error('Falha ao registrar a mudança.');
            }
        },

        async limparMudancas(idRevisao: number) {
            const api = useApi();
            try {
                // Presume um endpoint para limpar as mudanças de uma revisão
                await api.del(`/api/revisoes/${idRevisao}/mudancas`);
                this.items = [];
            } catch (error) {
                throw new Error('Falha ao limpar as mudanças.');
            }
        },

        // A lógica abaixo é mais relacionada à UI e depende de outros stores, então é mantida.
        obterIdsCompetenciasImpactadas(atividadeId: number, siglaUnidade: string, idProcesso: number): number[] {
            const mapasStore = useMapasStore();
            const idsImpactados: number[] = [];
            const mapaAtual = mapasStore.getMapaByUnidadeId(siglaUnidade, idProcesso);

            if (mapaAtual) {
                mapaAtual.competencias.forEach(comp => {
                    if (comp.atividadesAssociadas.includes(atividadeId)) {
                        idsImpactados.push(comp.id);
                    }
                });
            }
            return idsImpactados;
        },
        setMudancasParaImpacto(mudancas: Mudanca[]) {
            this.mudancasParaImpacto = mudancas;
        },
    },
});