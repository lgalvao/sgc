import {type ComputedRef, onActivated, onMounted, ref, watch} from "vue";

type ContextoEdicaoDireto = { detalhes: { codigo: number } } | null;
type ContextoEdicaoPorProcesso = { codigo: number } | null;

type DependenciasSubprocessoCarregamento = {
    codProcesso: number;
    siglaUnidade: string;
    codSubprocesso?: number;
    erroIntegracaoContexto: ComputedRef<unknown>;
    garantirContextoEdicao: (codigoSubprocesso: number, limpar: boolean) => Promise<ContextoEdicaoDireto>;
    garantirContextoEdicaoPorProcessoEUnidade: (
        codProcesso: number,
        siglaUnidade: string,
        limpar: boolean,
    ) => Promise<ContextoEdicaoPorProcesso>;
    dadosEdicaoValidos: (codigoSubprocesso: number) => boolean;
    exibirToastPendente: () => void;
};

export function useSubprocessoCarregamento({
                                               codProcesso,
                                               siglaUnidade,
                                               codSubprocesso,
                                                erroIntegracaoContexto,
                                                garantirContextoEdicao,
                                                garantirContextoEdicaoPorProcessoEUnidade,
                                                dadosEdicaoValidos,
                                                exibirToastPendente,
                                            }: DependenciasSubprocessoCarregamento) {
    const codigoSubprocesso = ref<number | null>(null);
    const erroNaoEncontrado = ref(false);
    const carregamentoInicialConcluido = ref(false);
    let carregamentoEmAndamento: Promise<void> | null = null;

    async function carregarSubprocesso(limpar = false) {
        if (!limpar && carregamentoEmAndamento) {
            await carregamentoEmAndamento;
            return;
        }
        const tarefaCarregamento = (async () => {
        const resultadoDireto = typeof codSubprocesso === "number"
            ? await garantirContextoEdicao(codSubprocesso, limpar)
            : null;

        if (resultadoDireto) {
            codigoSubprocesso.value = resultadoDireto.detalhes.codigo;
            erroNaoEncontrado.value = false;
            return;
        }

        const resultado = await garantirContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade, limpar);
        if (!resultado) {
            codigoSubprocesso.value = null;
            erroNaoEncontrado.value = !erroIntegracaoContexto.value;
            return;
        }

        codigoSubprocesso.value = resultado.codigo;
        erroNaoEncontrado.value = false;
        })();

        carregamentoEmAndamento = tarefaCarregamento;
        try {
            await tarefaCarregamento;
        } finally {
            if (carregamentoEmAndamento === tarefaCarregamento) {
                carregamentoEmAndamento = null;
            }
        }
    }

    async function atualizarSubprocessoAtual() {
        const codigo = codigoSubprocesso.value;
        if (!codigo) return;

        await garantirContextoEdicao(codigo, true);
        exibirToastPendente();
    }

    onMounted(async () => {
        exibirToastPendente();
        await carregarSubprocesso(false);
        carregamentoInicialConcluido.value = true;
    });

    watch(
        () => [codProcesso, siglaUnidade, codSubprocesso],
        async () => {
            await carregarSubprocesso(false);
        },
    );

    onActivated(async () => {
        exibirToastPendente();
        if (!carregamentoInicialConcluido.value) return;
        const codigoAtual = codigoSubprocesso.value;
        if (typeof codigoAtual === "number" && dadosEdicaoValidos(codigoAtual)) {
            return;
        }
        await carregarSubprocesso(false);
    });

    return {
        codigoSubprocesso,
        erroNaoEncontrado,
        carregarSubprocesso,
        atualizarSubprocessoAtual,
    };
}
