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
  modelValue: number[];
  filtrarPor?: (unidade: Unidade) => boolean;
  ocultarRaiz?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  filtrarPor: () => true,
  ocultarRaiz: true,
});

const emit = defineEmits<(e: "update:modelValue", value: number[]) => void>();

// Estado local sincronizado com props.modelValue
const unidadesSelecionadasLocal = ref<number[]>([...props.modelValue]);

// Mapas computados uma única vez para acesso O(1)
// Bolt Optimization: Pre-calculate maps to avoid O(N^2) lookups during rendering
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

// Filtrar unidades pela função customizada e ocultar Raiz se configurado
const unidadesExibidas = computed(() => {
  const filtradas = props.unidades.filter(props.filtrarPor);
  const lista: Unidade[] = [];

  for (const u of filtradas) {
    if (props.ocultarRaiz) {
      // Se ocultarRaiz for true, ignoramos o nó raiz e mostramos suas filhas
      if (u.filhas) lista.push(...u.filhas);
    } else {
      lista.push(u);
    }
  }
  return lista;
});



// Obtém todas subunidades recursivamente (retorna objetos para evitar lookups)
// Bolt Optimization: Return Unidade objects directly to avoid O(N) lookup for each child
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

// Verifica se está marcada
function isChecked(codigo: number): boolean {
  return unidadesSelecionadasLocal.value.includes(codigo);
}

// Verifica se unidade deve estar habilitada (recursivo)
// Habilitado se: elegível OU tem pelo menos uma filha elegível
// Bolt Optimization: This could be memoized, but relying on object identity is fast enough for now
function isHabilitado(unidade: Unidade): boolean {
  if (unidade.isElegivel) return true;

  if (!unidade.filhas || unidade.filhas.length === 0) return false;

  return unidade.filhas.some(filha => isHabilitado(filha));
}

// Obtém estado de seleção (true, false ou 'indeterminate')
// Baseado APENAS no modelValue - fonte única de verdade
function getEstadoSelecao(unidade: Unidade): boolean | "indeterminate" {
  const selfSelected = isChecked(unidade.codigo);

  // 1. Se não tem filhas, retorna se está no modelValue
  if (!unidade.filhas || unidade.filhas.length === 0) {
    return selfSelected;
  }

  // 2. Conta descendentes ELEGÍVEIS
  // Bolt Optimization: Access objects directly, avoid O(N) lookup
  const descendentesElegiveis = getTodasSubunidades(unidade).filter(desc => desc.isElegivel);

  // 3. Se não tem descendentes elegíveis, retorna próprio estado
  if (descendentesElegiveis.length === 0) {
    return selfSelected;
  }

  // 4. Conta quantas descendentes elegíveis estão no modelValue
  const descendentesSelecionadas = descendentesElegiveis.filter(desc =>
      isChecked(desc.codigo)
  ).length;

  // 5. Todas descendentes selecionadas? → marcada
  if (descendentesSelecionadas === descendentesElegiveis.length) {
    return true;
  }

  // 6. Nenhuma descendente selecionada? → desmarcada (ou marcada se INTEROPERACIONAL)
  if (descendentesSelecionadas === 0) {
    // INTEROPERACIONAL pode estar marcada sozinha
    return unidade.tipo === "INTEROPERACIONAL" && selfSelected;
  }

  // 7. Algumas descendentes selecionadas → indeterminada (ou marcada se INTEROPERACIONAL)
  // INTEROPERACIONAL pode estar marcada mesmo sem todas filhas
  if (unidade.tipo === "INTEROPERACIONAL" && selfSelected) {
    return true;
  }

  return "indeterminate";
}

function toggle(unidade: Unidade, checked: boolean) {
  const newSelection = new Set(unidadesSelecionadasLocal.value);

  // 1. Handle Self and Descendants
  // Bolt Optimization: Use already available object references
  const unitsToToggle = [unidade, ...getTodasSubunidades(unidade)];

  if (checked) {
    // Adiciona apenas unidades elegíveis (filtra INTERMEDIARIA automaticamente)
    unitsToToggle.forEach(u => {
      if (u.isElegivel) {
        newSelection.add(u.codigo);
      }
    });
  } else {
    unitsToToggle.forEach(u => newSelection.delete(u.codigo));
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
      // Clone arrays before sorting to avoid mutating props or local state
      const sortedNew = [...novoValor].sort();
      const sortedLocal = [...unidadesSelecionadasLocal.value].sort();

      if (JSON.stringify(sortedNew) !== JSON.stringify(sortedLocal)) {
        unidadesSelecionadasLocal.value = [...novoValor];
      }
    },
    {deep: true},
);

// Estado de expansão das unidades
// Inicializa com as raízes expandidas
// Bolt Optimization: Calculate initial set in O(N) once
const expandedUnits = ref<Set<number>>(new Set());

// Initialize expanded units when units prop changes
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

// Watch para emitir mudanças locais para o pai
watch(
    unidadesSelecionadasLocal,
    (newValue) => {
      // Só emite se for diferente do props (evita loop)
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
