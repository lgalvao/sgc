<template>
  <ModalConfirmacao
      v-model="mostrarComputado"
      :auto-close="false"
      :ok-title="TEXTOS.comum.BOTAO_CONFIRMAR"
      :titulo="isRevisao ? TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TITULO : TEXTOS.atividades.MODAL_DISPONIBILIZAR_TITULO"
      test-codigo-cancelar="btn-disponibilizar-revisao-cancelar"
      test-codigo-confirmar="btn-confirmar-disponibilizacao"
      variant="success"
      @confirmar="emit('confirmar')"
  >
    <p>
      {{
        isRevisao ? TEXTOS.atividades.MODAL_DISPONIBILIZAR_REVISAO_TEXTO : TEXTOS.atividades.MODAL_DISPONIBILIZAR_TEXTO
      }}
    </p>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {computed} from "vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  mostrar: boolean;
  isRevisao: boolean;
}>();

const emit = defineEmits<{
  (e: 'fechar'): void;
  (e: 'confirmar'): void;
}>();

const mostrarComputado = computed({
  get: () => props.mostrar,
  set: (mostrar: boolean) => {
    if (!mostrar) emit('fechar');
  }
});
</script>
