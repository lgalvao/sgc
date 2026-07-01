import type {ComputedRef, Ref} from "vue";
import type {Analise} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_MAPA} from "@/constants/textos-mapa";
import {TEXTOS_SUCESSO_SUBPROCESSO} from "@/constants/textos-subprocesso";
import type {VarianteAlerta} from "@/composables/useNotification";
import logger from "@/utils/logger";
import {useAsyncAction} from "@/composables/useAsyncAction";

type AcaoPrincipalMapa = {
    codigo: "ACEITAR" | "HOMOLOGAR";
    mensagemSucesso: string;
};

type DependenciasMapaAnaliseFluxo = {
    obterCodigoSubprocessoObrigatorio: () => number;
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
    homologarMapa: (codigoSubprocesso: number, dados: { observacao: string }) => Promise<void>;
    aceitarMapa: (codigoSubprocesso: number, dados: { observacao: string }) => Promise<void>;
    devolverMapa: (codigoSubprocesso: number, dados: { justificativa: string }) => Promise<void>;
};

export function useMapaAnaliseFluxo({
                                        obterCodigoSubprocessoObrigatorio,
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
    const acaoFluxo = useAsyncAction();

    async function executarComNotificacaoDeErro(
        mensagemErro: string,
        acao: () => Promise<void>
    ) {
        await acaoFluxo.executar(
            acao,
            mensagemErro,
            {
                relancarErro: false,
                aoOcorrerErro: (_erro, causa) => {
                    logger.error(mensagemErro, causa);
                    notify(mensagemErro, "danger");
                },
            },
        );
    }

    function abrirModalAceitar() {
        mostrarModalAceitar.value = true;
    }

    function fecharModalAceitar() {
        mostrarModalAceitar.value = false;
    }

    function abrirModalValidar() {
        mostrarModalValidar.value = true;
    }

    function fecharModalValidar() {
        mostrarModalValidar.value = false;
    }

    function abrirModalDevolucao() {
        resetarValidacao();
        mostrarModalDevolucao.value = true;
    }

    function fecharModalDevolucao() {
        mostrarModalDevolucao.value = false;
        observacaoDevolucao.value = "";
    }

    async function confirmarValidacao() {
        await executarComNotificacaoDeErro(TEXTOS.mapa.ERRO_VALIDAR, async () => {
            await validarMapa(obterCodigoSubprocessoObrigatorio());
            await concluirAcaoPainel(TEXTOS_SUCESSO_MAPA.MAPA_VALIDADO_SUBMETIDO, fecharModalValidar);
        });
    }

    async function confirmarAceitacao(observacao = "") {
        const acao = acaoPrincipalMapa.value;
        if (!acao) return;
        const codigo = obterCodigoSubprocessoObrigatorio();
        await executarComNotificacaoDeErro(TEXTOS.comum.ERRO_OPERACAO, async () => {
            if (acao.codigo === "HOMOLOGAR") {
                await homologarMapa(codigo, {observacao});
            } else {
                await aceitarMapa(codigo, {observacao});
            }
            await concluirAcaoPainel(acao.mensagemSucesso, fecharModalAceitar);
        });
    }

    async function confirmarDevolucao() {
        if (!validarSubmissao(!!observacaoDevolucao.value.trim())) {
            await focarPrimeiroErroInvalido();
            return;
        }
        await executarComNotificacaoDeErro(TEXTOS.mapa.ERRO_DEVOLVER, async () => {
            await devolverMapa(obterCodigoSubprocessoObrigatorio(), {justificativa: observacaoDevolucao.value});
            await concluirAcaoPainel(TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA, fecharModalDevolucao);
        });
    }

    async function abrirModalHistorico() {
        analisesCadastro.value = await listarAnalisesCadastro(obterCodigoSubprocessoObrigatorio());
        mostrarModalHistorico.value = true;
    }

    function fecharModalHistorico() {
        mostrarModalHistorico.value = false;
    }

    function verHistorico() {
        void abrirModalHistorico();
    }

    return {
        abrirModalAceitar,
        fecharModalAceitar,
        abrirModalValidar,
        fecharModalValidar,
        abrirModalDevolucao,
        fecharModalDevolucao,
        confirmarValidacao,
        confirmarAceitacao,
        confirmarDevolucao,
        fecharModalHistorico,
        verHistorico,
    };
}
