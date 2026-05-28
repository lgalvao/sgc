import {ref, type Ref} from "vue";
import {obterSugestoesMapa} from "@/services/subprocessoService";
import logger from "@/utils/logger";
import {TEXTOS} from "@/constants/textos";
import type {VarianteAlerta} from "@/composables/useNotification";

interface UseVerSugestoesOptions {
    obterCodigoSubprocessoObrigatorio: () => number;
    notify: (message: string, variant?: VarianteAlerta) => void;
}

function limparTexto(destino: Ref<string>) {
    destino.value = "";
}

export function useVerSugestoes(options: UseVerSugestoesOptions) {
    const {notify} = options;
    const sugestoesVisualizacao = ref("");
    const loadingSugestoesVisualizacao = ref(false);
    const mostrarModalVerSugestoes = ref(false);

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
            sugestoesVisualizacao.value = await buscarSugestoesMapa();
        }, TEXTOS.mapa.ERRO_SUGESTOES);
        if (!sucesso) {
            limparTexto(sugestoesVisualizacao);
        }
        return sucesso;
    }

    async function verSugestoes() {
        if (loadingSugestoesVisualizacao.value) return;
        limparTexto(sugestoesVisualizacao);
        loadingSugestoesVisualizacao.value = true;
        try {
            const sucesso = await preencherSugestoes();
            if (sucesso) mostrarModalVerSugestoes.value = true;
        } finally {
            loadingSugestoesVisualizacao.value = false;
        }
    }

    function fecharModalVerSugestoes() {
        mostrarModalVerSugestoes.value = false;
        limparTexto(sugestoesVisualizacao);
    }

    return {
        sugestoesVisualizacao,
        loadingSugestoesVisualizacao,
        mostrarModalVerSugestoes,
        verSugestoes,
        fecharModalVerSugestoes,
    };
}
