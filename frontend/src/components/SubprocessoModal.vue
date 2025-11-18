<template>
  <BModal
    :model-value="mostrarModal"
    title="Alterar data limite"
    centered
    hide-footer
    @hide="$emit('fecharModal')"
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
      <button
        type="button"
        class="btn btn-secondary"
        data-testid="btn-modal-cancelar"
        @click="$emit('fecharModal')"
      >
        <i class="bi bi-x-circle me-1" />
        Cancelar
      </button>
      <button
        type="button"
        class="btn btn-primary"
        :disabled="!novaDataLimite || !isDataValida"
        data-testid="btn-modal-confirmar"
        @click="$emit('confirmarAlteracao', novaDataLimite)"
      >
        <i class="bi bi-check-circle me-1" />
        Confirmar
      </button>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {BModal} from 'bootstrap-vue-next';
import {formatDateBR, formatDateForInput, isDateValidAndFuture, parseDate} from '@/utils';

interface Props {
   mostrarModal: boolean;
   dataLimiteAtual: Date | null;
   etapaAtual: number | null;
}

const props = defineProps<Props>();

defineEmits<{
   fecharModal: [];
   confirmarAlteracao: [novaData: string];
}>();

const novaDataLimite = ref('');

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
}, { immediate: true });
</script>
