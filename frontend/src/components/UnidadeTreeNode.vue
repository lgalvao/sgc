<template>
  <div>
    <div class="unidade-node">
      <!-- Expansor ANTES do checkbox -->
      <button
          v-if="unidade.filhas && unidade.filhas.length > 0"
          type="button"
          :data-testid="`btn-arvore-expand-${unidade.sigla}`"
          class="expansor"
          :aria-expanded="isExpanded(unidade)"
          :aria-label="isExpanded(unidade) ? `Recolher ${unidade.sigla}` : `Expandir ${unidade.sigla}`"
          @click="onToggleExpand(unidade)"
      >
        <i
          :class="isExpanded(unidade) ? 'bi bi-caret-down-fill' : 'bi bi-caret-right-fill'"
          aria-hidden="true"
        />
      </button>
      <span v-else class="expansor-placeholder"></span>

      <!-- Checkbox com label -->
      <BFormCheckbox
          :id="`chk-${unidade.sigla}`"
          :data-testid="`chk-arvore-unidade-${unidade.sigla}`"
          :disabled="!isHabilitado(unidade)"
          :indeterminate="getEstadoSelecao(unidade) === 'indeterminate'"
          :model-value="getEstadoSelecao(unidade) === true"
          class="unidade-checkbox"
          @update:model-value="(val) => onToggle(unidade, val as boolean)"
      >
        <label
            :class="{ 'text-muted': !isHabilitado(unidade) }"
            :for="`chk-${unidade.sigla}`"
            class="unidade-label"
        >
          {{ unidade.sigla }}
        </label>
      </BFormCheckbox>
    </div>

    <!-- Filhas (recursivo) -->
    <div
        v-if="unidade.filhas && unidade.filhas.length && isExpanded(unidade)"
        class="unidade-children"
    >
      <UnidadeTreeNode
          v-for="filha in unidade.filhas"
          :key="filha.sigla"
          :depth="depth + 1"
          :get-estado-selecao="getEstadoSelecao"
          :is-checked="isChecked"
          :is-expanded="isExpanded"
          :is-habilitado="isHabilitado"
          :on-toggle="onToggle"
          :on-toggle-expand="onToggleExpand"
          :unidade="filha"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
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
const {isChecked, getEstadoSelecao, isExpanded, isHabilitado, onToggle, onToggleExpand} = props;
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
  width: 1rem;
  font-size: 0.75rem;
  color: #6c757d;
  transition: color 0.2s;

  /* Reset button styles */
  background: none;
  border: none;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.expansor:hover {
  color: #495057;
}

.expansor:focus-visible {
  outline: 2px solid #0d6efd;
  border-radius: 2px;
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
