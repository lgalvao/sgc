/// <reference types="@playwright/test" />

declare global {
     
  interface Window {
    generateUniqueId: () => number;
      waitForVue: (page: object) => Promise<void>; // Adicionado
      Vue: any;  
      __VUE__: any;  
  }
}