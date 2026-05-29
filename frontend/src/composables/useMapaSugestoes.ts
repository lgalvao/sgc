// @sgc-auditoria ignorar: superficieAmpla | Dois modais relacionados (visualizar + enviar sugestões); contrato coeso consumido integralmente por useMapaTela
import {ref, type Ref} from "vue";
import logger from "@/utils/logger";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_MAPA} from "@/constants/textos-mapa";
import type {VarianteAlerta} from "@/composables/useNotification";

interface UseMapaSugestoesOptions {
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

export function useMapaSugestoes(options: UseMapaSugestoesOptions) {
    const {
        notify,
        concluirAcaoPainel,
        validarSubmissao,
        focarPrimeiroErroInvalido,
        resetarValidacao
    } = options;

    const sugestoes = ref("");
    const sugestoesVisualizacao = ref("");
    const loadingSugestoesVisualizacao = ref(false);
    const loadingSugestoesEnvio = ref(false);
    const mostrarModalSugestoes = ref(false);
    const mostrarModalVerSugestoes = ref(false);

    async function executarOperacaoSugestoes(
        operacao: () => Promise<void>,
        mensagemErro: string
    ): Promise<boolean> {
        try {
            await operacao();
            return true;
        } catch (error) {
            logger.error(error);
            notify(mensagemErro, 'danger');
            return false;
        }
    }

    async function buscarSugestoesMapa(): Promise<string> {
        return obterSugestoesMapa(options.obterCodigoSubprocessoObrigatorio());
    }

    async function preencherSugestoes(destino: Ref<string>) {
        const sucesso = await executarOperacaoSugestoes(async () => {
            destino.value = await buscarSugestoesMapa();
        }, TEXTOS.mapa.ERRO_SUGESTOES);

        if (!sucesso) {
            limparTexto(destino);
        }

        return sucesso;
    }

    async function verSugestoes() {
        if (loadingSugestoesVisualizacao.value) {
            return;
        }

        limparTexto(sugestoesVisualizacao);
        loadingSugestoesVisualizacao.value = true;

        try {
            const sucesso = await preencherSugestoes(sugestoesVisualizacao);
            if (!sucesso) {
                return;
            }
            mostrarModalVerSugestoes.value = true;
        } finally {
            loadingSugestoesVisualizacao.value = false;
        }
    }

    function fecharModalVerSugestoes() {
        mostrarModalVerSugestoes.value = false;
        limparTexto(sugestoesVisualizacao);
    }

    function abrirModalSugestoes() {
        resetarValidacao();
        mostrarModalSugestoes.value = true;
        void preencherSugestoes(sugestoes);
    }

    function fecharModalSugestoes() {
        mostrarModalSugestoes.value = false;
        limparTexto(sugestoes);
        resetarValidacao();
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
            if (!sucesso) {
                return;
            }
        } finally {
            loadingSugestoesEnvio.value = false;
        }
    }

    return {
        sugestoes,
        sugestoesVisualizacao,
        loadingSugestoesVisualizacao,
        loadingSugestoesEnvio,
        mostrarModalSugestoes,
        mostrarModalVerSugestoes,
        verSugestoes,
        fecharModalVerSugestoes,
        abrirModalSugestoes,
        fecharModalSugestoes,
        confirmarSugestoes,
    };
}
