import {ref, type Ref} from "vue";
import {apresentarSugestoes, obterSugestoesMapa} from "@/services/subprocessoService";
import logger from "@/utils/logger";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_MAPA} from "@/constants/textos-mapa";
import type {VarianteAlerta} from "@/composables/useNotification";

interface UseEnviarSugestoesOptions {
    obterCodigoSubprocessoObrigatorio: () => number;
    notify: (message: string, variant?: VarianteAlerta) => void;
    concluirAcaoPainel: (mensagem: string, fecharModal: () => void) => Promise<void>;
    validarSubmissao: (val: boolean) => boolean;
    focarPrimeiroErroInvalido: () => Promise<void>;
    resetarValidacao: () => void;
}

function limparTexto(destino: Ref<string>) {
    destino.value = "";
}

export function useEnviarSugestoes(options: UseEnviarSugestoesOptions) {
    const {notify, concluirAcaoPainel, validarSubmissao, focarPrimeiroErroInvalido, resetarValidacao} = options;
    const sugestoes = ref("");
    const loadingSugestoesEnvio = ref(false);
    const mostrarModalSugestoes = ref(false);

    async function executarOperacaoSugestoes(operacao: () => Promise<void>, mensagemErro: string): Promise<boolean> {
        try {
            await operacao();
            return true;
        } catch (error) {
            logger.error(error);
            notify(mensagemErro, "danger");
            return false;
        }
    }

    async function buscarSugestoesMapa(): Promise<string> {
        return obterSugestoesMapa(options.obterCodigoSubprocessoObrigatorio());
    }

    async function preencherSugestoes(): Promise<boolean> {
        const sucesso = await executarOperacaoSugestoes(async () => {
            sugestoes.value = await buscarSugestoesMapa();
        }, TEXTOS.mapa.ERRO_SUGESTOES);
        if (!sucesso) {
            limparTexto(sugestoes);
        }
        return sucesso;
    }

    function fecharModalSugestoes() {
        mostrarModalSugestoes.value = false;
        limparTexto(sugestoes);
        resetarValidacao();
    }

    function abrirModalSugestoes() {
        resetarValidacao();
        mostrarModalSugestoes.value = true;
        void preencherSugestoes();
    }

    async function confirmarSugestoes() {
        if (!validarSubmissao(!!sugestoes.value.trim())) {
            await focarPrimeiroErroInvalido();
            return;
        }
        try {
            loadingSugestoesEnvio.value = true;
            const sucesso = await executarOperacaoSugestoes(async () => {
                await apresentarSugestoes(options.obterCodigoSubprocessoObrigatorio(), {sugestoes: sugestoes.value});
                await concluirAcaoPainel(TEXTOS_SUCESSO_MAPA.MAPA_SUBMETIDO_COM_SUGESTOES, fecharModalSugestoes);
            }, TEXTOS.mapa.ERRO_SUGESTOES);
            if (!sucesso) return;
        } finally {
            loadingSugestoesEnvio.value = false;
        }
    }

    return {
        sugestoes,
        loadingSugestoesEnvio,
        mostrarModalSugestoes,
        abrirModalSugestoes,
        fecharModalSugestoes,
        confirmarSugestoes,
    };
}
