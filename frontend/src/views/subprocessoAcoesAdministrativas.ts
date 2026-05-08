import {computed, type ComputedRef, reactive, ref, type Ref, toRefs} from "vue";
import type {VarianteAlerta} from "@/composables/useNotification";
import {TEXTOS} from "@/constants/textos";
import type {SubprocessoDetalhe} from "@/types/tipos";

type TipoReabertura = "cadastro" | "revisao";

type DependenciasSubprocessoAcoesAdministrativas = {
    subprocesso: ComputedRef<SubprocessoDetalhe | null>;
    codigoSubprocesso: Ref<number | null>;
    codProcesso: number;
    habilitarAlterarDataLimite: ComputedRef<boolean>;
    deveExibirErro: (condicao: boolean) => boolean;
    resetarValidacao: () => void;
    validarSubmissao: (valido: boolean) => boolean;
    focarPrimeiroErroInvalido: () => Promise<void>;
    notify: (mensagem: string, variante: VarianteAlerta) => void;
    atualizarSubprocessoAtual: () => Promise<void>;
    exibirToastPendente: () => void;
    alterarDataLimiteSubprocesso: (codigoSubprocesso: number, payload: { novaData: string }) => Promise<unknown>;
    reabrirCadastro: (codigoSubprocesso: number, justificativa: string) => Promise<boolean>;
    reabrirRevisaoCadastro: (codigoSubprocesso: number, justificativa: string) => Promise<boolean>;
    enviarLembrete: (codProcesso: number, unidadeCodigo: number) => Promise<unknown>;
    garantirContextoEdicao: (codigoSubprocesso: number, limpar: boolean) => Promise<unknown>;
};

export function useSubprocessoAcoesAdministrativas({
                                                       subprocesso,
                                                       codigoSubprocesso,
                                                       codProcesso,
                                                       habilitarAlterarDataLimite,
                                                       deveExibirErro,
                                                       resetarValidacao,
                                                       validarSubmissao,
                                                       focarPrimeiroErroInvalido,
                                                       notify,
                                                       atualizarSubprocessoAtual,
                                                       exibirToastPendente,
                                                       alterarDataLimiteSubprocesso,
                                                       reabrirCadastro,
                                                       reabrirRevisaoCadastro,
                                                       enviarLembrete,
                                                       garantirContextoEdicao,
                                                   }: DependenciasSubprocessoAcoesAdministrativas) {
    const estadoModais = reactive({
        modalLembreteAberto: false,
        mostrarModalAlterarDataLimite: false,
        mostrarModalReabrir: false,
    });
    const {modalLembreteAberto, mostrarModalAlterarDataLimite, mostrarModalReabrir} = toRefs(estadoModais);

    const tipoReabertura = ref<TipoReabertura>("cadastro");
    const justificativaReabertura = ref("");
    const loadingDataLimite = ref(false);
    const loadingReabertura = ref(false);
    const loadingLembrete = ref(false);
    const mensagemErroJustificativa = computed(() =>
        deveExibirErro(!justificativaReabertura.value.trim()) ? "Informe a justificativa para reabrir." : "",
    );

    async function executarComCarregamento(loading: Ref<boolean>, acao: () => Promise<void>) {
        loading.value = true;
        try {
            await acao();
        } finally {
            loading.value = false;
        }
    }

    async function executarComErroNotificado(
        loading: Ref<boolean>,
        mensagemErro: string,
        acao: () => Promise<void>
    ) {
        await executarComCarregamento(loading, async () => {
            try {
                await acao();
            } catch {
                notify(mensagemErro, "danger");
            }
        });
    }

    function abrirModalAlterarDataLimite() {
        if (habilitarAlterarDataLimite.value) {
            mostrarModalAlterarDataLimite.value = true;
            return;
        }
        notify(TEXTOS.subprocesso.ERRO_SEM_PERMISSAO_DATA, "danger");
    }

    function fecharModalAlterarDataLimite() {
        mostrarModalAlterarDataLimite.value = false;
    }

    async function confirmarAlteracaoDataLimite(novaData: string) {
        const detalhe = subprocesso.value;
        if (!novaData || !detalhe) return;

        await executarComErroNotificado(loadingDataLimite, TEXTOS.subprocesso.ERRO_DATA_ALTERADA, async () => {
            await alterarDataLimiteSubprocesso(detalhe.codigo, {novaData});
            fecharModalAlterarDataLimite();
            notify(TEXTOS.subprocesso.SUCESSO_DATA_ALTERADA, "success");
            await atualizarSubprocessoAtual();
        });
    }

    function abrirModalReabrirCadastro() {
        resetarValidacao();
        tipoReabertura.value = "cadastro";
        justificativaReabertura.value = "";
        mostrarModalReabrir.value = true;
    }

    function abrirModalReabrirRevisao() {
        resetarValidacao();
        tipoReabertura.value = "revisao";
        justificativaReabertura.value = "";
        mostrarModalReabrir.value = true;
    }

    function fecharModalReabrir() {
        mostrarModalReabrir.value = false;
        justificativaReabertura.value = "";
    }

    async function confirmarReabertura() {
        if (!validarSubmissao(!!justificativaReabertura.value.trim())) {
            void focarPrimeiroErroInvalido();
            return;
        }

        const codigo = codigoSubprocesso.value;
        if (!codigo) return;

        await executarComCarregamento(loadingReabertura, async () => {
            const fn = tipoReabertura.value === "revisao" ? reabrirRevisaoCadastro : reabrirCadastro;
            const sucesso = await fn(codigo, justificativaReabertura.value);
            if (sucesso) {
                fecharModalReabrir();
                exibirToastPendente();
            }
        });
    }

    function confirmarEnviarLembrete() {
        if (!subprocesso.value) return;
        modalLembreteAberto.value = true;
    }

    async function enviarLembreteConfirmado() {
        const detalhe = subprocesso.value;
        const codigo = codigoSubprocesso.value;
        if (!detalhe || !codigo || loadingLembrete.value) return;

        await executarComErroNotificado(loadingLembrete, TEXTOS.subprocesso.ERRO_LEMBRETE_ENVIADO, async () => {
            await enviarLembrete(codProcesso, detalhe.unidade.codigo);
            await garantirContextoEdicao(codigo, true);
            modalLembreteAberto.value = false;
            notify(TEXTOS.subprocesso.SUCESSO_LEMBRETE_ENVIADO, "success");
        });
    }

    return {
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
        fecharModalReabrir,
        confirmarReabertura,
        confirmarEnviarLembrete,
        enviarLembreteConfirmado,
    };
}
