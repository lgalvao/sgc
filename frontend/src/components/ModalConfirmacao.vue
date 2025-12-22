<template>
  <BModal
      v-model="modelValueComputed"
      :title="titulo"
      centered
      @hide="fechar"
  >
    <slot>
      <p>{{ mensagem }}</p>
    </slot>
    <template #footer>
      <BButton
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
import {computed} from "vue";

const props = defineProps<{
  modelValue: boolean;
  titulo: string;
  mensagem?: string;
  variant?: any;
  testIdConfirmar?: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirmar'): void;
}>();

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
</script>
