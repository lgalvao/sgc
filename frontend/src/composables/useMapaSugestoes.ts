import {ref, type Ref} from "vue";
import {apresentarSugestoes, obterSugestoesMapa} from "@/services/subprocessoService";
import logger from "@/utils/logger";
import {TEXTOS} from "@/constants/textos";
import type {VarianteAlerta} from "@/composables/useNotification";

interface UseMapaSugestoesOptions {
    codigoSubprocesso: Ref<number | null>;
    notify: (message: string, variant?: VarianteAlerta) => void;
    concluirAcaoPainel: (mensagem: string, fecharModal: () => void) => Promise<void>;
    validarSubmissao: (val: boolean) => boolean;
    focarPrimeiroErroInvalido: () => Promise<void>;
    resetarValidacao: () => void;
}

export function useMapaSugestoes(options: UseMapaSugestoesOptions) {
    const {
        codigoSubprocesso,
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

    async function sincronizarSugestoesMapa(): Promise<string> {
        if (!codigoSubprocesso.value) {
            return "";
        }
        return await obterSugestoesMapa(codigoSubprocesso.value);
    }

    async function verSugestoes() {
        if (loadingSugestoesVisualizacao.value) {
            return;
        }

        sugestoesVisualizacao.value = "";
        loadingSugestoesVisualizacao.value = true;

        try {
            sugestoesVisualizacao.value = await sincronizarSugestoesMapa();
            mostrarModalVerSugestoes.value = true;
        } catch (error) {
            logger.error(error);
            notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
        } finally {
            loadingSugestoesVisualizacao.value = false;
        }
    }

    function fecharModalVerSugestoes() {
        mostrarModalVerSugestoes.value = false;
        sugestoesVisualizacao.value = "";
    }

    async function carregarSugestoesParaEdicao() {
        try {
            sugestoes.value = await sincronizarSugestoesMapa();
        } catch (error) {
            logger.error(error);
            notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
        }
    }

    function abrirModalSugestoes() {
        resetarValidacao();
        mostrarModalSugestoes.value = true;
        void carregarSugestoesParaEdicao();
    }

    function fecharModalSugestoes() {
        mostrarModalSugestoes.value = false;
        sugestoes.value = "";
        resetarValidacao();
    }

    async function handleConfirmarSugestoes() {
        if (!validarSubmissao(!!sugestoes.value.trim())) {
            await focarPrimeiroErroInvalido();
            return;
        }

        if (!codigoSubprocesso.value) return;

        try {
            loadingSugestoesEnvio.value = true;
            await apresentarSugestoes(codigoSubprocesso.value, {sugestoes: sugestoes.value});
            await concluirAcaoPainel(TEXTOS.sucesso.MAPA_SUBMETIDO_COM_SUGESTOES, fecharModalSugestoes);
        } catch (error) {
            logger.error(error);
            notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
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
        handleConfirmarSugestoes,
        sincronizarSugestoesMapa,
        carregarSugestoesParaEdicao
    };
}
