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

type EstadoSubprocessoCarregamento = ReturnType<typeof criarEstado>;

function criarEstado() {
    const codigoSubprocesso = ref<number | null>(null);
    const erroNaoEncontrado = ref(false);
    const carregamentoInicialConcluido = ref(false);

    return {codigoSubprocesso, erroNaoEncontrado, carregamentoInicialConcluido};
}

function criarCarregador(dependencias: DependenciasSubprocessoCarregamento, estado: EstadoSubprocessoCarregamento) {
    let carregamentoEmAndamento: Promise<void> | null = null;

    async function resolverCodigoSubprocesso(limpar: boolean) {
        if (typeof dependencias.codSubprocesso === "number") {
            const contextoDireto = await dependencias.garantirContextoEdicao(dependencias.codSubprocesso, limpar);
            if (contextoDireto) {
                estado.codigoSubprocesso.value = contextoDireto.detalhes.codigo;
                estado.erroNaoEncontrado.value = false;
                return;
            }
        }

        const contexto = await dependencias.garantirContextoEdicaoPorProcessoEUnidade(
            dependencias.codProcesso,
            dependencias.siglaUnidade,
            limpar,
        );
        if (contexto) {
            estado.codigoSubprocesso.value = contexto.codigo;
            estado.erroNaoEncontrado.value = false;
            return;
        }

        estado.codigoSubprocesso.value = null;
        estado.erroNaoEncontrado.value = !dependencias.erroIntegracaoContexto.value;
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

    return {carregarSubprocesso};
}

function registrarRecargas(
    dependencias: DependenciasSubprocessoCarregamento,
    estado: EstadoSubprocessoCarregamento,
    carregarSubprocesso: (limpar?: boolean) => Promise<void>
) {
    onMounted(async () => {
        dependencias.exibirToastPendente();
        await carregarSubprocesso(false);
        estado.carregamentoInicialConcluido.value = true;
    });

    watch(
        () => [dependencias.codProcesso, dependencias.siglaUnidade, dependencias.codSubprocesso],
        async () => {
            await carregarSubprocesso(false);
        },
    );

    onActivated(async () => {
        dependencias.exibirToastPendente();
        if (!estado.carregamentoInicialConcluido.value) {
            return;
        }
        if (typeof estado.codigoSubprocesso.value === "number" && dependencias.dadosEdicaoValidos(estado.codigoSubprocesso.value)) {
            return;
        }
        await carregarSubprocesso(false);
    });
}

export function useSubprocessoCarregamento(dependencias: DependenciasSubprocessoCarregamento) {
    const estado = criarEstado();
    const {carregarSubprocesso} = criarCarregador(dependencias, estado);

    async function atualizarSubprocessoAtual() {
        if (typeof estado.codigoSubprocesso.value !== "number") {
            return;
        }

        await dependencias.garantirContextoEdicao(estado.codigoSubprocesso.value, true);
        dependencias.exibirToastPendente();
    }

    registrarRecargas(dependencias, estado, carregarSubprocesso);

    return {
        codigoSubprocesso: estado.codigoSubprocesso,
        erroNaoEncontrado: estado.erroNaoEncontrado,
        carregarSubprocesso,
        atualizarSubprocessoAtual,
    };
}
