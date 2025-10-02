<template>
  <!-- Modal para alterar data limite -->
  <div
    v-if="mostrarModal"
    class="modal fade show"
    style="display: block;"
    tabindex="-1"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-calendar text-primary me-2" />
            Alterar data limite
          </h5>
          <button
            type="button"
            class="btn-close"
            @click="$emit('fecharModal')"
          />
        </div>
        <div class="modal-body">
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
          <div
            v-if="etapaAtual"
            class="alert alert-info"
          >
            <i class="bi bi-info-circle me-2" />
            Alterando data limite da etapa {{ etapaAtual }}: {{ situacaoEtapaAtual }}
          </div>
        </div>
        <div class="modal-footer">
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
        </div>
      </div>
    </div>
  </div>
  <div
    v-if="mostrarModal"
    class="modal-backdrop fade show"
  />
</template>

<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {formatDateBR, formatDateForInput, isDateValidAndFuture, parseDate} from '@/utils';
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes';

interface Props {
   mostrarModal: boolean;
   dataLimiteAtual: Date | null;
   etapaAtual: number | null;
   situacaoEtapaAtual: typeof SITUACOES_SUBPROCESSO[keyof typeof SITUACOES_SUBPROCESSO] | 'Não informado';
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
});
</script>