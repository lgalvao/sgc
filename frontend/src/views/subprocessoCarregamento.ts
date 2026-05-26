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

function criarEstado() {
    const codigoSubprocesso = ref<number | null>(null);
    const erroNaoEncontrado = ref(false);
    const carregamentoInicialConcluido = ref(false);

    return {codigoSubprocesso, erroNaoEncontrado, carregamentoInicialConcluido};
}

export function useSubprocessoCarregamento(dependencias: DependenciasSubprocessoCarregamento) {
    const {codigoSubprocesso, erroNaoEncontrado, carregamentoInicialConcluido} = criarEstado();
    let carregamentoEmAndamento: Promise<void> | null = null;

    async function resolverCodigoSubprocesso(limpar: boolean) {
        if (typeof dependencias.codSubprocesso === "number") {
            const contextoDireto = await dependencias.garantirContextoEdicao(dependencias.codSubprocesso, limpar);
            if (contextoDireto) {
                codigoSubprocesso.value = contextoDireto.detalhes.codigo;
                erroNaoEncontrado.value = false;
                return;
            }
        }

        const contexto = await dependencias.garantirContextoEdicaoPorProcessoEUnidade(
            dependencias.codProcesso,
            dependencias.siglaUnidade,
            limpar,
        );
        if (contexto) {
            codigoSubprocesso.value = contexto.codigo;
            erroNaoEncontrado.value = false;
            return;
        }

        codigoSubprocesso.value = null;
        erroNaoEncontrado.value = !dependencias.erroIntegracaoContexto.value;
    }

    async function carregarSubprocesso(limpar = false) {
        if (!limpar && carregamentoEmAndamento) {
            await carregamentoEmAndamento;
            return;
        }

        const tarefa = resolverCodigoSubprocesso(limpar);
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

        await dependencias.garantirContextoEdicao(codigoSubprocesso.value, true);
        dependencias.exibirToastPendente();
    }

    onMounted(async () => {
        dependencias.exibirToastPendente();
        await carregarSubprocesso(false);
        carregamentoInicialConcluido.value = true;
    });

    watch(
        () => [dependencias.codProcesso, dependencias.siglaUnidade, dependencias.codSubprocesso],
        async () => {
            await carregarSubprocesso(false);
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
        await carregarSubprocesso(false);
    });

    return {
        codigoSubprocesso,
        erroNaoEncontrado,
        carregarSubprocesso,
        atualizarSubprocessoAtual,
    };
}
