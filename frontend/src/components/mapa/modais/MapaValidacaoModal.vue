<script lang="ts" setup>
import {computed} from "vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  modelValue: boolean;
  loading: boolean;
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
      :ok-title="TEXTOS.comum.BOTAO_VALIDAR"
      test-codigo-cancelar="btn-validar-mapa-cancelar"
      test-codigo-confirmar="btn-validar-mapa-confirmar"
      titulo="Validação de mapa"
      variant="success"
      @confirmar="$emit('confirmar')"
  >
    <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
  </ModalConfirmacao>
</template>
