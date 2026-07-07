<template>
  <ModalConfirmacao
      :model-value="mostrarConfirmacao"
      :auto-close="false"
      :loading="excluindo"
      :ok-title="TEXTOS.comum.BOTAO_REMOVER"
      :titulo="TEXTOS.administracao.LIMPEZA_MODAL_TITULO"
      variant="danger"
      @confirmar="$emit('confirmarExclusao')"
      @update:model-value="$emit('update:mostrarConfirmacao', $event)"
  >
    <p v-if="descricaoConfirmacao">
      {{ TEXTOS.administracao.LIMPEZA_MODAL_TEXTO(descricaoConfirmacao) }}
    </p>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import {TEXTOS} from '@/constants/textos';

defineProps<{
  descricaoConfirmacao: string | null;
  excluindo: boolean;
  mostrarConfirmacao: boolean;
}>();

defineEmits<{
  (e: 'confirmarExclusao'): void;
  (e: 'update:mostrarConfirmacao', valor: boolean): void;
}>();
</script>
