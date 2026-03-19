<template>
  <BInputGroup>
    <BFormInput
        :id="id"
        ref="inputRef"
        :aria-required="required ? 'true' : undefined"
        :model-value="modelValue"
        :state="state"
        :data-testid="dataTestid"
        :max="max"
        :min="min"
        type="date"
        :class="inputClass"
        @update:model-value="(val) => $emit('update:modelValue', String(val))"
    />
    <BInputGroupText class="cursor-pointer" @click="abrirCalendario">
      <i class="bi bi-calendar-event"></i>
    </BInputGroupText>
  </BInputGroup>
</template>
<!--KHASDIHJASPODJJAS-->
<script lang="ts" setup>
import {BFormInput, BInputGroup, BInputGroupText} from "bootstrap-vue-next";
import {ref} from "vue";

defineProps<{
  modelValue: string;
  id?: string;
  state?: boolean | null;
  dataTestid?: string;
  required?: boolean;
  max?: string;
  min?: string;
  inputClass?: string | object | string[];
}>();

defineEmits<{
  'update:modelValue': [value: string];
}>();

const inputRef = ref<InstanceType<typeof BFormInput> | null>(null);

function abrirCalendario() {
  if (inputRef.value?.$el) {
    const input = inputRef.value.$el as HTMLInputElement;
    if (typeof input.showPicker === 'function') {
      input.showPicker();
    } else {
      input.focus();
    }
  }
}

function focus() {
  inputRef.value?.$el?.focus?.();
}

defineExpose({focus});
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

/* Esconde o ícone de calendário nativo do navegador para evitar duplicidade */
:deep(input[type="date"]::-webkit-calendar-picker-indicator) {
  display: none !important;
  -webkit-appearance: none;
}

/* Garante altura mínima para o input para evitar cortes no ícone */
:deep(input[type="date"]) {
  min-height: calc(1.5em + 0.75rem + 2px);
}
</style>
