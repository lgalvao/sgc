import { defineStore } from 'pinia';
import alertasMock from '../mocks/alertas.json';

/**
 * @typedef { import('../types/domain').Alerta } Alerta
 */

export const useAlertasStore = defineStore('alertas', {
  state: () => ({
    /** @type {Alerta[]} */
    alertas: alertasMock
  }),
  actions: {
    // Ações para manipular alertas podem ser adicionadas aqui no futuro
  }
});
