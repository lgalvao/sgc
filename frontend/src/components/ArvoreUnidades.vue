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
}

const props = withDefaults(defineProps<Props>(), {
  filtrarPor: () => true,
});

const emit = defineEmits<(e: "update:modelValue", value: number[]) => void>();

// Estado local sincronizado com props.modelValue
const unidadesSelecionadasLocal = ref<number[]>([...props.modelValue]);

// Watch para sincronizar props.modelValue -> local (apenas quando props mudam externamente)
watch(
    () => props.modelValue,
    (newValue) => {
      // Só atualiza se for diferente (evita loop)
      if (JSON.stringify(newValue) !== JSON.stringify(unidadesSelecionadasLocal.value)) {
        unidadesSelecionadasLocal.value = [...newValue];
      }
    },
    {deep: true}
);

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
// Baseado APENAS no modelValue - fonte única de verdade
function getEstadoSelecao(unidade: Unidade): boolean | "indeterminate" {
  const selfSelected = isChecked(unidade.codigo);

  // 1. Se não tem filhas, retorna se está no modelValue
  if (!unidade.filhas || unidade.filhas.length === 0) {
    return selfSelected;
  }

  // 2. Conta descendentes ELEGÍVEIS
  const descendentesElegiveis = getTodasSubunidades(unidade).filter(codigo => {
    const desc = findUnidadeById(codigo);
    return desc?.isElegivel;
  });

  // 3. Se não tem descendentes elegíveis, retorna próprio estado
  if (descendentesElegiveis.length === 0) {
    return selfSelected;
  }

  // 4. Conta quantas descendentes elegíveis estão no modelValue
  const descendentesSelecionadas = descendentesElegiveis.filter(codigo =>
      isChecked(codigo)
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
// Inicializa com as raízes expandidas
const expandedUnits = ref<Set<number>>(
    new Set(props.unidades.map(u => u.codigo))
);

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