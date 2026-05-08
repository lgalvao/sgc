<script lang="ts" setup>
import {computed} from "vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  modelValue: boolean;
  loading: boolean;
  descricao: string;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", valor: boolean): void;
  (e: "confirmar"): void;
}>();

const mostrar = computed({
  get: () => props.modelValue,
  set: (valor: boolean) => emit("update:modelValue", valor),
});
</script>

<template>
  <ModalConfirmacao
      v-model="mostrar"
      :loading="loading"
      :mensagem="TEXTOS.mapa.EXCLUSAO_CONFIRMACAO(descricao)"
      :titulo="TEXTOS.mapa.EXCLUSAO_TITULO"
      data-testid="mdl-excluir-competencia"
      test-id-confirmar="btn-confirmar-exclusao-competencia"
      variant="danger"
      @confirmar="$emit('confirmar')"
  />
</template>

