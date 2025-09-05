<template>
  <div>
    <div class="form-check">
      <input
          :id="`chk-${unidade.sigla}`"
          :checked="isChecked(unidade.sigla)"
          class="form-check-input"
          type="checkbox"
          v-bind:indeterminate="isIndeterminate(unidade)"
          @change="() => toggleUnidade(unidade)"
      />
      <label :for="`chk-${unidade.sigla}`" class="form-check-label ms-2">
        <strong>{{ unidade.sigla }}</strong> - {{ unidade.nome }}
      </label>
    </div>
    <div v-if="unidade.filhas && unidade.filhas.length" class="ms-4">
      <UnidadeTreeItem
          v-for="filha in unidade.filhas"
          :key="filha.sigla"
          :unidade="filha"
          :is-checked="isChecked"
          :toggle-unidade="toggleUnidade"
          :is-indeterminate="isIndeterminate"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {PropType} from 'vue';
import {Unidade} from '@/types/tipos';

defineProps({
  unidade: {
    type: Object as PropType<Unidade>,
    required: true,
  },
  isChecked: {
    type: Function as PropType<(sigla: string) => boolean>,
    required: true,
  },
  toggleUnidade: {
    type: Function as PropType<(unidade: Unidade) => void>,
    required: true,
  },
  isIndeterminate: {
    type: Function as PropType<(unidade: Unidade) => boolean | 'indeterminate'>,
    required: true,
  },
});
</script>

<style scoped>
.form-check {
  margin-bottom: 0.25rem;
  padding-left: 1.5em;
}

.ms-4 {
  border-left: 1px dashed #dee2e6;
  padding-left: 1rem;
  margin-left: 0.5rem;
}

input[type="checkbox"]:indeterminate {
  background-color: #0d6efd;
  border-color: #0d6efd;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20'%3e%3cpath fill='none' stroke='%23fff' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M6 10h8'/%3e%3c/svg%3e");
}
</style>