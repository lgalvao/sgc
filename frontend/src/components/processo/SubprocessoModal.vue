<template>
  <BModal
      :fade="false"
      :model-value="mostrarModal"
      centered
      title="Alterar data limite"
      @hide="$emit('fecharModal')"
  >
    <BFormGroup
        description="Selecione uma data futura"
        label="Nova data limite"
    >
      <InputData
          v-model="novaDataLimite"
          :min="dataLimiteMinima"
          data-testid="input-nova-data-limite"
          max="2099-12-31"
      />
      <template #description>
        Data limite atual: {{ dataLimiteAtualFormatada }}
      </template>
    </BFormGroup>

    <template #footer>
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            :disabled="loading"
            class="text-decoration-none text-secondary fw-medium btn-cancelar-link"
            data-testid="subprocesso-modal__btn-modal-cancelar"
            variant="link"
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
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BFormGroup, BModal} from "bootstrap-vue-next";
import InputData from "@/components/comum/InputData.vue";
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

<style scoped>
.btn-cancelar-link {
  padding: 0.375rem 0.75rem;
  transition: all 0.2s;
  border-radius: 0.375rem;
}

.btn-cancelar-link:hover {
  color: var(--bs-emphasis-color) !important;
  background-color: var(--bs-secondary-bg);
}
</style>
