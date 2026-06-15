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

    function marcarPainelParaRecarregar(): void {
        painelStore.invalidar();
    }

    async function atualizarFluxoProcesso(): Promise<void> {
        marcarPainelParaRecarregar();
        await Promise.all([
            invalidarPainel(),
            invalidarProcesso(),
            invalidarUnidade(),
            invalidarDadosTelaUnidade(),
            invalidarArvoreElegibilidade(),
        ]);
        subprocessoStore.invalidar();
        invalidarMapa();
    }

    function atualizarFluxoSubprocesso(): void {
        subprocessoStore.invalidar();
    }

    async function atualizarFluxoSubprocessoEPainel(): Promise<void> {
        marcarPainelParaRecarregar();
        await invalidarPainel();
        subprocessoStore.invalidar();
    }

    async function atualizarFluxoCadastro(codigoSubprocesso: number): Promise<void> {
        marcarPainelParaRecarregar();
        await invalidarPainel();
        subprocessoStore.invalidar();
        invalidarMapa(codigoSubprocesso);
    }

    async function atualizarFluxoSubprocessoEProcesso(): Promise<void> {
        await invalidarProcesso();
        subprocessoStore.invalidar();
    }

    async function atualizarFluxoMapa(codigoSubprocesso?: number): Promise<void> {
        marcarPainelParaRecarregar();
        await Promise.all([
            invalidarPainel(),
            invalidarProcesso(),
        ]);
        subprocessoStore.invalidar();
        invalidarMapa(codigoSubprocesso);
    }

    async function atualizarDadosOrganizacionais(): Promise<void> {
        marcarPainelParaRecarregar();
        await Promise.all([
            invalidarDiagnostico(),
            invalidarPainel(),
            invalidarUnidade(),
            invalidarDadosTelaUnidade(),
            invalidarArvoreElegibilidade(),
        ]);
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
