import type {Ref} from "vue";
import {computed, ref, watch} from "vue";
import type {Unidade} from "@/types/tipos";
import {
    coletarCodigosElegiveis,
    ehAgrupadorVisual,
    getTodasSubunidades,
    mapearHierarquia,
    ordenarCodigos
} from "./arvoreSelecaoHelpers";

type PropsArvoreSelecao = {
    unidades: Unidade[];
    modelValue: number[];
    modoSelecao: boolean;
    mostrarSuperioresNaoElegiveisComoIndeterminados: boolean;
};

function criarFiltradorSelecao(props: PropsArvoreSelecao) {
    return (selecao: number[]): number[] => {
        const codigosElegiveis = coletarCodigosElegiveis(props.unidades);
        return selecao.filter(codigo => codigosElegiveis.has(codigo));
    };
}

function criarCalculadorEstadoSelecao(props: PropsArvoreSelecao) {
    const calcularEstadoSelecao = (
        unidade: Unidade,
        selectionSet: ReadonlySet<number>
    ): boolean | "indeterminate" => {
        const selfSelected = selectionSet.has(unidade.codigo);
        if (!unidade.filhas || unidade.filhas.length === 0) {
            return selfSelected;
        }

        const estadosFilhas = unidade.filhas.map(filha => calcularEstadoSelecao(filha, selectionSet));
        const todasFilhasMarcadas = estadosFilhas.every(estado => estado === true);
        const algumaFilhaSelecionada = estadosFilhas.some(estado => estado !== false);

        if (props.mostrarSuperioresNaoElegiveisComoIndeterminados && !unidade.isElegivel && algumaFilhaSelecionada) {
            return "indeterminate";
        }
        if (todasFilhasMarcadas) return true;
        if (!algumaFilhaSelecionada) return selfSelected;
        return "indeterminate";
    };

    return calcularEstadoSelecao;
}

function sincronizarSelecaoLocal(novoValor: number[], unidadesSelecionadasLocal: Ref<number[]>, filtrarSelecaoPorElegibilidade: (selecao: number[]) => number[]) {
    const novoValorFiltrado = filtrarSelecaoPorElegibilidade(novoValor);
    if (JSON.stringify(ordenarCodigos(novoValorFiltrado)) !== JSON.stringify(ordenarCodigos(unidadesSelecionadasLocal.value))) {
        unidadesSelecionadasLocal.value = [...novoValorFiltrado];
    }
}

function criarAtualizadorAncestrais(
    parentMap: Ref<Map<number, Unidade>>,
    calcularEstadoSelecao: (unidade: Unidade, selectionSet: ReadonlySet<number>) => boolean | "indeterminate"
) {
    return (node: Unidade, selectionSet: Set<number>) => {
        let current = node;
        while (true) {
            const parent = parentMap.value.get(current.codigo);
            if (!parent) break;

            const allChildrenSelected = (parent.filhas || []).every(child => calcularEstadoSelecao(child, selectionSet) === true);
            if (allChildrenSelected && parent.isElegivel) {
                selectionSet.add(parent.codigo);
            } else {
                selectionSet.delete(parent.codigo);
            }
            current = parent;
        }
    };
}

function criarAlternadorSelecao(props: PropsArvoreSelecao, unidadesSelecionadasLocal: Ref<number[]>, updateAncestors: (node: Unidade, selectionSet: Set<number>) => void) {
    return (unidade: Unidade, checked: boolean) => {
        if (!props.modoSelecao) return;

        const newSelection = new Set(unidadesSelecionadasLocal.value);
        [unidade, ...getTodasSubunidades(unidade)].forEach((unidadeAtual) => {
            if (checked) {
                if (unidadeAtual.isElegivel) {
                    newSelection.add(unidadeAtual.codigo);
                }
                return;
            }
            newSelection.delete(unidadeAtual.codigo);
        });

        if (ehAgrupadorVisual(unidade)) {
            (unidade.filhas ?? []).forEach(filha => updateAncestors(filha, newSelection));
        } else {
            updateAncestors(unidade, newSelection);
        }
        unidadesSelecionadasLocal.value = Array.from(newSelection);
    };
}

function criarSelecionadorTotal(props: PropsArvoreSelecao, unidadesSelecionadasLocal: Ref<number[]>) {
    return (unidadesParaSelecionar: Unidade[]) => {
        if (!props.modoSelecao) return;

        const newSelection = new Set<number>(unidadesSelecionadasLocal.value);
        const traverse = (nodes: Unidade[]) => {
            nodes.forEach(node => {
                if (node.isElegivel) {
                    newSelection.add(node.codigo);
                }
                if (node.filhas) {
                    traverse(node.filhas);
                }
            });
        };

        traverse(unidadesParaSelecionar);
        unidadesSelecionadasLocal.value = Array.from(newSelection);
    };
}

function isHabilitadoParaSelecao(modoSelecao: boolean, unidade: Unidade): boolean {
    if (!modoSelecao) return false;
    if (unidade.isElegivel) return true;
    return (unidade.filhas ?? []).some(filha => isHabilitadoParaSelecao(modoSelecao, filha));
}

function configurarWatchers(args: {
    props: PropsArvoreSelecao;
    unidadesSelecionadasLocal: Ref<number[]>;
    filtrarSelecaoPorElegibilidade: (selecao: number[]) => number[];
    emit: (e: "update:modelValue", value: number[]) => void;
}) {
    watch(
        () => args.props.modelValue,
        (novoValor) => sincronizarSelecaoLocal(novoValor, args.unidadesSelecionadasLocal, args.filtrarSelecaoPorElegibilidade),
        {deep: true}
    );

    watch(() => args.unidadesSelecionadasLocal.value, (newValue) => args.emit("update:modelValue", newValue), {deep: true});
}

export function useArvoreSelecao(props: PropsArvoreSelecao, emit: (e: "update:modelValue", value: number[]) => void) {
    const unidadesSelecionadasLocal = ref<number[]>([...props.modelValue]);
    const maps = computed(() => mapearHierarquia(props.unidades));
    const parentMap = computed(() => maps.value.parentMap);
    const filtrarSelecaoPorElegibilidade = criarFiltradorSelecao(props);
    const calcularEstadoSelecao = criarCalculadorEstadoSelecao(props);
    const updateAncestors = criarAtualizadorAncestrais(parentMap, calcularEstadoSelecao);
    const toggle = criarAlternadorSelecao(props, unidadesSelecionadasLocal, updateAncestors);
    const isChecked = (codigo: number) => props.modoSelecao && unidadesSelecionadasLocal.value.includes(codigo);
    const isHabilitado = (unidade: Unidade) => isHabilitadoParaSelecao(props.modoSelecao, unidade);
    const getEstadoSelecao = (unidade: Unidade) => props.modoSelecao
        ? calcularEstadoSelecao(unidade, new Set(unidadesSelecionadasLocal.value))
        : false;
    const selecionarTodas = criarSelecionadorTotal(props, unidadesSelecionadasLocal);
    const deselecionarTodas = () => {
        if (props.modoSelecao) {
            unidadesSelecionadasLocal.value = [];
        }
    };

    configurarWatchers({props, unidadesSelecionadasLocal, filtrarSelecaoPorElegibilidade, emit});

    return {
        unidadesSelecionadasLocal,
        isChecked,
        isHabilitado,
        getEstadoSelecao,
        toggle,
        selecionarTodas,
        deselecionarTodas,
        filtrarSelecaoPorElegibilidade
    };
}
