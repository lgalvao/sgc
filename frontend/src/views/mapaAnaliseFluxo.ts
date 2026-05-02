import type {ComputedRef, Ref} from "vue";
import type {Analise} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import type {VarianteAlerta} from "@/composables/useNotification";

type AcaoPrincipalMapa = {
    codigo: "ACEITAR" | "HOMOLOGAR";
    mensagemSucesso: string;
};

type DependenciasMapaAnaliseFluxo = {
    codigoSubprocesso: Ref<number | null>;
    acaoPrincipalMapa: ComputedRef<AcaoPrincipalMapa | null>;
    mostrarModalAceitar: Ref<boolean>;
    mostrarModalValidar: Ref<boolean>;
    mostrarModalDevolucao: Ref<boolean>;
    mostrarModalHistorico: Ref<boolean>;
    observacaoDevolucao: Ref<string>;
    analisesCadastro: Ref<Analise[]>;
    resetarValidacao: () => void;
    validarSubmissao: (valido: boolean) => boolean;
    focarPrimeiroErroInvalido: () => Promise<void>;
    concluirAcaoPainel: (mensagem: string, fecharModal: () => void) => Promise<void>;
    notify: (mensagem: string, variante: VarianteAlerta) => void;
    listarAnalisesCadastro: (codigoSubprocesso: number) => Promise<Analise[]>;
    validarMapa: (codigoSubprocesso: number) => Promise<void>;
    homologarMapa: (codigoSubprocesso: number, dados: {observacao: string}) => Promise<void>;
    aceitarMapa: (codigoSubprocesso: number, dados: {observacao: string}) => Promise<void>;
    devolverMapa: (codigoSubprocesso: number, dados: {justificativa: string}) => Promise<void>;
};

export function useMapaAnaliseFluxo({
    codigoSubprocesso,
    acaoPrincipalMapa,
    mostrarModalAceitar,
    mostrarModalValidar,
    mostrarModalDevolucao,
    mostrarModalHistorico,
    observacaoDevolucao,
    analisesCadastro,
    resetarValidacao,
    validarSubmissao,
    focarPrimeiroErroInvalido,
    concluirAcaoPainel,
    notify,
    listarAnalisesCadastro,
    validarMapa,
    homologarMapa,
    aceitarMapa,
    devolverMapa,
}: DependenciasMapaAnaliseFluxo) {
    function abrirModalAceitar() { mostrarModalAceitar.value = true; }
    function fecharModalAceitar() { mostrarModalAceitar.value = false; }
    function abrirModalValidar() { mostrarModalValidar.value = true; }
    function fecharModalValidar() { mostrarModalValidar.value = false; }
    function abrirModalDevolucao() {
        resetarValidacao();
        mostrarModalDevolucao.value = true;
    }
    function fecharModalDevolucao() {
        mostrarModalDevolucao.value = false;
        observacaoDevolucao.value = "";
    }

    async function confirmarValidacao() {
        const codigo = codigoSubprocesso.value;
        if (!codigo) return;
        try {
            await validarMapa(codigo);
            await concluirAcaoPainel(TEXTOS.sucesso.MAPA_VALIDADO_SUBMETIDO, fecharModalValidar);
        } catch {
            notify(TEXTOS.mapa.ERRO_VALIDAR, "danger");
        }
    }

    async function confirmarAceitacao(observacao = "") {
        const codigo = codigoSubprocesso.value;
        const acao = acaoPrincipalMapa.value;
        if (!codigo || !acao) return;
        try {
            if (acao.codigo === "HOMOLOGAR") {
                await homologarMapa(codigo, {observacao});
            } else {
                await aceitarMapa(codigo, {observacao});
            }
            await concluirAcaoPainel(acao.mensagemSucesso, fecharModalAceitar);
        } catch {
            notify(TEXTOS.comum.ERRO_OPERACAO, "danger");
        }
    }

    async function handleConfirmarDevolucao() {
        if (!validarSubmissao(!!observacaoDevolucao.value.trim())) {
            await focarPrimeiroErroInvalido();
            return;
        }
        const codigo = codigoSubprocesso.value;
        if (!codigo) return;
        try {
            await devolverMapa(codigo, {justificativa: observacaoDevolucao.value});
            await concluirAcaoPainel(TEXTOS.sucesso.DEVOLUCAO_REALIZADA, fecharModalDevolucao);
        } catch {
            notify(TEXTOS.mapa.ERRO_DEVOLVER, "danger");
        }
    }

    async function abrirModalHistorico() {
        const codigo = codigoSubprocesso.value;
        if (codigo) {
            analisesCadastro.value = await listarAnalisesCadastro(codigo);
        }
        mostrarModalHistorico.value = true;
    }

    function fecharModalHistorico() { mostrarModalHistorico.value = false; }
    function verHistorico() { void abrirModalHistorico(); }

    return {
        abrirModalAceitar,
        fecharModalAceitar,
        abrirModalValidar,
        fecharModalValidar,
        abrirModalDevolucao,
        fecharModalDevolucao,
        confirmarValidacao,
        confirmarAceitacao,
        handleConfirmarDevolucao,
        fecharModalHistorico,
        verHistorico,
    };
}
