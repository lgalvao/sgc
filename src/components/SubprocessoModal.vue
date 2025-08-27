<template>
  <!-- Modal para alterar data limite -->
  <div v-if="mostrarModal" class="modal fade show" style="display: block;" tabindex="-1">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-calendar text-primary me-2"></i>
            Alterar data limite
          </h5>
          <button type="button" class="btn-close" @click="$emit('fecharModal')"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label class="form-label">Nova data limite</label>
            <input
              v-model="novaDataLimite"
              type="date"
              class="form-control"
              :min="dataLimiteMinima"
            >
            <div class="form-text">
              Data limite atual: {{ dataLimiteAtualFormatada }}
            </div>
          </div>
          <div v-if="etapaAtual" class="alert alert-info">
            <i class="bi bi-info-circle me-2"></i>
            Alterando data limite da etapa {{ etapaAtual }}: {{ situacaoEtapaAtual }}
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="$emit('fecharModal')">
            <i class="bi bi-x-circle me-1"></i>
            Cancelar
          </button>
          <button
            type="button"
            class="btn btn-primary"
            :disabled="!novaDataLimite || !isDataValida"
            @click="$emit('confirmarAlteracao', novaDataLimite)"
          >
            <i class="bi bi-check-circle me-1"></i>
            Confirmar
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-if="mostrarModal" class="modal-backdrop fade show"></div>

</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { formatDateForInput, formatDateBR, isDateValidAndFuture, parseDate } from '@/utils/dateUtils';

interface Props {
  mostrarModal: boolean;
  mostrarAlertaSucesso: boolean;
  dataLimiteAtual: Date | null;
  etapaAtual: number | null;
  situacaoEtapaAtual: string;
}

const props = defineProps<Props>();

defineEmits<{
  fecharModal: [];
  confirmarAlteracao: [novaData: string];
  fecharAlerta: [];
  sucesso: [mensagem: string];
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
});
</script>