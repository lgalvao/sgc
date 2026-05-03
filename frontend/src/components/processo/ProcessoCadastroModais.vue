<template>
  <div>
    <!-- Modal de confirmação cadastro -->
    <ModalConfirmacao
        :model-value="mostrarConfirmacao"
        :auto-close="false"
        :loading="isLoading"
        :cancel-title="TEXTOS.comum.BOTAO_CANCELAR"
        :ok-title="TEXTOS.comum.BOTAO_INICIAR"
        variant="success"
        test-codigo-cancelar="btn-iniciar-processo-cancelar"
        test-codigo-confirmar="btn-iniciar-processo-confirmar"
        :titulo="TEXTOS.processo.cadastro.INICIAR_TITULO"
        @update:model-value="$emit('update:mostrarConfirmacao', $event)"
        @confirmar="$emit('confirmar-iniciar')"
    >
      <div class="confirmacao-resumo">
        <p><strong>Descrição:</strong> {{ descricao }}</p>
        <p><strong>Tipo:</strong> {{ tipoLabel }}</p>
        <p><strong>Unidades selecionadas:</strong> {{ totalUnidades }}</p>
      </div>
      <hr>
      <p class="mb-0">
        {{ TEXTOS.processo.cadastro.INICIAR_CONFIRMACAO }}
      </p>
    </ModalConfirmacao>

    <!-- Modal de confirmação de remoção -->
    <ModalConfirmacao
        :model-value="mostrarRemocao"
        :auto-close="false"
        :loading="isLoading"
        :ok-title="TEXTOS.processo.cadastro.BOTAO_REMOVER"
        :titulo="TEXTOS.processo.cadastro.REMOVER_TITULO"
        variant="danger"
        @update:model-value="$emit('update:mostrarRemocao', $event)"
        @confirmar="$emit('confirmar-remocao')"
    >
      <p class="mb-0">{{ TEXTOS.processo.cadastro.REMOVER_CONFIRMACAO(descricao) }}</p>
    </ModalConfirmacao>
  </div>
</template>

<script lang="ts" setup>
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {TEXTOS} from "@/constants/textos";

defineProps<{
  mostrarConfirmacao: boolean;
  mostrarRemocao: boolean;
  isLoading: boolean;
  descricao: string;
  tipoLabel: string;
  totalUnidades: number;
}>();

defineEmits<{
  'update:mostrarConfirmacao': [value: boolean];
  'update:mostrarRemocao': [value: boolean];
  'confirmar-iniciar': [];
  'confirmar-remocao': [];
}>();
</script>

<style scoped>
.confirmacao-resumo p {
  margin-bottom: 0.5rem;
}
</style>
