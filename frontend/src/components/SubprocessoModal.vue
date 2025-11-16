<template>
  <b-modal
    v-model="show"
    title="Alterar data limite"
    centered
    @hidden="fecharModal"
  >
    <div class="mb-3">
      <label class="form-label">Nova data limite</label>
      <input
        v-model="novaDataLimite"
        type="date"
        class="form-control"
        :min="dataLimiteMinima"
        data-testid="input-nova-data-limite"
      >
      <div class="form-text">
        Data limite atual: {{ dataLimiteAtualFormatada }}
      </div>
    </div>

    <template #footer>
      <b-button
        variant="secondary"
        data-testid="btn-modal-cancelar"
        @click="fecharModal"
      >
        <i class="bi bi-x-circle me-1" />
        Cancelar
      </b-button>
      <b-button
        variant="primary"
        :disabled="!novaDataLimite || !isDataValida"
        data-testid="btn-modal-confirmar"
        @click="$emit('confirmarAlteracao', novaDataLimite)"
      >
        <i class="bi bi-check-circle me-1" />
        Confirmar
      </b-button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {formatDateBR, formatDateForInput, isDateValidAndFuture, parseDate} from '@/utils';

interface Props {
   mostrarModal: boolean;
   dataLimiteAtual: Date | null;
   etapaAtual: number | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
   (e: 'update:mostrarModal', value: boolean): void;
   (e: 'confirmarAlteracao', novaData: string): void;
}>();

const novaDataLimite = ref('');

const show = computed({
  get: () => props.mostrarModal,
  set: (value) => emit('update:mostrarModal', value)
})

const dataLimiteMinima = computed(() => {
  return formatDateForInput(new Date());
});

const dataLimiteAtualFormatada = computed(() => {
  return formatDateBR(props.dataLimiteAtual);
});

const isDataValida = computed(() => {
  if (!novaDataLimite.value) return false;
  return isDateValidAndFuture(parseDate(novaDataLimite.value));
});

// Watch para mostrarModal e inicializar quando abrir
watch(() => props.mostrarModal, (novoValor: boolean) => {
  if (novoValor && props.dataLimiteAtual) {
    novaDataLimite.value = formatDateForInput(props.dataLimiteAtual);
  } else {
    novaDataLimite.value = '';
  }
});

function fecharModal() {
  emit('update:mostrarModal', false);
}
</script>
