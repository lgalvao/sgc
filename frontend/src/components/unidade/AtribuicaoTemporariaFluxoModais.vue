<script lang="ts" setup>
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

defineProps<{
  carregando: boolean;
  mostrarModalRemocao: boolean;
}>();

defineEmits<{
  (e: "confirmarRemocao"): void;
  (e: "update:mostrarModalRemocao", valor: boolean): void;
}>();
</script>

<template>
  <ModalConfirmacao
      :model-value="mostrarModalRemocao"
      :loading="carregando"
      :auto-close="false"
      :ok-title="TEXTOS.comum.BOTAO_REMOVER"
      :titulo="TEXTOS.atribuicaoTemporaria.MODAL_REMOVER_TITULO"
      test-id-confirmar="btn-confirmar-remover-atribuicao"
      variant="danger"
      @confirmar="$emit('confirmarRemocao')"
      @update:model-value="$emit('update:mostrarModalRemocao', $event)"
  >
    <p class="mb-0">{{ TEXTOS.atribuicaoTemporaria.MODAL_REMOVER_TEXTO }}</p>
  </ModalConfirmacao>
</template>
