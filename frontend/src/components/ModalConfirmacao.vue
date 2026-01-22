<template>
  <BModal
      v-model="modelValueComputed"
      :title="titulo"
      centered
      @hide="fechar"
      @shown="onShown"
  >
    <slot>
      <div class="d-flex align-items-start">
        <i
            v-if="variant === 'danger'"
            class="bi bi-exclamation-triangle-fill text-danger fs-3 me-3"
            aria-hidden="true"
        />
        <p class="mb-0 pt-1">{{ mensagem }}</p>
      </div>
    </slot>
    <template #footer>
      <BButton
          ref="btnCancelar"
          :data-testid="testIdCancelar || 'btn-modal-confirmacao-cancelar'"
          variant="secondary"
          :disabled="loading"
          @click="fechar"
      >
        {{ cancelTitle }}
      </BButton>
      <BButton
          :variant="okVariant || variant || 'primary'"
          :data-testid="testIdConfirmar || 'btn-modal-confirmacao-confirmar'"
          :disabled="loading || okDisabled"
          @click="confirmar"
      >
        <span
            v-if="loading"
            class="spinner-border spinner-border-sm me-1"
            role="status"
            aria-hidden="true"
        />
        {{ loading ? (okTitle === 'Confirmar' ? 'Processando...' : okTitle) : okTitle }}
      </BButton>
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
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirmar'): void;
}>();

const btnCancelar = ref<InstanceType<typeof BButton> | null>(null);

const modelValueComputed = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

function fechar() {
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
}
</script>
