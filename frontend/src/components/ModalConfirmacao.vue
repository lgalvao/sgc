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
          @click="fechar"
      >
        Cancelar
      </BButton>
      <BButton
          :variant="variant || 'primary'"
          :data-testid="testIdConfirmar || 'btn-modal-confirmacao-confirmar'"
          @click="confirmar"
      >
        Confirmar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BModal} from "bootstrap-vue-next";
import {computed, ref} from "vue";

const props = defineProps<{
  modelValue: boolean;
  titulo: string;
  mensagem?: string;
  variant?: any;
  testIdConfirmar?: string;
  testIdCancelar?: string;
}>();

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
  modelValueComputed.value = false;
}

function onShown() {
  // UX Improvement: Auto-focus Cancel button for destructive actions
  if (props.variant === 'danger' && btnCancelar.value?.$el) {
    btnCancelar.value.$el.focus();
  }
}
</script>
