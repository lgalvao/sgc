import {computed, ref, type ComputedRef, type Ref} from "vue";
import type {
    AceitarCadastroRequest,
    Analise,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
} from "@/types/tipos";

type AcaoPrincipalCadastro = {
    codigo: "ACEITAR" | "HOMOLOGAR";
    mensagemSucesso: string;
    redirecionarParaPainel: boolean;
};

type DependenciasCadastroAnaliseFluxo = {
    codigoSubprocesso: Ref<number | null>;
    codProcesso: number | string;
    sigla: string;
    isRevisao: ComputedRef<boolean>;
    acaoPrincipalCadastro: ComputedRef<AcaoPrincipalCadastro | null>;
    mostrarModalHistorico: Ref<boolean>;
    mostrarModalValidarAnalise: Ref<boolean>;
    mostrarModalDevolverAnalise: Ref<boolean>;
    resetarValidacao: () => void;
    validarSubmissao: (valido: boolean) => boolean;
    focarPrimeiroErroInvalido: () => Promise<void>;
    listarAnalisesCadastro: (codigoSubprocesso: number) => Promise<Analise[]>;
    homologarCadastro: (
        codigoSubprocesso: number,
        request: HomologarCadastroRequest,
        revisao: boolean,
        opcoes: { mensagemSucesso: string; redirecionarParaPainel: boolean; redirecionarPara?: { name: string; params: { codProcesso: number | string; siglaUnidade: string } } },
    ) => Promise<boolean>;
    aceitarCadastro: (
        codigoSubprocesso: number,
        request: AceitarCadastroRequest,
        revisao: boolean,
        opcoes: { mensagemSucesso: string },
    ) => Promise<boolean>;
    devolverCadastro: (
        codigoSubprocesso: number,
        request: DevolverCadastroRequest,
        revisao: boolean,
    ) => Promise<boolean>;
};

export function useCadastroAnaliseFluxo({
    codigoSubprocesso,
    codProcesso,
    sigla,
    isRevisao,
    acaoPrincipalCadastro,
    mostrarModalHistorico,
    mostrarModalValidarAnalise,
    mostrarModalDevolverAnalise,
    resetarValidacao,
    validarSubmissao,
    focarPrimeiroErroInvalido,
    listarAnalisesCadastro,
    homologarCadastro,
    aceitarCadastro,
    devolverCadastro,
}: DependenciasCadastroAnaliseFluxo) {
    const analisesCadastro = ref<Analise[]>([]);
    const historicoAnalises = computed(() => analisesCadastro.value);
    const loadingAnaliseCadastro = ref(false);
    const loadingDevolucaoAnalise = ref(false);
    const observacaoValidacao = ref("");
    const observacaoDevolucao = ref("");

    async function abrirModalHistorico() {
        const codigo = codigoSubprocesso.value;
        if (codigo) {
            analisesCadastro.value = await listarAnalisesCadastro(codigo);
        }
        mostrarModalHistorico.value = true;
    }

    function abrirModalValidarAnalise() { mostrarModalValidarAnalise.value = true; }
    function fecharModalValidarAnalise() {
        mostrarModalValidarAnalise.value = false;
        observacaoValidacao.value = "";
    }

    function abrirModalDevolverAnalise() {
        resetarValidacao();
        mostrarModalDevolverAnalise.value = true;
    }

    function fecharModalDevolverAnalise() {
        mostrarModalDevolverAnalise.value = false;
        observacaoDevolucao.value = "";
        resetarValidacao();
    }

    async function confirmarValidacaoAnalise() {
        const codigo = codigoSubprocesso.value;
        const acao = acaoPrincipalCadastro.value;
        if (!codigo || !acao) return;

        loadingAnaliseCadastro.value = true;
        try {
            if (acao.codigo === "HOMOLOGAR") {
                const req: HomologarCadastroRequest = {observacoes: observacaoValidacao.value};
                const redirecionarPara = acao.redirecionarParaPainel
                    ? undefined
                    : {name: "Subprocesso", params: {codProcesso, siglaUnidade: sigla}};
                const sucesso = await homologarCadastro(codigo, req, isRevisao.value, {
                    mensagemSucesso: acao.mensagemSucesso,
                    redirecionarParaPainel: acao.redirecionarParaPainel,
                    redirecionarPara,
                });
                if (sucesso) {
                    fecharModalValidarAnalise();
                }
                return;
            }

            const req: AceitarCadastroRequest = {observacoes: observacaoValidacao.value};
            const sucesso = await aceitarCadastro(codigo, req, isRevisao.value, {
                mensagemSucesso: acao.mensagemSucesso,
            });
            if (sucesso) {
                fecharModalValidarAnalise();
            }
        } finally {
            loadingAnaliseCadastro.value = false;
        }
    }

    async function confirmarDevolucaoAnalise() {
        if (!validarSubmissao(!!observacaoDevolucao.value.trim())) {
            void focarPrimeiroErroInvalido();
            return;
        }

        const codigo = codigoSubprocesso.value;
        if (!codigo) return;

        loadingDevolucaoAnalise.value = true;
        try {
            const req: DevolverCadastroRequest = {observacoes: observacaoDevolucao.value};
            const sucesso = await devolverCadastro(codigo, req, isRevisao.value);
            if (sucesso) {
                fecharModalDevolverAnalise();
            }
        } finally {
            loadingDevolucaoAnalise.value = false;
        }
    }

    return {
        historicoAnalises,
        loadingAnaliseCadastro,
        loadingDevolucaoAnalise,
        observacaoValidacao,
        observacaoDevolucao,
        abrirModalHistorico,
        abrirModalValidarAnalise,
        fecharModalValidarAnalise,
        abrirModalDevolverAnalise,
        fecharModalDevolverAnalise,
        confirmarValidacaoAnalise,
        confirmarDevolucaoAnalise,
    };
}
