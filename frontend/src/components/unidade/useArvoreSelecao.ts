import {computed, ref, watch} from "vue";
import type {Unidade} from "@/types/tipos";
import {mapearHierarquia, coletarCodigosElegiveis, getTodasSubunidades} from "./arvoreSelecaoHelpers";

export function useArvoreSelecao(props: {
  unidades: Unidade[];
  modelValue: number[];
  modoSelecao: boolean;
}, emit: (e: "update:modelValue", value: number[]) => void) {
  const unidadesSelecionadasLocal = ref<number[]>([...props.modelValue]);

  // Mapas para acesso rápido (Pai e Unidade)
  const maps = computed(() => mapearHierarquia(props.unidades));

  const parentMap = computed(() => maps.value.parentMap);

  function filtrarSelecaoPorElegibilidade(selecao: number[]): number[] {
    const codigosElegiveis = coletarCodigosElegiveis(props.unidades);
    return selecao.filter(codigo => codigosElegiveis.has(codigo));
  }

  function isChecked(codigo: number): boolean {
    if (!props.modoSelecao) return false;
    return unidadesSelecionadasLocal.value.includes(codigo);
  }

  function isHabilitado(unidade: Unidade): boolean {
    if (!props.modoSelecao) return false;
    if (unidade.isElegivel) return true;
    if (!unidade.filhas || unidade.filhas.length === 0) return false;
    return unidade.filhas.some(filha => isHabilitado(filha));
  }

  function getEstadoSelecao(unidade: Unidade): boolean | "indeterminate" {
    if (!props.modoSelecao) return false;

    const selfSelected = isChecked(unidade.codigo);

    if (!unidade.filhas || unidade.filhas.length === 0) {
      return selfSelected;
    }

    const estadosFilhas = unidade.filhas.map(filha => getEstadoSelecao(filha));
    const todasFilhasMarcadas = estadosFilhas.every(estado => estado === true);
    const algumaFilhaSelecionada = estadosFilhas.some(estado => estado !== false);

    if (todasFilhasMarcadas) {
      return true;
    }

    if (!algumaFilhaSelecionada) {
      return selfSelected;
    }

    if (unidade.tipo === "INTEROPERACIONAL" && selfSelected) {
      return true;
    }

    return "indeterminate";
  }

  function updateAncestors(node: Unidade, selectionSet: Set<number>) {
    let current = node;
    while (true) {
      const parent = parentMap.value.get(current.codigo);
      if (!parent) break;

      const children = parent.filhas || [];
      const allChildrenSelected = children.every(child => selectionSet.has(child.codigo));

      if (allChildrenSelected) {
        if (parent.isElegivel) {
          selectionSet.add(parent.codigo);
        }
      } else if (parent.tipo !== 'INTEROPERACIONAL') {
        selectionSet.delete(parent.codigo);
      }
      current = parent;
    }
  }

  function toggle(unidade: Unidade, checked: boolean) {
    if (!props.modoSelecao) return;

    const newSelection = new Set(unidadesSelecionadasLocal.value);
    const unitsToToggle = [unidade, ...getTodasSubunidades(unidade)];

    if (checked) {
      unitsToToggle.forEach(u => {
        if (u.isElegivel) {
          newSelection.add(u.codigo);
        }
      });
    } else {
      unitsToToggle.forEach(u => newSelection.delete(u.codigo));
    }

    const unidadeVisual = unidade as Unidade & { agrupadorVisual?: boolean };
    if (unidadeVisual.agrupadorVisual) {
      (unidade.filhas ?? []).forEach(filha => updateAncestors(filha, newSelection));
    } else {
      updateAncestors(unidade, newSelection);
    }
    unidadesSelecionadasLocal.value = Array.from(newSelection);
  }

  function selecionarTodas(unidadesParaSelecionar: Unidade[]) {
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
  }

  function deselecionarTodas() {
    if (!props.modoSelecao) return;
    unidadesSelecionadasLocal.value = [];
  }

  watch(
    () => props.modelValue,
    (novoValor) => {
      const novoValorFiltrado = filtrarSelecaoPorElegibilidade(novoValor);
      const sortedNew = [...novoValorFiltrado].sort((a, b) => a - b);
      const sortedLocal = [...unidadesSelecionadasLocal.value].sort((a, b) => a - b);

      if (JSON.stringify(sortedNew) !== JSON.stringify(sortedLocal)) {
        unidadesSelecionadasLocal.value = [...novoValorFiltrado];
      }
    },
    {deep: true}
  );

  watch(
    unidadesSelecionadasLocal,
    (newValue) => {
      if (JSON.stringify(newValue) !== JSON.stringify(props.modelValue)) {
        emit("update:modelValue", newValue);
      }
    },
    {deep: true}
  );

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
