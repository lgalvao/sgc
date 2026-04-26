<script setup lang="ts">
import { computed } from 'vue';
import { BFormGroup, BFormTextarea } from 'bootstrap-vue-next';
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import { TEXTOS } from "@/constants/textos";

interface Props {
  modelValue: boolean;
  loading: boolean;
  acao?: {
    tituloModal: string;
    rotuloConfirmacao: string;
    textoModal: string;
  } | null;
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
</script>

<template>
  <ModalConfirmacao
      v-model="model"
      :auto-close="false"
      :loading="loading"
      :titulo="acao?.tituloModal ?? ''"
      :ok-title="acao?.rotuloConfirmacao ?? ''"
      test-codigo-confirmar="btn-aceite-cadastro-confirmar"
      variant="success"
      @confirmar="$emit('confirmar')"
  >
    <p>{{ acao?.textoModal }}</p>
    <BFormGroup class="mb-3" :label="TEXTOS.comum.OBSERVACAO" label-for="observacaoValidacao">
      <BFormTextarea
          id="observacaoValidacao"
          v-model="obs"
          data-testid="inp-aceite-cadastro-obs"
          rows="3"
      />
    </BFormGroup>
  </ModalConfirmacao>
</template>
