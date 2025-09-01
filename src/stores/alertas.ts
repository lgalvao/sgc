import {defineStore} from 'pinia';
import alertasMock from '../mocks/alertas.json';
import type {Alerta} from '@/types/tipos';
import {useConfiguracoesStore} from './configuracoes'; // Import the new store

function parseAlertaDates(alerta: Omit<Alerta, 'dataHora'> & { dataHora: string }): Alerta {
    return {
        ...alerta,
        dataHora: new Date(alerta.dataHora),
    };
}

export const useAlertasStore = defineStore('alertas', {
    state: () => ({
        alertas: alertasMock.map(parseAlertaDates) as Alerta[]
    }),
    getters: {
        isAlertaNovo: () => (alerta: Alerta): boolean => {
            const configuracoesStore = useConfiguracoesStore();
            const alertaDate = new Date(alerta.dataHora);
            const today = new Date();
            const diffTime = Math.abs(today.getTime() - alertaDate.getTime());
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            return diffDays <= configuracoesStore.diasAlertaNovo;
        }
    },
    actions: {
        pesquisarAlertas(query?: string): Alerta[] {
            if (!query) {
                return this.alertas;
            }
            const lowerCaseQuery = query.toLowerCase();
            return this.alertas.filter(alerta =>
                alerta.descricao.toLowerCase().includes(lowerCaseQuery) ||
                alerta.unidadeOrigem.toLowerCase().includes(lowerCaseQuery) ||
                alerta.unidadeDestino.toLowerCase().includes(lowerCaseQuery)
            );
        },

        getAlertaById(id: number): Alerta | undefined {
            return this.alertas.find(alerta => alerta.id === id);
        },

        criarAlerta(novoAlerta: Omit<Alerta, 'id'>): Alerta {
            const newId = this.alertas.length > 0 ? Math.max(...this.alertas.map(a => a.id)) + 1 : 1;
            const alertaComId: Alerta = {...novoAlerta, id: newId};
            this.alertas.push(alertaComId);
            return alertaComId;
        },

        atualizarAlerta(alertaAtualizado: Alerta): boolean {
            const index = this.alertas.findIndex(alerta => alerta.id === alertaAtualizado.id);
            if (index !== -1) {
                this.alertas[index] = alertaAtualizado;
                return true;
            }
            return false;
        },

        excluirAlerta(id: number): boolean {
            const initialLength = this.alertas.length;
            this.alertas = this.alertas.filter(alerta => alerta.id !== id);
            return this.alertas.length < initialLength;
        }
    }
});