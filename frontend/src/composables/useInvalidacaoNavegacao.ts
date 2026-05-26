import {useQueryCache} from "@pinia/colada";
import {CHAVE_QUERY_PAINEL} from "@/composables/usePainelQuery";
import {CHAVE_QUERY_PROCESSO} from "@/composables/useProcessoQuery";
import {usePainelStore} from "@/stores/painel";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import {useUnidadeStore} from "@/stores/unidade";

interface OpcoesInvalidacaoSubprocesso {
    incluirPainel?: boolean;
    incluirProcesso?: boolean;
    incluirMapas?: boolean;
    codigoSubprocessoMapa?: number;
}

/**
 * Ponto central de invalidação após mutações de processo/subprocesso.
 *
 * `painel` e `processo` já vivem em query cache; `subprocesso`, `mapas`
 * e `unidade` ainda mantêm estado local que precisa ser invalidado.
 */
export function useInvalidacaoNavegacao() {
    const queryCache = useQueryCache();
    const painelStore = usePainelStore();
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();
    const unidadeStore = useUnidadeStore();

    function invalidarPainel(): void {
        painelStore.invalidar();
        void queryCache.invalidateQueries({key: CHAVE_QUERY_PAINEL});
    }

    function invalidarProcesso(): void {
        void queryCache.invalidateQueries({key: CHAVE_QUERY_PROCESSO});
    }

    function limparEstadoSubprocessoAtual(): void {
        subprocessoStore.limparContextoAtual();
    }

    function invalidarCachesProcesso(): void {
        invalidarPainel();
        invalidarProcesso();
        subprocessoStore.invalidar();
        mapasStore.invalidar();
        unidadeStore.invalidar();
    }

    function invalidarMapas(opcoes?: OpcoesInvalidacaoSubprocesso) {
        if (!(opcoes?.incluirMapas ?? false)) {
            return;
        }

        const codigoSubprocessoMapa = opcoes?.codigoSubprocessoMapa;
        if (typeof codigoSubprocessoMapa === "number") {
            mapasStore.invalidar(codigoSubprocessoMapa);
            return;
        }

        mapasStore.invalidar();
    }

    function invalidarCachesSubprocesso(opcoes?: OpcoesInvalidacaoSubprocesso): void {
        if (opcoes?.incluirPainel) {
            invalidarPainel();
        }
        if (opcoes?.incluirProcesso) {
            invalidarProcesso();
        }
        subprocessoStore.invalidar();
        // Mapas são pesados e nem toda ação de subprocesso altera esse domínio.
        // Mantemos opt-in explícito para evitar recarregamentos desnecessários.
        invalidarMapas(opcoes);
    }

    return {
        invalidarCachesProcesso,
        invalidarCachesSubprocesso,
        limparEstadoSubprocessoAtual,
    };
}
