import {useInvalidacaoPainel} from "@/composables/usePainelQuery";
import {useInvalidacaoProcesso} from "@/composables/useProcessoQuery";

export function useInvalidacaoQueriesNavegacao() {
    const {invalidarPainel} = useInvalidacaoPainel();
    const {invalidarProcesso} = useInvalidacaoProcesso();

    function invalidarFluxoProcessoRemoto(): void {
        invalidarPainel();
        invalidarProcesso();
    }

    function invalidarFluxoSubprocessoEPainelRemoto(): void {
        invalidarPainel();
    }

    function invalidarFluxoSubprocessoEProcessoRemoto(): void {
        invalidarProcesso();
    }

    function invalidarFluxoMapaRemoto(): void {
        invalidarFluxoProcessoRemoto();
    }

    function invalidarDadosOrganizacionaisRemotos(): void {
        invalidarPainel();
    }

    return {
        invalidarDadosOrganizacionaisRemotos,
        invalidarFluxoMapaRemoto,
        invalidarFluxoProcessoRemoto,
        invalidarFluxoSubprocessoEProcessoRemoto,
        invalidarFluxoSubprocessoEPainelRemoto,
    };
}
