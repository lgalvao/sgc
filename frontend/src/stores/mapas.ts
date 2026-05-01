import {defineStore} from "pinia";
import {ref} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";

/**
 * Store de cache de sessão para dados do mapa de competências.
 *
 * Substitui o estado de módulo singleton que existia em `useMapas.ts`,
 * garantindo ciclo de vida correto: os dados são invalidados no logout,
 * na troca de perfil e nas ações de workflow que afetam o mapa.
 */
export const useMapasStore = defineStore("mapas", () => {
    const mapaCompleto = ref<MapaCompleto | null>(null);
    const impactoMapa = ref<ImpactoMapa | null>(null);

    function invalidar(): void {
        mapaCompleto.value = null;
        impactoMapa.value = null;
    }

    return {
        mapaCompleto,
        impactoMapa,
        invalidar,
    };
});
