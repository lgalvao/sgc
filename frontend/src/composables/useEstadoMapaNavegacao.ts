import {useCacheMapa} from "@/composables/useMapaQuery";

export function useEstadoMapaNavegacao() {
    const cacheMapa = useCacheMapa();

    function atualizarEstadoMapa(codigoSubprocesso?: number): void {
        cacheMapa.invalidarMapa(codigoSubprocesso);
    }

    function resetarEstadoMapa(): void {
        cacheMapa.invalidarMapa();
        cacheMapa.invalidarImpacto();
    }

    return {
        atualizarEstadoMapa,
        resetarEstadoMapa,
    };
}
