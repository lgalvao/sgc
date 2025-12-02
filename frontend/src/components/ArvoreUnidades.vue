<template>
  <div class="arvore-unidades">
    <template
      v-for="unidade in unidadesExibidas"
      :key="unidade.sigla"
    >
      <div class="form-check">
        <BFormCheckbox
          :id="`chk-${unidade.sigla}`"
          :model-value="isChecked(unidade.codigo)"
          :indeterminate="getEstadoSelecao(unidade) === 'indeterminate'"
          :disabled="!unidade.isElegivel"
          :data-testid="`chk-arvore-unidade-${unidade.sigla}`"
          @update:model-value="(val) => toggle(unidade, val as boolean)"
        >
          <label
            :for="`chk-${unidade.sigla}`"
            class="form-check-label ms-2"
            :class="{ 'text-muted': !unidade.isElegivel }"
          >
            <strong>{{ unidade.sigla }}</strong> - {{ unidade.nome }}
            <span
              v-if="!unidade.isElegivel"
              class="badge bg-warning text-dark ms-2"
            >
              Não elegível
            </span>
          </label>
        </BFormCheckbox>
      </div>

      <!-- Mostrar filhas se a unidade tem filhas -->
      <span
        v-if="unidade.filhas && unidade.filhas.length > 0"
        class="me-2 cursor-pointer user-select-none"
        :data-testid="`btn-arvore-expand-${unidade.sigla}`"
        @click="toggleExpand(unidade)"
      >
        {{ isExpanded(unidade) ? '[-]' : '[+]' }}
      </span>
      <div
        v-if="unidade.filhas && unidade.filhas.length"
        class="ms-4"
      >
        <template
          v-for="filha in unidade.filhas"
          :key="filha.sigla"
        >
          <div class="form-check">
            <BFormCheckbox
              :id="`chk-${filha.sigla}`"
              :model-value="isChecked(filha.codigo)"
              :indeterminate="getEstadoSelecao(filha) === 'indeterminate'"
              :disabled="!filha.isElegivel"
              :data-testid="`chk-arvore-unidade-${filha.sigla}`"
              @update:model-value="(val) => toggle(filha, val as boolean)"
            >
              <label
                :for="`chk-${filha.sigla}`"
                class="form-check-label ms-2"
                :class="{ 'text-muted': !filha.isElegivel }"
              >
                <strong>{{ filha.sigla }}</strong> - {{ filha.nome }}
                <span
                  v-if="!filha.isElegivel"
                  class="badge bg-warning text-dark ms-2"
                >
                  Não elegível
                </span>
              </label>
            </BFormCheckbox>
          </div>

          <div
            v-if="filha.filhas && filha.filhas.length"
            class="ms-4"
          >
            <template
              v-for="neta in filha.filhas"
              :key="neta.sigla"
            >
              <div class="form-check">
                <BFormCheckbox
                  :id="`chk-${neta.sigla}`"
                  :model-value="isChecked(neta.codigo)"
                  :disabled="!neta.isElegivel"
                  :data-testid="`chk-arvore-unidade-${neta.sigla}`"
                  @update:model-value="(val) => toggle(neta, val as boolean)"
                >
                  <label
                    :for="`chk-${neta.sigla}`"
                    class="form-check-label ms-2"
                    :class="{ 'text-muted': !neta.isElegivel }"
                  >
                    <strong>{{ neta.sigla }}</strong> - {{ neta.nome }}
                    <span
                      v-if="!neta.isElegivel"
                      class="badge bg-warning text-dark ms-2"
                    >
                      Não elegível
                    </span>
                  </label>
                </BFormCheckbox>
              </div>
            </template>
          </div>
        </template>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import {BFormCheckbox} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import type {Unidade} from "@/types/tipos";

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

// Obtém estado de seleção (true, false ou 'indeterminate')
function getEstadoSelecao(unidade: Unidade): boolean | "indeterminate" {
  const selfSelected = isChecked(unidade.codigo);

  if (isFolha(unidade)) {
    return selfSelected;
  }

  const subunidades = getTodasSubunidades(unidade);
  if (subunidades.length === 0) {
    return selfSelected;
  }
  const selecionadas = subunidades.filter((codigo) => isChecked(codigo)).length;

  if (selecionadas === 0 && !selfSelected) {
    return false;
  }
  if (selecionadas === subunidades.length && selfSelected) {
    return true;
  }
  
  // Se for Interoperacional e estiver selecionada, mostra como true (não indeterminate),
  // mesmo que nem todas as filhas estejam selecionadas.
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
    // Filtrar apenas unidades elegíveis ao adicionar
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
      // Apenas adiciona o pai se ele for elegível
      if (parent.isElegivel) {
        selectionSet.add(parent.codigo);
      }
    } else {
      // Se não forem todas as filhas selecionadas, remove o pai
      // EXCETO se o pai for INTEROPERACIONAL (regra 2.3.2.5)
      if (parent.tipo !== 'INTEROPERACIONAL') {
         selectionSet.delete(parent.codigo);
      }
      // Se for INTEROPERACIONAL, mantemos o estado atual dele (seja selecionado ou não)
      // a menos que a ação explicita de desmarcar tenha ocorrido nele (tratado no passo 1)
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