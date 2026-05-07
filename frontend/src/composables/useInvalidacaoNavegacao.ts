import {usePainelStore} from "@/stores/painel";
import {useProcessoStore} from "@/stores/processo";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";

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
    const mapasStore = useMapasStore();

    function limparEstadoSubprocessoAtual(): void {
        subprocessoStore.limparContextoAtual();
    }

    function invalidarCachesProcesso(): void {
        painelStore.invalidar();
        processoStore.invalidar();
        subprocessoStore.invalidar();
        mapasStore.invalidar();
    }

    function invalidarCachesSubprocesso(opcoes?: { incluirPainel?: boolean; incluirProcesso?: boolean; incluirMapas?: boolean }): void {
        if (opcoes?.incluirPainel ?? false) {
            painelStore.invalidar();
        }
        if (opcoes?.incluirProcesso ?? false) {
            processoStore.invalidar();
        }
        subprocessoStore.invalidar();
        if (opcoes?.incluirMapas ?? true) {
            mapasStore.invalidar();
        }
    }

    return {
        invalidarCachesProcesso,
        invalidarCachesSubprocesso,
        limparEstadoSubprocessoAtual,
    };
}
