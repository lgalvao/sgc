<template>
  <div class="arvore-unidades">
    <UnidadeTreeNode
        v-for="unidade in unidadesExibidas"
        :key="unidade.sigla"
        :unidade="unidade"
        :is-checked="isChecked"
        :get-estado-selecao="getEstadoSelecao"
        :is-expanded="isExpanded"
        :is-habilitado="isHabilitado"
        :on-toggle="toggle"
        :on-toggle-expand="toggleExpand"
    />
  </div>
</template>

<script setup lang="ts">
import {computed, ref, watch} from "vue";
import type {Unidade} from "@/types/tipos";
import UnidadeTreeNode from "./UnidadeTreeNode.vue";

interface Props {
  unidades: Unidade[];
  modelValue: number[];
  filtrarPor?: (unidade: Unidade) => boolean;
}

const props = withDefaults(defineProps<Props>(), {
  filtrarPor: () => true,
});

const emit = defineEmits<(e: "update:modelValue", value: number[]) => void>();

const unidadesSelecionadasLocal = ref<number[]>([...props.modelValue]);

// Map to find parent of a unit
const parentMap = computed(() => {
  const map = new Map<number, Unidade>();
  const traverse = (node: Unidade, parent?: Unidade) => {
    if (parent) map.set(node.codigo, parent);
    if (node.filhas) node.filhas.forEach(child => traverse(child, node));
  };
  props.unidades.forEach(u => traverse(u));
  return map;
});

// Filtrar unidades pela função customizada e ocultar SEDOC (raiz)
const unidadesExibidas = computed(() => {
  const filtradas = props.unidades.filter(props.filtrarPor);
  const lista: Unidade[] = [];

  for (const u of filtradas) {
    // Se for SEDOC (pela sigla ou código 1), não mostra ela, mas mostra as filhas
    if (u.sigla === 'SEDOC' || u.codigo === 1) {
      if (u.filhas) lista.push(...u.filhas);
    } else {
      lista.push(u);
    }
  }
  return lista;
});

// Verifica se é folha (sem filhas)
function isFolha(unidade: Unidade): boolean {
  return !unidade.filhas || unidade.filhas.length === 0;
}

// Busca uma unidade por código na árvore
function findUnidadeById(codigo: number): Unidade | null {
  const search = (nodes: Unidade[]): Unidade | null => {
    for (const node of nodes) {
      if (node.codigo === codigo) return node;
      if (node.filhas) {
        const found = search(node.filhas);
        if (found) return found;
      }
    }
    return null;
  };
  return search(props.unidades);
}

// Obtém todas subunidades recursivamente
function getTodasSubunidades(unidade: Unidade): number[] {
  const result: number[] = [];
  if (unidade.filhas) {
    for (const filha of unidade.filhas) {
      result.push(filha.codigo);
      if (filha.filhas) {
        result.push(...getTodasSubunidades(filha));
      }
    }
  }
  return result;
}

// Verifica se está marcada
function isChecked(codigo: number): boolean {
  return unidadesSelecionadasLocal.value.includes(codigo);
}

// Verifica se unidade deve estar habilitada (recursivo)
// Habilitado se: elegível OU tem pelo menos uma filha elegível
function isHabilitado(unidade: Unidade): boolean {
  if (unidade.isElegivel) return true;
  
  if (!unidade.filhas || unidade.filhas.length === 0) return false;
  
  return unidade.filhas.some(filha => isHabilitado(filha));
}

// Obtém estado de seleção (true, false ou 'indeterminate')
function getEstadoSelecao(unidade: Unidade): boolean | "indeterminate" {
  const selfSelected = isChecked(unidade.codigo);

  // Se é folha, retorna o próprio estado
  if (isFolha(unidade)) {
    return selfSelected;
  }

  // Se não tem filhas, retorna o próprio estado
  if (!unidade.filhas || unidade.filhas.length === 0) {
    return selfSelected;
  }

  // Verifica o estado de cada filha DIRETA (recursivamente)
  let todasMarcadas = true;
  let algumaMarcada = false;

  for (const filha of unidade.filhas) {
    const estadoFilha = getEstadoSelecao(filha);
    
    if (estadoFilha === true) {
      algumaMarcada = true;
    } else if (estadoFilha === false) {
      todasMarcadas = false;
    } else {
      // Se alguma filha está indeterminada, o pai também fica indeterminado
      todasMarcadas = false;
      algumaMarcada = true;
    }
  }

  // Se todas as filhas estão marcadas
  if (todasMarcadas) {
    return true;
  }

  // Se nenhuma filha está marcada
  if (!algumaMarcada) {
    return false;
  }

  // Exceção INTEROPERACIONAL: pode estar marcada mesmo sem todas filhas
  if (unidade.tipo === "INTEROPERACIONAL" && selfSelected) {
    return true;
  }

  // Algumas filhas marcadas, mas não todas
  return "indeterminate";
}

function toggle(unidade: Unidade, checked: boolean) {
  const newSelection = new Set(unidadesSelecionadasLocal.value);

  // 1. Handle Self and Descendants
  const idsToToggle = [unidade.codigo, ...getTodasSubunidades(unidade)];

  if (checked) {
    // Adiciona apenas unidades elegíveis (filtra INTERMEDIARIA automaticamente)
    // INTERMEDIARIA nunca é elegível, então nunca será adicionada
    idsToToggle.forEach(id => {
      const unidadeParaAdicionar = findUnidadeById(id);
      if (unidadeParaAdicionar?.isElegivel) {
        newSelection.add(id);
      }
    });
  } else {
    idsToToggle.forEach(id => newSelection.delete(id));
  }

  // 2. Handle Ancestors (Upwards)
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
      // Adiciona o pai se ele for elegível
      // INTERMEDIARIA nunca é elegível, então nunca será adicionada aqui
      if (parent.isElegivel) {
        selectionSet.add(parent.codigo);
      }
    } else {
      // Se não forem todas as filhas selecionadas, remove o pai
      // EXCETO se o pai for INTEROPERACIONAL (regra especial)
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
      if (
          JSON.stringify(novoValor.sort()) !==
          JSON.stringify(unidadesSelecionadasLocal.value.sort())
      ) {
        unidadesSelecionadasLocal.value = [...novoValor];
      }
    },
    {deep: true},
);

// Estado de expansão das unidades
const expandedUnits = ref<Set<number>>(new Set());

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

// Watch para reagir a mudanças internas e emitir para o pai
watch(
    unidadesSelecionadasLocal,
    (novoValor) => {
      emit("update:modelValue", novoValor);
    },
    {deep: true},
);
</script>

<style scoped>
.arvore-unidades .form-check-input:indeterminate {
  background-color: #6c757d;
  border-color: #6c757d;
}
</style>