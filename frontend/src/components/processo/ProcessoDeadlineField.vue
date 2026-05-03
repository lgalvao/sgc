<template>
  <BFormGroup
      class="mb-3"
      description="Prazo para conclusão da primeira etapa"
      label-for="dataLimite">
    <template #label>
      Data limite <span aria-hidden="true" class="text-danger">*</span>
    </template>

    <InputData
        id="dataLimite"
        ref="inputRef"
        :model-value="modelValue"
        :state="erro ? false : null"
        data-testid="inp-processo-data-limite"
        max="2099-12-31"
        :min="obterAmanhaFormatado()"
        :required="true"
        @update:model-value="(val) => $emit('update:modelValue', String(val))"
    />

    <BFormInvalidFeedback :state="erro ? false : null">
      {{ erro }}
    </BFormInvalidFeedback>
  </BFormGroup>
</template>

<script lang="ts" setup>
import {BFormGroup, BFormInvalidFeedback} from "bootstrap-vue-next";
import {ref} from "vue";
import InputData from "@/components/comum/InputData.vue";
import {obterAmanhaFormatado} from "@/utils/date";

defineProps<{
  modelValue: string;
  erro?: string;
}>();

defineEmits<{
  'update:modelValue': [value: string];
}>();

const inputRef = ref<InstanceType<typeof InputData> | null>(null);

defineExpose({
  focar: () => inputRef.value?.focus?.()
});
</script>

<style scoped>
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
