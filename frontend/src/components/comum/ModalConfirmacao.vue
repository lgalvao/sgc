<template>
  <BModal
      v-model="modelValueComputed"
      :title="titulo"
      centered
      modal-class="modal-responsivo"
      @hide="fechar"
      @shown="onShown"
  >
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
    <template #footer>
      <div class="d-flex justify-content-between w-100 footer-confirmacao">
        <BButton
            ref="btnCancelar"
            :data-testid="testIdCancelar || 'btn-modal-confirmacao-cancelar'"
            :disabled="loading"
            variant="secondary"
            @click="fechar"
        >
          {{ cancelTitle }}
        </BButton>
        <BButton
            :data-testid="testIdConfirmar || 'btn-modal-confirmacao-confirmar'"
            :disabled="loading || okDisabled"
            :variant="(okVariant || variant || 'primary') as any"
            @click="confirmar"
        >
          <output
              v-if="loading"
              aria-hidden="true"
              class="spinner-border spinner-border-sm me-1"
          />
          {{ loading ? (okTitle === 'Confirmar' ? 'Processando...' : okTitle) : okTitle }}
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BModal} from "bootstrap-vue-next";
import {computed, ref} from "vue";

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

const btnCancelar = ref<InstanceType<typeof BButton> | null>(null);

const modelValueComputed = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

function fechar() {
  emit('hide');
  modelValueComputed.value = false;
}

function confirmar() {
  emit('confirmar');
  if (props.autoClose) {
    modelValueComputed.value = false;
  }
}

function onShown() {
  // UX Improvement: Auto-focus Cancel button for destructive actions
  if (props.variant === 'danger' && btnCancelar.value?.$el) {
    btnCancelar.value.$el.focus();
  }
  emit('shown');
}
</script>

<style scoped>
@media (max-width: 576px) {
  .footer-confirmacao {
    flex-direction: column-reverse;
    gap: 0.5rem;
  }

  .footer-confirmacao > button {
    width: 100%;
  }

  :deep(.modal-responsivo .modal-dialog) {
    margin: 0.5rem;
  }
}
</style>
