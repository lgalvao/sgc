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
          :state="estadoInput"
          placeholder="Nova atividade"
          type="text"
          required
      />
      <BFormInvalidFeedback :state="estadoInput">
        {{ mensagemErro }}
      </BFormInvalidFeedback>
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
import {BCol, BForm, BFormInput, BFormInvalidFeedback} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";

const modelValue = defineModel<string>({ default: '' });

const props = defineProps<{
  loading?: boolean;
  disabled?: boolean;
  erro?: string | null;
}>();

const emit = defineEmits<{
  'submit': [];
}>();

const inputRef = ref<InstanceType<typeof BFormInput> | null>(null);
const validacaoSubmetida = ref(false);

const mensagemErro = computed(() => {
  if (props.erro) return props.erro;
  if (validacaoSubmetida.value && !modelValue.value.trim()) return "Informe a atividade.";
  return "";
});

const estadoInput = computed(() => (mensagemErro.value ? false : null));

function onSubmit() {
  validacaoSubmetida.value = true;
  if (!modelValue.value.trim()) return;
  emit('submit');
  validacaoSubmetida.value = false;
}

watch(
    () => modelValue.value,
    (valor) => {
      if (valor.trim()) {
        validacaoSubmetida.value = false;
      }
    }
);

defineExpose({
  modelValue,
  inputRef,
  onSubmit
});
</script>
