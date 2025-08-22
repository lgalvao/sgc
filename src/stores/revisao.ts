import {defineStore} from 'pinia';

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
}

export const useRevisaoStore = defineStore('revisao', {
    state: () => {
        const storedMudancas = sessionStorage.getItem('revisaoMudancas');
        return {
            mudancasRegistradas: storedMudancas ? JSON.parse(storedMudancas) : [] as Mudanca[],
        };
    },
    actions: {
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

// Expor a store globalmente para depuração em testes
(window as any).revisaoStoreParaTeste = useRevisaoStore();
