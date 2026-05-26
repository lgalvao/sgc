import {useQueryCache} from "@pinia/colada";
import {CHAVE_QUERY_PAINEL} from "@/composables/usePainelQuery";
import {CHAVE_QUERY_PROCESSO} from "@/composables/useProcessoQuery";
import {usePainelStore} from "@/stores/painel";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import {useUnidadeStore} from "@/stores/unidade";
import {useOrganizacaoStore} from "@/stores/organizacao";

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
    const organizacaoStore = useOrganizacaoStore();

    function invalidarQueries(...chaves: Array<typeof CHAVE_QUERY_PAINEL | typeof CHAVE_QUERY_PROCESSO>): void {
        chaves.forEach((key) => {
            void queryCache.invalidateQueries({key});
        });
    }

    function invalidarStores(...stores: Array<{ invalidar: () => void }>): void {
        stores.forEach((store) => {
            store.invalidar();
        });
    }

    function invalidarPainel(): void {
        painelStore.invalidar();
        invalidarQueries(CHAVE_QUERY_PAINEL);
    }

    function invalidarProcesso(): void {
        invalidarQueries(CHAVE_QUERY_PROCESSO);
    }

    function invalidarMapas(codigoSubprocesso?: number): void {
        if (typeof codigoSubprocesso === "number") {
            mapasStore.invalidar(codigoSubprocesso);
            return;
        }

        mapasStore.invalidar();
    }

    function limparEstadoSubprocessoAtual(): void {
        subprocessoStore.limparContextoAtual();
    }

    function atualizarFluxoProcesso(): void {
        invalidarPainel();
        invalidarProcesso();
        invalidarStores(subprocessoStore, unidadeStore);
        invalidarMapas();
    }

    function atualizarFluxoSubprocesso(): void {
        invalidarStores(subprocessoStore);
    }

    function atualizarFluxoSubprocessoEPainel(): void {
        invalidarPainel();
        invalidarStores(subprocessoStore);
    }

    function atualizarFluxoSubprocessoEProcesso(): void {
        invalidarProcesso();
        invalidarStores(subprocessoStore);
    }

    function atualizarFluxoMapa(codigoSubprocesso?: number): void {
        invalidarPainel();
        invalidarProcesso();
        invalidarStores(subprocessoStore);
        invalidarMapas(codigoSubprocesso);
    }

    function atualizarDadosOrganizacionais(): void {
        invalidarStores(unidadeStore, organizacaoStore);
        invalidarPainel();
    }

    return {
        atualizarDadosOrganizacionais,
        atualizarFluxoMapa,
        atualizarFluxoProcesso,
        atualizarFluxoSubprocesso,
        atualizarFluxoSubprocessoEProcesso,
        atualizarFluxoSubprocessoEPainel,
        limparEstadoSubprocessoAtual,
    };
}
