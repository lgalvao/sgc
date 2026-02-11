<template>
  <BForm
      class="row g-2 align-items-center mb-4"
      data-testid="form-nova-atividade"
      @submit.prevent="onSubmit"
  >
    <BCol>
      <BFormInput
          ref="inputRef"
          v-model="modelValue"
          aria-label="Nova atividade"
          data-testid="inp-nova-atividade"
          :disabled="loading"
          placeholder="Nova atividade"
          type="text"
          required
      />
    </BCol>
    <BCol cols="auto">
      <LoadingButton
          aria-label="Adicionar atividade"
          :disabled="disabled || !modelValue.trim()"
          :loading="loading"
          data-testid="btn-adicionar-atividade"
          size="sm"
          type="submit"
          variant="outline-primary"
          icon="plus-lg"
      />
    </BCol>
  </BForm>
</template>

<script lang="ts" setup>
import {BCol, BForm, BFormInput} from "bootstrap-vue-next";
import {ref} from "vue";
import LoadingButton from "@/components/ui/LoadingButton.vue";

const modelValue = defineModel<string>({ default: '' });

defineProps<{
  loading?: boolean;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  'submit': [];
}>();

const inputRef = ref<InstanceType<typeof BFormInput> | null>(null);

function onSubmit() {
  emit('submit');
}

defineExpose({
  modelValue,
  inputRef,
  onSubmit
});
</script>
