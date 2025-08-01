import { defineStore } from 'pinia';

export const usePerfilStore = defineStore('perfil', {
  state: () => ({
    servidorId: localStorage.getItem('servidorId') ? Number(localStorage.getItem('servidorId')) : 9, // Default para Giuseppe Corleone (CHEFE)
    perfilSelecionado: localStorage.getItem('perfilSelecionado') || null,
    unidadeSelecionada: localStorage.getItem('unidadeSelecionada') || null,
  }),
  actions: {
    setServidorId(novoId) {
      this.servidorId = novoId;
      localStorage.setItem('servidorId', novoId);
    },
    setPerfilUnidade(perfil, unidadeSigla) {
      this.perfilSelecionado = perfil;
      this.unidadeSelecionada = unidadeSigla;
      localStorage.setItem('perfilSelecionado', perfil);
      localStorage.setItem('unidadeSelecionada', unidadeSigla);
    },
  },
}); 