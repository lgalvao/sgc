import {usePainelStore} from "@/stores/painel";
import {useProcessoStore} from "@/stores/processo";
import {useSubprocessoStore} from "@/stores/subprocesso";

/**
 * Centraliza a invalidação dos caches de navegação da SPA.
 *
 * O frontend usa stores Pinia como cache de sessão e várias rotas em keepAlive.
 * Quando uma ação de workflow esquece de invalidar um store relacionado, a
 * navegação interna reaproveita snapshots antigos até um refresh completo.
 */
export function useInvalidacaoNavegacao() {
    const painelStore = usePainelStore();
    const processoStore = useProcessoStore();
    const subprocessoStore = useSubprocessoStore();

    function limparEstadoSubprocessoAtual(): void {
        subprocessoStore.limparContextoAtual();
    }

    function invalidarCachesProcesso(): void {
        painelStore.invalidar();
        processoStore.invalidar();
        subprocessoStore.invalidar();
    }

    function invalidarCachesSubprocesso(opcoes?: {incluirPainel?: boolean; incluirProcesso?: boolean}): void {
        if (opcoes?.incluirPainel ?? true) {
            painelStore.invalidar();
        }
        if (opcoes?.incluirProcesso ?? true) {
            processoStore.invalidar();
        }
        subprocessoStore.invalidar();
    }

    return {
        invalidarCachesProcesso,
        invalidarCachesSubprocesso,
        limparEstadoSubprocessoAtual,
    };
}
