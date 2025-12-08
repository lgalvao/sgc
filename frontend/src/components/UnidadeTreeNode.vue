<template>
  <div>
    <div class="unidade-node">
      <!-- Expansor ANTES do checkbox -->
      <span
          v-if="unidade.filhas && unidade.filhas.length > 0"
          class="expansor"
          :data-testid="`btn-arvore-expand-${unidade.sigla}`"
          @click="onToggleExpand(unidade)"
      >
        {{ isExpanded(unidade) ? '▼' : '▶' }}
      </span>
      <span v-else class="expansor-placeholder"></span>

      <!-- Checkbox com label -->
      <BFormCheckbox
          :id="`chk-${unidade.sigla}`"
          :model-value="isChecked(unidade.codigo)"
          :indeterminate="getEstadoSelecao(unidade) === 'indeterminate'"
          :disabled="!isHabilitado(unidade)"
          :data-testid="`chk-arvore-unidade-${unidade.sigla}`"
          class="unidade-checkbox"
          @update:model-value="(val) => onToggle(unidade, val as boolean)"
      >
        <label
            :for="`chk-${unidade.sigla}`"
            class="unidade-label"
            :class="{ 'text-muted': !isHabilitado(unidade) }"
        >
          {{ unidade.sigla }}
        </label>
      </BFormCheckbox>
    </div>

    <!-- Filhas (recursivo) -->
    <div
        v-if="unidade.filhas && unidade.filhas.length && (depth === 0 || isExpanded(unidade))"
        class="unidade-children"
    >
      <UnidadeTreeNode
          v-for="filha in unidade.filhas"
          :key="filha.sigla"
          :unidade="filha"
          :depth="depth + 1"
          :is-checked="isChecked"
          :get-estado-selecao="getEstadoSelecao"
          :is-expanded="isExpanded"
          :is-habilitado="isHabilitado"
          :on-toggle="onToggle"
          :on-toggle-expand="onToggleExpand"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {BFormCheckbox} from "bootstrap-vue-next";
import type {Unidade} from "@/types/tipos";

interface Props {
  unidade: Unidade;
  depth?: number;
  isChecked: (codigo: number) => boolean;
  getEstadoSelecao: (unidade: Unidade) => boolean | "indeterminate";
  isExpanded: (unidade: Unidade) => boolean;
  isHabilitado: (unidade: Unidade) => boolean;
  onToggle: (unidade: Unidade, checked: boolean) => void;
  onToggleExpand: (unidade: Unidade) => void;
}

const props = withDefaults(defineProps<Props>(), {
  depth: 0,
});

// Desestruturar para uso no template
const { isChecked, getEstadoSelecao, isExpanded, isHabilitado, onToggle, onToggleExpand } = props;
</script>

<style scoped>
.unidade-node {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.expansor {
  cursor: pointer;
  user-select: none;
  width: 1rem;
  text-align: center;
  font-size: 0.75rem;
  color: #6c757d;
  transition: color 0.2s;
  line-height: 1;
}

.expansor:hover {
  color: #495057;
}

.expansor-placeholder {
  width: 1rem;
  display: inline-block;
}

.unidade-checkbox {
  margin: 0;
}

.unidade-label {
  margin: 0;
  cursor: pointer;
  font-weight: 500;
  user-select: none;
}

.unidade-children {
  margin-left: 1.5rem;
}
</style>
