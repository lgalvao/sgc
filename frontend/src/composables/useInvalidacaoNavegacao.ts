import {useCacheMapa} from "@/composables/useMapaQuery";
import {useInvalidacaoPainel} from "@/composables/usePainelQuery";
import {useInvalidacaoProcesso} from "@/composables/useProcessoQuery";
import {useInvalidacaoUnidade} from "@/composables/useUnidadeQuery";
import {useOrganizacaoStore} from "@/stores/organizacao";
import {usePainelStore} from "@/stores/painel";
import {useSubprocessoStore} from "@/stores/subprocesso";

/**
 * Ponto central de invalidação após mutações de processo/subprocesso.
 *
 * `painel`, `processo` e `mapas` vivem em query cache; `subprocesso`,
 * `organizacao` e `unidade` ainda mantêm estado local que precisa ser invalidado.
 */
export function useInvalidacaoNavegacao() {
    const {invalidarPainel} = useInvalidacaoPainel();
    const {invalidarProcesso} = useInvalidacaoProcesso();
    const {invalidarMapa} = useCacheMapa();
    const {invalidarUnidade, invalidarDadosTelaUnidade, invalidarArvoreElegibilidade} = useInvalidacaoUnidade();
    const painelStore = usePainelStore();
    const subprocessoStore = useSubprocessoStore();
    const organizacaoStore = useOrganizacaoStore();

    function atualizarFluxoProcesso(): void {
        invalidarPainel();
        invalidarProcesso();
        painelStore.invalidar();
        subprocessoStore.invalidar();
        invalidarMapa();
        invalidarUnidade();
        invalidarDadosTelaUnidade();
        invalidarArvoreElegibilidade();
    }

    function atualizarFluxoSubprocesso(): void {
        subprocessoStore.invalidar();
    }

    function atualizarFluxoSubprocessoEPainel(): void {
        invalidarPainel();
        painelStore.invalidar();
        subprocessoStore.invalidar();
    }

    function atualizarFluxoSubprocessoEProcesso(): void {
        invalidarProcesso();
        subprocessoStore.invalidar();
    }

    function atualizarFluxoMapa(codigoSubprocesso?: number): void {
        invalidarPainel();
        invalidarProcesso();
        painelStore.invalidar();
        subprocessoStore.invalidar();
        invalidarMapa(codigoSubprocesso);
    }

    function atualizarDadosOrganizacionais(): void {
        organizacaoStore.invalidar();
        painelStore.invalidar();
        invalidarPainel();
        invalidarUnidade();
        invalidarDadosTelaUnidade();
        invalidarArvoreElegibilidade();
    }

    function limparEstadoSubprocessoAtual(): void {
        subprocessoStore.limparContextoAtual();
    }

    function resetarEstadoSessao(): void {
        painelStore.resetar();
        subprocessoStore.resetar();
        invalidarMapa();
        organizacaoStore.resetar();
        invalidarUnidade();
        invalidarDadosTelaUnidade();
        invalidarArvoreElegibilidade();
    }

    return {
        atualizarDadosOrganizacionais,
        atualizarFluxoMapa,
        atualizarFluxoProcesso,
        atualizarFluxoSubprocesso,
        atualizarFluxoSubprocessoEProcesso,
        atualizarFluxoSubprocessoEPainel,
        limparEstadoSubprocessoAtual,
        resetarEstadoSessao,
    };
}
