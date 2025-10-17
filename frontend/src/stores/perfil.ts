import {defineStore} from 'pinia';
import {Perfil} from "@/types/tipos";

export const usePerfilStore = defineStore('perfil', {
    state: () => ({
        servidorId: localStorage.getItem('idServidor') ? Number(localStorage.getItem('idServidor')) : 9, // Default para Giuseppe Corleone (CHEFE)
        perfilSelecionado: (localStorage.getItem('perfilSelecionado') || null) as Perfil | null,
        unidadeSelecionada: (localStorage.getItem('unidadeSelecionada') || null) as string | null,
    }),
    actions: {
        setServidorId(novoId: number) {
            this.servidorId = novoId;
            localStorage.setItem('idServidor', novoId.toString());
        },
        setPerfilUnidade(perfil: Perfil, unidadeSigla: string) {
            this.perfilSelecionado = perfil;
            this.unidadeSelecionada = unidadeSigla;
            localStorage.setItem('perfilSelecionado', perfil);
            localStorage.setItem('unidadeSelecionada', unidadeSigla);
        },
    },
}); 