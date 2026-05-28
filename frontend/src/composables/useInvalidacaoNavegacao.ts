import {useCacheMapa} from "@/composables/useMapaQuery";
import {useInvalidacaoPainel} from "@/composables/usePainelQuery";
import {useInvalidacaoProcesso} from "@/composables/useProcessoQuery";
import {useInvalidacaoUnidade} from "@/composables/useUnidadeQuery";
import {useInvalidacaoDiagnosticoOrganizacional} from "@/composables/useDiagnosticoOrganizacionalQuery";
import {usePainelStore} from "@/stores/painel";
import {useSubprocessoStore} from "@/stores/subprocesso";

/**
 * Ponto central de invalidação após mutações de processo/subprocesso.
 *
 * `painel`, `processo`, `mapas` e `diagnostico-organizacional` vivem em query cache.
 * `subprocesso` ainda mantém estado local que precisa ser invalidado manualmente.
 */
export function useInvalidacaoNavegacao() {
    const {invalidarPainel} = useInvalidacaoPainel();
    const {invalidarProcesso} = useInvalidacaoProcesso();
    const {invalidarMapa} = useCacheMapa();
    const {invalidarUnidade, invalidarDadosTelaUnidade, invalidarArvoreElegibilidade} = useInvalidacaoUnidade();
    const {invalidarDiagnostico} = useInvalidacaoDiagnosticoOrganizacional();
    const painelStore = usePainelStore();
    const subprocessoStore = useSubprocessoStore();

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

    function atualizarFluxoCadastro(codigoSubprocesso: number): void {
        invalidarPainel();
        painelStore.invalidar();
        subprocessoStore.invalidar();
        invalidarMapa(codigoSubprocesso);
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
        invalidarDiagnostico();
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
        invalidarDiagnostico();
        invalidarUnidade();
        invalidarDadosTelaUnidade();
        invalidarArvoreElegibilidade();
    }

    return {
        atualizarDadosOrganizacionais,
        atualizarFluxoCadastro,
        atualizarFluxoMapa,
        atualizarFluxoProcesso,
        atualizarFluxoSubprocesso,
        atualizarFluxoSubprocessoEProcesso,
        atualizarFluxoSubprocessoEPainel,
        limparEstadoSubprocessoAtual,
        resetarEstadoSessao,
    };
}
