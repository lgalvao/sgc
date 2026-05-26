import {useSubprocessoStore} from "@/stores/subprocesso";

export function useEstadoSubprocessoNavegacao() {
    const subprocessoStore = useSubprocessoStore();

    function atualizarEstadoSubprocesso(): void {
        subprocessoStore.invalidar();
    }

    function limparSubprocessoAtual(): void {
        subprocessoStore.limparContextoAtual();
    }

    function resetarEstadoSubprocesso(): void {
        subprocessoStore.resetar();
    }

    return {
        atualizarEstadoSubprocesso,
        limparSubprocessoAtual,
        resetarEstadoSubprocesso,
    };
}
