import {useMapasStore} from "@/stores/mapas";

export function useEstadoMapaNavegacao() {
    const mapasStore = useMapasStore();

    function atualizarEstadoMapa(codigoSubprocesso?: number): void {
        if (typeof codigoSubprocesso === "number") {
            mapasStore.marcarMapaParaAtualizacao(codigoSubprocesso);
            return;
        }

        mapasStore.invalidar();
    }

    function resetarEstadoMapa(): void {
        mapasStore.resetar();
    }

    return {
        atualizarEstadoMapa,
        resetarEstadoMapa,
    };
}
