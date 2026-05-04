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
          :disabled="loading"
          :state="estadoInput"
          aria-label="Nova atividade"
          aria-required="true"
          data-testid="inp-nova-atividade"
          placeholder="Nova atividade"
          type="text"
      />
      <BFormInvalidFeedback :state="estadoInput">
        {{ mensagemErro }}
      </BFormInvalidFeedback>
    </BCol>
    <BCol cols="auto">
      <LoadingButton
          :disabled="disabled || loading"
          :loading="loading"
          aria-label="Adicionar atividade"
          data-testid="btn-adicionar-atividade"
          icon="plus-lg"
          size="sm"
          type="submit"
          variant="outline-primary"
      />
    </BCol>
  </BForm>
</template>

<script lang="ts" setup>
import {BCol, BForm, BFormInput, BFormInvalidFeedback} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

const modelValue = defineModel<string>({default: ''});

const props = defineProps<{
  loading?: boolean;
  disabled?: boolean;
  erro?: string | null;
}>();

const emit = defineEmits<{
  'submit': [];
}>();

const inputRef = ref<InstanceType<typeof BFormInput> | null>(null);
const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
} = useValidacaoFormulario();

const mensagemErro = computed(() => {
  if (props.erro) return props.erro;
  if (deveExibirErro(!modelValue.value.trim())) return "Informe a atividade.";
  return "";
});

const estadoInput = computed(() => (mensagemErro.value ? false : null));

function onSubmit() {
  if (!validarSubmissao(!!modelValue.value.trim())) return;
  emit('submit');
  resetarValidacao();
}

watch(
    () => modelValue.value,
    (valor) => {
      if (valor.trim()) {
        resetarValidacao();
      }
    }
);

defineExpose({
  modelValue,
  inputRef,
  onSubmit
});
</script>
