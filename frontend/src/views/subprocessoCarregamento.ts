import {type ComputedRef, onActivated, onMounted, ref, watch} from "vue";

type ContextoEdicaoDireto = { detalhes: { codigo: number } } | null;
type ContextoEdicaoPorProcesso = { codigo: number } | null;

type DependenciasSubprocessoCarregamento = {
    codProcesso: number;
    siglaUnidade: string;
    codSubprocesso?: number;
    erroIntegracaoContexto: ComputedRef<unknown>;
    obterContextoEdicao: (codigoSubprocesso: number) => Promise<ContextoEdicaoDireto>;
    recarregarContextoEdicao: (codigoSubprocesso: number) => Promise<ContextoEdicaoDireto>;
    obterContextoEdicaoPorProcessoEUnidade: (
        codProcesso: number,
        siglaUnidade: string,
    ) => Promise<ContextoEdicaoPorProcesso>;
    recarregarContextoEdicaoPorProcessoEUnidade: (
        codProcesso: number,
        siglaUnidade: string,
    ) => Promise<ContextoEdicaoPorProcesso>;
    dadosEdicaoValidos: (codigoSubprocesso: number) => boolean;
    exibirToastPendente: () => void;
};

function criarEstado() {
    const codigoSubprocesso = ref<number | null>(null);
    const erroNaoEncontrado = ref(false);
    const carregamentoInicialConcluido = ref(false);

    return {codigoSubprocesso, erroNaoEncontrado, carregamentoInicialConcluido};
}

export function useSubprocessoCarregamento(dependencias: DependenciasSubprocessoCarregamento) {
    const {codigoSubprocesso, erroNaoEncontrado, carregamentoInicialConcluido} = criarEstado();
    let carregamentoEmAndamento: Promise<void> | null = null;

    async function resolverCodigoSubprocesso(recarregar: boolean) {
        const carregarContextoDireto = recarregar
            ? dependencias.recarregarContextoEdicao
            : dependencias.obterContextoEdicao;
        const carregarContextoPorProcesso = recarregar
            ? dependencias.recarregarContextoEdicaoPorProcessoEUnidade
            : dependencias.obterContextoEdicaoPorProcessoEUnidade;

        if (typeof dependencias.codSubprocesso === "number") {
            const contextoDireto = await carregarContextoDireto(dependencias.codSubprocesso);
            if (contextoDireto) {
                codigoSubprocesso.value = contextoDireto.detalhes.codigo;
                erroNaoEncontrado.value = false;
                return;
            }
        }

        const contexto = await carregarContextoPorProcesso(
            dependencias.codProcesso,
            dependencias.siglaUnidade,
        );
        if (contexto) {
            codigoSubprocesso.value = contexto.codigo;
            erroNaoEncontrado.value = false;
            return;
        }

        codigoSubprocesso.value = null;
        erroNaoEncontrado.value = !dependencias.erroIntegracaoContexto.value;
    }

    async function carregarSubprocesso(recarregar = false) {
        if (!recarregar && carregamentoEmAndamento) {
            await carregamentoEmAndamento;
            return;
        }

        const tarefa = resolverCodigoSubprocesso(recarregar);
        carregamentoEmAndamento = tarefa;
        try {
            await tarefa;
        } finally {
            if (carregamentoEmAndamento === tarefa) {
                carregamentoEmAndamento = null;
            }
        }
    }

    async function atualizarSubprocessoAtual() {
        if (typeof codigoSubprocesso.value !== "number") {
            return;
        }

        await dependencias.recarregarContextoEdicao(codigoSubprocesso.value);
        dependencias.exibirToastPendente();
    }

    onMounted(async () => {
        dependencias.exibirToastPendente();
        await carregarSubprocesso();
        carregamentoInicialConcluido.value = true;
    });

    watch(
        () => [dependencias.codProcesso, dependencias.siglaUnidade, dependencias.codSubprocesso],
        async () => {
            await carregarSubprocesso();
        },
    );

    onActivated(async () => {
        dependencias.exibirToastPendente();
        if (!carregamentoInicialConcluido.value) {
            return;
        }
        if (typeof codigoSubprocesso.value === "number" && dependencias.dadosEdicaoValidos(codigoSubprocesso.value)) {
            return;
        }
        try {
            await carregarSubprocesso();
        } catch (e) {
            // Erros em recarga de background são ignorados para manter a estabilidade da UI
        }
    });

    return {
        codigoSubprocesso,
        erroNaoEncontrado,
        carregarSubprocesso,
        atualizarSubprocessoAtual,
    };
}
