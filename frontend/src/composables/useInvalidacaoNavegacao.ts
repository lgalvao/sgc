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
        void invalidarPainel();
        void invalidarProcesso();
        painelStore.invalidar();
        subprocessoStore.invalidar();
        invalidarMapa();
        void invalidarUnidade();
        void invalidarDadosTelaUnidade();
        void invalidarArvoreElegibilidade();
    }

    function atualizarFluxoSubprocesso(): void {
        subprocessoStore.invalidar();
    }

    function atualizarFluxoSubprocessoEPainel(): void {
        void invalidarPainel();
        painelStore.invalidar();
        subprocessoStore.invalidar();
    }

    function atualizarFluxoCadastro(codigoSubprocesso: number): void {
        void invalidarPainel();
        painelStore.invalidar();
        subprocessoStore.invalidar();
        invalidarMapa(codigoSubprocesso);
    }

    function atualizarFluxoSubprocessoEProcesso(): void {
        void invalidarProcesso();
        subprocessoStore.invalidar();
    }

    function atualizarFluxoMapa(codigoSubprocesso?: number): void {
        void invalidarPainel();
        void invalidarProcesso();
        painelStore.invalidar();
        subprocessoStore.invalidar();
        invalidarMapa(codigoSubprocesso);
    }

    function atualizarDadosOrganizacionais(): void {
        void invalidarDiagnostico();
        painelStore.invalidar();
        void invalidarPainel();
        void invalidarUnidade();
        void invalidarDadosTelaUnidade();
        void invalidarArvoreElegibilidade();
    }

    function limparEstadoSubprocessoAtual(): void {
        subprocessoStore.limparContextoAtual();
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

    };
}
