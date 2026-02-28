<template>
  <BModal
      :fade="false"
      :model-value="mostrarModal"
      centered
      hide-footer
      title="Alterar data limite"
      @hide="$emit('fecharModal')"
  >
    <BFormGroup
        description="Selecione uma data futura"
        label="Nova data limite"
    >
      <BFormInput
          v-model="novaDataLimite"
          :min="dataLimiteMinima"
          data-testid="input-nova-data-limite"
          type="date"
      />
      <template #description>
        Data limite atual: {{ dataLimiteAtualFormatada }}
      </template>
    </BFormGroup>

    <template #footer>
      <BButton
          :disabled="loading"
          data-testid="subprocesso-modal__btn-modal-cancelar"
          variant="secondary"
          @click="$emit('fecharModal')"
      >
        <i aria-hidden="true" class="bi bi-x-circle me-1"/>
        Cancelar
      </BButton>
      <BButton
          :disabled="!novaDataLimite || !isDataValida || loading"
          data-testid="btn-modal-confirmar"
          variant="primary"
          @click="$emit('confirmarAlteracao', novaDataLimite)"
      >
        <output
            v-if="loading"
            aria-hidden="true"
            class="spinner-border spinner-border-sm me-1"
        />
        <i v-else aria-hidden="true" class="bi bi-check-circle me-1"/>
        {{ loading ? 'Processando...' : 'Confirmar' }}
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BFormGroup, BFormInput, BModal} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import {formatDateBR, formatDateForInput, isDateValidAndFuture, parseDate,} from "@/utils";

interface Props {
  mostrarModal: boolean;
  dataLimiteAtual: Date | null;
  etapaAtual: number | null;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
});

defineEmits<{
  fecharModal: [];
  confirmarAlteracao: [novaData: string];
}>();

const novaDataLimite = ref("");

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
watch(
    () => props.mostrarModal,
    (novoValor: boolean) => {
      if (novoValor && props.dataLimiteAtual) {
        novaDataLimite.value = formatDateForInput(props.dataLimiteAtual);
      } else {
        novaDataLimite.value = "";
      }
    },
    {immediate: true},
);
</script>
