import {defineStore} from 'pinia';
import {useMapasStore} from './mapas';


export enum TipoMudanca {
    AtividadeAdicionada = 'AtividadeAdicionada',
    AtividadeRemovida = 'AtividadeRemovida',
    AtividadeAlterada = 'AtividadeAlterada',
    ConhecimentoAdicionado = 'ConhecimentoAdicionado',
    ConhecimentoRemovido = 'ConhecimentoRemovido',
    ConhecimentoAlterado = 'ConhecimentoAlterado',
}

export interface Mudanca {
    id: number; // ID único da mudança
    tipo: TipoMudanca;
    idAtividade?: number; // ID da atividade envolvida
    idConhecimento?: number; // ID do conhecimento envolvido
    descricaoAtividade?: string; // Descrição da atividade no momento da mudança
    descricaoConhecimento?: string; // Descrição do conhecimento no momento da mudança
    valorAntigo?: string; // Valor antigo (para alterações)
    valorNovo?: string; // Valor novo (para alterações)
    competenciasImpactadasIds?: number[]; // IDs das competências impactadas pela mudança
}

export const useRevisaoStore = defineStore('revisao', {
    state: () => {
        const storedMudancas = sessionStorage.getItem('revisaoMudancas');
        return {
            mudancasRegistradas: storedMudancas ? JSON.parse(storedMudancas) : [] as Mudanca[],
            mudancasParaImpacto: [] as Mudanca[],
        };
    },
    actions: {
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
        registrarMudanca(mudanca: Omit<Mudanca, 'id'>) {
            this.mudancasRegistradas.push({...mudanca, id: Date.now()});
            sessionStorage.setItem('revisaoMudancas', JSON.stringify(this.mudancasRegistradas)); // Salva após cada registro
        },
        limparMudancas() {
            this.mudancasRegistradas = [];
            sessionStorage.removeItem('revisaoMudancas'); // Limpa o sessionStorage também
        },
    },
});


