import {useInvalidacaoEstadoNavegacao} from "@/composables/useInvalidacaoEstadoNavegacao";
import {useInvalidacaoQueriesNavegacao} from "@/composables/useInvalidacaoQueriesNavegacao";

/**
 * Ponto central de invalidação após mutações de processo/subprocesso.
 *
 * `painel` e `processo` já vivem em query cache; `subprocesso`, `mapas`
 * e `unidade` ainda mantêm estado local que precisa ser invalidado.
 */
export function useInvalidacaoNavegacao() {
    const queries = useInvalidacaoQueriesNavegacao();
    const estado = useInvalidacaoEstadoNavegacao();

    function atualizarFluxoProcesso(): void {
        queries.invalidarFluxoProcessoRemoto();
        estado.atualizarEstadoPainel();
        estado.atualizarEstadoSubprocesso();
        estado.atualizarEstadoUnidade();
        estado.atualizarEstadoMapa();
    }

    function atualizarFluxoSubprocesso(): void {
        estado.atualizarEstadoSubprocesso();
    }

    function atualizarFluxoSubprocessoEPainel(): void {
        queries.invalidarFluxoSubprocessoEPainelRemoto();
        estado.atualizarEstadoPainel();
        estado.atualizarEstadoSubprocesso();
    }

    function atualizarFluxoSubprocessoEProcesso(): void {
        queries.invalidarFluxoSubprocessoEProcessoRemoto();
        estado.atualizarEstadoSubprocesso();
    }

    function atualizarFluxoMapa(codigoSubprocesso?: number): void {
        queries.invalidarFluxoMapaRemoto();
        estado.atualizarEstadoPainel();
        estado.atualizarEstadoSubprocesso();
        estado.atualizarEstadoMapa(codigoSubprocesso);
    }

    function atualizarDadosOrganizacionais(): void {
        estado.atualizarEstadoOrganizacional();
        estado.atualizarEstadoPainel();
        queries.invalidarDadosOrganizacionaisRemotos();
    }

    function limparEstadoSubprocessoAtual(): void {
        estado.limparSubprocessoAtual();
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
