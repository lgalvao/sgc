import {usePainelStore} from "@/stores/painel";
import {useProcessoStore} from "@/stores/processo";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useSubprocessos} from "@/composables/useSubprocessos";

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
    const subprocessos = useSubprocessos();

    function limparEstadoSubprocessoAtual(): void {
        subprocessos.subprocessoDetalhe = null;
    }

    function invalidarCachesProcesso(): void {
        painelStore.invalidar();
        processoStore.invalidar();
        subprocessoStore.invalidar();
        limparEstadoSubprocessoAtual();
    }

    function invalidarCachesSubprocesso(opcoes?: {incluirPainel?: boolean}): void {
        if (opcoes?.incluirPainel ?? true) {
            painelStore.invalidar();
        }
        processoStore.invalidar();
        subprocessoStore.invalidar();
        limparEstadoSubprocessoAtual();
    }

    return {
        invalidarCachesProcesso,
        invalidarCachesSubprocesso,
        limparEstadoSubprocessoAtual,
    };
}
