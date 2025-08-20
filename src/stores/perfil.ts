import {defineStore} from 'pinia';

export const usePerfilStore = defineStore('perfil', {
    state: () => ({
        servidorId: localStorage.getItem('servidorId') ? Number(localStorage.getItem('servidorId')) : 9, // Default para Giuseppe Corleone (CHEFE)
        perfilSelecionado: (localStorage.getItem('perfilSelecionado') || null) as string | null,
        unidadeSelecionada: (localStorage.getItem('unidadeSelecionada') || null) as string | null,
    }),
    actions: {
        setServidorId(novoId: number) {
            this.servidorId = novoId;
            localStorage.setItem('servidorId', novoId.toString());
        },
        setPerfilUnidade(perfil: string, unidadeSigla: string) {
            this.perfilSelecionado = perfil;
            this.unidadeSelecionada = unidadeSigla;
            localStorage.setItem('perfilSelecionado', perfil);
            localStorage.setItem('unidadeSelecionada', unidadeSigla);
        },
    },
}); 