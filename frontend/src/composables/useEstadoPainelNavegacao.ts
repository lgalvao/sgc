import {usePainelStore} from "@/stores/painel";

export function useEstadoPainelNavegacao() {
    const painelStore = usePainelStore();

    function atualizarEstadoPainel(): void {
        painelStore.invalidar();
    }

    function resetarEstadoPainel(): void {
        painelStore.resetar();
    }

    return {
        atualizarEstadoPainel,
        resetarEstadoPainel,
    };
}
