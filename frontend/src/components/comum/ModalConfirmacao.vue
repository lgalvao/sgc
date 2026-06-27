<template>
  <ModalPadrao
      v-model="modelValueComputed"
      :acao-desabilitada="okDisabled"
      :loading="loading"
      :test-id-cancelar="testIdCancelar"
      :test-id-confirmar="testIdConfirmar"
      :texto-acao="okTitle"
      :texto-acao-carregando="okTitle === 'Confirmar' ? 'Processando...' : okTitle"
      :texto-cancelar="cancelTitle"
      :titulo="titulo"
      :variant-acao="variantAcao"
      centralizado
      @shown="onShown"
      @confirmar="confirmar"
      @fechar="emit('hide')"
  >
    <template #alerta>
      <slot name="alerta"></slot>
    </template>
    <slot>
      <div class="d-flex align-items-start">
        <i
            v-if="variant === 'danger'"
            aria-hidden="true"
            class="bi bi-exclamation-triangle-fill text-danger fs-3 me-3"
        />
        <p class="mb-0 pt-1">{{ mensagem }}</p>
      </div>
    </slot>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {computed, nextTick} from "vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";

const props = withDefaults(defineProps<{
  modelValue: boolean;
  titulo: string;
  mensagem?: string;
  variant?: string;
  testIdConfirmar?: string;
  testIdCancelar?: string;
  loading?: boolean;
  okTitle?: string;
  cancelTitle?: string;
  okDisabled?: boolean;
  okVariant?: string;
  autoClose?: boolean;
}>(), {
  variant: 'primary',
  loading: false,
  okTitle: 'Confirmar',
  cancelTitle: 'Cancelar',
  okDisabled: false,
  autoClose: true,
  mensagem: '',
  testIdConfirmar: '',
  testIdCancelar: '',
  okVariant: '',
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirmar'): void;
  (e: 'shown'): void;
  (e: 'hide'): void;
}>();

const modelValueComputed = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});
const variantAcao = computed((): "primary" | "secondary" | "success" | "danger" => {
  const variantCandidata = props.okVariant || props.variant || "primary";
  return variantCandidata === "primary"
    || variantCandidata === "secondary"
    || variantCandidata === "success"
    || variantCandidata === "danger"
    ? variantCandidata
    : "primary";
});

function confirmar() {
  emit('confirmar');
  if (props.autoClose) {
    modelValueComputed.value = false;
  }
}

async function onShown() {
  if (props.variant === 'danger') {
    await nextTick();
    const testIdCancelar = props.testIdCancelar || 'btn-modal-confirmacao-cancelar';
    document.querySelector<HTMLButtonElement>(`[data-testid="${testIdCancelar}"]`)?.focus();
  }
  emit('shown');
}
</script>
