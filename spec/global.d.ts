/// <reference types="@playwright/test" />

declare global {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
  interface Window {
    generateUniqueId: () => number;
      waitForVue: (page: object) => Promise<void>; // Adicionado
      Vue: any; // eslint-disable-line @typescript-eslint/no-explicit-any
      __VUE__: any; // eslint-disable-line @typescript-eslint/no-explicit-any
  }
}