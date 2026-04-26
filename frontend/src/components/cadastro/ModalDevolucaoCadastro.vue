<script setup lang="ts">
import { computed } from 'vue';
import { BFormGroup, BFormTextarea, BFormInvalidFeedback } from 'bootstrap-vue-next';
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import { TEXTOS } from "@/utils/mensagens";

interface Props {
  modelValue: boolean;
  loading: boolean;
  isRevisao: boolean;
  observacao: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'update:observacao', value: string): void;
  (e: 'confirmar'): void;
}>();

const model = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const obs = computed({
  get: () => props.observacao,
  set: (val) => emit('update:observacao', val)
});

const estadoObservacao = computed(() => {
  if (obs.value.trim().length > 0) return true;
  return null; // Não exibe erro até tentar confirmar (gerenciado pelo pai se necessário, ou aqui)
});
</script>

<template>
  <ModalConfirmacao
      v-model="model"
      :auto-close="false"
      :loading="loading"
      :titulo="isRevisao ? TEXTOS.atividades.MODAL_DEVOLVER_REVISAO_TITULO : TEXTOS.atividades.MODAL_DEVOLVER_TITULO"
      :ok-title="TEXTOS.comum.BOTAO_DEVOLVER"
      test-codigo-confirmar="btn-devolucao-cadastro-confirmar"
      variant="danger"
      @confirmar="$emit('confirmar')"
  >
    <p>{{ isRevisao ? TEXTOS.atividades.MODAL_DEVOLVER_REVISAO_TEXTO : TEXTOS.atividades.MODAL_DEVOLVER_TEXTO }}</p>
    <BFormGroup class="mb-3" label-for="observacaoDevolucao">
      <template #label>
        {{ TEXTOS.comum.OBSERVACAO }} <span aria-hidden="true" class="text-danger">*</span>
      </template>
      <BFormTextarea
          id="observacaoDevolucao"
          v-model="obs"
          :state="estadoObservacao"
          data-testid="inp-devolucao-cadastro-obs"
          rows="3"
      />
      <BFormInvalidFeedback :state="estadoObservacao">
        {{ TEXTOS.atividades.ERRO_DEVOLUCAO_JUSTIFICATIVA }}
      </BFormInvalidFeedback>
    </BFormGroup>
  </ModalConfirmacao>
</template>
