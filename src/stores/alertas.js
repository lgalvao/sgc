import { defineStore } from 'pinia';
import alertasMock from '../mocks/alertas.json';

export const useAlertasStore = defineStore('alertas', {
  state: () => ({
    alertas: [...alertasMock]
  }),
  actions: {
    // Ações para manipular alertas podem ser adicionadas aqui no futuro
  }
});
