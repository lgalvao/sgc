<template>
  <div class="arvore-unidades">
    <UnidadeTreeNode
        v-for="unidade in unidadesExibidas"
        :key="unidade.sigla"
        :get-estado-selecao="getEstadoSelecao"
        :is-checked="isChecked"
        :is-expanded="isExpanded"
        :is-habilitado="isHabilitado"
        :on-toggle="toggle"
        :on-toggle-expand="toggleExpand"
        :unidade="unidade"
    />
  </div>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from "vue";
import type {Unidade} from "@/types/tipos";
import UnidadeTreeNode from "./UnidadeTreeNode.vue";

interface Props {
  unidades: Unidade[];
  modelValue: number[]; // V-model: Lista de IDs selecionados
  filtrarPor?: (unidade: Unidade) => boolean;
  ocultarRaiz?: boolean;
  modoSelecao?: boolean; // Novo prop para ativar/desativar checkboxes (default true)
}

const props = withDefaults(defineProps<Props>(), {
  filtrarPor: () => true,
  ocultarRaiz: true,
  modoSelecao: true
});

const emit = defineEmits<(e: "update:modelValue", value: number[]) => void>();

const unidadesSelecionadasLocal = ref<number[]>([...props.modelValue]);

// Mapas para acesso rápido (Pai e Unidade)
const maps = computed(() => {
  const pMap = new Map<number, Unidade>();
  const uMap = new Map<number, Unidade>();

  const traverse = (node: Unidade, parent?: Unidade) => {
    uMap.set(node.codigo, node);
    if (parent) {
      pMap.set(node.codigo, parent);
    }
    if (node.filhas) {
      node.filhas.forEach(child => traverse(child, node));
    }
  };

  props.unidades.forEach(u => traverse(u));
  return { parentMap: pMap, unitMap: uMap };
});

const parentMap = computed(() => maps.value.parentMap);

const unidadesExibidas = computed(() => {
  const filtradas = props.unidades.filter(props.filtrarPor);
  const lista: Unidade[] = [];

  for (const u of filtradas) {
    if (props.ocultarRaiz) {
      if (u.filhas) lista.push(...u.filhas);
    } else {
      lista.push(u);
    }
  }
  return lista;
});

function getTodasSubunidades(unidade: Unidade): Unidade[] {
  const result: Unidade[] = [];
  if (unidade.filhas) {
    for (const filha of unidade.filhas) {
      result.push(filha);
      if (filha.filhas) {
        result.push(...getTodasSubunidades(filha));
      }
    }
  }
  return result;
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

  const descendentesElegiveis = getTodasSubunidades(unidade).filter(desc => desc.isElegivel);

  if (descendentesElegiveis.length === 0) {
    return selfSelected;
  }

  const descendentesSelecionadas = descendentesElegiveis.filter(desc =>
      isChecked(desc.codigo)
  ).length;

  if (descendentesSelecionadas === descendentesElegiveis.length) {
    return true;
  }

  if (descendentesSelecionadas === 0) {
    return unidade.tipo === "INTEROPERACIONAL" && selfSelected;
  }

  if (unidade.tipo === "INTEROPERACIONAL" && selfSelected) {
    return true;
  }

  return "indeterminate";
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

  updateAncestors(unidade, newSelection);
  unidadesSelecionadasLocal.value = Array.from(newSelection);
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
    } else {
      if (parent.tipo !== 'INTEROPERACIONAL') {
        selectionSet.delete(parent.codigo);
      }
    }
    current = parent;
  }
}

watch(
    () => props.modelValue,
    (novoValor) => {
      const sortedNew = [...novoValor].sort();
      const sortedLocal = [...unidadesSelecionadasLocal.value].sort();

      if (JSON.stringify(sortedNew) !== JSON.stringify(sortedLocal)) {
        unidadesSelecionadasLocal.value = [...novoValor];
      }
    },
    {deep: true},
);

const expandedUnits = ref<Set<number>>(new Set());

watch(() => props.unidades, (newUnidades) => {
   if (newUnidades && newUnidades.length > 0) {
       expandedUnits.value = new Set(newUnidades.map(u => u.codigo));
   }
}, { immediate: true });

function isExpanded(unidade: Unidade): boolean {
  return expandedUnits.value.has(unidade.codigo);
}

function toggleExpand(unidade: Unidade) {
  if (expandedUnits.value.has(unidade.codigo)) {
    expandedUnits.value.delete(unidade.codigo);
  } else {
    expandedUnits.value.add(unidade.codigo);
  }
}

watch(
    unidadesSelecionadasLocal,
    (newValue) => {
      if (JSON.stringify(newValue) !== JSON.stringify(props.modelValue)) {
        emit("update:modelValue", newValue);
      }
    },
    {deep: true}
);
</script>

<style scoped>
.arvore-unidades .form-check-input:indeterminate {
  background-color: #6c757d;
  border-color: #6c757d;
}
</style>
