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

    async function atualizarFluxoProcesso(): Promise<void> {
        await Promise.all([
            invalidarPainel(),
            invalidarProcesso(),
            invalidarUnidade(),
            invalidarDadosTelaUnidade(),
            invalidarArvoreElegibilidade(),
        ]);
        painelStore.invalidar();
        subprocessoStore.invalidar();
        invalidarMapa();
    }

    function atualizarFluxoSubprocesso(): void {
        subprocessoStore.invalidar();
    }

    async function atualizarFluxoSubprocessoEPainel(): Promise<void> {
        await invalidarPainel();
        painelStore.invalidar();
        subprocessoStore.invalidar();
    }

    async function atualizarFluxoCadastro(codigoSubprocesso: number): Promise<void> {
        await invalidarPainel();
        painelStore.invalidar();
        subprocessoStore.invalidar();
        invalidarMapa(codigoSubprocesso);
    }

    async function atualizarFluxoSubprocessoEProcesso(): Promise<void> {
        await invalidarProcesso();
        subprocessoStore.invalidar();
    }

    async function atualizarFluxoMapa(codigoSubprocesso?: number): Promise<void> {
        await Promise.all([
            invalidarPainel(),
            invalidarProcesso(),
        ]);
        painelStore.invalidar();
        subprocessoStore.invalidar();
        invalidarMapa(codigoSubprocesso);
    }

    async function atualizarDadosOrganizacionais(): Promise<void> {
        await Promise.all([
            invalidarDiagnostico(),
            invalidarPainel(),
            invalidarUnidade(),
            invalidarDadosTelaUnidade(),
            invalidarArvoreElegibilidade(),
        ]);
        painelStore.invalidar();
    }

    function limparEstadoSubprocessoAtual(): void {
        subprocessoStore.limparContextoAtual();
    }

    async function resetarEstadoSessao(): Promise<void> {
        painelStore.resetar();
        subprocessoStore.resetar();
        await Promise.all([
            invalidarPainel(),
            invalidarProcesso(),
            invalidarUnidade(),
            invalidarDadosTelaUnidade(),
            invalidarArvoreElegibilidade(),
            invalidarDiagnostico(),
        ]);
        invalidarMapa();
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
