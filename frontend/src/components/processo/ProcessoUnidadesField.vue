<template>
  <BFormGroup class="mb-3">
    <template #label>
      Unidades participantes <span aria-hidden="true" class="text-danger">*</span>
    </template>
    <div
        ref="containerRef"
        :class="{ 'is-invalid border-danger': erro }"
        class="border rounded p-3 container-arvore"
        data-testid="container-processo-unidades"
        tabindex="-1"
    >
      <ArvoreUnidades
          v-if="!isLoading"
          :model-value="modelValue"
          :unidades="unidades"
          @update:model-value="$emit('update:modelValue', $event)"
      />
      <div v-else class="text-center py-3">
        <BSpinner
            aria-hidden="true"
            class="me-2"
            small
        />
        Carregando unidades...
      </div>
    </div>
    <BFormInvalidFeedback :state="erro ? false : null" class="d-block">
      {{ erro }}
    </BFormInvalidFeedback>
  </BFormGroup>
</template>

<script lang="ts" setup>
import {BFormGroup, BFormInvalidFeedback, BSpinner} from "bootstrap-vue-next";
import {ref} from "vue";
import ArvoreUnidades from "@/components/unidade/ArvoreUnidades.vue";
import type {Unidade} from "@/types/tipos";

defineProps<{
  modelValue: number[];
  unidades: Unidade[];
  isLoading: boolean;
  erro?: string;
}>();

defineEmits<{
  'update:modelValue': [value: number[]];
}>();

const containerRef = ref<HTMLElement | null>(null);

defineExpose({
  focar: () => containerRef.value?.focus?.()
});
</script>

<style scoped>
.container-arvore {
  overflow-x: hidden;
}

@media (max-width: 576px) {
  .container-arvore {
    padding: 0.75rem !important;
  }
}
</style>
