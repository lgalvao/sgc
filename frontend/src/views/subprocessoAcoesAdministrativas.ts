import {computed, type ComputedRef, reactive, ref, type Ref, toRefs} from "vue";
import type {VarianteAlerta} from "@/composables/useNotification";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {TEXTOS} from "@/constants/textos";
import type {SubprocessoDetalhe} from "@/types/tipos";
import {formatarDataBR} from "@/utils";
import logger from "@/utils/logger";
import {useToastStore} from "@/stores/toast";
import {useSubprocessoStore} from "@/stores/subprocesso";

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
};

function criarEstado() {
    const modais = reactive({
        modalLembreteAberto: false,
        mostrarModalAlterarDataLimite: false,
        mostrarModalReabrir: false,
    });

    return {
        ...toRefs(modais),
        tipoReabertura: ref<TipoReabertura>("cadastro"),
        justificativaReabertura: ref(""),
        loadingDataLimite: ref(false),
        loadingReabertura: ref(false),
        loadingLembrete: ref(false),
    };
}

function abrirReabertura(estado: ReturnType<typeof criarEstado>, resetarValidacao: () => void, tipo: TipoReabertura) {
    resetarValidacao();
    estado.tipoReabertura.value = tipo;
    estado.justificativaReabertura.value = "";
    estado.mostrarModalReabrir.value = true;
}

export function useSubprocessoAcoesAdministrativas(dependencias: DependenciasSubprocessoAcoesAdministrativas) {
    const {atualizarFluxoSubprocessoEPainel} = useInvalidacaoNavegacao();
    const toastStore = useToastStore();
    const subprocessoStore = useSubprocessoStore();
    const estado = criarEstado();

    async function confirmarAlteracaoDataLimite(novaData: string) {
        const detalhe = dependencias.subprocesso.value;
        if (!novaData || !detalhe) {
            return;
        }

        estado.loadingDataLimite.value = true;
        try {
            await dependencias.alterarDataLimiteSubprocesso(detalhe.codigo, {novaData});
            estado.mostrarModalAlterarDataLimite.value = false;
            toastStore.setPending(`${TEXTOS.subprocesso.SUCESSO_DATA_ALTERADA} para ${formatarDataBR(novaData)}.`);
            atualizarFluxoSubprocessoEPainel();
            await dependencias.atualizarSubprocessoAtual();
        } catch (error) {
            logger.error(TEXTOS.subprocesso.ERRO_DATA_ALTERADA, error);
            dependencias.notify(TEXTOS.subprocesso.ERRO_DATA_ALTERADA, "danger");
        } finally {
            estado.loadingDataLimite.value = false;
        }
    }

    async function confirmarReabertura() {
        const justificativa = estado.justificativaReabertura.value.trim();
        if (!dependencias.validarSubmissao(Boolean(justificativa))) {
            void dependencias.focarPrimeiroErroInvalido();
            return;
        }

        estado.loadingReabertura.value = true;
        try {
            const reabrir = estado.tipoReabertura.value === "revisao"
                ? dependencias.reabrirRevisaoCadastro
                : dependencias.reabrirCadastro;
            const sucesso = await reabrir(dependencias.codigoSubprocesso.value!, justificativa);
            if (!sucesso) {
                return;
            }

            estado.mostrarModalReabrir.value = false;
            estado.justificativaReabertura.value = "";
            dependencias.exibirToastPendente();
        } finally {
            estado.loadingReabertura.value = false;
        }
    }

    async function enviarLembreteConfirmado() {
        const detalhe = dependencias.subprocesso.value;
        if (!detalhe || estado.loadingLembrete.value) {
            return;
        }

        estado.loadingLembrete.value = true;
        try {
            await dependencias.enviarLembrete(dependencias.codProcesso, detalhe.unidade.codigo);
            await subprocessoStore.obterContextoEdicao(dependencias.codigoSubprocesso.value!, {forcar: true});
            estado.modalLembreteAberto.value = false;
            toastStore.setPending(TEXTOS.subprocesso.SUCESSO_LEMBRETE_ENVIADO);
            dependencias.exibirToastPendente();
        } catch (error) {
            logger.error(TEXTOS.subprocesso.ERRO_LEMBRETE_ENVIADO, error);
            dependencias.notify(TEXTOS.subprocesso.ERRO_LEMBRETE_ENVIADO, "danger");
        } finally {
            estado.loadingLembrete.value = false;
        }
    }

    return {
        tipoReabertura: estado.tipoReabertura,
        justificativaReabertura: estado.justificativaReabertura,
        modalLembreteAberto: estado.modalLembreteAberto,
        mostrarModalAlterarDataLimite: estado.mostrarModalAlterarDataLimite,
        mostrarModalReabrir: estado.mostrarModalReabrir,
        loadingDataLimite: estado.loadingDataLimite,
        loadingReabertura: estado.loadingReabertura,
        loadingLembrete: estado.loadingLembrete,
        mensagemErroJustificativa: computed(() =>
            dependencias.deveExibirErro(!estado.justificativaReabertura.value.trim())
                ? "Informe a justificativa para reabrir."
                : ""
        ),
        abrirModalAlterarDataLimite: () => {
            if (!dependencias.habilitarAlterarDataLimite.value) {
                dependencias.notify(TEXTOS.subprocesso.ERRO_SEM_PERMISSAO_DATA, "danger");
                return;
            }
            estado.mostrarModalAlterarDataLimite.value = true;
        },
        fecharModalAlterarDataLimite: () => {
            estado.mostrarModalAlterarDataLimite.value = false;
        },
        confirmarAlteracaoDataLimite,
        abrirModalReabrirCadastro: () => abrirReabertura(estado, dependencias.resetarValidacao, "cadastro"),
        abrirModalReabrirRevisao: () => abrirReabertura(estado, dependencias.resetarValidacao, "revisao"),
        fecharModalReabrir: () => {
            estado.mostrarModalReabrir.value = false;
            estado.justificativaReabertura.value = "";
        },
        confirmarReabertura,
        confirmarEnviarLembrete: () => {
            if (dependencias.subprocesso.value) {
                estado.modalLembreteAberto.value = true;
            }
        },
        enviarLembreteConfirmado,
    };
}
