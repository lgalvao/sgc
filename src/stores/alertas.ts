import {defineStore} from 'pinia';
import alertasMock from '../mocks/alertas.json';
import type {Alerta} from '@/types/tipos';

function parseAlertaDates(alerta: any): Alerta {
    return {
        ...alerta,
        dataHora: new Date(alerta.dataHora),
    };
}

export const useAlertasStore = defineStore('alertas', {
  state: () => ({
    alertas: alertasMock.map(parseAlertaDates) as Alerta[]
  }),
  actions: {
    // Ações para manipular alertas podem ser adicionadas aqui no futuro
  }
});
