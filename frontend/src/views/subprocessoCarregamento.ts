import {onActivated, onMounted, ref, watch} from "vue";
import {useSubprocessoStore} from "@/stores/subprocesso";

type DependenciasSubprocessoCarregamento = {
    codProcesso: number;
    siglaUnidade: string;
    codSubprocesso?: number;
    exibirToastPendente: () => void;
};

function criarEstado() {
    const codigoSubprocesso = ref<number | null>(null);
    const erroNaoEncontrado = ref(false);
    const carregamentoInicialConcluido = ref(false);

    return {codigoSubprocesso, erroNaoEncontrado, carregamentoInicialConcluido};
}

export function useSubprocessoCarregamento(dependencias: DependenciasSubprocessoCarregamento) {
    const subprocessoStore = useSubprocessoStore();
    const {codigoSubprocesso, erroNaoEncontrado, carregamentoInicialConcluido} = criarEstado();
    let carregamentoEmAndamento: Promise<void> | null = null;

    async function resolverCodigoSubprocesso(recarregar: boolean) {
        if (typeof dependencias.codSubprocesso === "number") {
            const contextoDireto = await subprocessoStore.obterContextoEdicao(
                dependencias.codSubprocesso,
                {recarregar},
            );
            if (contextoDireto) {
                codigoSubprocesso.value = contextoDireto.detalhes.codigo;
                erroNaoEncontrado.value = false;
            } else {
                codigoSubprocesso.value = null;
                erroNaoEncontrado.value = true;
            }
            return;
        }

        const contexto = await subprocessoStore.obterContextoEdicaoPorProcessoEUnidade(
            dependencias.codProcesso,
            dependencias.siglaUnidade,
            {recarregar},
        );
        if (contexto) {
            codigoSubprocesso.value = contexto.codigo;
            erroNaoEncontrado.value = false;
            return;
        }

        codigoSubprocesso.value = null;
        erroNaoEncontrado.value = !subprocessoStore.erroIntegracaoContexto;
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

        await subprocessoStore.obterContextoEdicao(codigoSubprocesso.value, {recarregar: true});
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
        if (typeof codigoSubprocesso.value === "number" && subprocessoStore.dadosEdicaoValidos(codigoSubprocesso.value)) {
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
