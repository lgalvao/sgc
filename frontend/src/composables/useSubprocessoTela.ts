import {computed} from "vue";
import {useNotification} from "@/composables/useNotification";
import {useToast} from "@/composables/useToast";
import {useFluxoSubprocesso} from "@/composables/useFluxoSubprocesso";
import {useAcesso} from "@/composables/acesso";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useSubprocessoAcoesAdministrativas} from "@/views/subprocessoAcoesAdministrativas";
import {useSubprocessoCarregamento} from "@/views/subprocessoCarregamento";
import {enviarLembrete as enviarLembreteService} from "@/services/processo";
import {analisarData, formatarDataBR} from "@/utils";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {TEXTOS} from "@/constants/textos";
import {type Movimentacao, type ResponsavelDto, type SubprocessoDetalhe, TipoProcesso} from "@/types/tipos";

interface SubprocessoTelaProps {
    codProcesso: number;
    siglaUnidade: string;
    codSubprocesso?: number;
}

export function useSubprocessoTela(props: SubprocessoTelaProps) {
    function formatDataSimples(dataStr: string | null): string {
        return dataStr ? formatarDataBR(dataStr) : '';
    }

    function formatTipoResponsabilidade(resp: ResponsavelDto | null): string {
        if (!resp?.tipo) return '';
        if (resp.tipo === 'Substituição' && resp.dataFim) {
            return `Substituição (até ${formatDataSimples(resp.dataFim)})`;
        } else if (resp.tipo === 'Atribuição temporária' && resp.dataFim) {
            return `Atrib. temporária (até ${formatDataSimples(resp.dataFim)})`;
        }
        return resp.tipo;
    }

    const subprocessoStore = useSubprocessoStore();
    const fluxoSubprocesso = useFluxoSubprocesso();
    const {notificacao, notify, clear} = useNotification();
    const {exibirPendente} = useToast();
    const {
        validarSubmissao,
        resetarValidacao,
        deveExibirErro,
        focarPrimeiroErroInvalido
    } = useValidacaoFormulario();

    function exibirToastPendente() {
        exibirPendente();
    }

    const {
        codigoSubprocesso,
        erroNaoEncontrado,
        atualizarSubprocessoAtual,
    } = useSubprocessoCarregamento({
        codProcesso: props.codProcesso,
        siglaUnidade: props.siglaUnidade,
        codSubprocesso: props.codSubprocesso,
        exibirToastPendente,
    });

    const subprocesso = computed<SubprocessoDetalhe | null>(() => {
        const detalhes = subprocessoStore.contextoEdicao?.detalhes ?? null;
        if (!detalhes) return null;
        if (typeof codigoSubprocesso.value !== "number") return null;
        return detalhes.codigo === codigoSubprocesso.value ? detalhes : null;
    });

    const acesso = useAcesso(subprocesso);
    const {
        habilitarAlterarDataLimite,
        habilitarReabrirCadastro,
        habilitarReabrirRevisao,
        habilitarEnviarLembrete,
        mostrarAlterarDataLimite,
        mostrarReabrirCadastro,
        mostrarReabrirRevisao,
        mostrarEnviarLembrete
    } = acesso;

    const mostrarAcoesCabecalho = computed(() =>
        mostrarAlterarDataLimite.value
        || mostrarReabrirCadastro.value
        || mostrarReabrirRevisao.value
        || mostrarEnviarLembrete.value
    );

    const movimentacoes = computed<Movimentacao[]>(
        () => subprocesso.value?.movimentacoes ?? [],
    );

    const dataLimite = computed(() => {
        if (subprocesso.value?.prazoEtapaAtual) {
            return analisarData(subprocesso.value.prazoEtapaAtual);
        }
        const ultimaDataLimite = subprocesso.value?.ultimaDataLimiteSubprocesso;
        return ultimaDataLimite ? analisarData(ultimaDataLimite) : null;
    });

    const {
        tipoReabertura,
        justificativaReabertura,
        modalLembreteAberto,
        mostrarModalAlterarDataLimite,
        mostrarModalReabrir,
        loadingDataLimite,
        loadingReabertura,
        loadingLembrete,
        mensagemErroJustificativa,
        abrirModalAlterarDataLimite,
        fecharModalAlterarDataLimite,
        confirmarAlteracaoDataLimite,
        abrirModalReabrirCadastro,
        abrirModalReabrirRevisao,
        confirmarReabertura,
        confirmarEnviarLembrete,
        enviarLembreteConfirmado,
    } = useSubprocessoAcoesAdministrativas({
        subprocesso,
        codigoSubprocesso,
        codProcesso: props.codProcesso,
        habilitarAlterarDataLimite,
        deveExibirErro,
        resetarValidacao,
        validarSubmissao,
        focarPrimeiroErroInvalido,
        notify,
        atualizarSubprocessoAtual,
        exibirToastPendente,
        alterarDataLimiteSubprocesso: fluxoSubprocesso.alterarDataLimiteSubprocesso,
        reabrirCadastro: fluxoSubprocesso.reabrirCadastro,
        reabrirRevisaoCadastro: fluxoSubprocesso.reabrirRevisaoCadastro,
        enviarLembrete: enviarLembreteService,
    });

    return {
        erroIntegracaoContexto: computed(() => subprocessoStore.erroIntegracaoContexto),
        limparErroIntegracao: () => subprocessoStore.limparErroIntegracao(),
        notificacao,
        clear,
        subprocesso,
        formatDataSimples,
        formatSituacaoSubprocesso,
        formatTipoResponsabilidade,
        habilitarAlterarDataLimite,
        habilitarEnviarLembrete,
        habilitarReabrirCadastro,
        habilitarReabrirRevisao,
        mostrarAcoesCabecalho,
        mostrarAlterarDataLimite,
        mostrarEnviarLembrete,
        mostrarReabrirCadastro,
        mostrarReabrirRevisao,
        codigoSubprocesso,
        movimentacoes,
        erroNaoEncontrado,
        TEXTOS,
        TipoProcesso,
        dataLimite,
        analisarData,
        justificativaReabertura,
        loadingDataLimite,
        loadingLembrete,
        loadingReabertura,
        mensagemErroJustificativa,
        modalLembreteAberto,
        mostrarModalAlterarDataLimite,
        mostrarModalReabrir,
        tipoReabertura,
        abrirModalAlterarDataLimite,
        abrirModalReabrirCadastro,
        abrirModalReabrirRevisao,
        confirmarEnviarLembrete,
        confirmarAlteracaoDataLimite,
        enviarLembreteConfirmado,
        confirmarReabertura,
        fecharModalAlterarDataLimite,
        notify
    };
}
