import {computed, reactive, ref, toRefs, type ComputedRef, type Ref} from "vue";
import {TEXTOS} from "@/constants/textos";
import type {SubprocessoDetalhe} from "@/types/tipos";

type TipoReabertura = "cadastro" | "revisao";

type DependenciasSubprocessoAcoesAdministrativas = {
    subprocesso: ComputedRef<SubprocessoDetalhe | null>;
    codigoSubprocesso: Ref<number | null>;
    codProcesso: number;
    podeAlterarDataLimite: ComputedRef<boolean>;
    deveExibirErro: (condicao: boolean) => boolean;
    resetarValidacao: () => void;
    validarSubmissao: (valido: boolean) => boolean;
    focarPrimeiroErroInvalido: () => Promise<void>;
    notify: (mensagem: string, variante: string) => void;
    atualizarSubprocessoAtual: () => Promise<void>;
    exibirToastPendente: () => void;
    alterarDataLimiteSubprocesso: (codigoSubprocesso: number, payload: { novaData: string }) => Promise<unknown>;
    reabrirCadastro: (codigoSubprocesso: number, justificativa: string, isRevisao: boolean) => Promise<boolean>;
    enviarLembrete: (codProcesso: number, unidadeCodigo: number) => Promise<unknown>;
    garantirContextoEdicao: (codigoSubprocesso: number, limpar: boolean) => Promise<unknown>;
};

export function useSubprocessoAcoesAdministrativas({
    subprocesso,
    codigoSubprocesso,
    codProcesso,
    podeAlterarDataLimite,
    deveExibirErro,
    resetarValidacao,
    validarSubmissao,
    focarPrimeiroErroInvalido,
    notify,
    atualizarSubprocessoAtual,
    exibirToastPendente,
    alterarDataLimiteSubprocesso,
    reabrirCadastro,
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

    function abrirModalAlterarDataLimite() {
        if (podeAlterarDataLimite.value) {
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

        await executarComCarregamento(loadingDataLimite, async () => {
            try {
                await alterarDataLimiteSubprocesso(detalhe.codigo, {novaData});
                fecharModalAlterarDataLimite();
                notify(TEXTOS.subprocesso.SUCESSO_DATA_ALTERADA, "success");
                await atualizarSubprocessoAtual();
            } catch {
                notify(TEXTOS.subprocesso.ERRO_DATA_ALTERADA, "danger");
            }
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
        const codigo = codigoSubprocesso.value;
        if (!codigo) return;

        if (!validarSubmissao(Boolean(justificativaReabertura.value.trim()))) {
            void focarPrimeiroErroInvalido();
            return;
        }

        await executarComCarregamento(loadingReabertura, async () => {
            const sucesso = await reabrirCadastro(codigo, justificativaReabertura.value, tipoReabertura.value === "revisao");
            if (!sucesso) return;

            fecharModalReabrir();
            exibirToastPendente();
            await atualizarSubprocessoAtual();
        });
    }

    async function confirmarEnviarLembrete() {
        if (!subprocesso.value) return;
        modalLembreteAberto.value = true;
        return true;
    }

    async function enviarLembreteConfirmado() {
        const detalhe = subprocesso.value;
        const codigo = codigoSubprocesso.value;
        if (!detalhe || !codigo || loadingLembrete.value) return;

        loadingLembrete.value = true;
        try {
            await enviarLembrete(codProcesso, detalhe.unidade.codigo);
            await garantirContextoEdicao(codigo, true);
            modalLembreteAberto.value = false;
            notify(TEXTOS.subprocesso.SUCESSO_LEMBRETE_ENVIADO, "success");
        } catch {
            notify(TEXTOS.subprocesso.ERRO_LEMBRETE_ENVIADO, "danger");
        } finally {
            loadingLembrete.value = false;
        }
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
